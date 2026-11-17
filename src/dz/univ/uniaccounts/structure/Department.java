package dz.univ.uniaccounts.structure;

/** Pattern: COMPOSITE — Role: Composite (contains groups). */
public class Department extends CompositeUnit {

    public Department(String name) {
        super(name);
    }

    @Override
    public String getType() {
        return "Department";
    }
}
