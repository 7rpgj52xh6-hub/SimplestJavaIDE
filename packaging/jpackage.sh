#!/usr/bin/env bash
#
# Builds a native, self-contained SimplestJavaIDE package for the *current*
# platform using the JDK's jpackage tool. jpackage cannot cross-compile, so run
# this on each target OS (macOS / Linux / Windows-via-Git-Bash).
#
# The whole JDK is bundled as the runtime image on purpose: the IDE compiles the
# student's code in-process (javax.tools / jdk.compiler) and steps through it
# with the debugger (jdk.jdi + jdk.jdwp.agent), so a stripped-down JRE is not
# enough.
#
# Usage:  packaging/jpackage.sh [app-image|dmg|pkg|deb|rpm|msi|exe]
# Default type is "app-image" (works on every OS without extra tooling).

set -euo pipefail

TYPE="${1:-app-image}"
APP_NAME="SimplestJavaIDE"
APP_VERSION="2.0"
VENDOR="Daniel Trageser"
MAIN_CLASS="simplestJavaIDEpackage.StartingWindow"
MAIN_JAR="SimplestJavaIDE.jar"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

JPACKAGE="${JAVA_HOME:+$JAVA_HOME/bin/}jpackage"

# Pick the platform icon.
case "$(uname -s)" in
  Darwin*)  ICON="packaging/icons/$APP_NAME.icns" ;;
  Linux*)   ICON="packaging/icons/$APP_NAME.png" ;;
  *)        ICON="packaging/icons/$APP_NAME.ico" ;;  # Windows (MINGW/MSYS)
esac

# Ensure the runnable jar exists.
if [ ! -f "target/$MAIN_JAR" ]; then
  echo "target/$MAIN_JAR not found -- run 'mvn -B clean package' first." >&2
  exit 1
fi

# jpackage copies the *entire* --input directory into the app, so stage a clean
# folder that holds only the jar.
STAGE="$(mktemp -d)"
cp "target/$MAIN_JAR" "$STAGE/"

rm -rf dist && mkdir -p dist

echo "Building '$TYPE' for $APP_NAME $APP_VERSION ..."
"$JPACKAGE" \
  --type "$TYPE" \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --vendor "$VENDOR" \
  --description "Eine minimalistische Java-IDE fuer den Unterricht" \
  --input "$STAGE" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --runtime-image "${JAVA_HOME:-$(dirname "$(dirname "$(command -v java)")")}" \
  --icon "$ICON" \
  --dest dist

rm -rf "$STAGE"
echo "Done. Output in: dist/"
ls -la dist
