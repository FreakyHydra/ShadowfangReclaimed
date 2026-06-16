# Shadowfang Reclaimed

A custom Folia 26.1.2 server plugin featuring factions, economy, bounties, lore, cross-world travel, and path building — all accessed through a unified `/sr` command system.

## RosettaStone — Folia Compatibility Layer

RosettaStone is the core compatibility bridge that makes non-Folia-aware plugins run on Folia's region-threaded architecture. It intercepts Bukkit scheduler, teleportation, and collection calls at runtime, routing them to the correct Folia region threads. On a standard Paper server it falls back to vanilla Bukkit — no code changes needed.

**Components:**
- `FoliaCompat` — runtime Folia detection + scheduler routing (`Bukkit.getScheduler()` → `GlobalRegionScheduler`)
- `RegionDispatcher` — chunk-scoped task execution (`server.execute()` → per-region dispatch)
- `FoliaPlayer` — thread-safe teleportation (`teleport()` → `teleportAsync()`)
- `ThreadSafeCollections` — concurrent data structures

**Used by:** Axiom and Worlds — third-party plugins that speak vanilla Bukkit and need translation to Folia's threading model.

The `/sr` sub-plugins (factions, economy, path tools, etc.) are native Folia code and do **not** use RosettaStone.

## /sr Command System

All plugin commands route through a single root: `/sr <sub-plugin> <subcommand>`. Each sub-plugin has a letter key and optional word keys.

| Sub-plugin | Keys | Purpose |
|---|---|---|
| **Faction** | `f`, `faction` | Faction creation, territory, members |
| **Economy** | `e`, `economy` | Silver Coin balance and transfers |
| **Bounty** | `b`, `bounty` | Bounty hunting quests |
| **Lore** | `l`, `lore` | Faction lore archive |
| **Road / Path** | `r`, `we`, `road`, `worldedit`, `path`, `pave` | Path building tools (WorldEdit-style) |
| **Verse** | `v`, `verse`, `sign`, `list`, `worlds` | Cross-world travel and management |
| **InfoBoard** | `i`, `infoboard`, `board` | Info board display terminals |

**Quick shortcuts:** `/sr h` (hub), `/sr s` (spawn), `/sr w <world>` (warp), `/sr t <world>` (travel).

A full command reference by category is available in-game via the web dashboard at `http://localhost:56552` > **COMMANDS** tab.

**Permissions:**
- `shadowfang.cmd` — base permission for `/sr` access (default: op)
- `shadowfang.we.use` — access the path tools (default: op)
- `shadowfang.we.admin` — admin path operations
- `shadowfang.admin` — admin operations across all sub-plugins

## Path Tools (WorldEdit) Plugin

A Folia-safe path-paving toolset. Build a road segment by hand, capture it as a 3D pattern, then tile it by walking or pasting.

**Quick start:**
```
/sr r wand          Get the path wand
/sr r pos1          Set first corner (or left-click with wand)
/sr r pos2          Set second corner (or right-click with wand)
/sr r copy          Capture the pattern (your position = anchor)
/sr r paste         Paste 2 blocks in front, rotated to your facing
/sr r start         Start walk-paste (road tiles as you walk)
/sr r stop          Stop walk-paste
/sr r undo          Revert last paste
```

**Key features:**
- **Template-stamping** — build once, stamp forever. The pattern follows you as you walk, rotating to match your facing.
- **Air offsets carve terrain** — air blocks in the template clear the world, so roads tunnel through mountains.
- **Undo support** — blocks are snapshotted before writing; `/sr r undo` restores them.
- **Folia-safe** — all block I/O goes through `RegionScheduler.execute()`; writes are batched by chunk.

## Credits &amp; Thanks

- **Axiom (Moulberry)** — World editing plugin by Moulberry (https://github.com/Moulberry/AxiomPaperPlugin). Our copy has been patched for Folia 26.1.2: all non-thread-safe collections replaced with `ConcurrentHashMap`, all `Bukkit.getScheduler()` calls routed through `FoliaCompat`, all `server.execute()` calls routed through `RegionDispatcher`, and all `player.teleport()` replaced with `teleportAsync()`. 7 files modified.

- **Worlds (Minecraft Worlds Plugin)** — Multi-world management plugin (https://www.spigotmc.org/resources/worlds.64947/). Made Folia-compatible through RosettaStone's scheduler routing and thread-safe world-loading wrappers.

- **PaperMC / Folia** — The server platform (https://papermc.io/software/folia).

If you are the creator of **Axiom** or **Worlds** and wish for your plugin (or the ported copy distributed here) to be removed, please contact FreakyHydra and it will be taken down immediately.

---

## Changelog

### /sr Command System &amp; Path Tools Plugin
**Version:** 1.1.0 | **Date:** June 17, 2026

Unified all commands under a single `/sr <sub-plugin> <subcommand>` root. Added a Folia-safe WorldEdit-style path building plugin (`com.shadowfang.core.worldedit`).

### Faction Bell Claiming System
**Version:** 1.0.0 | **Date:** June 15, 2026

**How it works:**
- Place a **Bell** anywhere to claim a territory around it
- Craft a Bell with 8 Gold Blocks (custom recipe, no vanilla bell)
- Only faction members can craft and place faction bells
- Territory is permanent until the faction disbands

**Commands:**

| Command | Description |
|---------|-------------|
| `/sr f create <name>` | Create a new faction |
| `/sr f invite <player>` | Invite a player to your faction |
| `/sr f accept` | Accept a pending invitation |
| `/sr f deny` | Decline a pending invitation |
| `/sr f claim <radius>` | Claim a (2×radius+1)² chunk area around your bell (Alpha only) |
| `/sr f info` | View faction info, hoard balance, member count |
| `/sr f setspawn` | Set faction spawn point (Alpha only) |
| `/sr f spawn` | Teleport to faction spawn |
| `/sr f deposit <amount>` | Deposit Silver Coins into the faction hoard |
| `/sr f withdraw <amount>` | Withdraw Silver Coins from the hoard (Alpha only) |
| `/sr f promote <player>` | Promote a member (Alpha only) |
| `/sr f demote <player>` | Demote a member (Alpha only) |
| `/sr f kick <player>` | Kick a member (Alpha only) |
| `/sr f leave` | Leave your faction |
| `/sr f disband` | Dissolve your faction (Alpha only) |

**Protections:**
- Territory blocks block breaking, placement, and interaction from non-members
- Faction Bells drop themselves when broken (with PDC data preserved)
- Admins with `shadowfang.admin` bypass all protections

**Economy Integration:**
- Faction hoard linked to Silver Coins economy
- Travel costs (10 coins) waived for faction members

**Technical:**
- Data persists to `config/shadowfang-core/factions.json` and `faction_chunks.json`
- Fully compatible with Folia's multi-threaded architecture

---

*The pack that howls together, hunts together.*
