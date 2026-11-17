package dz.univ.uniaccounts.export;

import dz.univ.uniaccounts.model.StudentAccount;

import java.util.List;

/**
 * Pattern: STRATEGY — Role: ConcreteStrategy
 * CSV: one header line "id,name,level,status", then one line per account (assignment format).
 */
public class CsvExport implements ExportStrategy {

    @Override
    public String formatName() {
        return "csv";
    }

    @Override
    public String export(List<StudentAccount> accounts) {
        StringBuilder sb = new StringBuilder("id,name,level,status\n");
        for (StudentAccount a : accounts) {
            sb.append(cell(a.getId())).append(',')
              .append(cell(a.getFullName())).append(',')
              .append(cell(a.getLevel())).append(',')
              .append(a.getStatus()).append('\n');
        }
        return sb.toString();
    }

    /** A value containing a comma or a quote is put between quotes (standard CSV rule). */
    private static String cell(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
