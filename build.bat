@echo off
echo ================================================
echo   SOLUCION SIMPLE - SIN CAMBIAR CODIGO
echo   Solo 1 opcion agregada a jpackage
echo ================================================
echo.

set JAVAFX_JMODS=C:\JavaFX\javafx-jmods-21
set RUNTIME_DIR=%CD%\target\custom-runtime
set APP_DIR=%CD%\target\app-extracted

REM ============================================
REM PASO 1: Verificar jmods
REM ============================================
echo [1/4] Verificando JavaFX jmods...

if not exist "%JAVAFX_JMODS%\javafx.base.jmod" (
    echo [ERROR] JavaFX jmods no encontrados
    pause
    exit /b 1
)

echo [OK] JavaFX jmods encontrados
echo.

REM ============================================
REM PASO 2: Crear runtime
REM ============================================
echo [2/4] Creando runtime con java.exe...

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

echo [OK] Runtime creado
echo.

REM ============================================
REM PASO 3: Extraer recursos
REM ============================================
echo [3/4] Extrayendo recursos del JAR...

if exist "%APP_DIR%" rmdir /s /q "%APP_DIR%"
mkdir "%APP_DIR%"

copy "target\iclassq-kiosko.jar" "%APP_DIR%\"

pushd "%APP_DIR%"

jar xf iclassq-kiosko.jar vosk-model-es 2>nul
if exist "vosk-model-es" (
    echo [OK] vosk-model-es extraido
)

jar xf iclassq-kiosko.jar fonts 2>nul
if exist "fonts" (
    echo [OK] fonts extraido
)

jar xf iclassq-kiosko.jar images 2>nul
if exist "images" (
    echo [OK] images extraido
)

popd

echo [OK] Recursos extraidos
echo.

REM ============================================
REM PASO 4: Generar MSI
REM ============================================
echo [4/4] Generando MSI con user.dir configurado...
echo.
echo CLAVE: Agregando --java-options "-Duser.dir=$APPDIR"
echo Esto hace que tu codigo busque en app/ automaticamente
echo SIN CAMBIAR NADA EN EL CODIGO
echo.

if exist "target\dist" rmdir /s /q "target\dist"

jpackage ^
  --type msi ^
  --input "%APP_DIR%" ^
  --main-jar iclassq-kiosko.jar ^
  --main-class org.iclassq.KioskoApplication ^
  --name "iClassQ-Kiosko" ^
  --app-version 1.0.5 ^
  --vendor "Ginnet" ^
  --win-console ^
  --dest target\dist ^
  --runtime-image "%RUNTIME_DIR%" ^
  --java-options "-Duser.dir=$APPDIR" ^
  --verbose

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] jpackage fallo
    pause
    exit /b 1
)

echo.
echo ================================================
echo   MSI GENERADO - Version 1.0.5
echo ================================================
echo.
dir target\dist\*.msi
echo.
echo IMPORTANTE:
echo   user.dir ahora apunta a app/
echo   Tu codigo NO necesita cambios
echo   Buscara vosk-model-es en app/vosk-model-es
echo.
echo PENDIENTE: Solo falta solucionar logs
echo   (requiere 1 linea en logging.properties)
echo.
pause