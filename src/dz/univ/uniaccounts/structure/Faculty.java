package dz.univ.uniaccounts.structure;

/** Pattern: COMPOSITE — Role: Composite (contains departments). */
public class Faculty extends CompositeUnit {

    public Faculty(String name) {
        super(name);
    }

    @Override
    public String getType() {
        return "Faculty";
    }
}
