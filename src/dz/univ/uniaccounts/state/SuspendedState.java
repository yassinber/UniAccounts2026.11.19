package dz.univ.uniaccounts.state;

/**
 * Pattern: STATE — Role: ConcreteState
 * SUSPENDED: temporarily blocked. No login (assignment rule). Can be re-activated or closed.
 */
public class SuspendedState implements AccountState {

    @Override
    public AccountStatus getStatus() {
        return AccountStatus.SUSPENDED;
    }

    @Override
    public AccountState activate() {
        return new ActiveState(); // re-activation after the suspension is lifted
    }

    @Override
    public AccountState suspend() {
        throw new IllegalStateException("account is already SUSPENDED");
    }

    @Override
    public AccountState close() {
        return new ClosedState();
    }

    @Override
    public void login() {
        throw new IllegalStateException("login refused: account is SUSPENDED");
    }
}
