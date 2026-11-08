@echo off
title KEC Student Sports Evaluator - CLI Mode
cls
echo =======================================================================
echo          KONGU ENGINEERING COLLEGE - SPORTS EVALUATOR
echo =======================================================================
echo.

cd /d "%~dp0"

echo [1/2] Checking build tools...
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Apache Maven detected. Launching application via Maven...
    echo.
    call mvn compile exec:java -Dexec.mainClass="com.kec.sports.MainApp"
    goto end
)

echo [WARNING] Maven not found in PATH. Checking Java compiler...
where javac >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Neither Maven nor javac found in system PATH.
    echo Please ensure JDK 17 or above is installed and added to PATH.
    pause
    exit /b 1
)

echo [INFO] Compiling Java classes directly...
if not exist "bin" mkdir bin
javac -encoding UTF-8 -d bin src\main\java\com\kec\sports\model\*.java src\main\java\com\kec\sports\util\*.java src\main\java\com\kec\sports\dao\*.java src\main\java\com\kec\sports\export\*.java src\main\java\com\kec\sports\evaluation\*.java src\main\java\com\kec\sports\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)

echo [2/2] Starting application...
echo.
java -cp "bin;src\main\resources" com.kec.sports.MainApp

:end
echo.
pause
