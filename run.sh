#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build
javac -d build src/*.java
java -cp build CRCApp
