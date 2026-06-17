# Testing & Debugging Skill

Guidelines for testing and debugging the Shadowfang Folia plugin.

## Build & Deploy Cycle

1. **Make code changes**
2. **Build:** `./gradlew build` from the plugin directory
3. **Stop server** (must stop before replacing JAR)
4. **Deploy:** Copy JAR to `foliaserver/plugins/`
5. **Start server**
6. **Test the change**

## Viewing Logs

### Server Logs
- Location: `foliaserver/logs/latest.log`
- Recent: `foliaserver/logs/` (gzipped archives)

### Reading Logs
```powershell
Get-Content foliaserver/logs/latest.log -Tail 100
```

### Searching Logs
```powershell
Select-String "ERROR" foliaserver/logs/latest.log
Select-String "Shadowfang" foliaserver/logs/latest.log
Select-String "elevator" foliaserver/logs/latest.log
```

## Common Issues

### Plugin Not Loading
- Check for duplicate JAR files in `plugins/`
- Check log for "Ambiguous plugin name" errors
- Verify plugin.yml is correct

### Changes Not Taking Effect
- Server must be fully stopped, not just reloaded
- Delete plugin's cache/data folders if corrupted
- Check for serialization errors in stored data

### Entity Issues
- Entities are removed when chunks unload
- Check tick task for entity recovery logic
- Verify entity UUIDs are being stored correctly

## Debug Techniques

### Add Debug Logging
```java
plugin.getLogger().info("Debug: variable=" + value);
plugin.getLogger().warning("Warning: something unexpected");
```

### Check Player Position
```java
player.sendMessage("Position: " + player.getLocation().getBlockX() + "," 
    + player.getLocation().getBlockY() + "," 
    + player.getLocation().getBlockZ());
```

### Verify Data Storage
```java
plugin.getLogger().info("Loaded " + data.size() + " items");
```

## Chat-Based Testing

### Test Commands
- Use a player account, not console
- Check command permissions (`shadowfang.admin`)
- Use `/sr` root command with subcommands

### Teleport Testing
- Use `/sr verse spawn` or `/sr home` to teleport
- Verify world is loaded before teleport

## Pre-Commit Checklist

- [ ] Code compiles without errors
- [ ] New files added to git
- [ ] Old files removed from git
- [ ] Version bumped if needed
- [ ] README/docs updated if needed
- [ ] TODO updated if deferring anything
