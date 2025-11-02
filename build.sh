#!/bin/bash
set -e

WD="$(pwd)"

# If no arguments provided, default to building both server and client
if [ $# -eq 0 ]; then
    MODULES="server client"
else
    MODULES="$@"
fi

echo "=== Building project ==="
echo "Modules to build: $MODULES"

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean
rm -rf scripts/

# Build project
echo "Building project..."
mvn install -DskipTests

# Copy from each module to avoid missing transitive dependencies
for module in $MODULES; do
    cd "$WD"
    mkdir -p scripts/"$module"
    if [ -d "$module" ]; then
        cd "$module"/target
        echo "Decompiling bins from $module..."
        tar -xzf tp2-"$module"-1.0-SNAPSHOT-bin.tar.gz -C "$WD"/scripts/"$module" --strip-components=1
    fi
done

echo ""
echo "=== Build completed successfully ==="

cd "$WD"