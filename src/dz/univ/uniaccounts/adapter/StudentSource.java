package dz.univ.uniaccounts.adapter;

import dz.univ.uniaccounts.model.StudentAccount;

import java.util.List;

/**
 * Pattern: ADAPTER — Role: Target
 * The interface OUR system expects from any source of students:
 * a list of StudentAccount objects.
 */
public interface StudentSource {

    List<StudentAccount> loadAccounts();
}
