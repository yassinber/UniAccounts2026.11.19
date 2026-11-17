package dz.univ.uniaccounts.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A student's account.
 *
 * Pattern: BUILDER   — Role: Product (built by StudentAccountBuilder)
 * Pattern: PROTOTYPE — Role: ConcretePrototype (knows how to copy itself)
 *
 * Mandatory fields : id, fullName, email
 * Optional fields  : phone (null), level ("L1"), major, scholarship (false), courses (empty)
 *
 * There is no public constructor: an account is created either with the
 * Builder or by copying an existing account (Prototype).
 */
public class StudentAccount implements AccountPrototype {

    // Identity: not final, because a copy made from a template receives a new identity.
    private String id;
    private String fullName;
    private String email;

    // Profile
    private final String phone;
    private final String level;
    private final String major;
    private final boolean scholarship;
    private final List<String> courses;

    /**
     * Builder: package-private constructor, only StudentAccountBuilder (same package)
     * can call it, after it has validated the data.
     */
    StudentAccount(StudentAccountBuilder b) {
        this.id = b.getId();
        this.fullName = b.getFullName();
        this.email = b.getEmail();
        this.phone = b.getPhone();
        this.level = b.getLevel();
        this.major = b.getMajor();
        this.scholarship = b.isScholarship();
        this.courses = new ArrayList<String>(b.getCourses()); // defensive copy
    }

    /**
     * Prototype: copy constructor. The list of courses is copied into a NEW list
     * (deep copy). With a shallow copy, the template and all its copies would
     * share the same list: adding a course to one student would add it to everybody.
     */
    private StudentAccount(StudentAccount source) {
        this.id = source.id;
        this.fullName = source.fullName;
        this.email = source.email;
        this.phone = source.phone;
        this.level = source.level;
        this.major = source.major;
        this.scholarship = source.scholarship;
        this.courses = new ArrayList<String>(source.courses); // deep copy of the list
    }

    /** Prototype: returns an independent copy of this account. */
    @Override
    public StudentAccount copy() {
        return new StudentAccount(this);
    }

    /**
     * Prototype: copy this account (typically a batch template) and give the copy
     * a new identity. Only the copy is modified; this object is left untouched.
     * The new identity goes through the same validation rules as the Builder.
     */
    public StudentAccount copyWithIdentity(String newId, String newFullName, String newEmail) {
        AccountRules.checkId(newId);
        AccountRules.checkFullName(newFullName);
        AccountRules.checkEmail(newEmail);
        StudentAccount clone = copy();
        clone.id = newId.trim();
        clone.fullName = newFullName.trim();
        clone.email = newEmail.trim();
        return clone;
    }

    /** Registers the student in one more course. */
    public void addCourse(String course) {
        if (course == null || course.trim().isEmpty()) {
            throw new IllegalArgumentException("course name must not be empty");
        }
        courses.add(course.trim());
    }

    // ---------------------------------------------------------------- getters

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getLevel() {
        return level;
    }

    public String getMajor() {
        return major;
    }

    public boolean isScholarship() {
        return scholarship;
    }

    /** Read-only view: the list can only be changed through addCourse(). */
    public List<String> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    @Override
    public String toString() {
        return "StudentAccount{id=" + id
                + ", fullName=" + fullName
                + ", email=" + email
                + ", phone=" + phone
                + ", level=" + level
                + ", major=" + major
                + ", scholarship=" + scholarship
                + ", courses=" + courses + "}";
    }
}
