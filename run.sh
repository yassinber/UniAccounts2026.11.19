#!/bin/sh
# UniAccounts - compile and run (Linux / macOS / Git Bash)
#   ./run.sh          compile + run the Work 1 demo
#   ./run.sh test     compile + run the automated tests
set -e
cd "$(dirname "$0")"

rm -rf out
mkdir -p out
find src test -name "*.java" > out/sources.txt
javac -encoding UTF-8 -d out @out/sources.txt

case "$1" in
  test) java -cp out dz.univ.uniaccounts.Work1Tests ;;
  *)    java -cp out dz.univ.uniaccounts.app.Work1Demo ;;
esac
