#!/bin/sh

set -e

ensure_files_end_with_newline() {
  for f in $(git grep --cached -Il ''); do
    tail -c1 $f | read -r _ || echo >> $f;
  done
}

restage_files() {
  FILES=$(git diff --name-only --cached)
  if [ "$FILES" != "" ]; then
    echo "$FILES" | xargs git add
  fi
}

run_local_pileline() {
  export $(cat .env)
  export CI=true
  sbt --client compile || local_pipeline_failed
  sbt --client test || local_pipeline_failed
}

local_pipeline_failed() {
  echo -e "\033[1;31m === GITHUB PIPELINE WILL FAIL! ===\033[0m"
  echo -e "\033[1;31m ===        FIX YOUR CODE       ===\033[0m"
  exit 1
}

cd peer
run_local_pileline
sbt --client scalafmtAll
sbt --client scalafixAll

ensure_files_end_with_newline
restage_files