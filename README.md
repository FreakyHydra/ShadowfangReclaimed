# Shadowfang Reclaimed

A custom Folia server plugin featuring factions, economy, bounties, lore, cross-world travel, and path building — all accessed through a unified `/sr` command system.

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

## Credits & Thanks

- **RosettaStone (FreakyHydra)** — A Folia compatibility bridge developed by FreakyHydra. It detects Folia at runtime and intercepts Bukkit scheduler, teleportation, and collection calls, routing them transparently to Folia's region-threaded architecture. On standard Paper it falls back to vanilla Bukkit. This layer is what makes third-party plugins run unmodified on Folia.  
  *Components: `FoliaCompat` (runtime detection + scheduler routing), `RegionDispatcher` (chunk-scoped task execution), `FoliaPlayer` (thread-safe teleportation), `ThreadSafeCollections` (concurrent data structures).*

- **Axiom (Moulberry)** — World editing plugin, made Folia-compatible through RosettaStone's `RegionDispatcher` and `FoliaCompat`. Axiom's own source is unmodified — RosettaStone handles scheduling its tasks on the correct Folia region threads.  
  Source: `Borrowed/AxiomPaperPlugin/`  
  https://github.com/Moulberry/AxiomPaperPlugin

- **Worlds (Minecraft Worlds Plugin)** — Multi-world management plugin, made Folia-compatible through RosettaStone's scheduler routing and thread-safe world-loading wrappers.  
  Source: `Borrowed/worlds/`  
  https://www.spigotmc.org/resources/worlds.64947/

- **PaperMC / Folia** — The server platform this plugin runs on.  
  https://papermc.io/software/folia

If you are the creator of **Axiom** or **Worlds** and wish for your plugin (or the ported copy distributed here) to be removed, please contact FreakyHydra and it will be taken down immediately.

---

## Changelog

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
| `/f create <name>` | Create a new faction |
| `/f invite <player>` | Invite a player to your faction |
| `/f accept` | Accept a pending invitation |
| `/f deny` | Decline a pending invitation |
| `/f claim <radius>` | Claim a (2×radius+1)² chunk area around your bell (Alpha only) |
| `/f info` | View faction info, hoard balance, member count |
| `/f setspawn` | Set faction spawn point (Alpha only) |
| `/f spawn` | Teleport to faction spawn |
| `/f deposit <amount>` | Deposit Silver Coins into the faction hoard |
| `/f withdraw <amount>` | Withdraw Silver Coins from the hoard (Alpha only) |
| `/f promote <player>` | Promote a member (Alpha only) |
| `/f demote <player>` | Demote a member (Alpha only) |
| `/f kick <player>` | Kick a member (Alpha only) |
| `/f leave` | Leave your faction |
| `/f disband` | Dissolve your faction (Alpha only) |

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
