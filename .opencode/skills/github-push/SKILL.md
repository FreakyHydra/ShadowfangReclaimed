# GitHub Push Skill

Guide for pushing Shadowfang Core plugin updates to GitHub and triggering auto-builds.

## Push New Changes

```bash
git add -A
git commit -m "Description of changes"
git push origin main
```

## CI Auto-Build

After push, GitHub Actions builds the plugin and uploads the JAR to:
```
https://github.com/FreakyHydra/ShadowfangReclaimed/releases/latest/download/ShadowfangReclaimed.jar
```

## Main PC Update

Run `update-plugins.bat` on the main PC to download the latest JAR.
