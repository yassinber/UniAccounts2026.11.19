package dz.univ.uniaccounts.state;

/**
 * Pattern: STATE — Role: State (interface)
 *
 * Problem: the behaviour of an account depends on its status (a SUSPENDED account
 * cannot log in, a PENDING account must be activated first...). Writing
 * "if (status == ...)" in every method of StudentAccount would repeat the same
 * switch 4 times, and adding a status would mean editing all of them.
 *
 * Solution: one class per status. Each operation returns the NEXT state,
 * or throws IllegalStateException when the operation is not allowed in this state.
 *
 * Transitions (our design; the assignment only fixes the 4 statuses):
 *   PENDING   --activate--> ACTIVE      PENDING   --close--> CLOSED
 *   ACTIVE    --suspend---> SUSPENDED   ACTIVE    --close--> CLOSED
 *   SUSPENDED --activate--> ACTIVE      SUSPENDED --close--> CLOSED
 *   CLOSED    : final, every operation is refused
 *   login     : allowed only in ACTIVE
 */
public interface AccountState {

    AccountStatus getStatus();

    AccountState activate();

    AccountState suspend();

    AccountState close();

    /** Does nothing if login is allowed, throws IllegalStateException otherwise. */
    void login();
}
