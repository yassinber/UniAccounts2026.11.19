package dz.univ.uniaccounts.structure;

/** Pattern: COMPOSITE — Role: Composite (root of the tree, contains faculties). */
public class University extends CompositeUnit {

    public University(String name) {
        super(name);
    }

    @Override
    public String getType() {
        return "University";
    }
}
