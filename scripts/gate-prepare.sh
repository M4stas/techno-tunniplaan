#!/bin/sh
set -eu

if [ -f local.properties ] || [ -n "${ANDROID_HOME:-}" ]; then
    exit 0
fi

for sdk in "$HOME/Library/Android/sdk" /opt/homebrew/share/android-commandlinetools "$HOME/Android/Sdk"; do
    if [ -d "$sdk" ]; then
        printf 'sdk.dir=%s\n' "$sdk" > local.properties
        exit 0
    fi
done

echo "Android SDK not found. Set ANDROID_HOME or create local.properties." >&2
exit 1
