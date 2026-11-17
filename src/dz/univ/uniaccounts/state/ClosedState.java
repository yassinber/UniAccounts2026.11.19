package dz.univ.uniaccounts.state;

/**
 * Pattern: STATE — Role: ConcreteState
 * CLOSED: final state (graduation or withdrawal). Every operation is refused.
 */
public class ClosedState implements AccountState {

    @Override
    public AccountStatus getStatus() {
        return AccountStatus.CLOSED;
    }

    @Override
    public AccountState activate() {
        throw new IllegalStateException("cannot activate a CLOSED account");
    }

    @Override
    public AccountState suspend() {
        throw new IllegalStateException("cannot suspend a CLOSED account");
    }

    @Override
    public AccountState close() {
        throw new IllegalStateException("account is already CLOSED");
    }

    @Override
    public void login() {
        throw new IllegalStateException("login refused: account is CLOSED");
    }
}
