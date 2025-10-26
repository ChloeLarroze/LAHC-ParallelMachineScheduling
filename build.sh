#!/bin/bash

# -------------------------------
# Configuration
# -------------------------------

# Path to your ZuluFX JDK
JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home
PATH="$JAVA_HOME/bin:$PATH"

# Path to your JavaFX SDK (from your project)
JAVAFX_LIB="$HOME/doc/S9/Java/LAHC-ParallelMachineScheduling/javafx-sdk-11.0.2/lib"

# Output directory for compiled classes
BIN_DIR="$PWD/bin"

# -------------------------------
# Compile all Java files
# -------------------------------

echo "Compiling Java sources..."
javac \
  --module-path "$JAVAFX_LIB" \
  --add-modules javafx.controls,javafx.fxml \
  -d "$BIN_DIR" \
  $(find . -name "*.java")

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed"
    exit 1
fi

# -------------------------------
# Run the GUI
# -------------------------------

echo "Running JavaFX GUI..."
java \
  --module-path "$JAVAFX_LIB" \
  --add-modules javafx.controls,javafx.fxml \
  -cp "$BIN_DIR" MainFX