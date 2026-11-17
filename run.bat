@echo off
REM UniAccounts - compile and run (Windows)
REM   run.bat          compile + run the Work 2 demo (complete system, through the Facade)
REM   run.bat work1    compile + run the Work 1 demo (Builder, Singleton, Prototype)
REM   run.bat test     compile + run all automated tests (Work 1 + Work 2)
cd /d "%~dp0"

if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java test\*.java > out\sources.txt
javac -encoding UTF-8 -d out @out\sources.txt
if errorlevel 1 exit /b 1

if "%1"=="work1" (
  java -cp out dz.univ.uniaccounts.app.Work1Demo
) else if "%1"=="test" (
  java -cp out dz.univ.uniaccounts.AllTests
) else (
  java -cp out dz.univ.uniaccounts.app.Work2Demo
)
