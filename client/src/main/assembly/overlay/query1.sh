#!/bin/bash
set -e

MAIN_CLASS="ar.edu.itba.pod.client.Query1Client"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

JAVA_OPTS="-Dio.netty.transport.noNative=true"

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

has_addresses=false
has_inPath=false
has_outPath=false

for prop in $JAVA_SYS_PROPS; do
    case "$prop" in
        -Daddresses=*) has_addresses=true ;;
        -DinPath=*) has_inPath=true ;;
        -DoutPath=*) has_outPath=true ;;
    esac
done

if [ "$has_addresses" = false ]; then
    echo "Error: Missing required parameter -Daddresses"
    echo "Usage: query1.sh -Daddresses='IP:PORT[;IP:PORT...]' -DinPath=PATH -DoutPath=PATH"
    echo "Example: query1.sh -Daddresses='127.0.0.1:5701' -DinPath=. -DoutPath=."
    exit 1
fi

if [ "$has_inPath" = false ]; then
    echo "Error: Missing required parameter -DinPath"
    echo "Usage: query1.sh -Daddresses='IP:PORT[;IP:PORT...]' -DinPath=PATH -DoutPath=PATH"
    exit 1
fi

if [ "$has_outPath" = false ]; then
    echo "Error: Missing required parameter -DoutPath"
    echo "Usage: query1.sh -Daddresses='IP:PORT[;IP:PORT...]' -DinPath=PATH -DoutPath=PATH"
    exit 1
fi

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