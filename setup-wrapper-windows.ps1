# Lädt die offizielle Gradle Wrapper JAR in dieses Projekt.
# Falls das Gerät Zugriff auf GitHub hat, einmalig vor dem ersten Android-Studio-Import ausführen.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$target = Join-Path $root 'gradle\wrapper\gradle-wrapper.jar'
$url = 'https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar'
Write-Host 'Gradle Wrapper 8.11.1 wird geladen...'
Invoke-WebRequest -Uri $url -OutFile $target
if ((Get-Item $target).Length -lt 20000) { throw 'Download fehlerhaft.' }
Write-Host 'Fertig. Jetzt Android Studio starten oder .\gradlew.bat assembleDebug ausführen.'
