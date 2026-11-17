package dz.univ.uniaccounts.services;

/** Pattern: DECORATOR — Role: ConcreteDecorator (adds the library). */
public class LibraryService extends ServiceDecorator {

    public static final String NAME = "LIBRARY";

    public LibraryService(StudentServices wrapped) {
        super(wrapped, NAME);
    }

    @Override
    protected String label() {
        return "Library";
    }
}
