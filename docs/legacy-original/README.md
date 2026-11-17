# LegacyStudentSystem — original file

`LegacyStudentSystem.java` in this folder is the class **exactly as given in the assignment** (copied character for character from page 4). It is kept here as a reference and is **not compiled**.

As given, it does **not compile**:

1. After records 2, 4, 6 and 8 the line ends with `");` instead of `",`. The `);` closes `Arrays.asList(` too early, so the next string is a statement on its own → `error: not a statement` / `';' expected`.
2. It has **no `package` declaration** (default package). Since Java 1.4 a class in a named package (all our code is in `dz.univ.uniaccounts.*`) **cannot import** a class of the default package.

The compiled copy is `src/dz/univ/uniaccounts/legacy/LegacyStudentSystem.java`. It differs from this file **only** by:

- the line `package dz.univ.uniaccounts.legacy;`
- `");` replaced by `",` on the 4 lines listed above.

The data (including the duplicate id `19990008`, the missing `19990009` and the email `y. Rida@old.univ.dz`), the method name and the return type are unchanged. No line of our code adapts or patches the legacy class: all the conversion work is done in the Adapter (`adapter/LegacyStudentAdapter`).

Check it yourself:

```sh
diff docs/legacy-original/LegacyStudentSystem.java src/dz/univ/uniaccounts/legacy/LegacyStudentSystem.java
```
