#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER="$ROOT/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER" ]; then
  echo 'Lade originalen Gradle Wrapper 8.11.1 herunter ...'
  URL='https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar'
  if command -v curl >/dev/null 2>&1; then
    curl -fL --retry 2 "$URL" -o "$WRAPPER"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$WRAPPER" "$URL"
  else
    echo 'curl oder wget fehlt. Bitte Gradle 8.11.1 installieren.' >&2
    exit 1
  fi
fi
exec java -classpath "$WRAPPER" org.gradle.wrapper.GradleWrapperMain "$@"
