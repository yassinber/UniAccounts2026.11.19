package dz.univ.uniaccounts.services;

/** Pattern: DECORATOR — Role: ConcreteDecorator (adds internet access). */
public class InternetService extends ServiceDecorator {

    public static final String NAME = "INTERNET";

    public InternetService(StudentServices wrapped) {
        super(wrapped, NAME);
    }

    @Override
    protected String label() {
        return "Internet";
    }
}
