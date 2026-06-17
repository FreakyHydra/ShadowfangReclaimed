# On Hold: Info Board Duplication Bug

**Status:** ON HOLD  
**Priority:** Medium  
**Created:** 2026-06-17  
**Version when discovered:** 1.2.2

## Bug Description

When a player moves far away from an info board (causing the chunk to unload) and then returns, a duplicate board appears. Deleting the original leaves behind a ghost copy that cannot be removed.

## Root Cause

In `InfoBoardManager.startTickTask()` (lines 98-105), the entity recovery logic runs every 10 ticks:

```java
if (board.getTextDisplayUuid() != null && Bukkit.getEntity(board.getTextDisplayUuid()) == null) missing = true;
if (board.getInteractionUuid() != null && Bukkit.getEntity(board.getInteractionUuid()) == null) missing = true;
if (missing) {
    renderer.removeEntities(board);
    renderer.spawnEntities(board);
}
```

**Problem:** When a chunk unloads, `Bukkit.getEntity(uuid)` returns `null` even though the entity still exists in the unloaded chunk. The code interprets this as "entity missing" and spawns new entities. When the chunk reloads, both the original and new entities exist, creating duplicates.

**Why deletion fails:** `removeBoard()` only removes entities with the current UUIDs (the new ones). The original entities remain as untracked ghosts.

## Planned Fix

### 1. Add chunk load awareness

Add a helper method to check if the board's chunk is loaded:

```java
private boolean isChunkLoaded(InfoBoard board) {
    World world = Bukkit.getWorld(board.getWorld());
    if (world == null) return false;
    int chunkX = (int) board.getX() >> 4;
    int chunkZ = (int) board.getZ() >> 4;
    return world.isChunkLoaded(chunkX, chunkZ);
}
```

### 2. Modify tick task entity recovery

Only attempt entity recovery if the chunk is loaded:

```java
for (InfoBoard board : boards.values()) {
    if (!isChunkLoaded(board)) continue; // Skip unloaded chunks
    
    boolean missing = false;
    if (board.getTextDisplayUuid() != null && Bukkit.getEntity(board.getTextDisplayUuid()) == null) missing = true;
    if (board.getInteractionUuid() != null && Bukkit.getEntity(board.getInteractionUuid()) == null) missing = true;
    if (missing) {
        renderer.removeEntities(board);
        renderer.spawnEntities(board);
    }
    // ... rest of update logic
}
```

### 3. Consider additional safeguards

- Track entity spawn state more explicitly (e.g., `board.setEntitiesSpawned(true/false)`)
- Add cleanup logic to detect and remove orphaned entities on server start
- Consider using `ChunkLoadEvent` to re-spawn entities when chunks load

## Testing Checklist

- [ ] Create a board
- [ ] Move far away (chunk unloads)
- [ ] Return (chunk reloads)
- [ ] Verify no duplicate appears
- [ ] Delete the board
- [ ] Verify it's completely gone (no ghost entities)
- [ ] Test with multiple boards in different chunks

## Files to Modify

- `Shadowfang-Core-Folia/src/main/java/com/shadowfang/core/infoboard/InfoBoardManager.java`
- `Shadowfang-Core-Folia/build.gradle` (bump to 1.2.3)
- `AGENTS.md` (update version table)

## Notes

This is a classic Minecraft entity management issue. The fix is straightforward but requires careful testing to ensure entities are properly tracked across chunk load/unload cycles.
