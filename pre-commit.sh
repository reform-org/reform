#!/bin/sh

set -e

SCRIPT=$(realpath "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH"

sbt scalafmtAll "Test / fastLinkJS"
npm run test run

# ensure every text file ends with a newline
for f in $(git grep --cached -Il ''); do tail -c1 $f | read -r _ || echo >> $f; done

FILES=$(git diff --name-only --cached)
echo "$FILES" | xargs git add

exit 0
