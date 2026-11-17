package dz.univ.uniaccounts.export;

import dz.univ.uniaccounts.model.StudentAccount;

import java.util.List;

/**
 * Pattern: STRATEGY — Role: ConcreteStrategy
 * JSON: an array with one object per account. Written by hand (no external library).
 */
public class JsonExport implements ExportStrategy {

    @Override
    public String formatName() {
        return "json";
    }

    @Override
    public String export(List<StudentAccount> accounts) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < accounts.size(); i++) {
            StudentAccount a = accounts.get(i);
            sb.append("  {\"id\": ").append(str(a.getId()))
              .append(", \"name\": ").append(str(a.getFullName()))
              .append(", \"level\": ").append(str(a.getLevel()))
              .append(", \"status\": ").append(str(a.getStatus().name()))
              .append('}');
            if (i < accounts.size() - 1) {
                sb.append(',');
            }
            sb.append('\n');
        }
        return sb.append("]\n").toString();
    }

    /** JSON string with the special characters escaped. */
    private static String str(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\t': sb.append("\\t");  break;
                default:   sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
