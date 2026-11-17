package dz.univ.uniaccounts.services;

/**
 * Pattern: DECORATOR — Role: Decorator (abstract)
 *
 * IS a StudentServices (same interface, so it can replace the object it wraps)
 * and HAS a StudentServices (the wrapped object, to which it delegates).
 * Subclasses add one service each.
 */
public abstract class ServiceDecorator implements StudentServices {

    protected final StudentServices wrapped;
    private final String serviceName;

    protected ServiceDecorator(StudentServices wrapped, String serviceName) {
        if (wrapped == null) {
            throw new IllegalArgumentException("wrapped services must not be null");
        }
        this.wrapped = wrapped;
        this.serviceName = serviceName;
    }

    /** Adds this service's name to the description of the wrapped object. */
    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + " + label();
    }

    /** Access to this service, otherwise ask the wrapped object. */
    @Override
    public boolean canAccess(String service) {
        return serviceName.equalsIgnoreCase(service) || wrapped.canAccess(service);
    }

    /** Display name of the added service, e.g. "Library". */
    protected abstract String label();
}
