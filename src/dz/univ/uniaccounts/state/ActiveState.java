package dz.univ.uniaccounts.state;

/**
 * Pattern: STATE — Role: ConcreteState
 * ACTIVE: the normal state. Login is allowed; the account can be suspended or closed.
 */
public class ActiveState implements AccountState {

    @Override
    public AccountStatus getStatus() {
        return AccountStatus.ACTIVE;
    }

    @Override
    public AccountState activate() {
        throw new IllegalStateException("account is already ACTIVE");
    }

    @Override
    public AccountState suspend() {
        return new SuspendedState();
    }

    @Override
    public AccountState close() {
        return new ClosedState(); // e.g. graduation
    }

    @Override
    public void login() {
        // allowed: nothing to refuse
    }
}
