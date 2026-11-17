# How to run UniAccounts / طريقة تشغيل المشروع

## Requirements / المتطلبات

- **JDK 8 or newer** (tested with OpenJDK 21; the code is also compiled with `--release 8`).
- No external library, no Maven/Gradle: only `javac` and `java`.

Check your installation / للتأكد من تثبيت Java:

```sh
java -version
javac -version
```

## Run with the scripts / التشغيل بالسكربتات

| | Linux / macOS / Git Bash | Windows (cmd) |
|---|---|---|
| Compile + run the demo | `./run.sh` | `run.bat` |
| Compile + run the tests | `./run.sh test` | `run.bat test` |

The scripts delete and recreate the `out/` folder, compile every `.java` file of `src/` and `test/`, then run the program.

## Run by hand / التشغيل يدوياً

```sh
# 1. compile (from the project root)
mkdir out
javac -encoding UTF-8 -d out $(find src test -name "*.java")        # Linux / macOS
#  Windows: dir /s /b src\*.java test\*.java > sources.txt
#           javac -encoding UTF-8 -d out @sources.txt

# 2. run the Work 1 demo
java -cp out dz.univ.uniaccounts.app.Work1Demo

# 3. run the automated tests (49 checks)
java -cp out dz.univ.uniaccounts.Work1Tests
```

## Run in an IDE / التشغيل من بيئة تطوير (IntelliJ, Eclipse, NetBeans, VS Code)

1. Open the project folder.
2. Mark `src` and `test` as **source folders**.
3. Run `dz.univ.uniaccounts.app.Work1Demo` (right click → Run).
4. If accented characters look wrong, set the project encoding to **UTF-8**.

## What the demo shows / ماذا يعرض البرنامج

| Section | Pattern | What you see |
|---|---|---|
| 1 | Builder | an account with only the 3 mandatory fields (defaults applied) and a full account |
| 2 | Builder | `build()` rejects: email without `@`, level `L4`, empty id, missing name |
| 3 | Singleton | `getInstance()` always returns the same repository / audit log |
| 4 | Singleton | the repository rejects a duplicate id |
| 5 | Prototype | 3 students copied from the `INFO-L1` template; changing one copy's courses does not change the others |
| 6 | Singleton | the audit log with every operation |

## Project layout / بنية المشروع

```
src/dz/univ/uniaccounts/
├── app/         Work1Demo                                   (client / demo)
├── model/       StudentAccount, StudentAccountBuilder,
│                AccountPrototype, AccountRules              (Builder, Prototype)
├── repository/  AccountRepository, DuplicateAccountException (Singleton)
├── audit/       AuditLogger                                 (Singleton)
└── template/    AccountTemplateRegistry                     (Prototype registry)
test/dz/univ/uniaccounts/
├── Check.java        (tiny assertion helper, no JUnit needed)
└── Work1Tests.java
docs/
├── HOW_TO_RUN.md     (this file)
├── REQUIREMENTS.md   (analysis and plan)
├── uml/              (class diagram: .dot source, .png, .svg)
├── screenshots/      (images of the real program output)
└── reports/          (PDF report)
```

To regenerate the UML image from its source: `dot -Tpng docs/uml/work1-class-diagram.dot -o docs/uml/work1-class-diagram.png` (needs Graphviz).
