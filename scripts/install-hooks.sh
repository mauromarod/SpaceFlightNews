#!/usr/bin/env bash
set -euo pipefail

HOOK=".git/hooks/pre-commit"
SCRIPT="scripts/pre-commit.sh"

cp "$SCRIPT" "$HOOK"
chmod +x "$HOOK"

echo "✓ pre-commit hook installed at $HOOK"
