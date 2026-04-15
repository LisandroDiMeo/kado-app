#!/usr/bin/env bash
#
# enable_hooks.sh - Configures git to use this directory for hooks.
#
# Usage (from the project root where gradlew lives):
#   ./hooks/enable_hooks.sh
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GIT_ROOT="$(git rev-parse --show-toplevel)"

echo "Git root:    $GIT_ROOT"
echo "Hooks dir:   $SCRIPT_DIR"

# Make hooks executable
chmod +x "$SCRIPT_DIR/pre-commit" "$SCRIPT_DIR/pre-push"

# Set hooks path relative to the git root
RELATIVE_PATH="$(python3 -c "import os.path; print(os.path.relpath('$SCRIPT_DIR', '$GIT_ROOT'))")"
git config core.hooksPath "$RELATIVE_PATH"

echo ""
echo "Git hooks enabled! core.hooksPath set to: $RELATIVE_PATH"
echo ""
echo "Hooks installed:"
echo "  pre-commit  - runs ktlint + detekt"
echo "  pre-push    - runs unit tests + version tag check"
echo ""
echo "To disable: git config --unset core.hooksPath"
