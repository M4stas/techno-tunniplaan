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
    if [ "$3" = scripts/privacy-check.sh ]; then
        printf '\n# %s\n' "$4" >> "$repo/$3"
    else
        printf '%s\n' "$4" > "$repo/$3"
    fi
    if [ -n "${5:-}" ]; then
        python3 - "$repo/$3" "$5" <<'PY'
from pathlib import Path
import sys
path = Path(sys.argv[1])
path.write_bytes(path.read_text(encoding="utf-8").encode(sys.argv[2]))
PY
    fi
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
check noreply-email 0 README.md '12345+someone@''users.noreply.github.com'
check long-noreply-id 0 README.md '1234''5678+someone@''users.noreply.github.com'
check numeric-noreply-user 0 README.md '1234''5678@''users.noreply.github.com'
check numeric-plus-noreply-user 0 README.md '12345+1234''5678@''users.noreply.github.com'
check numeric-example-user 0 README.md '1234''5678@''example.com'
check numeric-plus-example-user 0 README.md 'someone+1234''5678@''example.com'
check noreply-and-phone 1 README.md '1234''5678+someone@''users.noreply.github.com, call 5123''4567'
check phone-before-noreply 1 README.md 'Call 5123''4567, 1234''5678+someone@''users.noreply.github.com'
check numeric-personal-email 1 README.md '1234''5678@''gmail.com'
check example-email 0 README.md 'teacher@''example.com'
check github-noreply-email 0 README.md 'noreply@''github.com'
check uppercase-example-domain 0 README.md 'teacher@''EXAMPLE.COM'
check mixed-example-domain 0 README.md 'teacher@''ExAmPlE.OrG'
check uppercase-noreply-domain 0 README.md 'someone@''USERS.NOREPLY.GITHUB.COM'
check uppercase-github-domain 0 README.md 'noreply@''GITHUB.COM'
check uppercase-reserved-example-domain 0 README.md 'teacher@''School.EXAMPLE'
check uppercase-forbidden-domain 1 README.md 'teacher@''GMAIL.COM'
check noreply-hyphen-email 1 README.md 'someone@''users-noreply-github.com'
check noreply-first-dot-email 1 README.md 'someone@''users-noreply.github.com'
check noreply-second-dot-email 1 README.md 'someone@''users.noreply-github.com'
check noreply-last-dot-email 1 README.md 'someone@''users.noreply.github-com'
check reserved-dot-email 1 README.md 'someone@''dept.school-test'
check test-domain-email 1 README.md 'person@''school.test'
check invalid-domain-email 1 README.md 'person@''school.invalid'
check localhost-domain-email 1 README.md 'person@''school.localhost'
check example-domain-email 0 README.md 'person@''school.example'
check scanner-email 1 scripts/privacy-check.sh 'someone@''gmail.com'
check scanner-home 1 scripts/privacy-check.sh '/Us''ers/someone/app'
check scanner-phone 1 scripts/privacy-check.sh '5123''4567'
check test-script-email 1 scripts/privacy-check-test.sh 'someone@''gmail.com'
check test-script-home 1 scripts/privacy-check-test.sh '/Us''ers/someone/app'
check test-script-phone 1 scripts/privacy-check-test.sh '5123''4567'
check clean-test-script 0 scripts/privacy-check-test.sh "$(cat scripts/privacy-check-test.sh)"
check mac-home 1 README.md 'Built in /Us''ers/someone/Projects'
check linux-home 1 notes.txt 'see /ho''me/someone/app'
check windows-home 1 notes.txt 'C:\Us''ers\someone\app'
check lowercase-windows-home 1 notes.txt 'c:\us''ers\someone\app'
check mixed-case-windows-home 1 notes.txt 'C:/uS''eRs/用户/Projects'
check unicode-mac-home 1 notes.txt '/Us''ers/用户/Projects'
check unicode-linux-home 1 notes.txt '/ho''me/用户/app'
check unicode-windows-home 1 notes.txt 'C:\Us''ers\用户\Projects'
check windows-forward-home 1 notes.txt 'C:/Us''ers/someone/Projects'
check unicode-windows-forward-home 1 notes.txt 'C:/Us''ers/用户/Projects'
check windows-mixed-home 1 notes.txt 'C:/Us''ers\用户/Projects'
check windows-other-mixed-home 1 notes.txt 'C:\Us''ers/用户/Projects'
check bare-home-roots 0 notes.txt '/Us''ers/ /ho''me/ C:/Us''ers/ C:\Us''ers\'
check real-email 1 app/src/Foo.kt 'val contact = "someone@''gmail.com"'
check phone 1 README.md 'Call +372 51''23 45''67'
check tel-link 1 README.md '<a href="tel:''+3725''123456">'
check plain-phone 1 README.md 'Call 5123''4567'
check grouped-phone 1 README.md 'Call 5123 45''67'
check parenthesized-phone 1 README.md 'Call +1 (202) 55''5-01''23'
check local-parenthesized-phone 1 README.md 'Call (202) 55''5-01''23'
check date-not-phone 0 README.md 'Released 2026-11-01, version 1.2.3'
check readme-image 0 docs/assets/shot.png 'png'
check res-image 0 app/src/main/res/drawable/icon.png 'png'
check stray-image 1 docs/screenshot.png 'png'
check stray-svg 1 docs/screenshot.svg '<svg/>'
check stray-ico 1 docs/screenshot.ico 'ico'
check uppercase-svg 1 'docs/õpetaja screenshot.SVG' '<svg/>'
check uppercase-ico 1 'docs/õpetaja screenshot.ICO' 'ico'
check allowed-docs-svg 0 docs/assets/icon.svg '<svg/>'
check allowed-docs-ico 0 docs/assets/icon.ico 'ico'
check allowed-res-svg 0 app/src/main/res/raw/icon.svg '<svg/>'
check allowed-res-ico 0 app/src/main/res/raw/icon.ico 'ico'
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
for extension in JSON JsOn; do
    check "captured-$extension" 1 "data/response.$extension" '{"dbiAccessorRes":{}}'
    check "viewer-$extension" 1 "data/viewer.$extension" '{"regular":{"default_num":"22"}}'
    check "ttviewer-$extension" 1 "data/ttviewer.$extension" '{"ttviewer":{}}'
    check "fixture-$extension" 0 "app/src/test/resources/fixtures/response.$extension" '{"dbiAccessorRes":{}}'
    check "viewer-fixture-$extension" 0 "app/src/test/resources/fixtures/viewer.$extension" '{"regular":{"default_num":"22"}}'
    check "ttviewer-fixture-$extension" 0 "app/src/test/resources/fixtures/ttviewer.$extension" '{"ttviewer":{}}'
    check "plain-$extension" 0 "data/config.$extension" '{"name":"value"}'
done

big=$(awk 'BEGIN { for (i = 0; i < 2100; i++) printf "{\"id\":\"%05d\"},", i }')
check large-fixture 1 app/src/test/resources/fixtures/big.json "{\"r\":{\"dbiAccessorRes\":{\"tables\":[$big{}]}}}"
padding=$(awk 'BEGIN { for (i = 0; i < 21000; i++) printf " " }')
check large-viewer-fixture 1 app/src/test/resources/fixtures/viewer.json "{\"regular\":{
\"timetables\":[],\"default_num\":\"22\"}}$padding"
check large-spaced-fixture 1 'app/src/test/resources/fixtures/viewer response.json' "{\"regular\":{\"default_num\":\"22\"}}$padding"
for extension in JSON JsOn; do
    check "large-fixture-$extension" 1 "app/src/test/resources/fixtures/response.$extension" "{\"dbiAccessorRes\":{}}$padding"
    check "large-viewer-fixture-$extension" 1 "app/src/test/resources/fixtures/viewer.$extension" "{\"regular\":{\"default_num\":\"22\"}}$padding"
    check "large-ttviewer-fixture-$extension" 1 "app/src/test/resources/fixtures/ttviewer.$extension" "{\"ttviewer\":{}}$padding"
done
for encoding in utf-16 utf-16-le utf-16-be utf-32 utf-8-sig; do
    check "encoded-capture-$encoding" 1 data/capture.JSON '{"r":{"dbiAccessorRes":{}}}' "$encoding"
    check "encoded-ttviewer-$encoding" 1 data/capture.json '{"r":{"ttviewer":{}}}' "$encoding"
    check "encoded-viewer-$encoding" 1 data/capture.JsOn '{"r":{"regular":{"default_num":"22"}}}' "$encoding"
    check "encoded-fixture-$encoding" 0 app/src/test/resources/fixtures/response.JSON '{"r":{"dbiAccessorRes":{}}}' "$encoding"
    check "encoded-ttviewer-fixture-$encoding" 0 app/src/test/resources/fixtures/ttviewer.json '{"r":{"ttviewer":{}}}' "$encoding"
    check "encoded-viewer-fixture-$encoding" 0 app/src/test/resources/fixtures/viewer.json '{"r":{"regular":{"default_num":"22"}}}' "$encoding"
    check "encoded-large-fixture-$encoding" 1 app/src/test/resources/fixtures/response.JSON "{\"dbiAccessorRes\":{}}$padding" "$encoding"
    check "encoded-large-ttviewer-$encoding" 1 app/src/test/resources/fixtures/ttviewer.json "{\"ttviewer\":{}}$padding" "$encoding"
    check "encoded-large-viewer-$encoding" 1 app/src/test/resources/fixtures/viewer.json "{\"regular\":{\"default_num\":\"22\"}}$padding" "$encoding"
    check "encoded-home-$encoding" 1 notes.txt '/Us''ers/用户/Projects' "$encoding"
    check "encoded-windows-$encoding" 1 notes.txt 'c:\us''ers\someone\Projects' "$encoding"
    check "encoded-email-$encoding" 1 notes.txt 'someone@''gmail.com' "$encoding"
    check "encoded-phone-$encoding" 1 notes.txt 'Call 5123''4567' "$encoding"
    check "encoded-allowed-email-$encoding" 0 notes.txt 'teacher@''EXAMPLE.COM' "$encoding"
    check "encoded-clean-$encoding" 0 notes.txt 'Invented teacher A. Näide, room 101.' "$encoding"
done

# Commit messages are scanned only with --commits.
check_commit() {
    repo="$work/$1"
    mkdir -p "$repo/scripts"
    cp "$script" "$repo/scripts/"
    git -C "$repo" init -q
    git -C "$repo" add -A
    git -C "$repo" -c user.name=t -c user.email=t@example.com commit -qm 'Start'
    git -C "$repo" -c user.name=t -c user.email=t@example.com commit -q --allow-empty -m "$3"
    status=0
    (cd "$repo" && sh scripts/privacy-check.sh --commits HEAD~1..HEAD) > /dev/null 2>&1 || status=$?
    if [ "$status" -ne "$2" ]; then
        echo "FAIL $1: expected exit $2, got $status"
        failures=$((failures + 1))
    else
        echo "ok   $1"
    fi
}

check_commit commit-message 1 'Fix path /Us''ers/someone/app'
check_commit commit-linux-home 1 '/ho''me/someone/app'
check_commit commit-windows-home 1 'C:\Us''ers\someone\app'
check_commit commit-lowercase-windows-home 1 'c:\us''ers\someone\app'
check_commit commit-mixed-case-windows-home 1 'C:/uS''eRs/用户/Projects'
check_commit commit-unicode-mac-home 1 '/Us''ers/用户/Projects'
check_commit commit-unicode-linux-home 1 '/ho''me/用户/app'
check_commit commit-unicode-windows-home 1 'C:\Us''ers\用户\Projects'
check_commit commit-windows-forward-home 1 'C:/Us''ers/someone/Projects'
check_commit commit-unicode-windows-forward-home 1 'C:/Us''ers/用户/Projects'
check_commit commit-windows-mixed-home 1 'C:/Us''ers\用户/Projects'
check_commit commit-windows-other-mixed-home 1 'C:\Us''ers/用户/Projects'
check_commit commit-bare-home-roots 0 '/Us''ers/ /ho''me/ C:/Us''ers/ C:\Us''ers\'
check_commit commit-noreply-email 0 '12345+someone@''users.noreply.github.com'
check_commit commit-long-noreply-id 0 '1234''5678+someone@''users.noreply.github.com'
check_commit commit-numeric-noreply-user 0 '1234''5678@''users.noreply.github.com'
check_commit commit-numeric-plus-noreply-user 0 '12345+1234''5678@''users.noreply.github.com'
check_commit commit-numeric-example-user 0 '1234''5678@''example.com'
check_commit commit-numeric-plus-example-user 0 'someone+1234''5678@''example.com'
check_commit commit-noreply-and-phone 1 '1234''5678+someone@''users.noreply.github.com, call 5123''4567'
check_commit commit-phone-before-noreply 1 'Call 5123''4567, 1234''5678+someone@''users.noreply.github.com'
check_commit commit-numeric-personal-email 1 '1234''5678@''gmail.com'
check_commit commit-github-noreply-email 0 'noreply@''github.com'
check_commit commit-uppercase-example-domain 0 'teacher@''EXAMPLE.COM'
check_commit commit-mixed-example-domain 0 'teacher@''ExAmPlE.OrG'
check_commit commit-uppercase-noreply-domain 0 'someone@''USERS.NOREPLY.GITHUB.COM'
check_commit commit-uppercase-github-domain 0 'noreply@''GITHUB.COM'
check_commit commit-uppercase-reserved-example-domain 0 'teacher@''School.EXAMPLE'
check_commit commit-uppercase-forbidden-domain 1 'teacher@''GMAIL.COM'
check_commit commit-example-email 0 'teacher@''example.com'
check_commit commit-noreply-hyphen-email 1 'someone@''users-noreply-github.com'
check_commit commit-noreply-first-dot-email 1 'someone@''users-noreply.github.com'
check_commit commit-noreply-second-dot-email 1 'someone@''users.noreply-github.com'
check_commit commit-noreply-last-dot-email 1 'someone@''users.noreply.github-com'
check_commit commit-reserved-dot-email 1 'someone@''dept.school-test'
check_commit commit-test-domain-email 1 'person@''school.test'
check_commit commit-invalid-domain-email 1 'person@''school.invalid'
check_commit commit-localhost-domain-email 1 'person@''school.localhost'
check_commit commit-example-domain-email 0 'person@''school.example'
check_commit commit-plain-phone 1 'Call 5123''4567'
check_commit commit-grouped-phone 1 'Call 5123 45''67'
check_commit commit-parenthesized-phone 1 'Call +1 (202) 55''5-01''23'
check_commit commit-local-parenthesized-phone 1 'Call (202) 55''5-01''23'
check_commit commit-international-phone 1 'Call +372 51''23 45''67'
check_commit commit-tel-link 1 'tel:''+3725''123456'
check_commit commit-date 0 'Released 2026-11-01, version 1.2.3'

if [ "$failures" -ne 0 ]; then
    echo "$failures privacy-check test(s) failed" >&2
    exit 1
fi
echo "all privacy-check tests passed"
