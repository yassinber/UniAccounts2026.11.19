package dz.univ.uniaccounts.state;

/**
 * Pattern: STATE — Role: ConcreteState
 * PENDING: the account was just created and waits for activation. It cannot be used yet.
 */
public class PendingState implements AccountState {

    @Override
    public AccountStatus getStatus() {
        return AccountStatus.PENDING;
    }

    @Override
    public AccountState activate() {
        return new ActiveState();
    }

    @Override
    public AccountState suspend() {
        throw new IllegalStateException("cannot suspend a PENDING account (it was never activated)");
    }

    @Override
    public AccountState close() {
        return new ClosedState(); // e.g. the student withdrew before activation
    }

    @Override
    public void login() {
        throw new IllegalStateException("login refused: account is PENDING (not activated yet)");
    }
}
