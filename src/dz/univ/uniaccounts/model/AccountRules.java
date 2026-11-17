package dz.univ.uniaccounts.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Validation rules for {@link StudentAccount}, taken from the assignment table.
 * Used by the Builder (build) and by the Prototype (copyWithIdentity),
 * so the same rules are applied whatever way an account is created.
 */
public final class AccountRules {

    /** Allowed values for the "level" field. */
    public static final List<String> LEVELS =
            Collections.unmodifiableList(Arrays.asList("L1", "L2", "L3", "M1", "M2"));

    /** Default level when none is given. */
    public static final String DEFAULT_LEVEL = "L1";

    private AccountRules() {
        // utility class: no instances
    }

    /** id: mandatory, not empty. (Uniqueness is checked by the repository.) */
    public static void checkId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("id is mandatory and must not be empty");
        }
    }

    /** fullName: mandatory. */
    public static void checkFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("fullName is mandatory");
        }
    }

    /** email: mandatory and must contain '@'. */
    public static void checkEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("email must contain '@' (got: " + email + ")");
        }
    }

    /** level: one of L1, L2, L3, M1, M2. */
    public static void checkLevel(String level) {
        if (!LEVELS.contains(level)) {
            throw new IllegalArgumentException("level must be one of " + LEVELS + " (got: " + level + ")");
        }
    }
}
