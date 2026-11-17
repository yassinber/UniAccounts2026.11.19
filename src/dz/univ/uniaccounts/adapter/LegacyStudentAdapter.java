package dz.univ.uniaccounts.adapter;

import dz.univ.uniaccounts.legacy.LegacyStudentSystem;
import dz.univ.uniaccounts.model.StudentAccount;
import dz.univ.uniaccounts.model.StudentAccountBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Pattern: ADAPTER — Role: Adapter (object adapter)
 *
 * Problem: the old system (LegacyStudentSystem, an external library we must not
 * modify) returns text lines "ID;LASTNAME;FIRSTNAME;EMAIL;LEVEL", while our system
 * works with StudentAccount objects. The two interfaces are incompatible.
 *
 * Solution: this class implements OUR interface (StudentSource) and, inside,
 * calls the old one (fetchRecords) and converts each line.
 *  - Object adapter: it HAS a LegacyStudentSystem (composition), it does not
 *    extend it, so we depend on nothing but its public method.
 *  - Every account is created with the Builder of Work 1, so legacy data goes
 *    through exactly the same validation as new accounts.
 *  - A line that cannot be converted does not stop the import: it is skipped
 *    and recorded in getRejectedRecords().
 *  - Duplicate ids are NOT handled here: only the repository knows which ids
 *    exist. The Facade saves the accounts and reports the duplicates.
 */
public class LegacyStudentAdapter implements StudentSource {

    private static final int FIELDS = 5; // ID;LASTNAME;FIRSTNAME;EMAIL;LEVEL

    private final LegacyStudentSystem legacy; // the Adaptee
    private final List<String> rejected = new ArrayList<String>();

    public LegacyStudentAdapter(LegacyStudentSystem legacy) {
        if (legacy == null) {
            throw new IllegalArgumentException("legacy system must not be null");
        }
        this.legacy = legacy;
    }

    @Override
    public List<StudentAccount> loadAccounts() {
        rejected.clear();
        List<StudentAccount> result = new ArrayList<StudentAccount>();
        for (String line : legacy.fetchRecords()) {
            try {
                result.add(convert(line));
            } catch (IllegalArgumentException e) {
                rejected.add(line + "  ->  " + e.getMessage());
            }
        }
        return result;
    }

    /** "19990001;BENSALEM;Yacine;y.bensalem@old.univ.dz;L3" -> StudentAccount */
    StudentAccount convert(String line) {
        if (line == null) {
            throw new IllegalArgumentException("empty record");
        }
        String[] parts = line.split(";", -1);
        if (parts.length != FIELDS) {
            throw new IllegalArgumentException("expected " + FIELDS + " fields, found " + parts.length);
        }
        String id = parts[0].trim();
        String lastName = parts[1].trim();
        String firstName = parts[2].trim();
        String email = parts[3].trim();
        String level = parts[4].trim();

        return new StudentAccountBuilder(id, firstName + " " + lastName, email)
                .level(level)
                .build();
    }

    /** Lines of the last import that could not be converted, with the reason. */
    public List<String> getRejectedRecords() {
        return new ArrayList<String>(rejected);
    }
}
