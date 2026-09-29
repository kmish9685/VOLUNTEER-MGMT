#!/bin/sh
# Maven Start Up Shell script
if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
else
    echo "Maven (mvn) is not installed in PATH. Please install Maven or run via mvnw.cmd on Windows."
    exit 1
fi
