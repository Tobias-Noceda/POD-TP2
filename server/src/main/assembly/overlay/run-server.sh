#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

JAVA_OPTS="-Dio.netty.transport.noNative=true"

MAIN_CLASS="ar.edu.itba.pod.server.Server"

JAVA_SYS_PROPS=""
OTHER_ARGS=""

for arg in "$@"; do
    case "$arg" in
        -D*)
            JAVA_SYS_PROPS="$JAVA_SYS_PROPS $arg"
            ;;
        *)
            OTHER_ARGS="$OTHER_ARGS $arg"
            ;;
    esac
done

# Build classpath from lib/jars
CP=""
for jar in lib/jars/*.jar; do
    if [ -f "$jar" ]; then
        if [ -z "$CP" ]; then
            CP="$jar"
        else
            CP="$CP:$jar"
        fi
    fi
done

java $JAVA_OPTS $JAVA_SYS_PROPS -cp "$CP" $MAIN_CLASS $OTHER_ARGS