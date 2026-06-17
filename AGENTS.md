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
| Shadowfang-Core-Folia | `Shadowfang-Core-Folia/build.gradle` | 1.3.1 |
| RosettaStone | `RosettaStone/build.gradle` | 1.0.0 |

## Deployment rules

- **ALWAYS stop the server before replacing JARs.** The running server locks plugin files, causing copy/remove failures. Stop the server first, replace the JAR, then start it again.

## How to bump a version

1. Edit the `version` field in the plugin's `build.gradle`
2. Rebuild: `./gradlew jar --console=plain` (run from that plugin's directory)
3. **Stop the server**
4. Copy the JAR from `build/libs/<name>-<version>.jar` to `foliaserver/plugins/`
5. Start the server

### Example (Shadowfang-Core-Folia)

```
# Edit: Shadowfang-Core-Folia/build.gradle  line 6
  version = '1.1.0'  →  version = '1.2.0'

# Build
cd Shadowfang-Core-Folia
./gradlew jar

# Deploy
Copy-Item build/libs/Shadowfang-Core-Folia-1.3.1.jar -Destination ../foliaserver/plugins/

# Reload server
foliaserver> reload confirm
```

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
