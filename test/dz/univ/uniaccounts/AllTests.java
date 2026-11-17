package dz.univ.uniaccounts;

/**
 * Runs the tests of Work 1 then Work 2. Running Work 1's tests again after
 * Work 2 checks that the extension did not break the core.
 * Run: java -cp out dz.univ.uniaccounts.AllTests
 */
public class AllTests {

    public static void main(String[] args) {
        System.out.println("========== Work 1 tests ==========");
        Work1Tests.run();
        System.out.println();
        System.out.println("========== Work 2 tests ==========");
        Work2Tests.run();
        Check.finish();
    }
}
