package dz.univ.uniaccounts.services;

/** Pattern: DECORATOR — Role: ConcreteDecorator (adds the university restaurant). */
public class RestaurantService extends ServiceDecorator {

    public static final String NAME = "RESTAURANT";

    public RestaurantService(StudentServices wrapped) {
        super(wrapped, NAME);
    }

    @Override
    protected String label() {
        return "Restaurant";
    }
}
