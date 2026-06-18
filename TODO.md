# TODO / Backlog

## Bugs

### Quest Book Datapack Not Loading (Priority: High)
**Status:** NEW

The `shadowfang_quests` datapack loads (1617 vanilla advancements detected) but our ~34 custom advancements are not registered. The quest tab does not appear in the L menu.

**Root Cause (suspected):**
1. `pack.mcmeta` uses `min_format: 101.1` / `max_format: 101.1` — decimal values may be invalid; should likely use `pack_format: 61` (integer) for MC 26.1.2
2. Two advancement icons still use old `nbt` format instead of `components`:
   - `economy/bounty_hunter.json`: `"nbt": "{SkullOwner:'Shadowfang'}"` on player_head
   - `talismans/first_fragment.json`: `"nbt": "{title:'Lost Fragment...',author:'Unknown'}"` on written_book
3. `components` format for MC 26.1.2 requires different syntax (e.g., `minecraft:profile` for skulls, `minecraft:written_book_content` for books)

**Fix:**
1. Change `pack.mcmeta` to `pack_format: 61` (integer) and remove `min_format`/`max_format`
2. Convert icon `nbt` → `components` for the two remaining files
3. Verify `minecraft:tick` trigger works for root advancement in MC 26.1.2

**Files to modify:**
- `Shadowfang-Core-Folia/src/main/resources/datapacks/shadowfang_quests/pack.mcmeta`
- `Shadowfang-Core-Folia/src/main/resources/datapacks/shadowfang_quests/data/shadowfang/advancements/economy/bounty_hunter.json`
- `Shadowfang-Core-Folia/src/main/resources/datapacks/shadowfang_quests/data/shadowfang/advancements/talismans/first_fragment.json`

---

### Info Board Duplication Bug (Priority: Medium)
**Status:** ON HOLD

When a player moves far away from an info board and returns, a duplicate board appears. Deleting the original leaves a ghost copy.

**Root Cause:** The tick task checks `Bukkit.getEntity(uuid) == null` to detect missing entities. When a chunk unloads, entities become inaccessible (returns null) even though they still exist. The code interprets this as "entity missing" and spawns new ones, creating duplicates.

**Fix:** Check if chunk is loaded before attempting entity recovery. Only re-spawn if chunk is loaded AND entity is missing.

**Files to modify:**
- `Shadowfang-Core-Folia/src/main/java/com/shadowfang/core/infoboard/InfoBoardManager.java`

---

## Feature Ideas

### Custom Teleport Pads
Add the ability to customize teleport pad appearances with the resource pack (custom textures, glow effects).

### Teleport Cooldown Per Group
Allow configurable cooldown per teleport group instead of a global 1-second cooldown.

### Teleport Pad Naming
Allow players to name individual floors (e.g., "Lobby", "Roof", "Basement") using the wand with a name argument.

### Warp System for Verse
Allow admins to save and manage named warps with `/sr verse warp set <name>`.

### Faction Hierarchy Ranks
Add custom ranks (e.g., Officer, General) with different permissions within a faction.

### Bounty Board
A public board showing active bounties that players can post and collect.

### Lore Pages
Allow longer lore entries with multi-page support for faction archives.

---

## Technical Debt

- [ ] Clean up unused imports across all Java files
- [ ] Add Javadoc comments to public API methods
- [ ] Add unit tests for elevator destination selection logic
- [ ] Consider moving data storage from JSON to SQLite for better concurrent access
- [ ] Add logging configuration file
- [ ] Document plugin architecture in ARCHITECTURE.md

---

## Ideas That Were Considered

### Pressure Plate Teleporters
Used pressure plates as floor triggers instead of sneak detection. Decided against it because:
- Small hitbox makes them harder to stand on reliably
- Would need two block types per floor (up/down)
- Jump/sneak on solid blocks is more reliable

### Cross-World Teleporters
Allow teleporters to connect across different dimensions. Considered but deferred:
- Would need to handle different world spawn points
- Dimension transitions add complexity
- Current same-world scope is simpler for players to understand
