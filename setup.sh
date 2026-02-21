#!/bin/bash
set -e

echo "=== Handy Shulkers - Project Setup ==="
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Install JDK 21:"
    echo "   brew install openjdk@21"
    echo "   Then add to PATH: export JAVA_HOME=\$(/usr/libexec/java_home -v 21)"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "✅ Java found (version $JAVA_VERSION)"

if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "⚠️  Java 21+ required, found $JAVA_VERSION"
    echo "   brew install openjdk@21"
    exit 1
fi

# Bootstrap Gradle wrapper
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo ""
    echo "📦 Downloading Gradle wrapper..."

    if command -v gradle &> /dev/null; then
        gradle wrapper --gradle-version 8.14
    else
        echo "Gradle not found. Installing wrapper manually..."
        # Download the wrapper jar directly from Gradle's GitHub releases
        WRAPPER_URL="https://github.com/gradle/gradle/raw/v8.14.0/gradle/wrapper/gradle-wrapper.jar"
        curl -sL -o gradle/wrapper/gradle-wrapper.jar "$WRAPPER_URL" 2>/dev/null || {
            echo "❌ Could not download gradle-wrapper.jar"
            echo "   Option 1: brew install gradle && gradle wrapper --gradle-version 8.14"
            echo "   Option 2: Copy gradle-wrapper.jar from fabric-example-mod"
            exit 1
        }
    fi
    echo "✅ Gradle wrapper ready"
fi

echo ""
echo "🔨 Building project (first build downloads Minecraft + dependencies, may take a few minutes)..."
./gradlew build

echo ""
echo "📖 Generating Minecraft sources (for development reference)..."
./gradlew genSources

echo ""
echo "=== Setup Complete! ==="
echo ""
echo "Next steps:"
echo "  ./gradlew runClient    # Launch Minecraft with your mod"
echo "  ./gradlew build        # Rebuild the mod"
echo ""
echo "The mod JAR is in: build/libs/"
echo "Happy modding! 🎮"
