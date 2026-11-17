package dz.univ.uniaccounts.structure;

import dz.univ.uniaccounts.model.StudentAccount;

/** Pattern: COMPOSITE — Role: Composite (a group "fouj", contains student accounts). */
public class StudentGroup extends CompositeUnit {

    public StudentGroup(String name) {
        super(name);
    }

    @Override
    public String getType() {
        return "Group";
    }

    /** Convenience: wraps the account in a leaf and adds it. */
    public void addAccount(StudentAccount account) {
        add(new AccountLeaf(account));
    }
}
