package dz.univ.uniaccounts;

/**
 * Minimal assertion helper, so the tests run with the JDK alone (no JUnit download).
 */
public final class Check {

    private static int passed = 0;
    private static int failed = 0;

    private Check() {
    }

    public static void isTrue(String label, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + label);
        } else {
            failed++;
            System.out.println("  FAIL  " + label);
        }
    }

    public static void equal(String label, Object expected, Object actual) {
        boolean same = expected == null ? actual == null : expected.equals(actual);
        isTrue(label + (same ? "" : "  (expected " + expected + ", got " + actual + ")"), same);
    }

    /** Passes if the action throws an exception of the given type. */
    public static void throwsException(String label, Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
            isTrue(label + "  (no exception thrown)", false);
        } catch (Throwable t) {
            isTrue(label + (type.isInstance(t) ? "" : "  (got " + t.getClass().getSimpleName() + ")"),
                    type.isInstance(t));
        }
    }

    public static void section(String name) {
        System.out.println();
        System.out.println("[" + name + "]");
    }

    /** Prints the summary and exits with code 1 if any test failed. */
    public static void finish() {
        System.out.println();
        System.out.println("RESULT: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
