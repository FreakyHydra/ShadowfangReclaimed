# How to Push Updates to GitHub

## One-Time Setup

```bash
# Set your identity (if not already done)
git config user.name "FreakyHydra"
git config user.email "you@example.com"

# Verify remote is set correctly
git remote -v
# Should show: origin  https://github.com/FreakyHydra/ShadowfangReclaimed.git
```

## Daily Workflow: Make Changes & Push

```bash
# 1. Check what changed
git status

# 2. Stage your changes
git add -A

# 3. Commit with a descriptive message
git commit -m "Brief description of what you changed"

# 4. Push to GitHub — this triggers the auto-build
git push origin main
```

## What Happens After Push

1. **GitHub Actions** automatically builds the plugin
2. A new `ShadowfangReclaimed.jar` is uploaded to the **latest release**
3. Download URL: `https://github.com/FreakyHydra/ShadowfangReclaimed/releases/latest/download/ShadowfangReclaimed.jar`

## Updating the Main PC Server

Run `update-plugins.bat` on the main PC before starting the server.
It downloads the latest JAR from GitHub and replaces the old one.

## Branch: `main`

Always push to `main`. The CI workflow only triggers on pushes to this branch.

## What's Tracked in Git

- `Shadowfang-Core-Folia/` — plugin source code
- `resourcepack/` — custom resource pack
- `.github/workflows/build.yml` — CI build workflow
- `CHANGELOG.md`

The `foliaserver/` directory (server runtime, worlds, Java) is **not** in git — it stays local.
