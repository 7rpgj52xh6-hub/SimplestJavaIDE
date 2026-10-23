#!/usr/bin/env bash
# Regenerates every jar the CheerpJ spike needs. Jars are NOT checked in.
# macOS-oriented (uses /usr/libexec/java_home); adjust JAVA_* for Linux.
set -euo pipefail
cd "$(dirname "$0")"
SPIKE_DIR="$(pwd)"
REPO_ROOT="$(cd ../.. && pwd)"

# --- Tools -------------------------------------------------------------------
JDK24="${JDK24:-$(/usr/libexec/java_home -v 24)}"
# Maven: no `mvn` on PATH on this machine — use IntelliJ's bundled one (override via MVN).
MVN="${MVN:-/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn}"
ECJ_VERSION="${ECJ_VERSION:-3.39.0}"

echo "== 1/5  App-Jar (Java 17) =="
JAVA_HOME="$JDK24" "$MVN" -q -B -f "$REPO_ROOT/pom.xml" clean package -DskipTests -Dmaven.compiler.release=17
cp "$REPO_ROOT/target/SimplestJavaIDE.jar" "$SPIKE_DIR/SimplestJavaIDE-java17.jar"

echo "== 2/5  App-Jar (Java 21, zum Vorführen des Versions-Fehlers) =="
JAVA_HOME="$JDK24" "$MVN" -q -B -f "$REPO_ROOT/pom.xml" clean package -DskipTests
cp "$REPO_ROOT/target/SimplestJavaIDE.jar" "$SPIKE_DIR/SimplestJavaIDE-java21.jar"

echo "== 3/5  ECJ $ECJ_VERSION (reiner-Java-Compiler) =="
curl -fsSL -o "$SPIKE_DIR/ecj.jar" \
  "https://repo1.maven.org/maven2/org/eclipse/jdt/ecj/${ECJ_VERSION}/ecj-${ECJ_VERSION}.jar"

echo "== 4/5  Probe-Jar =="
rm -rf "$SPIKE_DIR/probe/classes"
"$JDK24/bin/javac" --release 17 -cp "$SPIKE_DIR/ecj.jar" \
  -d "$SPIKE_DIR/probe/classes" "$SPIKE_DIR"/probe/*.java
"$JDK24/bin/jar" --create --file "$SPIKE_DIR/probe.jar" -C "$SPIKE_DIR/probe/classes" .

echo "== 5/5  Plattform-Jars fuer ECJ-Bootclasspath =="
# rt17.jar = java.base aus einem JDK (hier 18, 17 waere ideal). Nur fuer Tests.
J_FOR_BASE="${J_FOR_BASE:-$(/usr/libexec/java_home -v 18 2>/dev/null || /usr/libexec/java_home -v 24)}"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
"$J_FOR_BASE/bin/jimage" extract --dir="$TMP/jimg" "$J_FOR_BASE/lib/modules" >/dev/null
( cd "$TMP/jimg/java.base" && rm -f module-info.class && "$JDK24/bin/jar" --create --file "$SPIKE_DIR/rt17.jar" . )
# rt8.jar = klassisches rt.jar aus einem JDK 8 (fuer den funktionierenden Java-8-ECJ-Weg).
J8="${J8:-$(/usr/libexec/java_home -v 1.8 2>/dev/null || true)}"
if [ -n "${J8:-}" ] && [ -f "$J8/jre/lib/rt.jar" ]; then
  cp "$J8/jre/lib/rt.jar" "$SPIKE_DIR/rt8.jar"
else
  echo "   (!) kein JDK 8 gefunden — rt8.jar nicht erzeugt; ecj-batch-probe S2 ueberspringt dann Java 8"
fi

echo "Fertig. Jars liegen in $SPIKE_DIR"
ls -1 "$SPIKE_DIR"/*.jar
