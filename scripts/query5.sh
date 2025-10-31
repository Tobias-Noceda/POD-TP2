#!/bin/bash
set -e

MAIN_CLASS="ar.edu.itba.pod.client.Query5Client"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT" || exit 1

JAVA_OPTS="-Dio.netty.transport.noNative=true"

JAVA_SYS_PROPS=()
OTHER_ARGS=()

for arg in "$@"; do
    if [[ "$arg" == -D* ]]; then
        JAVA_SYS_PROPS+=("$arg")
    else
        OTHER_ARGS+=("$arg")
    fi
done

has_addresses=false
has_inPath=false
has_outPath=false

for prop in "${JAVA_SYS_PROPS[@]}"; do
    [[ "$prop" == -Daddresses=* ]] && has_addresses=true
    [[ "$prop" == -DinPath=* ]] && has_inPath=true
    [[ "$prop" == -DoutPath=* ]] && has_outPath=true
done

if ! $has_addresses; then
    echo "Error: Missing required parameter -Daddresses"
    echo "Usage: query5.sh -Daddresses='IP:PORT[;IP:PORT...]' -DinPath=PATH -DoutPath=PATH"
    echo "Example: query5.sh -Daddresses='127.0.0.1:5701' -DinPath=. -DoutPath=."
    exit 1
fi

if ! $has_inPath; then
    echo "Error: Missing required parameter -DinPath"
    echo "Usage: query5.sh -Daddresses='IP:PORT[;IP:PORT...]' -DinPath=PATH -DoutPath=PATH"
    exit 1
fi

if ! $has_outPath; then
    echo "Error: Missing required parameter -DoutPath"
    echo "Usage: query5.sh -Daddresses='IP:PORT[;IP:PORT...]' -DinPath=PATH -DoutPath=PATH"
    exit 1
fi

CP="client/target/classes:api/target/classes:server/target/classes"

for jar in lib/jars/*.jar; do
    if [ -f "$jar" ]; then
        CP="$CP:$jar"
    fi
done

java $JAVA_OPTS "${JAVA_SYS_PROPS[@]}" -cp "$CP" $MAIN_CLASS "${OTHER_ARGS[@]}"