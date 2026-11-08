#!/bin/bash
#maven tests on JDK 21, prints only summary and failures
#usage: .claude/scripts/test.sh [-Dtest=ClassName]
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
cd "$(dirname "$0")/../.." || exit 1

./mvnw test -Dskip.installnodenpm -Dskip.npm "$@" 2>&1 \
  | grep -E "Tests run:|FAIL|ERROR\]|BUILD|expected|but was|Exception" \
  | grep -vE "^\[INFO\] Tests run: .* -- in " \
  | head -40
exit "${PIPESTATUS[0]}"
