# Shadowfang Core — Changelog

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
