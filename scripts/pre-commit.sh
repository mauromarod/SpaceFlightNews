#!/usr/bin/env bash
set -euo pipefail

MODULES=(
  "core:domain"
  "core:database"
  "core:data"
  "core:network"
  "core:designsystem"
  "core:ui-components"
  "features:auth"
  "features:profile"
  "features:news"
  "features:detail"
  "app"
)

echo "▶ ktlint check..."
./gradlew ktlintCheck --no-daemon --quiet || {
  echo ""
  echo "✗ ktlint failed. Run ./gradlew ktlintFormat to auto-fix."
  exit 1
}
echo "✓ ktlint"

echo "▶ detekt..."
for mod in "${MODULES[@]}"; do
  ./gradlew ":$mod:detekt" --no-daemon --quiet || {
    echo "✗ detekt failed on :$mod"
    exit 1
  }
done
echo "✓ detekt"

echo "▶ lint..."
./gradlew lintDebug --no-daemon --quiet || {
  echo "✗ lintDebug failed."
  exit 1
}
echo "✓ lint"

echo ""
echo "All checks passed."
