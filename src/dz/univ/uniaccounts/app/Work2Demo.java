package dz.univ.uniaccounts.app;

import dz.univ.uniaccounts.facade.ImportReport;
import dz.univ.uniaccounts.facade.UniAccountsFacade;

/**
 * Work 2 demo: the complete system.
 *
 * Pattern: FACADE — Role: Client
 * This class only talks to UniAccountsFacade. It never touches the repository,
 * the states, the observers, the exporter, the adapter or the decorators directly.
 */
public class Work2Demo {

    public static void main(String[] args) {
        UniAccountsFacade uni = new UniAccountsFacade("Universite Exemple");

        title("UniAccounts - Work 2 : the complete system through the Facade");

        section("1. FACADE + OBSERVER - registering students (Builder and Prototype reused)");
        System.out.println("  registerStudent(...) - built with the Builder of Work 1:");
        uni.registerStudent("20260001", "Yacine Hamdi", "y.hamdi@univ.dz", "M1", "Informatique");
        System.out.println("  registerFromTemplate(...) - copied from the INFO-L1 batch template (Prototype):");
        uni.defineBatchTemplate("INFO-L1", "L1", "Informatique", "Algorithmique 1", "Analyse 1", "Architecture");
        uni.registerFromTemplate("INFO-L1", "20260101", "Sara Khelifi", "s.khelifi@univ.dz");
        uni.registerFromTemplate("INFO-L1", "20260102", "Omar Zidane", "o.zidane@univ.dz");
        System.out.println("  each event reached the 3 observers (audit log, e-mail, dashboard)");

        section("2. STATE - life cycle of an account");
        uni.login("20260001");      // PENDING: refused
        uni.activate("20260001");   // PENDING -> ACTIVE
        uni.login("20260001");      // ACTIVE: ok
        uni.suspend("20260001");    // ACTIVE -> SUSPENDED
        uni.login("20260001");      // SUSPENDED: refused (assignment rule)
        uni.suspend("20260001");    // already SUSPENDED: refused
        uni.activate("20260001");   // SUSPENDED -> ACTIVE (re-activation)
        uni.close("20260001");      // ACTIVE -> CLOSED (graduation)
        uni.activate("20260001");   // CLOSED is final: refused
        uni.suspend("20260101");    // PENDING cannot be suspended: refused
        uni.activate("20260101");
        uni.activate("20260102");

        section("3. ADAPTER - importing the old system (LegacyStudentSystem)");
        ImportReport report = uni.importLegacyAccounts();
        System.out.println("  Result: " + report);
        for (String r : report.getRejected()) {
            System.out.println("  [REJECTED] " + r);
        }

        section("4. COMPOSITE - university > faculty > department > group > account");
        uni.addFaculty("Faculte des Sciences");
        uni.addDepartment("Faculte des Sciences", "Informatique");
        uni.addDepartment("Faculte des Sciences", "Mathematiques");
        uni.addGroup("Informatique", "INFO-L1-G1");
        uni.addGroup("Informatique", "INFO-M-G1");
        uni.addGroup("Mathematiques", "MATH-L-G1");
        uni.addFaculty("Faculte des Lettres");
        uni.addDepartment("Faculte des Lettres", "Langues");
        uni.addGroup("Langues", "LANG-G1");
        assign(uni, "INFO-L1-G1", "20260101", "20260102", "19990003", "19990005");
        assign(uni, "INFO-M-G1", "20260001", "19990002", "19990004", "19990010");
        assign(uni, "MATH-L-G1", "19990001", "19990007");
        assign(uni, "LANG-G1", "19990006", "19990008");
        uni.printStructure();
        System.out.println("  The same question asked to nodes of different levels:");
        for (String unit : new String[] {"Universite Exemple", "Faculte des Sciences", "Informatique", "INFO-L1-G1", "Langues"}) {
            System.out.println(String.format("    countAccounts(%-22s) = %d", "\"" + unit + "\"", uni.countAccounts(unit)));
        }

        section("5. STRATEGY - same accounts, two export formats");
        System.out.println("  export(\"csv\"):");
        print(uni.export("csv"));
        System.out.println("  export(\"json\"):");
        print(uni.export("json"));

        section("6. DECORATOR - extra services in different combinations");
        show(uni, "20260101", uni.subscribe("20260101", "LIBRARY").getDescription());
        show(uni, "20260102", uni.subscribe("20260102", "LIBRARY", "RESTAURANT", "INTERNET").getDescription());
        show(uni, "19990002", uni.subscribe("19990002", "INTERNET", "RESTAURANT").getDescription());
        show(uni, "19990001", uni.getServices("19990001").getDescription());
        System.out.println("  Restaurant door: 20260101 -> " + uni.canAccess("20260101", "RESTAURANT")
                + ", 20260102 -> " + uni.canAccess("20260102", "RESTAURANT"));

        section("7. OBSERVER - what the observers recorded");
        uni.printDashboard();
        System.out.println("  e-mails sent by EmailNotifier: " + uni.getEmailNotifier().getSentMessages().size());
        System.out.println();
        uni.printAuditLog();
    }

    private static void assign(UniAccountsFacade uni, String group, String... ids) {
        for (String id : ids) {
            uni.assignToGroup(group, id);
        }
    }

    private static void show(UniAccountsFacade uni, String id, String services) {
        System.out.println(String.format("  %-9s %-16s : %s", id, uni.getAccount(id).getFullName(), services));
    }

    private static void print(String text) {
        for (String line : text.split("\n")) {
            System.out.println("    " + line);
        }
    }

    private static void title(String text) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < text.length() + 4; i++) {
            line.append('=');
        }
        System.out.println(line);
        System.out.println("  " + text);
        System.out.println(line);
    }

    private static void section(String text) {
        System.out.println();
        System.out.println(">> " + text);
    }
}
