#!/bin/sh
set -ex

SCRIPT=$(realpath "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
cd "$SCRIPTPATH"

sbt --client scalafmtAll
sbt --client test

FILES=$(git diff --name-only --cached)
echo "$FILES" | xargs git add

exit 0