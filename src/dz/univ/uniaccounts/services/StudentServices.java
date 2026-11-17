package dz.univ.uniaccounts.services;

/**
 * Pattern: DECORATOR — Role: Component (interface)
 *
 * Problem: each student gets extra services in different combinations
 * (library only; library + restaurant + internet; ...). One subclass per
 * combination would explode: 3 services = 2^3 = 8 classes, 4 services = 16.
 *
 * Solution: start from the basic services and WRAP them with one decorator per
 * extra service, at run time, in any combination. Each decorator adds its own
 * behaviour, then delegates to the object it wraps.
 */
public interface StudentServices {

    /** e.g. "Basic account + Library + Internet" */
    String getDescription();

    /** true if this combination gives access to the service (e.g. "LIBRARY"). */
    boolean canAccess(String service);
}
