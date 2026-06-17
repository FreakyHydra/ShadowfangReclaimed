# Shadowfang Reclaimed

A custom Folia 26.1.2 server plugin featuring factions, economy, bounties, lore, cross-world travel, and teleporters.

---

## Version History

| Version | Notes |
|---------|-------|
| **v1.4.2** | **[NEW] Elevator GUI menu** — chest-based teleporter, sneak to open, click to teleport instantly |
| **v1.4.1** | Direct click-to-teleport for elevators |
| **v1.4.0** | **[NEW] Gold glassmorphic web dashboard**, clickable elevator menus, floor naming |
| v1.3.1 | Jun 17, 2026 | Elevator/Teleporter System replaces road builder |
| v1.3.0 | Jun 17, 2026 | Deprecated road builder |
| v1.2.2 | Jun 17, 2026 | Fixed walk-paste rotation and segment length bugs |
| v1.2.1 | Jun 17, 2026 | Fixed walk-paste rotation direction |
| v1.2.0 | Jun 17, 2026 | Added walk-paste mode to road builder |
| v1.1.0 | Jun 17, 2026 | InfoBoard display system added |
| v1.0.0 | Jun 15, 2026 | Faction bell claiming system |

---

## Quick Reference

### /sr Commands

All commands route through `/sr <sub-plugin> <subcommand>`.

| Sub-plugin | Keys | Purpose |
|-----------|------|---------|
| Faction | `f`, `faction` | Factions, territory, bells |
| Economy | `e`, `economy` | Silver Coins |
| Bounty | `b`, `bounty` | Bounty quests |
| Lore | `l`, `lore` | Faction lore archive |
| **Elevator** | `el`, `elevator`, `tp` | Teleporter pads |
| Verse | `v`, `verse`, `sign` | Cross-world travel |
| InfoBoard | `i`, `infoboard`, `board` | Display terminals |

**Shortcuts:** `/sr h` (hub), `/sr s` (spawn), `/sr w <world>` (warp), `/sr t <world>` (travel).

**Permissions:** `shadowfang.admin` for all admin commands.

---

## [NEW] Web Dashboard (v1.4.0)

Live web control panel at **http://localhost:56552** (starts automatically on server boot).

### Features
- **Telemetry** — Live TPS, RAM usage, online players
- **Teleporters** — Manage elevator groups, view floors, delete groups (full CRUD)
- **Dimensions** — List worlds, teleport yourself, create new worlds
- **Entities** — View online players with ping, heal/feed/kick actions
- **Commands** — Expandable card reference for all `/sr` commands
- **Info Boards** — Manage display terminals and custom programs
- **Terminal** — Live server log viewer with command execution
- **Config** — View and edit plugin config files (elevators.json, info_boards.json, worlds.yml) |

### Theme
Black and gold glassmorphic design with card animations, hover effects, and a cohesive cyberpunk aesthetic.

---

## [NEW] Elevator / Teleporter System (v1.3.1)

Turn any block into a teleporter pad. Link pads together and sneak to teleport.

### Setup
```
/sr elevator create tower1        Create group "tower1", get wand
Right-click blocks with wand     Add them as floors (type name in chat)
/sr elevator namefloor <name> <#> Rename a floor
/sneak on any floor             Teleport to other linked floors
```

### Commands
```
/sr elevator create <name>       Create group, get wand
/sr elevator assign <name>      Get wand for existing group
/sr elevator remove <name>        Delete entire group
/sr elevator list                List all groups
/sr elevator info <name>         Show floors in group
/sr elevator delfloor <name> <#> Remove floor by number
/sr elevator namefloor <name> <#> <display>  Rename a floor
```

### Wand Usage
- **Right-click** — add block as floor (prompts for floor name in chat, type "skip" for no name)
- **Left-click** — remove block from group

### How It Works
- All pads with the same group name connect
- Sneak on any pad to activate
- Single destination → instant teleport
- Multiple destinations → chest GUI menu (click floor item to teleport instantly)
- Full effects: particles, sound, resistance potion
- 1 second cooldown between teleports
- Floors spawn players on top of the block (not inside)

---

## Faction Bell System (v1.0.0)

Claim territory by placing faction bells.

### Crafting
```
8x Gold Block in a ring shape = 1 Faction Bell
(No vanilla bell recipe used)
```

### Faction Commands
```
/sr f create <name>             Create faction
/sr f invite <player>           Invite member
/sr f accept                   Accept invite
/sr f claim <radius>           Claim territory (Alpha only)
/sr f info                     View faction details
/sr f deposit <amount>         Add to faction hoard
/sr f withdraw <amount>        Take from hoard (Alpha only)
/sr f setspawn                 Set spawn point (Alpha only)
/sr f spawn                   Teleport to faction spawn
/sr f promote <player>         Promote member (Alpha only)
/sr f demote <player>         Demote member (Alpha only)
/sr f kick <player>           Kick member (Alpha only)
/sr f leave                   Leave faction
/sr f disband                 Delete faction (Alpha only)
```

### Territory Protection
- Bell holders and faction members can build
- Non-members cannot break, place, or interact in claimed territory
- Bells drop as items when broken (PDC data preserved)
- Admins with `shadowfang.admin` bypass protection

---

## InfoBoard Display System (v1.1.0)

Place floating text displays that show dynamic information.

### Commands
```
/sr board create <id>            Create board at your position
/sr board remove <id>            Delete a board
/sr board list                  List all boards
/sr board info <id>             Show board details
/sr board add <id> <program>    Add program to board
/sr board move <id>             Move board to your position
/sr board rotate <id> <deg>     Set rotation (0/90/180/270)
/sr board nudge <id> <dir> <#> Fine-tune position
```

### Programs
| Program | Shows |
|---------|-------|
| `players` | Online player count and names |
| `factions` | Faction leaderboard by hoard balance |
| `server` | TPS, RAM, uptime, version |
| `clock` | Real-world time |
| `motd` | Server MOTD |

Right-click a board to cycle programs. Sneak-right-click for board info.

---

## Verse Cross-World Travel (v1.0.0)

```
/sr verse worlds               List all worlds
/sr travel <world>            Teleport to world
/sr warp <name>               Teleport to saved warp
/sr sethome                   Set your home
/sr home                      Teleport to home
/sr spawn                     Teleport to world spawn
/sr setspawn                  Set world spawn (admin)
```

---

## RosettaStone Compatibility Layer

Makes non-Folia plugins run on Folia's region-threaded architecture.

**Components:**
- `FoliaCompat` — Scheduler routing (Bukkit → GlobalRegionScheduler)
- `RegionDispatcher` — Chunk-scoped task execution
- `FoliaPlayer` — Thread-safe teleportation
- `ThreadSafeCollections` — Concurrent data structures

**Used by:** Axiom and Worlds plugins.

---

## Credits

- **Axiom (Moulberry)** — World editing plugin, patched for Folia 26.1.2
- **Worlds (Minecraft Worlds Plugin)** — Multi-world management via RosettaStone
- **PaperMC / Folia** — Server platform

Contact FreakyHydra to remove any ported plugin.

---

*The pack that howls together, hunts together.*
