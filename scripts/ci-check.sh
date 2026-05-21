#!/usr/bin/env bash
# Full local CI simulation (mirrors ci.yml quality + unit-tests jobs).
# Run before pushing to catch everything the pipeline will catch.
set -euo pipefail

bash "$(dirname "$0")/pre-commit.sh"

echo "▶ unit tests..."
./gradlew testDebugUnitTest :core:domain:test --no-daemon --quiet || {
  echo "✗ unit tests failed."
  exit 1
}
echo "✓ unit tests"

echo ""
echo "Full CI simulation passed."
