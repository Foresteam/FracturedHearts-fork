# Commits

Commit completed changes before handing the task back to the user. Use separate commits for unrelated changes and stage only files within the task's scope.

# Gradle verification

Gradle cannot run in the Codex environment for this repository. The wrapper needs to write a lock file under `~/.gradle/wrapper/dists`, which is read-only in the sandbox. Skip Gradle commands and approval requests for Gradle. Use available checks that do not require Gradle and report the build as unverified.
