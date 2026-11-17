package dz.univ.uniaccounts.export;

import dz.univ.uniaccounts.model.StudentAccount;

import java.util.List;

/**
 * Pattern: STRATEGY — Role: Context
 * Holds the current export strategy and delegates the work to it.
 * It does not know any concrete format.
 */
public class AccountExporter {

    private ExportStrategy strategy;

    public AccountExporter(ExportStrategy strategy) {
        this.strategy = requireStrategy(strategy);
    }

    /** Changes the format at run time. */
    public void setStrategy(ExportStrategy strategy) {
        this.strategy = requireStrategy(strategy);
    }

    private static ExportStrategy requireStrategy(ExportStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("strategy must not be null");
        }
        return strategy;
    }

    public ExportStrategy getStrategy() {
        return strategy;
    }

    public String export(List<StudentAccount> accounts) {
        return strategy.export(accounts);
    }
}
