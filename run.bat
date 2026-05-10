@echo off
setlocal
echo Lancement de l'application HR Management...

:: 1. Tentative avec Maven global
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Utilisation de Maven...
    call mvn compile exec:java
    if %ERRORLEVEL% EQU 0 goto end
)

:: 2. Tentative avec le Maven Wrapper
if exist mvnw.cmd (
    echo Tentative avec Maven Wrapper...
    call mvnw.cmd compile exec:java
    if %ERRORLEVEL% EQU 0 goto end
    echo Le Maven Wrapper a echoue.
)

:: 3. Repli sur la compilation manuelle (Methode verifiee)
echo Tentative de lancement manuel...

:: Creation du dossier target s'il n'existe pas
if not exist target\classes mkdir target\classes

:: Compilation
echo Compilation des sources...
dir /s /b src\main\java\*.java > sources.txt
javac -cp "lib/*" -d target\classes @sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo Erreur lors de la compilation.
    del sources.txt
    pause
    exit /b 1
)
del sources.txt

:: Execution
echo Demarrage de l'application...
java -cp "target\classes;lib/*" com.hrmanager.App

:end
pause