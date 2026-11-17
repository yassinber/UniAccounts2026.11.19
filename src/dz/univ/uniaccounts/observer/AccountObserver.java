package dz.univ.uniaccounts.observer;

/**
 * Pattern: OBSERVER — Role: Observer (interface)
 * Anything that wants to be told about account events implements this method.
 */
public interface AccountObserver {

    void update(AccountEvent event);
}
