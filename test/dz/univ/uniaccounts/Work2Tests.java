package dz.univ.uniaccounts;

import dz.univ.uniaccounts.adapter.LegacyStudentAdapter;
import dz.univ.uniaccounts.audit.AuditLogger;
import dz.univ.uniaccounts.export.AccountExporter;
import dz.univ.uniaccounts.export.CsvExport;
import dz.univ.uniaccounts.export.JsonExport;
import dz.univ.uniaccounts.facade.ImportReport;
import dz.univ.uniaccounts.facade.UniAccountsFacade;
import dz.univ.uniaccounts.legacy.LegacyStudentSystem;
import dz.univ.uniaccounts.model.StudentAccount;
import dz.univ.uniaccounts.model.StudentAccountBuilder;
import dz.univ.uniaccounts.observer.AccountEvent;
import dz.univ.uniaccounts.observer.AccountEventPublisher;
import dz.univ.uniaccounts.observer.AccountObserver;
import dz.univ.uniaccounts.observer.AdminDashboard;
import dz.univ.uniaccounts.observer.EventType;
import dz.univ.uniaccounts.repository.AccountRepository;
import dz.univ.uniaccounts.repository.DuplicateAccountException;
import dz.univ.uniaccounts.services.BasicServices;
import dz.univ.uniaccounts.services.InternetService;
import dz.univ.uniaccounts.services.LibraryService;
import dz.univ.uniaccounts.services.RestaurantService;
import dz.univ.uniaccounts.services.StudentServices;
import dz.univ.uniaccounts.state.AccountStatus;
import dz.univ.uniaccounts.structure.AccountLeaf;
import dz.univ.uniaccounts.structure.Department;
import dz.univ.uniaccounts.structure.Faculty;
import dz.univ.uniaccounts.structure.StudentGroup;
import dz.univ.uniaccounts.structure.University;
import dz.univ.uniaccounts.template.AccountTemplateRegistry;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Automated checks for Work 2 (the 7 new patterns and their integration).
 * Run: java -cp out dz.univ.uniaccounts.Work2Tests
 */
public class Work2Tests {

    public static void main(String[] args) {
        run();
        Check.finish();
    }

    public static void run() {
        state();
        observer();
        strategy();
        adapter();
        composite();
        decorator();
        facade();
    }

    private static StudentAccount account(String id) {
        return new StudentAccountBuilder(id, "Student " + id, id + "@univ.dz").build();
    }

    /** The observers print to the console; the tests silence that output. */
    private static final PrintStream REAL_OUT = System.out;
    private static final PrintStream NO_OUT = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
    });

    private static void quiet() {
        System.setOut(NO_OUT);
    }

    private static void loud() {
        System.setOut(REAL_OUT);
    }

    // ---------------------------------------------------------------- State
    private static void state() {
        Check.section("State - transitions and login");
        StudentAccount a = account("S1");
        Check.equal("new account is PENDING", AccountStatus.PENDING, a.getStatus());
        Check.throwsException("PENDING: login refused", IllegalStateException.class, a::login);
        Check.throwsException("PENDING: suspend refused", IllegalStateException.class, a::suspend);
        a.activate();
        Check.equal("PENDING -activate-> ACTIVE", AccountStatus.ACTIVE, a.getStatus());
        a.login();
        Check.isTrue("ACTIVE: login allowed", true);
        Check.throwsException("ACTIVE: activate refused", IllegalStateException.class, a::activate);
        a.suspend();
        Check.equal("ACTIVE -suspend-> SUSPENDED", AccountStatus.SUSPENDED, a.getStatus());
        Check.throwsException("SUSPENDED: login refused", IllegalStateException.class, a::login);
        Check.throwsException("SUSPENDED: suspend refused", IllegalStateException.class, a::suspend);
        a.activate();
        Check.equal("SUSPENDED -activate-> ACTIVE", AccountStatus.ACTIVE, a.getStatus());
        a.close();
        Check.equal("ACTIVE -close-> CLOSED", AccountStatus.CLOSED, a.getStatus());
        Check.throwsException("CLOSED: activate refused", IllegalStateException.class, a::activate);
        Check.throwsException("CLOSED: suspend refused", IllegalStateException.class, a::suspend);
        Check.throwsException("CLOSED: close refused", IllegalStateException.class, a::close);
        Check.throwsException("CLOSED: login refused", IllegalStateException.class, a::login);
        Check.equal("a refused operation leaves the state unchanged", AccountStatus.CLOSED, a.getStatus());

        StudentAccount b = account("S2");
        b.activate();
        b.suspend();
        b.close();
        Check.equal("SUSPENDED -close-> CLOSED", AccountStatus.CLOSED, b.getStatus());
        StudentAccount c = account("S3");
        c.close();
        Check.equal("PENDING -close-> CLOSED", AccountStatus.CLOSED, c.getStatus());

        AccountTemplateRegistry reg = new AccountTemplateRegistry();
        StudentAccount t = account("T");
        t.activate();
        reg.register("K", t);
        Check.equal("Prototype copy of an ACTIVE template starts PENDING", AccountStatus.PENDING,
                reg.createFrom("K", "S4", "X", "x@y").getStatus());
    }

    // ---------------------------------------------------------------- Observer
    private static void observer() {
        Check.section("Observer - subject and observers");
        final List<String> received = new ArrayList<String>();
        AccountObserver spy = new AccountObserver() {
            @Override
            public void update(AccountEvent e) {
                received.add(e.toString());
            }
        };
        AdminDashboard dashboard = new AdminDashboard();
        AccountEventPublisher publisher = new AccountEventPublisher();
        publisher.attach(spy);
        publisher.attach(dashboard);
        publisher.attach(spy);
        Check.equal("the same observer is attached only once", 2, publisher.observerCount());

        quiet();
        publisher.publish(EventType.CREATED, account("O1"));
        publisher.publish(EventType.ACTIVATED, account("O1"));
        publisher.publish(EventType.ACTIVATED, account("O2"));
        loud();
        Check.equal("every event reaches the observer", Arrays.asList("CREATED O1", "ACTIVATED O1", "ACTIVATED O2"), received);
        Check.equal("dashboard counts CREATED", 1, dashboard.getCount(EventType.CREATED));
        Check.equal("dashboard counts ACTIVATED", 2, dashboard.getCount(EventType.ACTIVATED));
        Check.equal("dashboard total", 3, dashboard.getTotal());

        publisher.detach(spy);
        quiet();
        publisher.publish(EventType.CLOSED, account("O1"));
        loud();
        Check.equal("a detached observer is no longer notified", 3, received.size());
        Check.equal("the other observer still is", 1, dashboard.getCount(EventType.CLOSED));
    }

    // ---------------------------------------------------------------- Strategy
    private static void strategy() {
        Check.section("Strategy - CSV and JSON export");
        StudentAccount a = account("E1");
        StudentAccount b = new StudentAccountBuilder("E2", "Benali, \"Aicha\"", "a@b").level("M2").build();
        b.activate();
        List<StudentAccount> list = Arrays.asList(a, b);

        AccountExporter exporter = new AccountExporter(new CsvExport());
        String csv = exporter.export(list);
        String[] lines = csv.split("\n");
        Check.equal("CSV header", "id,name,level,status", lines[0]);
        Check.equal("CSV one line per account", 3, lines.length);
        Check.equal("CSV line", "E1,Student E1,L1,PENDING", lines[1]);
        Check.equal("CSV value with comma and quotes is quoted", "E2,\"Benali, \"\"Aicha\"\"\",M2,ACTIVE", lines[2]);

        exporter.setStrategy(new JsonExport());
        String json = exporter.export(list).trim();
        Check.isTrue("JSON is an array", json.startsWith("[") && json.endsWith("]"));
        Check.isTrue("JSON object", json.contains("{\"id\": \"E1\", \"name\": \"Student E1\", \"level\": \"L1\", \"status\": \"PENDING\"}"));
        Check.isTrue("JSON escapes quotes", json.contains("\"name\": \"Benali, \\\"Aicha\\\"\""));
        Check.equal("JSON of an empty list", "[\n]", new JsonExport().export(new ArrayList<StudentAccount>()).trim());
        Check.throwsException("null strategy refused", IllegalArgumentException.class,
                () -> exporter.setStrategy(null));
    }

    // ---------------------------------------------------------------- Adapter
    private static void adapter() {
        Check.section("Adapter - legacy system");
        LegacyStudentAdapter adapter = new LegacyStudentAdapter(new LegacyStudentSystem());
        List<StudentAccount> accounts = adapter.loadAccounts();
        Check.equal("the 10 legacy records are converted", 10, accounts.size());
        StudentAccount first = accounts.get(0);
        Check.equal("id from field 1", "19990001", first.getId());
        Check.equal("fullName = FIRSTNAME + LASTNAME", "Yacine BENSALEM", first.getFullName());
        Check.equal("email from field 4", "y.bensalem@old.univ.dz", first.getEmail());
        Check.equal("level from field 5", "L3", first.getLevel());
        Check.equal("imported account starts PENDING", AccountStatus.PENDING, first.getStatus());
        Check.equal("no line rejected by the adapter", 0, adapter.getRejectedRecords().size());

        // A fake legacy system with broken lines: the adapter must skip them, not crash.
        LegacyStudentSystem broken = new LegacyStudentSystem() {
            @Override
            public List<String> fetchRecords() {
                return Arrays.asList("1;A;B;a@b;L1", "2;A;B;no-at;L1", "3;A;B;c@d;X9", "only;three;fields");
            }
        };
        LegacyStudentAdapter adapter2 = new LegacyStudentAdapter(broken);
        Check.equal("valid line kept, invalid lines skipped", 1, adapter2.loadAccounts().size());
        Check.equal("3 lines reported as rejected", 3, adapter2.getRejectedRecords().size());
    }

    // ---------------------------------------------------------------- Composite
    private static void composite() {
        Check.section("Composite - university tree");
        University u = new University("U");
        Faculty f1 = new Faculty("F1");
        Faculty f2 = new Faculty("F2");
        Department d1 = new Department("D1");
        Department d2 = new Department("D2");
        StudentGroup g1 = new StudentGroup("G1");
        StudentGroup g2 = new StudentGroup("G2");
        StudentGroup g3 = new StudentGroup("G3");
        u.add(f1);
        u.add(f2);
        f1.add(d1);
        f1.add(d2);
        d1.add(g1);
        d1.add(g2);
        d2.add(g3);
        g1.addAccount(account("C1"));
        g1.addAccount(account("C2"));
        g2.addAccount(account("C3"));
        g3.addAccount(account("C4"));
        Check.equal("leaf counts 1", 1, new AccountLeaf(account("C9")).countAccounts());
        Check.equal("group G1", 2, g1.countAccounts());
        Check.equal("department D1", 3, d1.countAccounts());
        Check.equal("faculty F1", 4, f1.countAccounts());
        Check.equal("empty faculty F2", 0, f2.countAccounts());
        Check.equal("university", 4, u.countAccounts());
        Check.throwsException("a unit cannot contain itself", IllegalArgumentException.class, () -> u.add(u));
    }

    // ---------------------------------------------------------------- Decorator
    private static void decorator() {
        Check.section("Decorator - services");
        StudentServices basic = new BasicServices();
        Check.equal("basic description", "Basic account", basic.getDescription());
        Check.isTrue("basic: no library", !basic.canAccess("LIBRARY"));

        StudentServices lib = new LibraryService(new BasicServices());
        Check.equal("library only", "Basic account + Library", lib.getDescription());
        Check.isTrue("library only: library yes", lib.canAccess("LIBRARY"));
        Check.isTrue("library only: restaurant no", !lib.canAccess("RESTAURANT"));

        StudentServices all = new InternetService(new RestaurantService(new LibraryService(new BasicServices())));
        Check.equal("three services", "Basic account + Library + Restaurant + Internet", all.getDescription());
        Check.isTrue("three services: library (innermost) yes", all.canAccess("library"));
        Check.isTrue("three services: restaurant yes", all.canAccess("RESTAURANT"));
        Check.isTrue("three services: internet yes", all.canAccess("INTERNET"));
        Check.isTrue("three services: unknown service no", !all.canAccess("GYM"));
    }

    // ---------------------------------------------------------------- Facade + integration
    private static void facade() {
        Check.section("Facade - integration of all patterns");
        AccountRepository.getInstance().clear();
        AuditLogger.getInstance().clear();
        quiet();
        UniAccountsFacade uni = new UniAccountsFacade("U");
        uni.registerStudent("F1", "Student F1", "f1@univ.dz", "L2", "Info");
        uni.defineBatchTemplate("B", "M1", "Info", "C1", "C2");
        StudentAccount fromTemplate = uni.registerFromTemplate("B", "F2", "Student F2", "f2@univ.dz");
        boolean ok1 = uni.activate("F1");
        boolean ok2 = uni.login("F1");
        boolean refused = uni.suspend("F2");     // PENDING -> refused, no event
        boolean refusedLogin = uni.login("F2");
        ImportReport report = uni.importLegacyAccounts();
        loud();

        Check.equal("template copy has the batch courses", Arrays.asList("C1", "C2"), fromTemplate.getCourses());
        Check.isTrue("activate accepted", ok1);
        Check.isTrue("login of ACTIVE accepted", ok2);
        Check.isTrue("suspend of PENDING refused", !refused);
        Check.isTrue("login of PENDING refused", !refusedLogin);
        Check.equal("import: 9 accounts imported", 9, report.getImportedIds().size());
        Check.equal("import: 1 rejected (duplicate 19990008)", 1, report.getRejected().size());
        Check.isTrue("the rejected one is the 2nd 19990008 (Bahi SALEM)", report.getRejected().get(0).startsWith("19990008 Bahi SALEM"));
        Check.equal("the kept 19990008 is the first one (Hoda)", "Hoda triqui", uni.getAccount("19990008").getFullName());
        Check.equal("repository: 2 + 9 accounts", 11, AccountRepository.getInstance().count());
        Check.equal("dashboard CREATED", 2, uni.getDashboard().getCount(EventType.CREATED));
        Check.equal("dashboard ACTIVATED", 1, uni.getDashboard().getCount(EventType.ACTIVATED));
        Check.equal("dashboard SUSPENDED (refused = no event)", 0, uni.getDashboard().getCount(EventType.SUSPENDED));
        Check.equal("dashboard IMPORTED", 9, uni.getDashboard().getCount(EventType.IMPORTED));
        Check.equal("one e-mail per event", 12, uni.getEmailNotifier().getSentMessages().size());
        int eventLines = 0;
        for (String e : AuditLogger.getInstance().getEntries()) {
            if (e.contains("[EVENT]")) {
                eventLines++;
            }
        }
        Check.equal("AuditObserver wrote one EVENT line per event", 12, eventLines);

        quiet();
        Check.throwsException("registering an existing id is refused", DuplicateAccountException.class,
                () -> uni.registerStudent("F1", "X", "x@y", "L1", null));
        loud();
        Check.equal("CSV through the facade: header + 11 lines", 12, uni.export("csv").split("\n").length);
        Check.isTrue("JSON through the facade", uni.export("JSON").trim().startsWith("["));
        Check.throwsException("unknown export format refused", IllegalArgumentException.class, () -> uni.export("xml"));

        uni.addFaculty("Sciences");
        uni.addDepartment("Sciences", "Info");
        uni.addGroup("Info", "G1");
        uni.assignToGroup("G1", "F1");
        uni.assignToGroup("G1", "19990001");
        Check.equal("count at university level", 2, uni.countAccounts("U"));
        Check.equal("count at department level", 2, uni.countAccounts("Info"));
        Check.throwsException("unknown unit refused", IllegalArgumentException.class, () -> uni.countAccounts("Nope"));
        Check.throwsException("assigning to a non-group refused", IllegalArgumentException.class,
                () -> uni.assignToGroup("Info", "F2"));
        Check.throwsException("duplicate unit name refused", IllegalArgumentException.class,
                () -> uni.addGroup("Info", "G1"));

        StudentServices s = uni.subscribe("F1", "library", "INTERNET", "LIBRARY");
        Check.equal("services through the facade (duplicate ignored)", "Basic account + Library + Internet", s.getDescription());
        Check.isTrue("canAccess through the facade", uni.canAccess("F1", "INTERNET") && !uni.canAccess("F1", "RESTAURANT"));
        Check.equal("student without subscription has basic services", "Basic account", uni.getServices("F2").getDescription());
        Check.throwsException("unknown service refused", IllegalArgumentException.class, () -> uni.subscribe("F1", "GYM"));
        Check.throwsException("unknown account refused", IllegalArgumentException.class, () -> uni.activate("NOPE"));
    }
}
