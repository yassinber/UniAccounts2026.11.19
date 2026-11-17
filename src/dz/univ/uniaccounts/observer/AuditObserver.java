package dz.univ.uniaccounts.observer;

import dz.univ.uniaccounts.audit.AuditLogger;

/**
 * Pattern: OBSERVER — Role: ConcreteObserver
 * Writes every event into the AuditLogger.
 * (Reuses the Singleton of Work 1: it does not create its own log.)
 */
public class AuditObserver implements AccountObserver {

    @Override
    public void update(AccountEvent event) {
        AuditLogger.getInstance().log("EVENT", event.getType() + " " + event.getAccount().getId()
                + " (status=" + event.getAccount().getStatus() + ")");
        System.out.println("    [AUDIT]     " + event + " written to AuditLogger");
    }
}
