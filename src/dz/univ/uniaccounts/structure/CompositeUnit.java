package dz.univ.uniaccounts.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pattern: COMPOSITE — Role: Composite
 * A node that contains other nodes (sub-units or accounts).
 * countAccounts() = sum of countAccounts() of the children (recursion).
 */
public abstract class CompositeUnit extends UniversityComponent {

    private final List<UniversityComponent> children = new ArrayList<UniversityComponent>();

    protected CompositeUnit(String name) {
        super(name);
    }

    public void add(UniversityComponent child) {
        if (child == null) {
            throw new IllegalArgumentException("child must not be null");
        }
        if (child == this) {
            throw new IllegalArgumentException("a unit cannot contain itself");
        }
        children.add(child);
    }

    public void remove(UniversityComponent child) {
        children.remove(child);
    }

    public List<UniversityComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public int countAccounts() {
        int total = 0;
        for (UniversityComponent child : children) {
            total += child.countAccounts();  // same call for a leaf or a sub-tree
        }
        return total;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + getType() + " " + getName() + "  [" + countAccounts() + " accounts]");
        for (UniversityComponent child : children) {
            child.print(indent + "    ");
        }
    }
}
