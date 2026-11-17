#!/bin/sh
# UniAccounts - compile and run (Linux / macOS / Git Bash)
#   ./run.sh          compile + run the Work 2 demo (complete system, through the Facade)
#   ./run.sh work1    compile + run the Work 1 demo (Builder, Singleton, Prototype)
#   ./run.sh test     compile + run all automated tests (Work 1 + Work 2)
set -e
cd "$(dirname "$0")"

rm -rf out
mkdir -p out
find src test -name "*.java" > out/sources.txt
javac -encoding UTF-8 -d out @out/sources.txt

case "$1" in
  work1) java -cp out dz.univ.uniaccounts.app.Work1Demo ;;
  test)  java -cp out dz.univ.uniaccounts.AllTests ;;
  *)     java -cp out dz.univ.uniaccounts.app.Work2Demo ;;
esac
