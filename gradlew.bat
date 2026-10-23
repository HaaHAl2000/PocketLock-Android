@echo off
setlocal
set "APP_HOME=%~dp0"
if not exist "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" (
  echo Gradle Wrapper fehlt. Bitte einmalig setup-wrapper-windows.ps1 per PowerShell ausfuehren.
  exit /b 1
)
java -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
endlocal
