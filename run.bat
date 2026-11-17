@echo off
REM UniAccounts - compile and run (Windows)
REM   run.bat          compile + run the Work 1 demo
REM   run.bat test     compile + run the automated tests
cd /d "%~dp0"

if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java test\*.java > out\sources.txt
javac -encoding UTF-8 -d out @out\sources.txt
if errorlevel 1 exit /b 1

if "%1"=="test" (
  java -cp out dz.univ.uniaccounts.Work1Tests
) else (
  java -cp out dz.univ.uniaccounts.app.Work1Demo
)
