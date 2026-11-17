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
| Compile + run the Work 2 demo (complete system) | `./run.sh` | `run.bat` |
| Compile + run the Work 1 demo | `./run.sh work1` | `run.bat work1` |
| Compile + run all the tests (Work 1 + Work 2) | `./run.sh test` | `run.bat test` |

The scripts delete and recreate the `out/` folder, compile every `.java` file of `src/` and `test/`, then run the program.

## Run by hand / التشغيل يدوياً

```sh
# 1. compile (from the project root)
mkdir out
javac -encoding UTF-8 -d out $(find src test -name "*.java")        # Linux / macOS
#  Windows: dir /s /b src\*.java test\*.java > sources.txt
#           javac -encoding UTF-8 -d out @sources.txt

# 2. run a demo
java -cp out dz.univ.uniaccounts.app.Work2Demo     # Work 2: complete system through the Facade
java -cp out dz.univ.uniaccounts.app.Work1Demo     # Work 1: Builder, Singleton, Prototype

# 3. run the automated tests (140 checks: 49 for Work 1 + 91 for Work 2)
java -cp out dz.univ.uniaccounts.AllTests
```

## Run in an IDE / التشغيل من بيئة تطوير (IntelliJ, Eclipse, NetBeans, VS Code)

1. Open the project folder.
2. Mark `src` and `test` as **source folders**.
3. Run `dz.univ.uniaccounts.app.Work2Demo` or `Work1Demo` (right click → Run), and `dz.univ.uniaccounts.AllTests` for the tests.
4. If accented characters look wrong, set the project encoding to **UTF-8**.

## What the Work 2 demo shows / ماذا يعرض برنامج العمل الثاني

| Section | Pattern | What you see |
|---|---|---|
| 1 | Facade + Observer | 3 students registered (Builder, then Prototype template); each CREATED event reaches the 3 observers |
| 2 | State | full life cycle; login refused when PENDING / SUSPENDED; refused transitions send no notification |
| 3 | Adapter | import of the 10 legacy records: 9 imported, duplicate `19990008` rejected |
| 4 | Composite | the university tree and `countAccounts()` asked at every level |
| 5 | Strategy | the same accounts exported as CSV then JSON |
| 6 | Decorator | different combinations of library / restaurant / internet |
| 7 | Observer | dashboard counts by event type, then the complete audit log |

## What the Work 1 demo shows / ماذا يعرض برنامج العمل الأول

| Section | Pattern | What you see |
|---|---|---|
| 1 | Builder | an account with only the 3 mandatory fields (defaults applied) and a full account |
| 2 | Builder | `build()` rejects: email without `@`, level `L4`, empty id, missing name |
| 3 | Singleton | `getInstance()` always returns the same repository / audit log |
| 4 | Singleton | the repository rejects a duplicate id |
| 5 | Prototype | 3 students copied from the `INFO-L1` template; changing one copy's courses does not change the others |
| 6 | Singleton | the audit log with every operation |

## Project layout / بنية المشروع

See the [README](../README.md#project-layout).

To regenerate the UML images from their sources (needs Graphviz):

```sh
dot -Tpng docs/uml/work2-class-diagram.dot -o docs/uml/work2-class-diagram.png
dot -Tpng docs/uml/work2-state-diagram.dot -o docs/uml/work2-state-diagram.png
```
