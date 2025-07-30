@echo off
echo ========================================
echo    DIAGNOSTIC PLUGIN LEVEL - DEBUG
echo ========================================
echo.

echo [1/6] Verification de la structure du plugin...
if exist "target\LEVEL-1.13-shaded.jar" (
    echo ✓ JAR principal trouve: target\LEVEL-1.13-shaded.jar
    for %%A in ("target\LEVEL-1.13-shaded.jar") do echo   Taille: %%~zA bytes
) else (
    echo ✗ JAR principal manquant: target\LEVEL-1.13-shaded.jar
    echo   ERREUR CRITIQUE: Le plugin n'a pas ete compile!
    pause
    exit /b 1
)

echo.
echo [2/6] Verification du plugin.yml...
if exist "src\main\resources\plugin.yml" (
    echo ✓ plugin.yml trouve
    echo   Contenu:
    type "src\main\resources\plugin.yml"
) else (
    echo ✗ plugin.yml manquant
)

echo.
echo [3/6] Verification des classes principales...
set "classes_missing=0"

if exist "target\classes\fr\rudy\newhorizon\Main.class" (
    echo ✓ Main.class compile
) else (
    echo ✗ Main.class manquant
    set "classes_missing=1"
)

if exist "target\classes\fr\rudy\newhorizon\commands\LevelCommand.class" (
    echo ✓ LevelCommand.class compile
) else (
    echo ✗ LevelCommand.class manquant
    set "classes_missing=1"
)

if exist "target\classes\fr\rudy\newhorizon\commands\StatsCommand.class" (
    echo ✓ StatsCommand.class compile
) else (
    echo ✗ StatsCommand.class manquant
    set "classes_missing=1"
)

echo.
echo [4/6] Verification des dependances dans le JAR...
echo   Extraction du contenu du JAR pour verification...
if exist "temp_jar_check" rmdir /s /q "temp_jar_check"
mkdir "temp_jar_check"
cd "temp_jar_check"
jar -tf "..\target\LEVEL-1.13-shaded.jar" | findstr /C:"plugin.yml" >nul
if %errorlevel%==0 (
    echo ✓ plugin.yml present dans le JAR
) else (
    echo ✗ plugin.yml absent du JAR
)

jar -tf "..\target\LEVEL-1.13-shaded.jar" | findstr /C:"fr/rudy/newhorizon/Main.class" >nul
if %errorlevel%==0 (
    echo ✓ Main.class present dans le JAR
) else (
    echo ✗ Main.class absent du JAR
)
cd ..
rmdir /s /q "temp_jar_check"

echo.
echo [5/6] Verification des erreurs potentielles dans le code...
echo   Recherche de problemes connus:

findstr /n "new StatsGUIListener" "src\main\java\fr\rudy\newhorizon\Main.java"
if %errorlevel%==0 (
    echo ⚠️  PROBLEME DETECTE: Duplicate StatsGUIListener registration
)

findstr /n "sessionStatManager" "src\main\java\fr\rudy\newhorizon\Main.java" | find /c "sessionStatManager" >nul
echo   sessionStatManager utilise plusieurs fois - verification necessaire

echo.
echo [6/6] Recommandations de debug...
echo.
echo POUR TESTER LE PLUGIN:
echo 1. Copiez target\LEVEL-1.13-shaded.jar dans plugins\ du serveur
echo 2. Demarrez le serveur et verifiez les logs
echo 3. Cherchez ces messages dans les logs:
echo    - "✅ LEVEL plugin active !"
echo    - Erreurs Java (Exception, Error)
echo.
echo LOGS A SURVEILLER:
echo - [ERROR] Could not load plugin
echo - ClassNotFoundException
echo - NoSuchMethodException
echo - SQLException
echo.
echo Si le plugin ne se charge pas, verifiez:
echo - Java 17+ sur le serveur
echo - DatabaseAPI.jar present (optionnel)
echo - PlaceholderAPI.jar present (optionnel)
echo.
pause