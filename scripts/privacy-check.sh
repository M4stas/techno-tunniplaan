#!/bin/sh
# Checks tracked files for personal data that must not reach this public repository.
#
#   sh scripts/privacy-check.sh                  scan the tracked files
#   sh scripts/privacy-check.sh --commits A..B   also scan the messages of commits in A..B
#
# Run it from the repository root. It exits 1 when it finds something.
# Real names cannot be checked here, because a list of them would itself be personal data.
set -eu

# Images are allowed only here. README screenshots use invented data.
image_dirs='^(docs/assets/|app/src/[^/]+/res/)'
# EduPage-shaped JSON is allowed only here, and only in small hand-written fixtures.
fixture_dir='^app/src/test/resources/fixtures/'
fixture_max_bytes=20000

home_path='(/[U]sers/|/[h]ome/|[A-Za-z]:[/\\]Users[/\\])[^/\\[:space:]]+'
email='[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}'
allowed_email='(@users[.]noreply[.]github[.]com|^noreply@github[.]com|@example[.](com|org|net)|[.]example)$'
phone='(\+[0-9]{1,3}[ -]?[0-9]{2,4}[ -]?[0-9]{3,4}([ -]?[0-9]{2,4})?|(^|[^[:alnum:]_.])[1-9][0-9]{7,14}([^[:alnum:]_.]|$)|(^|[^[:alnum:]_.])([0-9]{2,4}[ -]){1,3}[0-9]{3,4}([^[:alnum:]_.]|$)|tel:[+0-9])'

failed=0
report() {
    printf 'privacy-check: %s\n' "$1" >&2
    failed=1
}

scan_files() {
    git grep -nIE "$1" -- . || true
}

hits=$(scan_files "$home_path")
[ -n "$hits" ] && report "home directory path:
$hits"

hits=$(git grep -nIoE "$email" -- . \
    | awk -F: -v ok="$allowed_email" '{ addr = $0; sub(/^[^:]*:[^:]*:/, "", addr); if (addr !~ ok) print }' || true)
[ -n "$hits" ] && report "email address outside noreply and example domains:
$hits"

hits=$(scan_files "$phone")
[ -n "$hits" ] && report "phone number:
$hits"

if ! python3 - "$image_dirs" "$fixture_dir" "$fixture_max_bytes" <<'PY'
import json
import os
from pathlib import Path
import re
import subprocess
import sys

image_dirs, fixture_dir, fixture_max_bytes = sys.argv[1:]
failed = False

def viewer_shape(value):
    if isinstance(value, dict):
        regular = value.get("regular")
        return (
            isinstance(regular, dict) and "default_num" in regular
        ) or any(viewer_shape(child) for child in value.values())
    if isinstance(value, list):
        return any(viewer_shape(child) for child in value)
    return False

paths = subprocess.check_output(["git", "ls-files", "-z"]).split(b"\0")
for raw_path in paths:
    if not raw_path:
        continue
    name = os.fsdecode(raw_path)
    if re.search(r"\.(png|jpe?g|webp|gif|bmp|heic|heif|tiff?|avif|svg|ico)$", name, re.I) and not re.match(image_dirs, name):
        print(f"privacy-check: image outside docs/assets/ or app resources: {name!r}", file=sys.stderr)
        failed = True
    if not name.endswith(".json"):
        continue
    data = Path(name).read_bytes()
    marked = re.search(rb'"dbiAccessorRes"|"ttviewer"', data)
    if not marked:
        try:
            marked = viewer_shape(json.loads(data))
        except (ValueError, UnicodeError):
            marked = False
    if not marked:
        continue
    if not re.match(fixture_dir, name):
        print(f"privacy-check: EduPage-shaped JSON outside {fixture_dir}: {name!r}", file=sys.stderr)
        failed = True
    elif len(data) > int(fixture_max_bytes):
        print(f"privacy-check: EduPage fixture larger than {fixture_max_bytes} bytes, which suggests a captured response: {name!r}", file=sys.stderr)
        failed = True

sys.exit(1 if failed else 0)
PY
then
    failed=1
fi

if [ "${1:-}" = "--commits" ] && [ -n "${2:-}" ]; then
    messages=$(git log --format='%h %B' "$2")
    hits=$(printf '%s\n' "$messages" | grep -nE "$home_path|$phone" || true)
    [ -n "$hits" ] && report "commit message in $2:
$hits"
    hits=$(printf '%s\n' "$messages" | grep -oE "$email" | grep -vE "$allowed_email" || true)
    [ -n "$hits" ] && report "email address in a commit message in $2:
$hits"
fi

if [ "$failed" -ne 0 ]; then
    echo "privacy-check: failed. Remove the data, or use invented values in fixtures." >&2
    exit 1
fi
echo "privacy-check: ok"
