@echo off
echo ================================================
echo   Generar MSI - VERSION PRODUCCION
echo   (Sin consola de debug)
echo ================================================
echo.

set JAVAFX_JMODS=C:\JavaFX\javafx-jmods-21
set RUNTIME_DIR=%CD%\target\custom-runtime
set APP_DIR=%CD%\target\app-extracted
set ICON_PATH=%CD%\src\main\resources\images\icon.ico

REM ============================================
REM PASO 1: Verificar que compilaste
REM ============================================
echo [1/5] Verificando JAR compilado...

if not exist "target\iclassq-kiosko.jar" (
    echo [ERROR] JAR no encontrado: target\iclassq-kiosko.jar
    echo.
    echo Por favor compila primero con IntelliJ:
    echo   1. Maven panel ^(derecha^)
    echo   2. Lifecycle ^> clean
    echo   3. Lifecycle ^> package
    echo.
    pause
    exit /b 1
)

echo [OK] JAR encontrado
echo.

REM ============================================
REM PASO 2: Verificar JavaFX jmods
REM ============================================
echo [2/5] Verificando JavaFX jmods...

if not exist "%JAVAFX_JMODS%\javafx.base.jmod" (
    echo [ERROR] JavaFX jmods no encontrados en: %JAVAFX_JMODS%
    pause
    exit /b 1
)

echo [OK] JavaFX jmods encontrados
echo.

REM ============================================
REM PASO 3: Crear runtime con jlink
REM ============================================
echo [3/5] Creando runtime con java.exe...

if exist "%RUNTIME_DIR%" rmdir /s /q "%RUNTIME_DIR%"

jlink ^
  --module-path "%JAVAFX_JMODS%;%JAVA_HOME%\jmods" ^
  --add-modules javafx.controls,javafx.graphics,javafx.base,java.base,java.desktop,java.logging,java.xml,java.naming,java.sql,java.management,java.instrument,java.prefs,java.net.http ^
  --output "%RUNTIME_DIR%" ^
  --strip-debug ^
  --no-man-pages ^
  --no-header-files ^
  --compress=2

if not exist "%RUNTIME_DIR%\bin\java.exe" (
    echo [ERROR] java.exe no se creo
    pause
    exit /b 1
)

echo [OK] Runtime creado con java.exe
echo.

REM Copiar application.properties a runtime/conf/
echo Copiando application.properties a runtime/conf/...
if exist "target\iclassq-kiosko.jar" (
    REM Extraer desde la raíz del proyecto
    jar xf "%CD%\target\iclassq-kiosko.jar" application.properties 2>nul
    
    REM Mover a runtime/conf/
    if exist "application.properties" (
        move /Y "application.properties" "%RUNTIME_DIR%\conf\" >nul
        echo [OK] application.properties copiado a runtime/conf/
    ) else (
        echo [WARNING] application.properties no encontrado en JAR
    )
)
echo.

REM ============================================
REM PASO 4: Extraer recursos del JAR
REM ============================================
echo [4/5] Extrayendo recursos del JAR...

if exist "%APP_DIR%" rmdir /s /q "%APP_DIR%"
mkdir "%APP_DIR%"

copy "target\iclassq-kiosko.jar" "%APP_DIR%\"

pushd "%APP_DIR%"

echo Extrayendo vosk-model-es...
jar xf iclassq-kiosko.jar vosk-model-es 2>nul
if exist "vosk-model-es" (
    echo [OK] vosk-model-es extraido
)

echo Extrayendo fonts...
jar xf iclassq-kiosko.jar fonts 2>nul
if exist "fonts" (
    echo [OK] fonts extraido
)

echo Extrayendo images...
jar xf iclassq-kiosko.jar images 2>nul
if exist "images" (
    echo [OK] images extraido
)

popd

echo [OK] Recursos extraidos
echo.

REM ============================================
REM PASO 5: Generar MSI de produccion
REM ============================================
echo [5/5] Generando MSI de produccion...
echo.
echo NOTA: Esta version NO muestra consola
echo La aplicacion abre directamente
echo.

if exist "target\dist" rmdir /s /q "target\dist"

if not exist "%ICON_PATH%" (
    echo [WARNING] Icono no encontrado: %ICON_PATH%
    set ICON_OPTION=
) else (
    echo [OK] Usando icono: %ICON_PATH%
    set ICON_OPTION=--icon "%ICON_PATH%"
)

jpackage ^
  --type msi ^
  --input "%APP_DIR%" ^
  --main-jar iclassq-kiosko.jar ^
  --main-class org.iclassq.KioskoApplication ^
  --name "iClassQ-Kiosko" ^
  --app-version 1.0.0 ^
  --vendor "Ginnet" ^
  --description "Sistema de Gestion de Colas - iClassQ Kiosko" ^
  --copyright "Copyright 2025 Ginnet" ^
  %ICON_OPTION% ^
  --win-menu ^
  --win-shortcut ^
  --win-dir-chooser ^
  --win-menu-group "iClassQ" ^
  --dest target\dist ^
  --runtime-image "%RUNTIME_DIR%" ^
  --java-options "-Duser.dir=$APPDIR" ^
  --verbose

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] jpackage fallo
    pause
    exit /b 1
)

echo.
echo ================================================
echo   MSI DE PRODUCCION GENERADO
echo ================================================
echo.
dir target\dist\*.msi
echo.
echo VERSION PRODUCCION:
echo   - Sin consola de debug
echo   - Abre directamente la ventana JavaFX
echo   - Listo para distribuir a hospitales
echo.
echo UBICACION: target\dist\iClassQ-Kiosko-1.0.0.msi
echo.
pause