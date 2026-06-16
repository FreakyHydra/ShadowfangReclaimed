# Shadowfang Core — Changelog

## /sr Command System & Path Tools Plugin
**Version:** 1.1.0
**Date:** June 17, 2026

---

### New Feature: Unified `/sr` Command Root

All plugin commands are now accessible through a single root: `/sr <sub-plugin> <subcommand>`.

| Sub-plugin | Keys | Purpose |
|---|---|---|
| Faction | `f`, `faction` | Faction management |
| Economy | `e`, `economy` | Silver Coins |
| Bounty | `b`, `bounty` | Bounty quests |
| Lore | `l`, `lore` | Lore archive |
| Road / Path | `r`, `we`, `road`, `worldedit`, `path`, `pave` | Path building (requires `shadowfang.we.use`) |
| Verse | `v`, `verse`, `sign`, `list`, `worlds` | Cross-world travel |
| InfoBoard | `i`, `infoboard`, `board` | Info board management (requires `shadowfang.admin`) |

**Quick shortcuts:**
| Shortcut | Effect |
|---|---|
| `/sr h` or `/sr hub` | Teleport to Hub |
| `/sr s` or `/sr spawn` | Teleport to spawn |
| `/sr w <world>` or `/sr warp <world>` | Warp to a world |
| `/sr t <world>` or `/sr travel <world>` | Travel to a world |

**Safety:** World-changing commands (road/path, infoboard) are permission-gated. Players without `shadowfang.we.use` or `shadowfang.admin` won't see those sub-plugins in tab completion or the help listing.

A full sorted command reference is available on the web dashboard at `http://localhost:56552` under the **COMMANDS** tab.

---

### New Feature: Path Tools (WorldEdit-style)

A Folia-safe, admin-grade path building tool. Works like a minimal WorldEdit clone.

**Commands (via `/sr r`, `/sr we`, `/sr road`, etc.):**

| Command | Description |
|---|---|
| `wand` | Get the path-selection wand (Wooden Shovel, custom model data 2001) |
| `pos1` / `pos2` | Set selection corners (or left-click / right-click with wand) |
| `copy` | Capture the 3D pattern from the pos1→pos2 cuboid. Your player position is the anchor. |
| `paste` | Paste the pattern 2 blocks in front of you, rotated to match your facing |
| `start` | Toggle walk-paste mode ON. Walk with the wand to stamp the pattern continuously. |
| `stop` | Toggle walk-paste mode OFF |
| `undo` | Revert your last paste action (history capped at 20) |
| `clear` | Clear your selection and clipboard |

**Key mechanics:**
- **Template-stamping:** Build a road segment by hand → select it (`pos1`/`pos2`) → `copy` → walk or `paste` to tile it.
- **Rotation-aware:** The pattern rotates to match your walking direction. Turn while walking and the road follows you.
- **Air offsets honored:** Air blocks in the template carve through terrain, so roads tunnel through mountains cleanly.
- **Snapshots for undo:** Blocks are snapshotted before writing. Undo restores them per chunk on the correct region thread.
- **Folia-safe:** All block I/O is dispatched to `RegionScheduler.execute()` — no cross-thread block writes. Pattern writes are batched by chunk.

**Permissions:**
- `shadowfang.we.use` — required for all path commands (default: op)
- `shadowfang.we.admin` — reserved for future admin ops

---

## Faction Bell Claiming System
**Version:** 1.0.0
**Date:** June 15, 2026

---

### New Feature: Faction Bells & Territory Claiming

A completely new way to establish your pack's territory in the wilderness.

**How it works:**
- Place a **Bell** block anywhere in the world to found your faction and claim a **5x5 chunk territory** (25 chunks) around it
- If you have no faction, one is automatically created for you
- If you are the **Alpha** of an existing faction, placing a Bell repositions your territory

**Territory Rules:**
- A **10-chunk buffer zone** must exist between faction claims — no two packs can claim adjacent land
- If your placement is rejected, **particle trails** will guide you to the nearest valid claim location
- Only the **Alpha** can reposition the faction Bell

---

### New Commands

| Command | Description |
|---------|-------------|
| `/f setspawn` | Set your faction's spawn point to your current location (Alpha only) |
| `/f spawn` | Teleport to your faction's spawn point |
| `/f disband` | Dissolve your faction and unclaim all territory (Alpha only) |

---

### Protections

- Faction territory now blocks **block breaking**, **block placement**, and **interactions** from non-members
- Faction Bells specifically show a message: *"This Faction Bell belongs to [faction name]"*
- Admins (`shadowfang.admin`) bypass all protections

---

### Technical Notes

- All faction data persists to `config/shadowfang-core/factions.json` and `faction_chunks.json`
- Bell and spawn locations are serialized with the faction data
- Compatible with Folia's multi-threaded architecture — uses `teleportAsync()` for safe teleportation
- Particle effect uses `TRIAL_SPAWNER_DETECTION` for the guidance trail

---

*The pack that howls together, hunts together.*
