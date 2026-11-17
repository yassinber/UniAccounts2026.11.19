package dz.univ.uniaccounts.model;

/**
 * Pattern: PROTOTYPE — Role: Prototype (interface)
 *
 * An object that knows how to copy itself. The client asks the object
 * for a copy instead of building a new one field by field.
 */
public interface AccountPrototype {

    /** Returns an independent (deep) copy of this account. */
    StudentAccount copy();
}
