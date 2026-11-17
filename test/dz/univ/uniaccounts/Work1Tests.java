package dz.univ.uniaccounts;

import dz.univ.uniaccounts.audit.AuditLogger;
import dz.univ.uniaccounts.model.AccountRules;
import dz.univ.uniaccounts.model.StudentAccount;
import dz.univ.uniaccounts.model.StudentAccountBuilder;
import dz.univ.uniaccounts.repository.AccountRepository;
import dz.univ.uniaccounts.repository.DuplicateAccountException;
import dz.univ.uniaccounts.template.AccountTemplateRegistry;

import java.util.Arrays;

/**
 * Automated checks for Work 1 (Builder, Singleton, Prototype).
 * Run: java -cp out dz.univ.uniaccounts.Work1Tests
 */
public class Work1Tests {

    public static void main(String[] args) {
        run();
        Check.finish();
    }

    public static void run() {
        AccountRepository.getInstance().clear();
        AuditLogger.getInstance().clear();
        builderDefaults();
        builderOptionalFields();
        builderValidation();
        singleton();
        prototype();
    }

    private static void builderDefaults() {
        Check.section("Builder - default values");
        StudentAccount a = new StudentAccountBuilder("1", "Ali Ali", "ali@univ.dz").build();
        Check.equal("phone defaults to null", null, a.getPhone());
        Check.equal("level defaults to L1", "L1", a.getLevel());
        Check.equal("major defaults to null", null, a.getMajor());
        Check.equal("scholarship defaults to false", false, a.isScholarship());
        Check.isTrue("courses default to an empty list", a.getCourses().isEmpty());
    }

    private static void builderOptionalFields() {
        Check.section("Builder - optional fields");
        StudentAccount a = new StudentAccountBuilder("2", "Nadia Ouali", "n@univ.dz")
                .major("Info").level("M2").scholarship(true).phone("0555")
                .courses(Arrays.asList("A", "B")).addCourse("C").build();
        Check.equal("level set", "M2", a.getLevel());
        Check.equal("major set", "Info", a.getMajor());
        Check.equal("phone set", "0555", a.getPhone());
        Check.isTrue("scholarship set", a.isScholarship());
        Check.equal("courses set", Arrays.asList("A", "B", "C"), a.getCourses());
        Check.throwsException("courses list is read-only from outside", UnsupportedOperationException.class,
                () -> a.getCourses().add("X"));
        for (String level : AccountRules.LEVELS) {
            Check.equal("level " + level + " accepted", level,
                    new StudentAccountBuilder("L-" + level, "X", "x@y").level(level).build().getLevel());
        }
    }

    private static void builderValidation() {
        Check.section("Builder - validation in build()");
        Check.throwsException("null id rejected", IllegalArgumentException.class,
                () -> new StudentAccountBuilder(null, "A", "a@b").build());
        Check.throwsException("blank id rejected", IllegalArgumentException.class,
                () -> new StudentAccountBuilder("  ", "A", "a@b").build());
        Check.throwsException("null fullName rejected", IllegalArgumentException.class,
                () -> new StudentAccountBuilder("1", null, "a@b").build());
        Check.throwsException("email without @ rejected", IllegalArgumentException.class,
                () -> new StudentAccountBuilder("1", "A", "ab.dz").build());
        Check.throwsException("null email rejected", IllegalArgumentException.class,
                () -> new StudentAccountBuilder("1", "A", null).build());
        Check.throwsException("level L4 rejected", IllegalArgumentException.class,
                () -> new StudentAccountBuilder("1", "A", "a@b").level("L4").build());
        Check.throwsException("lower-case l1 rejected", IllegalArgumentException.class,
                () -> new StudentAccountBuilder("1", "A", "a@b").level("l1").build());
        Check.throwsException("null level rejected", IllegalArgumentException.class,
                () -> new StudentAccountBuilder("1", "A", "a@b").level(null).build());
        Check.equal("id is trimmed", "77", new StudentAccountBuilder(" 77 ", "A", "a@b").build().getId());
    }

    private static void singleton() {
        Check.section("Singleton - repository and audit log");
        AccountRepository repo = AccountRepository.getInstance();
        Check.isTrue("AccountRepository.getInstance() returns the same object",
                repo == AccountRepository.getInstance());
        Check.isTrue("AuditLogger.getInstance() returns the same object",
                AuditLogger.getInstance() == AuditLogger.getInstance());

        repo.clear();
        AuditLogger.getInstance().clear();
        StudentAccount a = new StudentAccountBuilder("100", "A", "a@b").build();
        repo.save(a);
        Check.isTrue("account saved through one reference is found through another",
                AccountRepository.getInstance().findById("100") == a);
        Check.throwsException("duplicate id rejected", DuplicateAccountException.class,
                () -> repo.save(new StudentAccountBuilder("100", "B", "b@b").build()));
        Check.throwsException("duplicate id rejected even with spaces", DuplicateAccountException.class,
                () -> repo.save(new StudentAccountBuilder(" 100 ", "B", "b@b").build()));
        Check.equal("repository still holds 1 account", 1, repo.count());
        Check.equal("audit log has SAVE + 2 REJECT entries", 3, AuditLogger.getInstance().size());
        Check.isTrue("first log entry is the SAVE", AuditLogger.getInstance().getEntries().get(0).contains("[SAVE]"));
        Check.throwsException("findAll() is read-only", UnsupportedOperationException.class,
                () -> repo.findAll().clear());
    }

    private static void prototype() {
        Check.section("Prototype - templates and deep copy");
        AccountTemplateRegistry registry = new AccountTemplateRegistry();
        StudentAccount template = new StudentAccountBuilder("T", "Template", "t@univ.dz")
                .level("L2").major("Math").scholarship(true).addCourse("Analyse").addCourse("Algebre").build();
        registry.register("MATH-L2", template);

        StudentAccount s1 = registry.createFrom("MATH-L2", "200", "Student One", "one@univ.dz");
        StudentAccount s2 = registry.createFrom("MATH-L2", "201", "Student Two", "two@univ.dz");
        Check.equal("copy has new id", "200", s1.getId());
        Check.equal("copy has new name", "Student One", s1.getFullName());
        Check.equal("copy has new email", "one@univ.dz", s1.getEmail());
        Check.equal("copy keeps level", "L2", s1.getLevel());
        Check.equal("copy keeps major", "Math", s1.getMajor());
        Check.isTrue("copy keeps scholarship", s1.isScholarship());
        Check.equal("copy keeps courses", Arrays.asList("Analyse", "Algebre"), s1.getCourses());
        Check.isTrue("copy is a different object", s1 != registry.getTemplate("MATH-L2"));

        s1.addCourse("Probabilites");
        Check.equal("adding a course to a copy does not change the other copy", 2, s2.getCourses().size());
        Check.equal("... nor the template", 2, registry.getTemplate("MATH-L2").getCourses().size());
        template.addCourse("Changed after registration");
        Check.equal("registry stored its own copy of the template", 2,
                registry.getTemplate("MATH-L2").getCourses().size());
        Check.equal("template identity unchanged", "T", registry.getTemplate("MATH-L2").getId());

        Check.throwsException("new identity validated (email without @)", IllegalArgumentException.class,
                () -> registry.createFrom("MATH-L2", "300", "X", "bad-email"));
        Check.throwsException("new identity validated (empty id)", IllegalArgumentException.class,
                () -> registry.createFrom("MATH-L2", "", "X", "x@y"));
        Check.throwsException("unknown template rejected", IllegalArgumentException.class,
                () -> registry.createFrom("UNKNOWN", "300", "X", "x@y"));
    }
}
