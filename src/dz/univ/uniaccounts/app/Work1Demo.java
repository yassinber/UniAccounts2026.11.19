package dz.univ.uniaccounts.app;

import dz.univ.uniaccounts.audit.AuditLogger;
import dz.univ.uniaccounts.model.StudentAccount;
import dz.univ.uniaccounts.model.StudentAccountBuilder;
import dz.univ.uniaccounts.repository.AccountRepository;
import dz.univ.uniaccounts.repository.DuplicateAccountException;
import dz.univ.uniaccounts.template.AccountTemplateRegistry;

/**
 * Work 1 demo: the core of UniAccounts with the 3 creational patterns.
 * Each section shows one problem of the assignment and how the pattern solves it.
 */
public class Work1Demo {

    public static void main(String[] args) {
        AccountRepository.getInstance().clear();
        AuditLogger.getInstance().clear();

        title("UniAccounts - Work 1 : Builder, Singleton, Prototype");

        demoBuilder();
        demoValidation();
        demoSingleton();
        demoPrototype();

        section("6. Audit log (Singleton AuditLogger)");
        AuditLogger.getInstance().printAll();
    }

    // ------------------------------------------------------------------ Builder
    private static void demoBuilder() {
        section("1. BUILDER - creating accounts");

        StudentAccount minimal = new StudentAccountBuilder("20260001", "Yacine Bensalem", "y.bensalem@univ.dz")
                .build();
        System.out.println("Mandatory fields only (defaults applied):");
        System.out.println(details(minimal));

        StudentAccount full = new StudentAccountBuilder("20260002", "Nadia Ouali", "n.ouali@univ.dz")
                .phone("0555 12 34 56")
                .level("M1")
                .major("Informatique")
                .scholarship(true)
                .addCourse("Genie logiciel")
                .addCourse("Design Patterns")
                .build();
        System.out.println("All fields, named one by one (order does not matter):");
        System.out.println(details(full));

        AccountRepository.getInstance().save(minimal);
        AccountRepository.getInstance().save(full);
    }

    private static void demoValidation() {
        section("2. BUILDER - build() rejects invalid data");
        tryBuild("email without '@'", new StudentAccountBuilder("20260003", "Rida Benbordi", "rida.univ.dz"));
        tryBuild("level not in L1..M2", new StudentAccountBuilder("20260004", "Aicha Benali", "a.benali@univ.dz").level("L4"));
        tryBuild("empty id", new StudentAccountBuilder("   ", "Hoda Triqui", "h.triqui@univ.dz"));
        tryBuild("missing fullName", new StudentAccountBuilder("20260005", null, "x@univ.dz"));
    }

    private static void tryBuild(String label, StudentAccountBuilder builder) {
        try {
            builder.build();
            System.out.println("  [UNEXPECTED] " + label + " was accepted");
        } catch (IllegalArgumentException e) {
            System.out.println("  [REJECTED] " + label + " -> " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- Singleton
    private static void demoSingleton() {
        section("3. SINGLETON - one repository, one audit log");

        AccountRepository repoA = AccountRepository.getInstance();
        AccountRepository repoB = AccountRepository.getInstance();
        System.out.println("  repoA == repoB          : " + (repoA == repoB));
        System.out.println("  identityHashCode        : " + System.identityHashCode(repoA)
                + " / " + System.identityHashCode(repoB));
        System.out.println("  AuditLogger same object : "
                + (AuditLogger.getInstance() == AuditLogger.getInstance()));
        System.out.println("  saved through repoA, found through repoB: "
                + repoB.findById("20260002").getFullName());
        System.out.println("  accounts in repository  : " + repoB.count());

        section("4. SINGLETON - the repository rejects a duplicate id");
        StudentAccount duplicate = new StudentAccountBuilder("20260001", "Another Student", "other@univ.dz").build();
        try {
            repoA.save(duplicate);
            System.out.println("  [UNEXPECTED] duplicate accepted");
        } catch (DuplicateAccountException e) {
            System.out.println("  [REJECTED] " + e.getMessage());
        }
        System.out.println("  accounts in repository  : " + repoA.count());
    }

    // ---------------------------------------------------------------- Prototype
    private static void demoPrototype() {
        section("5. PROTOTYPE - batch template, then copies");

        AccountTemplateRegistry registry = new AccountTemplateRegistry();
        StudentAccount infoL1 = new StudentAccountBuilder("TEMPLATE-INFO-L1", "Template", "template@univ.dz")
                .level("L1")
                .major("Informatique")
                .addCourse("Algorithmique 1")
                .addCourse("Analyse 1")
                .addCourse("Architecture des ordinateurs")
                .build();
        registry.register("INFO-L1", infoL1);
        System.out.println("Template INFO-L1: " + brief(registry.getTemplate("INFO-L1")));

        StudentAccount s1 = registry.createFrom("INFO-L1", "20260101", "Mostapha Alla", "m.alla@univ.dz");
        StudentAccount s2 = registry.createFrom("INFO-L1", "20260102", "Laila Ouali", "l.ouali@univ.dz");
        StudentAccount s3 = registry.createFrom("INFO-L1", "20260103", "Bahi Salem", "b.salem@univ.dz");
        AccountRepository repo = AccountRepository.getInstance();
        repo.save(s1);
        repo.save(s2);
        repo.save(s3);
        System.out.println("3 students created from the template (only the identity changes):");
        System.out.println("  " + brief(s1));
        System.out.println("  " + brief(s2));
        System.out.println("  " + brief(s3));

        System.out.println("Deep copy check: Laila registers to one more course 'Anglais'");
        s2.addCourse("Anglais");
        System.out.println("  Laila    courses: " + s2.getCourses());
        System.out.println("  Mostapha courses: " + s1.getCourses());
        System.out.println("  Template courses: " + registry.getTemplate("INFO-L1").getCourses());
        System.out.println("  -> the template and the other copies are unchanged (each copy has its own list)");
        System.out.println("Templates are not stored in the repository: exists(TEMPLATE-INFO-L1) = "
                + repo.exists("TEMPLATE-INFO-L1") + ", total accounts = " + repo.count());
    }

    // ---------------------------------------------------------------- display helpers
    /** All 8 fields on two lines (same content as toString(), easier to read). */
    private static String details(StudentAccount a) {
        return "  id=" + a.getId() + " | fullName=" + a.getFullName() + " | email=" + a.getEmail() + "\n"
                + "  phone=" + a.getPhone() + " | level=" + a.getLevel() + " | major=" + a.getMajor()
                + " | scholarship=" + a.isScholarship() + " | courses=" + a.getCourses();
    }

    /** One-line summary of an account, shorter than toString(). */
    private static String brief(StudentAccount a) {
        return a.getId() + " | " + a.getFullName() + " | " + a.getEmail()
                + " | " + a.getLevel() + " " + a.getMajor() + " | courses=" + a.getCourses();
    }

    private static void title(String text) {
        String line = repeat('=', text.length() + 4);
        System.out.println(line);
        System.out.println("  " + text);
        System.out.println(line);
    }

    private static void section(String text) {
        System.out.println();
        System.out.println(">> " + text);
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
