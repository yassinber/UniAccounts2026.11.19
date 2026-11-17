package dz.univ.uniaccounts.export;

import dz.univ.uniaccounts.model.StudentAccount;

import java.util.List;

/**
 * Pattern: STRATEGY — Role: Strategy (interface)
 *
 * Problem: accounts must be exported in several formats (at least CSV and JSON).
 * Putting "if (format.equals("csv")) ... else if ("json") ..." in one method makes
 * that method grow with every new format.
 *
 * Solution: one class per format, all with the same method. The Context
 * (AccountExporter) receives one of them and can switch at run time.
 * Adding a format (XML...) = adding a class, without touching the others.
 */
public interface ExportStrategy {

    /** Short name of the format, e.g. "csv". */
    String formatName();

    /** Converts the accounts to text in this format. */
    String export(List<StudentAccount> accounts);
}
