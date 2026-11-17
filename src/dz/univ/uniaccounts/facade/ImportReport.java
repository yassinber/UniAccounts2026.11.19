package dz.univ.uniaccounts.facade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Result of an import from the old system: what was imported and what was rejected, and why. */
public class ImportReport {

    private final List<String> importedIds = new ArrayList<String>();
    private final List<String> rejected = new ArrayList<String>();

    void imported(String id) {
        importedIds.add(id);
    }

    void rejected(String reason) {
        rejected.add(reason);
    }

    public List<String> getImportedIds() {
        return Collections.unmodifiableList(importedIds);
    }

    public List<String> getRejected() {
        return Collections.unmodifiableList(rejected);
    }

    @Override
    public String toString() {
        return importedIds.size() + " imported, " + rejected.size() + " rejected";
    }
}
