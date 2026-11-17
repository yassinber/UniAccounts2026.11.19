package dz.univ.uniaccounts.facade;

import dz.univ.uniaccounts.adapter.LegacyStudentAdapter;
import dz.univ.uniaccounts.adapter.StudentSource;
import dz.univ.uniaccounts.audit.AuditLogger;
import dz.univ.uniaccounts.export.AccountExporter;
import dz.univ.uniaccounts.export.CsvExport;
import dz.univ.uniaccounts.export.ExportStrategy;
import dz.univ.uniaccounts.export.JsonExport;
import dz.univ.uniaccounts.legacy.LegacyStudentSystem;
import dz.univ.uniaccounts.model.StudentAccount;
import dz.univ.uniaccounts.model.StudentAccountBuilder;
import dz.univ.uniaccounts.observer.AccountEventPublisher;
import dz.univ.uniaccounts.observer.AdminDashboard;
import dz.univ.uniaccounts.observer.AuditObserver;
import dz.univ.uniaccounts.observer.EmailNotifier;
import dz.univ.uniaccounts.observer.EventType;
import dz.univ.uniaccounts.repository.AccountRepository;
import dz.univ.uniaccounts.repository.DuplicateAccountException;
import dz.univ.uniaccounts.services.BasicServices;
import dz.univ.uniaccounts.services.InternetService;
import dz.univ.uniaccounts.services.LibraryService;
import dz.univ.uniaccounts.services.RestaurantService;
import dz.univ.uniaccounts.services.StudentServices;
import dz.univ.uniaccounts.structure.CompositeUnit;
import dz.univ.uniaccounts.structure.Department;
import dz.univ.uniaccounts.structure.Faculty;
import dz.univ.uniaccounts.structure.StudentGroup;
import dz.univ.uniaccounts.structure.University;
import dz.univ.uniaccounts.structure.UniversityComponent;
import dz.univ.uniaccounts.template.AccountTemplateRegistry;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pattern: FACADE — Role: Facade
 *
 * Problem: the system now has about 40 classes (repository, audit log, templates,
 * states, observers, export strategies, adapter, tree, decorators). A client
 * that wants to "activate an account" would have to find it in the repository,
 * change its state, catch the refusal, then notify the observers, in that order.
 *
 * Solution: one simple class with one method per use case. It knows the
 * subsystems and calls them in the right order; the client only knows the Facade.
 * The Facade does not duplicate any logic: it COORDINATES the other classes.
 *
 * It reuses the 3 patterns of Work 1 without rewriting them:
 *   Builder   -> registerStudent(), defineBatchTemplate()
 *   Singleton -> AccountRepository.getInstance(), AuditLogger.getInstance()
 *   Prototype -> AccountTemplateRegistry in registerFromTemplate()
 */
public class UniAccountsFacade {

    // Work 1 subsystems
    private final AccountRepository repository = AccountRepository.getInstance();
    private final AuditLogger audit = AuditLogger.getInstance();
    private final AccountTemplateRegistry templates = new AccountTemplateRegistry();

    // Work 2 subsystems
    private final AccountEventPublisher events = new AccountEventPublisher();
    private final AdminDashboard dashboard = new AdminDashboard();
    private final EmailNotifier emailNotifier = new EmailNotifier();
    private final Map<String, ExportStrategy> exportFormats = new LinkedHashMap<String, ExportStrategy>();
    private final AccountExporter exporter;
    private final StudentSource legacySource;
    private final University university;
    private final Map<String, CompositeUnit> units = new HashMap<String, CompositeUnit>();
    private final Map<String, StudentServices> servicesByAccount = new HashMap<String, StudentServices>();

    /** Uses the real legacy system of the university. */
    public UniAccountsFacade(String universityName) {
        this(universityName, new LegacyStudentAdapter(new LegacyStudentSystem()));
    }

    /** Lets the caller give another student source (e.g. in the tests). */
    public UniAccountsFacade(String universityName, StudentSource legacySource) {
        this.legacySource = legacySource;

        // Observer: the 3 observers required by the assignment
        events.attach(new AuditObserver());
        events.attach(emailNotifier);
        events.attach(dashboard);

        // Strategy: available formats
        CsvExport csv = new CsvExport();
        JsonExport json = new JsonExport();
        exportFormats.put(csv.formatName(), csv);
        exportFormats.put(json.formatName(), json);
        exporter = new AccountExporter(csv);

        // Composite: root of the tree
        university = new University(universityName);
        units.put(university.getName(), university);
    }

    // =================================================================== accounts

    /** Creates an account with the Builder, saves it, publishes CREATED. */
    public StudentAccount registerStudent(String id, String fullName, String email, String level, String major) {
        StudentAccount account = new StudentAccountBuilder(id, fullName, email)
                .level(level == null ? "L1" : level)
                .major(major)
                .build();
        return saveAndPublish(account, EventType.CREATED);
    }

    /** Prototype: defines the template of a batch (same level, major, courses). */
    public void defineBatchTemplate(String key, String level, String major, String... courses) {
        StudentAccountBuilder builder = new StudentAccountBuilder("TEMPLATE-" + key, "Template " + key,
                "template@univ.dz").level(level).major(major);
        for (String c : courses) {
            builder.addCourse(c);
        }
        templates.register(key, builder.build());
    }

    /** Prototype: copies the batch template with a new identity, saves it, publishes CREATED. */
    public StudentAccount registerFromTemplate(String key, String id, String fullName, String email) {
        return saveAndPublish(templates.createFrom(key, id, fullName, email), EventType.CREATED);
    }

    private StudentAccount saveAndPublish(StudentAccount account, EventType type) {
        repository.save(account);             // throws DuplicateAccountException if the id exists
        events.publish(type, account);
        return account;
    }

    public StudentAccount getAccount(String id) {
        StudentAccount account = repository.findById(id);
        if (account == null) {
            throw new IllegalArgumentException("unknown account: " + id);
        }
        return account;
    }

    public List<StudentAccount> listAccounts() {
        return repository.findAll();
    }

    // =================================================================== life cycle (State)

    public boolean activate(String id) {
        return changeState(id, EventType.ACTIVATED);
    }

    public boolean suspend(String id) {
        return changeState(id, EventType.SUSPENDED);
    }

    public boolean close(String id) {
        return changeState(id, EventType.CLOSED);
    }

    /**
     * Asks the account (State) to change; publishes the event only if the
     * transition was accepted. A refused transition is logged, not notified.
     */
    private boolean changeState(String id, EventType type) {
        StudentAccount account = getAccount(id);
        String before = account.getStatus().name();
        try {
            switch (type) {
                case ACTIVATED: account.activate(); break;
                case SUSPENDED: account.suspend();  break;
                case CLOSED:    account.close();    break;
                default: throw new IllegalArgumentException("not a state change: " + type);
            }
        } catch (IllegalStateException refused) {
            audit.log("REFUSED", type + " " + id + ": " + refused.getMessage());
            System.out.println("  [REFUSED] " + type + " " + id + " -> " + refused.getMessage());
            return false;
        }
        System.out.println("  [OK] " + id + " " + before + " -> " + account.getStatus());
        events.publish(type, account);
        return true;
    }

    /** Login is allowed only for an ACTIVE account (decided by the State object). */
    public boolean login(String id) {
        StudentAccount account = getAccount(id);
        try {
            account.login();
            audit.log("LOGIN", id + " ok");
            System.out.println("  [OK] login " + id + " (" + account.getStatus() + ")");
            return true;
        } catch (IllegalStateException refused) {
            audit.log("REFUSED", "LOGIN " + id + ": " + refused.getMessage());
            System.out.println("  [REFUSED] login " + id + " -> " + refused.getMessage());
            return false;
        }
    }

    // =================================================================== import (Adapter)

    /**
     * Imports the old system's students through the Adapter. Each valid account is
     * saved and published as IMPORTED. Lines the adapter could not convert and
     * duplicate ids (refused by the repository) are reported, not imported.
     */
    public ImportReport importLegacyAccounts() {
        ImportReport report = new ImportReport();
        for (StudentAccount account : legacySource.loadAccounts()) {
            try {
                saveAndPublish(account, EventType.IMPORTED);
                report.imported(account.getId());
            } catch (DuplicateAccountException e) {
                report.rejected(account.getId() + " " + account.getFullName() + " -> duplicate id (" + e.getMessage() + ")");
            }
        }
        if (legacySource instanceof LegacyStudentAdapter) {
            for (String line : ((LegacyStudentAdapter) legacySource).getRejectedRecords()) {
                report.rejected(line);
            }
        }
        audit.log("IMPORT", report.toString());
        return report;
    }

    // =================================================================== export (Strategy)

    /** @param format "csv" or "json" */
    public String export(String format) {
        ExportStrategy strategy = exportFormats.get(format == null ? null : format.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException("unknown format " + format + ", available: " + exportFormats.keySet());
        }
        exporter.setStrategy(strategy);
        audit.log("EXPORT", strategy.formatName() + " (" + repository.count() + " accounts)");
        return exporter.export(repository.findAll());
    }

    // =================================================================== university tree (Composite)

    public void addFaculty(String name) {
        addUnit(university.getName(), new Faculty(name));
    }

    public void addDepartment(String facultyName, String name) {
        addUnit(facultyName, new Department(name));
    }

    public void addGroup(String departmentName, String name) {
        addUnit(departmentName, new StudentGroup(name));
    }

    private void addUnit(String parentName, CompositeUnit unit) {
        if (units.containsKey(unit.getName())) {
            throw new IllegalArgumentException("a unit named " + unit.getName() + " already exists");
        }
        unit(parentName).add(unit);
        units.put(unit.getName(), unit);
    }

    /** Puts an existing account in a group (a leaf of the tree). */
    public void assignToGroup(String groupName, String accountId) {
        CompositeUnit unit = unit(groupName);
        if (!(unit instanceof StudentGroup)) {
            throw new IllegalArgumentException(groupName + " is not a group");
        }
        ((StudentGroup) unit).addAccount(getAccount(accountId));
    }

    /** Asks any node of the tree how many accounts it contains. */
    public int countAccounts(String unitName) {
        return unit(unitName).countAccounts();
    }

    public void printStructure() {
        university.print("  ");
    }

    private CompositeUnit unit(String name) {
        CompositeUnit unit = units.get(name);
        if (unit == null) {
            throw new IllegalArgumentException("unknown unit: " + name);
        }
        return unit;
    }

    /** The whole tree, for clients that want to walk it themselves. */
    public UniversityComponent getUniversity() {
        return university;
    }

    // =================================================================== services (Decorator)

    /**
     * Gives a student a combination of services, e.g. subscribe(id, "LIBRARY", "INTERNET").
     * The Facade builds the chain of decorators around BasicServices.
     */
    public StudentServices subscribe(String accountId, String... services) {
        getAccount(accountId); // the account must exist
        StudentServices s = new BasicServices();
        for (String name : services) {
            String n = name.toUpperCase();
            if (s.canAccess(n)) {
                continue; // already in the combination
            }
            if (LibraryService.NAME.equals(n)) {
                s = new LibraryService(s);
            } else if (RestaurantService.NAME.equals(n)) {
                s = new RestaurantService(s);
            } else if (InternetService.NAME.equals(n)) {
                s = new InternetService(s);
            } else {
                throw new IllegalArgumentException("unknown service: " + name);
            }
        }
        servicesByAccount.put(accountId, s);
        audit.log("SERVICES", accountId + ": " + s.getDescription());
        return s;
    }

    public StudentServices getServices(String accountId) {
        StudentServices s = servicesByAccount.get(accountId);
        return s != null ? s : new BasicServices();
    }

    public boolean canAccess(String accountId, String service) {
        return getServices(accountId).canAccess(service);
    }

    // =================================================================== reports

    public AdminDashboard getDashboard() {
        return dashboard;
    }

    public EmailNotifier getEmailNotifier() {
        return emailNotifier;
    }

    public void printDashboard() {
        dashboard.printSummary();
    }

    public void printAuditLog() {
        audit.printAll();
    }
}
