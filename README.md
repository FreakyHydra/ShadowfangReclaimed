# Shadowfang Reclaimed

A custom Folia 1.21 server plugin featuring factions, economy, bounties, lore, and cross-world travel.

## Credits & Thanks

- **Axiom (Moulberry)** — Ported version of the Axiom Paper Plugin for world editing.  
  https://github.com/Moulberry/AxiomPaperPlugin  
  *Ported to Folia because the original uses threads and scheduling not compatible with Folia's region-threaded architecture.*

- **Worlds (Minecraft Worlds Plugin)** — Ported version used for multi-world management.  
  https://www.spigotmc.org/resources/worlds.64947/  
  *Ported to Folia because the original uses Bukkit schedulers and world-loading APIs that conflict with Folia's thread-per-region model.*

- **PaperMC / Folia** — The server platform this plugin runs on.  
  https://papermc.io/software/folia

If you are the creator of **Axiom** or **Worlds** and wish for your ported plugin to be removed from this distribution, please contact FreakyHydra and it will be taken down immediately.

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
