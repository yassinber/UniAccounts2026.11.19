package dz.univ.uniaccounts.state;

/**
 * The four statuses of an account (assignment: PENDING, ACTIVE, SUSPENDED, CLOSED).
 * Used for display and export; the BEHAVIOUR of each status is in the State classes.
 */
public enum AccountStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    CLOSED
}
