#!/bin/sh
# Checks tracked files for personal data that must not reach this public repository.
#
#   sh scripts/privacy-check.sh                  scan the tracked files
#   sh scripts/privacy-check.sh --commits A..B   also scan the messages of commits in A..B
#
# Run it from the repository root. It exits 1 when it finds something.
# Real names cannot be checked here, because a list of them would itself be personal data.
set -eu
exec python3 - "$@" <<'PYTHON'
import json
import os
from pathlib import Path
import re
import subprocess
import sys

image_dirs = r"^(docs/assets/|app/src/[^/]+/res/)"
fixture_dir = "app/src/test/resources/fixtures/"
fixture_max_bytes = 20000
home_path = re.compile(r"(/[U]sers/|/[h]ome/|[A-Za-z]:[/\\][Uu][Ss][Ee][Rr][Ss][/\\])[^/\\\s]+")
email = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")
phone = re.compile(r"(\+[0-9]{1,3}[ -]?[0-9]{2,4}[ -]?[0-9]{3,4}([ -]?[0-9]{2,4})?|(^|[^\w.])[1-9][0-9]{7,14}([^\w.]|$)|(^|[^\w.])([0-9]{2,4}[ -]){1,3}[0-9]{3,4}([^\w.]|$)|tel:[+0-9])", re.MULTILINE)
failed = False


def report(message):
    global failed
    print(f"privacy-check: {message}", file=sys.stderr)
    failed = True


def allowed_email(address):
    local, domain = address.rsplit("@", 1)
    domain = domain.lower()
    return (
        domain == "users.noreply.github.com"
        or (local == "noreply" and domain == "github.com")
        or domain in {"example.com", "example.org", "example.net"}
        or domain.endswith(".example")
    )


def scan_text(text, source):
    for number, line in enumerate(text.splitlines(), 1):
        if home_path.search(line):
            report(f"home directory path in {source}:{number}: {line}")
        for match in email.finditer(line):
            if not allowed_email(match.group()):
                report(f"email address outside noreply and example domains in {source}:{number}: {match.group()}")
        if phone.search(email.sub(" ", line)):
            report(f"phone number in {source}:{number}: {line}")


def edupage_shape(value):
    if isinstance(value, dict):
        regular = value.get("regular")
        return (
            "dbiAccessorRes" in value
            or "ttviewer" in value
            or (isinstance(regular, dict) and "default_num" in regular)
            or any(edupage_shape(child) for child in value.values())
        )
    if isinstance(value, list):
        return any(edupage_shape(child) for child in value)
    return False


paths = subprocess.check_output(["git", "ls-files", "-z"]).split(b"\0")
for raw_path in paths:
    if not raw_path:
        continue
    name = os.fsdecode(raw_path)
    if re.search(r"\.(png|jpe?g|webp|gif|bmp|heic|heif|tiff?|avif|svg|ico)$", name, re.I) and not re.match(image_dirs, name):
        report(f"image outside docs/assets/ or app resources: {name!r}")
    data = Path(name).read_bytes()
    try:
        text = data.decode(json.detect_encoding(data))
    except UnicodeError:
        continue
    if "\0" in text:
        continue
    scan_text(text, repr(name))
    if not name.lower().endswith(".json"):
        continue
    marked = re.search(r'"dbiAccessorRes"|"ttviewer"', text)
    if not marked:
        try:
            marked = edupage_shape(json.loads(text))
        except ValueError:
            marked = False
    if not marked:
        continue
    if not name.startswith(fixture_dir):
        report(f"EduPage-shaped JSON outside {fixture_dir}: {name!r}")
    elif len(data) > fixture_max_bytes:
        report(f"EduPage fixture larger than {fixture_max_bytes} bytes, which suggests a captured response: {name!r}")

if len(sys.argv) > 2 and sys.argv[1] == "--commits" and sys.argv[2]:
    messages = subprocess.check_output(["git", "log", "--format=%h %B", sys.argv[2]])
    scan_text(messages.decode("utf-8", errors="replace"), f"commit messages in {sys.argv[2]}")

if failed:
    print("privacy-check: failed. Remove the data, or use invented values in fixtures.", file=sys.stderr)
    sys.exit(1)
print("privacy-check: ok")
PYTHON
