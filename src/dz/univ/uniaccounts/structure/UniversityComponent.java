package dz.univ.uniaccounts.structure;

/**
 * Pattern: COMPOSITE — Role: Component
 *
 * Problem: the university is a tree (university > faculties > departments >
 * groups > student accounts) and we want to ask ANY node "how many accounts do
 * you contain?" without knowing whether it is a whole faculty or one student.
 *
 * Solution: nodes (Composite) and leaves (Leaf) share this common type.
 * A leaf answers 1; a composite asks each of its children and adds the answers.
 * The client calls countAccounts() the same way on every node.
 */
public abstract class UniversityComponent {

    private final String name;

    protected UniversityComponent(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be empty");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    /** Kind of node, used for display: "University", "Faculty", ... */
    public abstract String getType();

    /** Number of student accounts in this node (recursively). */
    public abstract int countAccounts();

    /** Prints this node and its children as an indented tree. */
    public abstract void print(String indent);
}
