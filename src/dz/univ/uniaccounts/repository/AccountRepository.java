package dz.univ.uniaccounts.repository;

import dz.univ.uniaccounts.audit.AuditLogger;
import dz.univ.uniaccounts.model.StudentAccount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pattern: SINGLETON — Role: Singleton
 *
 * The central repository of all accounts. There must be exactly ONE:
 * if every part of the program created its own repository, an account saved
 * in one copy would be missing from the others.
 *
 * Same implementation as AuditLogger (holder idiom: private constructor,
 * lazy and thread-safe creation by the JVM).
 *
 * The repository is also the place where the uniqueness of the id is checked,
 * because it is the only object that knows all the existing accounts.
 * Every operation is written to the AuditLogger (the other Singleton).
 */
public final class AccountRepository {

    /** Keeps insertion order, so listings are predictable. */
    private final Map<String, StudentAccount> accounts = new LinkedHashMap<String, StudentAccount>();

    /** Singleton: private constructor. */
    private AccountRepository() {
    }

    /** Singleton: holder class, loaded on first use only. */
    private static final class Holder {
        private static final AccountRepository INSTANCE = new AccountRepository();
    }

    /** Singleton: global access point. */
    public static AccountRepository getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Saves a new account.
     * @throws DuplicateAccountException if the id is already used
     */
    public synchronized void save(StudentAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("account must not be null");
        }
        if (accounts.containsKey(account.getId())) {
            AuditLogger.getInstance().log("REJECT", "duplicate id " + account.getId()
                    + " (" + account.getFullName() + ")");
            throw new DuplicateAccountException(account.getId());
        }
        accounts.put(account.getId(), account);
        AuditLogger.getInstance().log("SAVE", "account " + account.getId()
                + " (" + account.getFullName() + ")");
    }

    /** Returns the account with this id, or null if there is none. */
    public synchronized StudentAccount findById(String id) {
        return accounts.get(id);
    }

    public synchronized boolean exists(String id) {
        return accounts.containsKey(id);
    }

    /** Read-only snapshot of all accounts, in insertion order. */
    public synchronized List<StudentAccount> findAll() {
        return Collections.unmodifiableList(new ArrayList<StudentAccount>(accounts.values()));
    }

    public synchronized int count() {
        return accounts.size();
    }

    /** Empties the repository. Used by the tests and demos to start from a clean state. */
    public synchronized void clear() {
        accounts.clear();
    }
}
