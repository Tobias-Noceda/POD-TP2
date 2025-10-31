#!/bin/bash
set -e

echo "=== Building project ==="

# Determine project root
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

echo "Project root: $PROJECT_ROOT"

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean

# Build project
echo "Building project..."
mvn install -DskipTests

# Copy dependencies from all modules
echo "Copying dependencies to lib/jars..."
mkdir -p lib/jars

# Copy from each module to avoid missing transitive dependencies
for module in api server client; do
    if [ -d "$module" ]; then
        echo "  - Copying dependencies from $module..."
        (cd "$module" && mvn dependency:copy-dependencies \
            -DoutputDirectory="../lib/jars" \
            -DincludeScope=compile \
            -Dmdep.prependGroupId=true \
            -DoverWriteReleases=false \
            -DoverWriteSnapshots=false)
    fi
done

# Remove duplicate/unnecessary test dependencies (optional)
echo "Cleaning up test dependencies..."
rm -f lib/jars/*junit*.jar lib/jars/*mockito*.jar lib/jars/*hamcrest*.jar \
      lib/jars/*opentest4j*.jar lib/jars/*apiguardian*.jar \
      lib/jars/*byte-buddy*.jar lib/jars/*objenesis*.jar

echo ""
echo "=== Build completed successfully ==="
echo "Compiled classes: */target/classes"
echo "Dependencies: lib/jars"
echo ""
echo "Dependency JARs:"
ls -1 lib/jars/ | head -10
echo "... (total: $(ls -1 lib/jars/ | wc -l) files)"