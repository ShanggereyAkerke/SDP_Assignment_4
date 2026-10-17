#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/test-classes
find src/main/java src/test/java -name '*.java' | sort > build/test-sources.txt
javac --release 17 -Xlint:all -Werror -d build/test-classes @build/test-sources.txt
java -cp build/test-classes theater.facade.FacadeChecks
