package dz.univ.uniaccounts.structure;

import dz.univ.uniaccounts.model.StudentAccount;

/**
 * Pattern: COMPOSITE — Role: Leaf
 * One student account in the tree. It has no children and counts as 1.
 *
 * It WRAPS a StudentAccount instead of making StudentAccount extend
 * UniversityComponent: this way the Work 1 class is not modified again.
 */
public class AccountLeaf extends UniversityComponent {

    private final StudentAccount account;

    public AccountLeaf(StudentAccount account) {
        super(account == null ? null : account.getId());
        this.account = account;
    }

    public StudentAccount getAccount() {
        return account;
    }

    @Override
    public String getType() {
        return "Account";
    }

    @Override
    public int countAccounts() {
        return 1;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "- " + account.getId() + " " + account.getFullName()
                + " (" + account.getLevel() + ", " + account.getStatus() + ")");
    }
}
