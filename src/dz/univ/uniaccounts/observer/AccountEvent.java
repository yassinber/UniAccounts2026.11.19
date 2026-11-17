package dz.univ.uniaccounts.observer;

import dz.univ.uniaccounts.model.StudentAccount;

/**
 * Pattern: OBSERVER — the message sent by the Subject to every Observer:
 * what happened (type) and to which account.
 */
public final class AccountEvent {

    private final EventType type;
    private final StudentAccount account;

    public AccountEvent(EventType type, StudentAccount account) {
        this.type = type;
        this.account = account;
    }

    public EventType getType() {
        return type;
    }

    public StudentAccount getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return type + " " + account.getId();
    }
}
