# Branches and releases

`main` is the stable default branch. `dev` collects development changes. Start feature branches from the latest `origin/dev` and merge them into `dev` through pull requests.

## Protection rules

GitHub protects both `main` and `dev`:

- Every change requires a pull request.
- The GitHub Actions `build-test` check must pass with the branch up to date with its target.
- All review conversations must be resolved.
- The rules apply to administrators too.
- Force pushes and branch deletion are blocked.

The repository currently has one collaborator. Required approving reviews are set to zero so the owner can merge their own pull requests. Independent agent review runs through no-mistakes before a development branch is published.

These protections are repository settings, separate from the workflow files. Check them in GitHub when changing the branching policy.

## Development pull requests

The no-mistakes configuration targets `dev` by default. From a feature branch with committed changes, run:

```sh
no-mistakes axi run --base-branch dev --intent "Describe the intended change"
```

When several validated pull requests wait to merge into `dev`, merge them one at a time. A later pull request that falls behind `dev` only because an earlier one merged needs no new gate run. Update it with `gh pr update-branch <number>`, wait for `build-test` to pass, then merge.

Keep existing unpublished feature work on its branch. Bring it up to date with `origin/dev` before submitting it. Do not commit directly on either protected branch or publish a pull request branch directly to `origin`.

## Promoting development work

Create a `release/*` branch from the validated `dev` commit, then run no-mistakes with `--base-branch main`. This lets the gate publish fixes without pushing directly to protected `dev`. The release pull request must pass CI against the current `main` before merging.

CI runs on pull requests into either branch and on pushes after merges. The required `build-test` job runs the privacy check, Android lint, JVM unit tests and a debug APK build with JDK 21. A failed step blocks the merge.

## Privacy check

`scripts/privacy-check.sh` fails when a tracked file contains a home directory path, an email address outside the GitHub noreply and example domains, or a phone number. It also fails on an image outside `docs/assets/` and the app's `res/` folders, on EduPage-shaped JSON outside `app/src/test/resources/fixtures/`, and on a fixture larger than 20 KB. With `--commits <range>` it scans commit messages for the same data. CI runs it on every pull request, and the no-mistakes lint step runs it before Android lint. Run it locally with `sh scripts/privacy-check.sh`. `sh scripts/privacy-check-test.sh` tests the script itself.

The script cannot detect real names. Reading the staged diff before each commit is still required.

Passing CI does not verify every device interaction. Before promoting app behavior changes, test them on an emulator or device. Keep real timetable data and screenshots containing it out of the repository. README screenshots must use invented data.

After a release, synchronize `main` back into `dev` through a pull request if the release introduced fixes that are missing from `dev`.

## Building and publishing a release APK

The version defaults and signing configuration are defined in [app/build.gradle.kts](../app/build.gradle.kts). Override the version with `-PversionName=<version>` and `-PversionCode=<integer>` when needed. Increase `versionCode` for subsequent published updates.

Release signing reads Gradle properties. Keep the keystore outside the repository and put these entries in your Gradle user properties file, normally `~/.gradle/gradle.properties`:

```properties
TECHNO_RELEASE_STORE_FILE=/absolute/path/to/release.jks
TECHNO_RELEASE_STORE_PASSWORD=<store-password>
TECHNO_RELEASE_KEY_ALIAS=<key-alias>
TECHNO_RELEASE_KEY_PASSWORD=<key-password>
```

Do not commit this file or its values. Back up the keystore and keep its credentials safe. Updates must use the same signing key to preserve installed app data.

Run `./gradlew assembleRelease` to build the APK in `app/build/outputs/apk/release/`. With no `TECHNO_RELEASE_STORE_FILE`, the output is `app-release-unsigned.apk`, which cannot be installed as-is. Setting that property enables signing and requires all three remaining properties. Missing properties fail Gradle configuration; an invalid key or credentials fails the release build. A successful signed build produces `app-release.apk`.

The release asset is named `techno-tunniplaan-<version>.apk`, with the version written without a leading `v`. For installation steps, see [README.md](../README.md#paigaldamine) and its [English translation](../README.en.md#install).

## Release workflow

[`.github/workflows/release.yml`](../.github/workflows/release.yml) publishes a GitHub release when a tag matching `v*` is pushed. It runs the JVM unit tests, builds the signed release APK, checks the signature with `apksigner` and creates the release with notes generated from the merged pull requests.

Tags must use `vMAJOR.MINOR.PATCH`, optionally followed by a hyphen and a suffix containing letters, digits or dots. The workflow removes the leading `v` for `versionName` and calculates `versionCode` as `major * 10000 + minor * 100 + patch`. For example, `v1.2.3` builds `versionName` `1.2.3` and `versionCode` `10203`. Minor and patch must stay at 99 or lower. Rerunning the same tag keeps the same version values.

A stable tag must point at a commit reachable from `main`. A tag with a suffix, such as `v1.1.0-beta.1`, becomes a GitHub pre-release and must point at a commit reachable from `dev` or `main`. The commit need not be the branch tip. A pre-release shares its `versionCode` with the stable version it precedes.

The repository owner must create the signing key and set the four repository secrets. Agents must never handle the signing passwords. This script makes a new key with a random password, stores both only in GitHub secrets and deletes the local copy. Run it with JDK 21 on the `PATH`:

```sh
(
  set -e
  cd "$(mktemp -d)"
  pass=$(openssl rand -base64 24 | tr -d '/+=')
  keytool -genkeypair -keystore release.jks -storetype PKCS12 \
    -alias techno -keyalg RSA -keysize 4096 -validity 10000 \
    -dname "CN=techno-tunniplaan" -storepass "$pass" -keypass "$pass"
  repo=M4stas/techno-tunniplaan
  base64 -i release.jks | gh secret set TECHNO_RELEASE_KEYSTORE_BASE64 --repo "$repo"
  printf %s "$pass" | gh secret set TECHNO_RELEASE_STORE_PASSWORD --repo "$repo"
  printf %s techno | gh secret set TECHNO_RELEASE_KEY_ALIAS --repo "$repo"
  printf %s "$pass" | gh secret set TECHNO_RELEASE_KEY_PASSWORD --repo "$repo"
  rm release.jks
)
```

GitHub never shows a secret again, so the key cannot be recovered. If the secrets are lost, run the script again and release a new version. Android refuses an update signed with a different key, so existing users must uninstall the app before installing it. The workflow fails before building when any secret is missing.

To publish, merge the release pull request into `main`, then tag the merge commit:

```sh
git fetch origin
git tag v1.0.0 origin/main
git push origin v1.0.0
```

Follow the run under the repository's Actions tab. If it fails, delete the tag with `git push origin :refs/tags/v1.0.0` and `git tag -d v1.0.0`, fix the cause and tag again.
