package dz.univ.uniaccounts.template;

import dz.univ.uniaccounts.model.StudentAccount;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pattern: PROTOTYPE — Role: Prototype registry (client / prototype manager)
 *
 * Problem: every year hundreds of students join the same major and level and
 * follow the same courses. Building each account from scratch repeats the same
 * data hundreds of times.
 *
 * Solution: prepare ONE template per batch (e.g. "INFO-L1"), store it here,
 * then create each student by COPYING the template and changing only the
 * identity (id, fullName, email).
 *
 * Templates are models, not real students: they are NOT saved in the repository.
 */
public class AccountTemplateRegistry {

    private final Map<String, StudentAccount> templates = new LinkedHashMap<String, StudentAccount>();

    /**
     * Stores a template under a key. A copy is stored, so later changes to the
     * object passed by the caller do not alter the registered template.
     */
    public void register(String key, StudentAccount template) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("template key must not be empty");
        }
        if (template == null) {
            throw new IllegalArgumentException("template must not be null");
        }
        templates.put(key, template.copy());
    }

    /** Creates a new account by cloning the template and giving it a new identity. */
    public StudentAccount createFrom(String key, String id, String fullName, String email) {
        return getTemplate(key).copyWithIdentity(id, fullName, email);
    }

    /** Returns the registered template itself (read access, e.g. to display it). */
    public StudentAccount getTemplate(String key) {
        StudentAccount template = templates.get(key);
        if (template == null) {
            throw new IllegalArgumentException("unknown template: " + key);
        }
        return template;
    }

    public boolean contains(String key) {
        return templates.containsKey(key);
    }

    public List<String> keys() {
        return new ArrayList<String>(templates.keySet());
    }
}
