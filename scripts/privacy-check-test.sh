#!/bin/sh
# Tests scripts/privacy-check.sh against throwaway repositories.
# Run it from the repository root: sh scripts/privacy-check-test.sh
set -eu

script="$(pwd)/scripts/privacy-check.sh"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
failures=0

# Usage: check <name> <expected exit code> <path> <content>
check() {
    repo="$work/$1"
    mkdir -p "$repo/scripts" "$repo/$(dirname "$3")"
    cp "$script" "$repo/scripts/"
    printf '%s\n' "$4" > "$repo/$3"
    git -C "$repo" init -q
    git -C "$repo" add -A
    status=0
    (cd "$repo" && sh scripts/privacy-check.sh) > /dev/null 2>&1 || status=$?
    if [ "$status" -ne "$2" ]; then
        echo "FAIL $1: expected exit $2, got $status"
        failures=$((failures + 1))
    else
        echo "ok   $1"
    fi
}

check clean 0 README.md 'Invented teacher A. Näide, room 101.'
check noreply-email 0 README.md '12345+someone@users.noreply.github.com'
check example-email 0 README.md 'teacher@example.com'
check mac-home 1 README.md 'Built in /Users/someone/Projects'
check linux-home 1 notes.txt 'see /home/someone/app'
check windows-home 1 notes.txt 'C:\Users\someone\app'
check real-email 1 app/src/Foo.kt 'val contact = "someone@gmail.com"'
check phone 1 README.md 'Call +372 5123 4567'
check tel-link 1 README.md '<a href="tel:+3725123456">'
check date-not-phone 0 README.md 'Released 2026-11-01, version 1.2.3'
check readme-image 0 docs/assets/shot.png 'png'
check res-image 0 app/src/main/res/drawable/icon.png 'png'
check stray-image 1 docs/screenshot.png 'png'
check fixture-json 0 app/src/test/resources/fixtures/t.json '{"r":{"dbiAccessorRes":{"tables":[]}}}'
check captured-json 1 app/src/main/assets/t.json '{"r":{"dbiAccessorRes":{"tables":[]}}}'
check viewer-json 1 data/viewer.json '{"r":{"regular":{"default_num":"22"}}}'
check pretty-viewer-json 1 data/viewer.json '{
  "r": {
    "regular": {
      "default_num": "22"
    }
  }
}'
check reordered-viewer-json 1 data/viewer.json '{"r":{"regular":{"timetables":[],"default_num":"22"}}}'
check array-viewer-json 1 data/viewer.json '[{"regular":{"timetables":[],"default_num":"22"}}]'
check small-viewer-fixture 0 app/src/test/resources/fixtures/viewer.json '{"r":{"regular":{
"timetables":[],"default_num":"22"}}}'
check spaced-json 1 'data/viewer response.json' '{"regular":{"default_num":"22"}}'
check unicode-json 1 'data/õpetajad.json' '{"dbiAccessorRes":{}}'
check newline-json 1 'data/viewer
response.json' '{"ttviewer":{}}'
check spaced-fixture 0 'app/src/test/resources/fixtures/viewer response.json' '{"regular":{"default_num":"22"}}'
check unicode-image 1 'docs/õpetajad.png' 'png'
check spaced-image 1 'docs/viewer response.PNG' 'png'
check newline-image 1 'docs/viewer
response.png' 'png'
check unicode-allowed-image 0 'docs/assets/õpetajad.png' 'png'
check spaced-allowed-image 0 'app/src/main/res/drawable/viewer response.png' 'png'
check plain-json 0 app/config.json '{"name":"value"}'
check unrelated-regular 0 app/config.json '{"regular":{"other":{"default_num":"22"}}}'

big=$(awk 'BEGIN { for (i = 0; i < 2100; i++) printf "{\"id\":\"%05d\"},", i }')
check large-fixture 1 app/src/test/resources/fixtures/big.json "{\"r\":{\"dbiAccessorRes\":{\"tables\":[$big{}]}}}"
padding=$(awk 'BEGIN { for (i = 0; i < 21000; i++) printf " " }')
check large-viewer-fixture 1 app/src/test/resources/fixtures/viewer.json "{\"regular\":{
\"timetables\":[],\"default_num\":\"22\"}}$padding"
check large-spaced-fixture 1 'app/src/test/resources/fixtures/viewer response.json' "{\"regular\":{\"default_num\":\"22\"}}$padding"

# Commit messages are scanned only with --commits.
repo="$work/commit-message"
mkdir -p "$repo/scripts"
cp "$script" "$repo/scripts/"
git -C "$repo" init -q
git -C "$repo" add -A
git -C "$repo" -c user.name=t -c user.email=t@example.com commit -qm 'Start'
git -C "$repo" -c user.name=t -c user.email=t@example.com commit -q --allow-empty -m 'Fix path /Users/someone/app'
status=0
(cd "$repo" && sh scripts/privacy-check.sh --commits HEAD~1..HEAD) > /dev/null 2>&1 || status=$?
if [ "$status" -ne 1 ]; then
    echo "FAIL commit-message: expected exit 1, got $status"
    failures=$((failures + 1))
else
    echo "ok   commit-message"
fi

if [ "$failures" -ne 0 ]; then
    echo "$failures privacy-check test(s) failed" >&2
    exit 1
fi
echo "all privacy-check tests passed"
