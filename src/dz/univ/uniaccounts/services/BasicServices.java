package dz.univ.uniaccounts.services;

/**
 * Pattern: DECORATOR — Role: ConcreteComponent
 * What every student has, with no extra service. It is the core that decorators wrap.
 */
public class BasicServices implements StudentServices {

    @Override
    public String getDescription() {
        return "Basic account";
    }

    @Override
    public boolean canAccess(String service) {
        return false; // no extra service by default
    }
}
