@echo off
echo ================================================
echo   Generando MSI - iClassQ Kiosko
echo ================================================
echo.

echo PASO 1: Compilar con IntelliJ
echo.
echo Por favor:
echo   1. Abre IntelliJ
echo   2. Maven panel (derecha)
echo   3. Lifecycle ^> clean (doble clic)
echo   4. Lifecycle ^> package (doble clic)
echo   5. Espera a que termine
echo.
pause

echo.
echo PASO 2: Verificando JAR generado...
if not exist "target\kiosko-1.0-SNAPSHOT.jar" (
    echo ERROR: No se encontro el JAR
    echo Asegurate de haber ejecutado 'mvn package' en IntelliJ
    pause
    exit /b 1
)
echo OK - JAR encontrado

echo.
echo PASO 3: Generando MSI con jpackage...
jpackage ^
  --input target ^
  --name "iClassQ-Kiosko" ^
  --main-jar kiosko-1.0-SNAPSHOT.jar ^
  --main-class org.iclassq.KioskoApplication ^
  --type msi ^
  --app-version 1.0.0 ^
  --vendor "iClassQ Team" ^
  --icon src/main/resources/images/icon.ico ^
  --copyright "Copyright 2025 iClassQ Team" ^
  --description "Sistema de Gestion de Colas con Accesibilidad" ^
  --win-menu ^
  --win-shortcut ^
  --win-dir-chooser ^
  --win-menu-group "iClassQ" ^
  --dest target/dist ^
  --java-options "-Xms256m" ^
  --java-options "-Xmx1024m" ^
  --java-options "-Dfile.encoding=UTF-8"

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo jpackage
    pause
    exit /b 1
)

echo.
echo ================================================
echo   MSI GENERADO EXITOSAMENTE
echo ================================================
echo.
echo Ubicacion: target\dist\iClassQ-Kiosko-1.0.0.msi
echo.
echo NOTA: Los logs usaran configuracion por defecto de Java
echo.
pause