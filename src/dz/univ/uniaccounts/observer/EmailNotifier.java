package dz.univ.uniaccounts.observer;

import dz.univ.uniaccounts.model.StudentAccount;

import java.util.ArrayList;
import java.util.List;

/**
 * Pattern: OBSERVER — Role: ConcreteObserver
 * Prints an e-mail to the student for every event (a real system would send it).
 * Sent messages are also kept in memory so the tests can check them.
 */
public class EmailNotifier implements AccountObserver {

    private final List<String> sent = new ArrayList<String>();

    @Override
    public void update(AccountEvent event) {
        StudentAccount a = event.getAccount();
        String message = "to " + a.getEmail() + " : \"Dear " + a.getFullName() + ", " + text(event.getType()) + "\"";
        sent.add(message);
        System.out.println("    [EMAIL]     " + message);
    }

    private static String text(EventType type) {
        switch (type) {
            case CREATED:   return "your account was created and is waiting for activation.";
            case ACTIVATED: return "your account is now active.";
            case SUSPENDED: return "your account has been suspended.";
            case CLOSED:    return "your account has been closed.";
            case IMPORTED:  return "your account was imported from the old system.";
            default:        return "your account changed.";
        }
    }

    public List<String> getSentMessages() {
        return new ArrayList<String>(sent);
    }
}
