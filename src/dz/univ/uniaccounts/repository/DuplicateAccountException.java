package dz.univ.uniaccounts.repository;

/**
 * Thrown when an account is saved with an id that already exists in the repository
 * (the assignment requires the id to be unique).
 */
public class DuplicateAccountException extends RuntimeException {

    private final String accountId;

    public DuplicateAccountException(String accountId) {
        super("an account with id " + accountId + " already exists");
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
