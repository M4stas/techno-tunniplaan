#!/bin/sh
# Checks tracked files for personal data that must not reach this public repository.
#
#   sh scripts/privacy-check.sh                  scan the tracked files
#   sh scripts/privacy-check.sh --commits A..B   also scan the messages of commits in A..B
#
# Run it from the repository root. It exits 1 when it finds something.
# Real names cannot be checked here, because a list of them would itself be personal data.
set -eu

self=scripts/privacy-check.sh
self_test=scripts/privacy-check-test.sh
# Images are allowed only here. README screenshots use invented data.
image_dirs='^(docs/assets/|app/src/[^/]+/res/)'
# EduPage-shaped JSON is allowed only here, and only in small hand-written fixtures.
fixture_dir='^app/src/test/resources/fixtures/'
fixture_max_bytes=20000

home_path='(/Users/|/home/|[A-Za-z]:\\Users\\)[A-Za-z0-9._-]+'
email='[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}'
allowed_email='(@users\.noreply\.github\.com|^noreply@github\.com|@example\.(com|org|net)|\.(example|test|invalid|localhost))$'
# International numbers such as +372 5123 4567, and tel: links.
phone='(\+[0-9]{1,3}[ -]?[0-9]{2,4}[ -]?[0-9]{3,4}([ -]?[0-9]{2,4})?|tel:[+0-9])'
edupage_marker='"dbiAccessorRes"|"regular"[[:space:]]*:[[:space:]]*\{[[:space:]]*"default_num"|"ttviewer"'

failed=0
report() {
    printf 'privacy-check: %s\n' "$1" >&2
    failed=1
}

scan_files() {
    git grep -nIE "$1" -- . ":(exclude)$self" ":(exclude)$self_test" || true
}

hits=$(scan_files "$home_path")
[ -n "$hits" ] && report "home directory path:
$hits"

hits=$(git grep -nIoE "$email" -- . ":(exclude)$self" ":(exclude)$self_test" \
    | awk -F: -v ok="$allowed_email" '{ addr = $0; sub(/^[^:]*:[^:]*:/, "", addr); if (addr !~ ok) print }' || true)
[ -n "$hits" ] && report "email address outside noreply and example domains:
$hits"

hits=$(scan_files "$phone")
[ -n "$hits" ] && report "phone number:
$hits"

hits=$(git ls-files | grep -iE '\.(png|jpe?g|webp|gif|bmp|heic|heif|tiff?|avif)$' | grep -vE "$image_dirs" || true)
[ -n "$hits" ] && report "image outside docs/assets/ or app resources:
$hits"

for f in $(git ls-files '*.json'); do
    grep -qE "$edupage_marker" "$f" || continue
    if ! printf '%s\n' "$f" | grep -qE "$fixture_dir"; then
        report "EduPage-shaped JSON outside $fixture_dir: $f"
    elif [ "$(wc -c < "$f")" -gt "$fixture_max_bytes" ]; then
        report "EduPage fixture larger than $fixture_max_bytes bytes, which suggests a captured response: $f"
    fi
done

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
