# UniAccounts — University Student Account Management System

Java practical work on **Design Patterns** — Work 1: the core of the system with the creational patterns.

| Pattern | Where | Problem solved |
|---|---|---|
| **Builder** | `model/StudentAccountBuilder` → `model/StudentAccount` | 8 fields, 5 optional: readable creation + validation in `build()` |
| **Singleton** | `repository/AccountRepository`, `audit/AuditLogger` | exactly one repository and one audit log |
| **Prototype** | `model/StudentAccount.copy()`, `template/AccountTemplateRegistry` | create a batch of students by copying a template |

## Quick start

```sh
./run.sh          # Linux / macOS   (run.bat on Windows)
./run.sh test     # automated tests
```

Requires JDK 8+. Full instructions: [docs/HOW_TO_RUN.md](docs/HOW_TO_RUN.md).

## Documentation

- [docs/HOW_TO_RUN.md](docs/HOW_TO_RUN.md) — how to compile and run
- [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md) — requirements analysis and plan
- [docs/uml/work1-class-diagram.png](docs/uml/work1-class-diagram.png) — UML class diagram
- [docs/reports/Rapport_Work1.pdf](docs/reports/Rapport_Work1.pdf) — Work 1 report
- [docs/screenshots/](docs/screenshots/) — real program output
