#!/bin/bash
set -e

MAIN_CLASS="ar.edu.itba.pod.server.Server"

# Go to project root
cd "$(dirname "$0")/.."

# Build classpath: compiled classes
CP="server/target/classes:client/target/classes:api/target/classes"

# Append all jars from lib/jars
for jar in lib/jars/*.jar; do
    CP="$CP:$jar"
done

echo "Running server with classpath: $CP"
java -cp "$CP" $MAIN_CLASS "$@"
