package dz.univ.uniaccounts.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Pattern: BUILDER — Role: Builder
 *
 * Problem: StudentAccount has 8 fields, 6 of them are Strings and 5 are optional.
 * A constructor with 8 arguments is unreadable and the compiler cannot detect
 * a swap such as (..., "Informatique", "L2", ...) instead of (..., "L2", "Informatique", ...).
 *
 * Solution:
 *  - the 3 MANDATORY fields are parameters of the Builder's constructor,
 *    so they cannot be forgotten;
 *  - every OPTIONAL field has its own named method, so the order does not matter;
 *  - build() checks every rule and only then creates the account:
 *    an invalid StudentAccount can never exist.
 *
 * Usage:
 *   StudentAccount a = new StudentAccountBuilder("20260001", "Yacine Bensalem", "y.b@univ.dz")
 *           .level("L2").major("Informatique").scholarship(true)
 *           .addCourse("Algorithmique")
 *           .build();
 *
 * (Kept in its own file, and not as a static nested class, because the
 * assignment asks for one class per file.)
 */
public class StudentAccountBuilder {

    // mandatory
    private final String id;
    private final String fullName;
    private final String email;

    // optional, with the default values required by the assignment
    private String phone = null;
    private String level = AccountRules.DEFAULT_LEVEL; // "L1"
    private String major = null;
    private boolean scholarship = false;
    private final List<String> courses = new ArrayList<String>(); // empty

    public StudentAccountBuilder(String id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    public StudentAccountBuilder phone(String phone) {
        this.phone = phone;
        return this;
    }

    public StudentAccountBuilder level(String level) {
        this.level = level;
        return this;
    }

    public StudentAccountBuilder major(String major) {
        this.major = major;
        return this;
    }

    public StudentAccountBuilder scholarship(boolean scholarship) {
        this.scholarship = scholarship;
        return this;
    }

    public StudentAccountBuilder addCourse(String course) {
        this.courses.add(course);
        return this;
    }

    public StudentAccountBuilder courses(List<String> courses) {
        this.courses.clear();
        if (courses != null) {
            this.courses.addAll(courses);
        }
        return this;
    }

    /**
     * Validates all the rules, then creates the account.
     * @throws IllegalArgumentException if a rule is broken
     */
    public StudentAccount build() {
        AccountRules.checkId(id);
        AccountRules.checkFullName(fullName);
        AccountRules.checkEmail(email);
        AccountRules.checkLevel(level);
        for (String c : courses) {
            if (c == null || c.trim().isEmpty()) {
                throw new IllegalArgumentException("course name must not be empty");
            }
        }
        return new StudentAccount(this);
    }

    // Package-private getters, read by StudentAccount's constructor.
    // id/fullName/email are trimmed so " 2026 " and "2026" are the same id.
    String getId() {
        return id.trim();
    }

    String getFullName() {
        return fullName.trim();
    }

    String getEmail() {
        return email.trim();
    }

    String getPhone() {
        return phone;
    }

    String getLevel() {
        return level;
    }

    String getMajor() {
        return major;
    }

    boolean isScholarship() {
        return scholarship;
    }

    List<String> getCourses() {
        return courses;
    }
}
