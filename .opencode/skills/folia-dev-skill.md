# Folia Development Skill

Essential patterns for developing plugins on Folia (Paper fork with region-based threading).

## Folia-Specific Rules

### 1. Scheduling Tasks

**ALWAYS use Folia schedulers, NOT Bukkit schedulers:**
```java
// BAD - Bukkit scheduler
Bukkit.getScheduler().runTask(plugin, () -> { ... });

// GOOD - Folia GlobalRegionScheduler
Bukkit.getGlobalRegionScheduler().execute(plugin, () -> { ... });

// GOOD - For chunk-specific operations
Bukkit.getRegionScheduler().execute(plugin, location, () -> { ... });
```

### 2. Teleportation

**ALWAYS use teleportAsync for cross-region teleports:**
```java
// BAD - Vanilla teleport can fail on Folia
player.teleport(location);

// GOOD - Thread-safe teleportation
player.teleportAsync(location).thenAccept(result -> {
    if (result) {
        // Teleport successful
    }
});
```

### 3. Entity Manipulation

**Use entity's own scheduler for entity-related operations:**
```java
entity.getScheduler().run(plugin, task -> {
    // Safe entity manipulation
    entity.remove();
}, null);
```

### 4. Collections

**Use concurrent collections for shared state:**
```java
// BAD - Regular HashMap
Map<UUID, Player> players = new HashMap<>();

// GOOD - Thread-safe
Map<UUID, Player> players = new ConcurrentHashMap<>();
```

### 5. Chunk Access

**Check if chunk is loaded before accessing blocks:**
```java
int cx = x >> 4;
int cz = z >> 4;
if (world.isChunkLoaded(cx, cz)) {
    Block block = world.getBlockAt(x, y, z);
}
```

## Common Patterns

### Region-Aware Block I/O

```java
Bukkit.getRegionScheduler().execute(plugin, blockLocation, () -> {
    if (!location.getWorld().isChunkLoaded(cx, cz)) return;
    Material material = world.getBlockAt(x, y, z).getType();
    // Process...
});
```

### Async Data Persistence

```java
Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
    // Periodic saves
}, 20 * 60, 20 * 60); // Every minute
```

### Player-Independent Entity Spawning

```java
world.getChunkAt(cx, cz).addEntity(entity); // Spawn in specific chunk
```

## Common Mistakes

1. **Using Bukkit.getScheduler()** instead of Folia schedulers
2. **Using player.teleport()** instead of teleportAsync()
3. **Accessing blocks/chunks without checking if loaded**
4. **Using non-thread-safe collections in concurrent code**
5. **Assuming entities exist when chunks unload**

## File Locations

- Plugin data: `plugin.getDataFolder()`
- Config: `plugin.getConfig()`
- World data: `Bukkit.getWorld("worldname")`
- Plugin description: `plugin.getDescription().getMain()`
