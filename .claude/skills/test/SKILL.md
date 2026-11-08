---
name: test
description: Run this project's Maven tests on JDK 21 with compact output. Use whenever tests need to run.
---

Run `.claude/scripts/test.sh` (all tests) or `.claude/scripts/test.sh -Dtest=<ClassName>` (one class, from $ARGUMENTS if given).

The script already selects JDK 21 and filters Maven output. Do not run `./mvnw test` directly.
Report only: total run/failed, and for failures the test name plus the assertion line.
