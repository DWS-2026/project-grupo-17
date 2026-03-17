# Script para descargar e instalar Maven en Windows
$mavenVersion = "3.9.6"
$downloadUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$installPath = "C:\Maven"
$zipPath = "$env:TEMP\apache-maven-$mavenVersion-bin.zip"

Write-Host "Descargando Maven $mavenVersion..."
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath

Write-Host "Extrayendo a $installPath..."
if (!(Test-Path $installPath)) {
    New-Item -ItemType Directory -Path $installPath | Out-Null
}

# Extraer
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::ExtractToDirectory($zipPath, $installPath)

# Mover contenidos al nivel superior
Move-Item -Path "$installPath\apache-maven-$mavenVersion\*" -Destination $installPath -Force
Remove-Item -Path "$installPath\apache-maven-$mavenVersion" -Force

Write-Host "Configurando PATH..."
$mavenBin = "$installPath\bin"
$currentPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
if ($currentPath -notlike "*$mavenBin*") {
    [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;$mavenBin", "User")
    Write-Host "Agregado $mavenBin al PATH de Usuario"
}

Write-Host "Maven instalado exitosamente en $installPath"
Write-Host "Por favor, reinicia PowerShell o tu terminal para usar mvn"
Write-Host "Verifica con: mvn --version"
