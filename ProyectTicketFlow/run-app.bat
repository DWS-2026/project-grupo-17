@echo off
setlocal enabledelayedexpansion

REM Configurar rutas
set MAVEN_VERSION=3.9.6
set MAVEN_DOWNLOAD_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip
set INSTALL_PATH=C:\Maven
set ZIP_PATH=%TEMP%\apache-maven-%MAVEN_VERSION%-bin.zip

echo Descargando Maven %MAVEN_VERSION%...
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%MAVEN_DOWNLOAD_URL%' -OutFile '%ZIP_PATH%'"

if not exist "%INSTALL_PATH%" (
    mkdir "%INSTALL_PATH%"
)

echo Extrayendo Maven...
powershell -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; [System.IO.Compression.ZipFile]::ExtractToDirectory('%ZIP_PATH%', '%INSTALL_PATH%')"

REM Mover archivos al nivel superior
powershell -Command "Move-Item -Path '%INSTALL_PATH%\apache-maven-%MAVEN_VERSION%\*' -Destination '%INSTALL_PATH%' -Force; Remove-Item -Path '%INSTALL_PATH%\apache-maven-%MAVEN_VERSION%' -Force"

REM Agregar Maven al PATH del sistema
setx PATH "%PATH%;%INSTALL_PATH%\bin"

echo Maven instalado en %INSTALL_PATH%
echo Ahora ejecutando la aplicacion...

REM Cambiar al directorio del proyecto
cd /d "C:\Users\Usuario\Documents\GitHub\project-grupo-17\ProyectTicketFlow"

REM Ejecutar con la ruta completa a mvn
"%INSTALL_PATH%\bin\mvn.cmd" clean spring-boot:run

pause
