package dz.univ.uniaccounts.observer;

import dz.univ.uniaccounts.model.StudentAccount;

import java.util.ArrayList;
import java.util.List;

/**
 * Pattern: OBSERVER — Role: Subject
 *
 * Problem: when something happens to an account (created, activated, suspended,
 * closed, imported) several parties must know: the audit log, the e-mail service,
 * the admin dashboard. If the code that activates an account called each of them
 * directly, it would depend on all of them, and adding a 4th party would mean
 * editing every operation.
 *
 * Solution: the Subject keeps a list of observers and only knows the
 * AccountObserver interface. publish() sends the event to every attached observer.
 *
 * The Subject is a separate object (not StudentAccount itself) so that accounts
 * do not each carry a list of observers, and so that events that happen outside
 * an account (CREATED, IMPORTED) can be published from the same place.
 */
public class AccountEventPublisher {

    private final List<AccountObserver> observers = new ArrayList<AccountObserver>();

    public void attach(AccountObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detach(AccountObserver observer) {
        observers.remove(observer);
    }

    public int observerCount() {
        return observers.size();
    }

    /** Notifies every attached observer, in the order they were attached. */
    public void publish(EventType type, StudentAccount account) {
        AccountEvent event = new AccountEvent(type, account);
        for (AccountObserver observer : new ArrayList<AccountObserver>(observers)) {
            observer.update(event);
        }
    }
}
