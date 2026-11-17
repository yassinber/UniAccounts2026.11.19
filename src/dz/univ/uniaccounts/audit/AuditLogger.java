package dz.univ.uniaccounts.audit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Pattern: SINGLETON — Role: Singleton
 *
 * The one and only audit log of the application. Every part of the program
 * that calls AuditLogger.getInstance() gets the SAME object, so all operations
 * end up in a single log instead of being scattered across several copies.
 *
 * Implementation: "Initialization-on-demand holder" idiom.
 *  - the constructor is private: nobody can write "new AuditLogger()";
 *  - the instance lives in a nested static class (Holder) that the JVM loads
 *    only on the first call to getInstance() -> lazy creation;
 *  - the JVM guarantees that a class is initialized once -> thread-safe
 *    without "synchronized".
 */
public final class AuditLogger {

    private final List<String> entries = new ArrayList<String>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    /** Singleton: private constructor. */
    private AuditLogger() {
    }

    /** Singleton: holder class, loaded on first use only. */
    private static final class Holder {
        private static final AuditLogger INSTANCE = new AuditLogger();
    }

    /** Singleton: global access point. */
    public static AuditLogger getInstance() {
        return Holder.INSTANCE;
    }

    /** Records one operation, e.g. log("SAVE", "account 20260001"). */
    public synchronized void log(String action, String details) {
        entries.add(timeFormat.format(new Date()) + "  " + pad(action) + " " + details);
    }

    /** Read-only view of all entries, in order. */
    public synchronized List<String> getEntries() {
        return Collections.unmodifiableList(new ArrayList<String>(entries));
    }

    public synchronized int size() {
        return entries.size();
    }

    public synchronized void printAll() {
        System.out.println("---- Audit log (" + entries.size() + " entries) ----");
        for (String e : entries) {
            System.out.println("  " + e);
        }
    }

    /** Empties the log. Used by the tests and demos to start from a clean state. */
    public synchronized void clear() {
        entries.clear();
    }

    private static String pad(String action) {
        StringBuilder sb = new StringBuilder("[" + action + "]");
        while (sb.length() < 12) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
