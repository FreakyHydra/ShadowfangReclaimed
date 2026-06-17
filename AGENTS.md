# Shadowfang Folia Project — Agent Guide

## Version scheme: `MAJOR.MINOR.PATCH`

| Bump | When |
|------|------|
| **MAJOR** (X.0.0) | Breaking API changes, config format changes, world/DB schema changes, major reworks |
| **MINOR** (0.X.0) | New features, significant behavior changes, new commands/subcommands |
| **PATCH** (0.0.X) | Bug fixes, minor tweaks, message/color adjustments, internal refactors with no user-facing change |

## Plugin inventory

| Plugin | build file | Current version |
|--------|------------|-----------------|
| Shadowfang-Core-Folia | `Shadowfang-Core-Folia/build.gradle` | 1.6.1 |
| RosettaStone | `RosettaStone/build.gradle` | 1.0.0 |
| AbyssalTalisman | `AbyssalTalisman/build.gradle` | 0.1.1-alpha |

## Server version

- **Minecraft**: 26.1.2
- **Folia**: 26.1.2-8
- **Folia API dependency**: `dev.folia:folia-api:1.21.11-R0.1-SNAPSHOT` (confirmed working on Folia 26.1.2-8)

## Deployment rules

- **NEVER kill Java processes to stop the server** until after worlds have been saved. Killing Java processes (e.g., `Stop-Process -Name java`) before save completes leaves file locks on JARs, world files, and session.lock, causing crash loops and corrupted state on restart.
- **The ONLY correct stop sequence is:**
  1. Run `stop` command in the server console (or connect via RCON and send `stop`)
  2. Wait for the process to exit and port 25565 to be released
  3. **Never stop without saving** — the `stop` command auto-saves worlds; do not kill the process until it completes
  4. After the `stop` command is sent, wait 5 seconds for saves to complete, then verify port 25565 is released
  5. After save is confirmed complete, it is safe to brute-force kill remaining Java processes if needed
  6. Only then: delete old JARs, copy new JARs
  7. **Do NOT start the server** — leave it stopped. The user will start it themselves to verify.
  8. If startup must be verified, check the logs for errors after the user confirms the server is running.
- **If the server refuses to start due to file locks after a failed stop**, the workspace is corrupted. The user must manually fix it by:
  - Deleting `foliaserver/plugins/` duplicate JARs
  - Deleting `foliaserver/session.lock` (leftover from unclean shutdown)
  - Restarting the server normally
- **Server file locks also prevent JAR deletion while the server is running.** If `Remove-Item` fails with "file is being used by another process", the server is still running — do NOT force-delete, stop the server properly instead.

## How to bump a version

1. Edit the `version` field in the plugin's `build.gradle`
2. Rebuild: `./gradlew jar --console=plain` (run from that plugin's directory)
3. **Stop the server** using the `stop` command in the server console — wait for it to fully exit
4. Verify port 25565 is free: `Get-NetTCPConnection -LocalPort 25565` returns nothing
5. Delete old JAR versions from `foliaserver/plugins/` to avoid "Ambiguous plugin name" errors
6. Copy the new JAR from `build/libs/<name>-<version>.jar` to `foliaserver/plugins/`
7. **Do NOT start the server** — leave it stopped. The user will start it themselves.
8. After deployment is confirmed working, **commit and push** to GitHub:
   ```
   git add <changed files>
   git commit -m "<version>: <brief description>

   - <change 1>
   - <change 2>
   - ..."
   git push
   ```

### Example (Shadowfang-Core-Folia)

```
# Edit: Shadowfang-Core-Folia/build.gradle  line 6
  version = '1.3.2'  →  version = '1.3.3'

# Build
cd Shadowfang-Core-Folia
./gradlew jar

# Deploy (server must already be stopped)
Remove-Item ../foliaserver/plugins/Shadowfang-Core-Folia-1.3.2.jar
Copy-Item build/libs/Shadowfang-Core-Folia-1.3.3.jar ../foliaserver/plugins/

# Commit & Push
git add Shadowfang-Core-Folia/build.gradle Shadowfang-Core-Folia/src/main/java/... README.md
git commit -m "v1.3.3: Fix Y-offset bug in elevator teleport

- Elevator: teleport now uses y+1 so players spawn on top of floor blocks
- ..."
git push
```

## Deployment Checklist

Run through this checklist for every deployment:

- [ ] Edit version in `build.gradle`
- [ ] Run `./gradlew jar --console=plain` — verify BUILD SUCCESSFUL
- [ ] Stop server via `stop` command in console
- [ ] Wait 5 seconds for saves to complete
- [ ] Verify port 25565 is free (`Get-NetTCPConnection -LocalPort 25565` returns nothing)
- [ ] Delete old JAR versions from `foliaserver/plugins/`
- [ ] Copy new JAR to `foliaserver/plugins/`
- [ ] **Do NOT start the server** — leave it stopped
- [ ] User confirms deployment working
- [ ] **Commit** with descriptive message including changelog
- [ ] **Push** to GitHub
- [ ] **Update README.md** version history and any changed feature docs before pushing

## Handling Deferred Issues

When a bug or feature cannot be addressed immediately:

1. **Load the TODO skill:** When an issue needs to be tracked for later, say "load the TODO skill" and it will be injected with instructions
2. **Fill out TODO.md:** Document the issue with:
   - Clear description
   - Priority (High/Medium/Low)
   - Status (ON HOLD for bugs, NEW for features)
   - Root cause or implementation notes
   - Files that would need modification
3. **Commit:** Add to git with descriptive message

See `TODO.md` for the full backlog of known issues and planned features.
