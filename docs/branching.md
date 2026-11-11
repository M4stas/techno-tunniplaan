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

Keep existing unpublished feature work on its branch. Bring it up to date with `origin/dev` before submitting it. Do not commit directly on either protected branch or publish a pull request branch directly to `origin`.

## Promoting development work

Create a `release/*` branch from the validated `dev` commit, then run no-mistakes with `--base-branch main`. This lets the gate publish fixes without pushing directly to protected `dev`. The release pull request must pass CI against the current `main` before merging.

CI runs on pull requests into either branch and on pushes after merges. The required `build-test` job runs Android lint, JVM unit tests and a debug APK build with JDK 21. A failed step blocks the merge.

Passing CI does not verify every device interaction. Before promoting app behavior changes, complete the applicable emulator checks in `docs/agent-policy.md`. Keep screenshots and real timetable data out of the repository.

After a release, synchronize `main` back into `dev` through a pull request if the release introduced fixes that are missing from `dev`.
