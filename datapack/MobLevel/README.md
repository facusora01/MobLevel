# MobLevel — Datapack (vanilla server compatible)

A data-driven port of the MobLevel mod. Works on vanilla servers/clients (1.21+),
no Forge required. Because datapacks cannot touch entity AI or client rendering,
some mod features are approximated — see Limitations.

## Install
Drop the `MobLevel` folder (or the zip) into `<world>/datapacks/` and run
`/reload` (or `/datapack enable`).

## What it does
- **Levels** — every mob in `#moblevel:leveled` gets a level on spawn.
  93.5% land in 1-20; 6.5% roll the high band, tiered to mirror the mod's
  rarity curve (a level 150 is roughly 1 in 3000 mobs).
- **Health scaling** — level 20 = vanilla. Level 1 = 0.5x, level 150 = 7.5x.
  Baselines are captured per mob, so re-applying can never compound.
- **Colored name** — `[LvN]` colored by tier, shown when you look at the mob.
- **Level 150 perks** — purple particles, sun immunity (zombies/skeletons),
  1.5x movement speed, and contact damage to nearby players.
- **Boss limits** — Ender Dragon and Wither are clamped to levels 20-80.
- **Breeding** — a baby inherits the average level of the two nearest adults,
  with a ~1% mutation that adds +3..8 (shiny-style climb).
- **Hostile despawn** — the level label would normally make hostiles persistent
  forever; a 10s sweep re-creates the vanilla far-despawn (PersistenceRequired
  is honored).
- **Kill reward** — killing a leveled mob grants the player bonus XP.

## Admin functions
| Function | What it does |
|---|---|
| `/function moblevel:restart_levels` | One-time per world: re-rolls pre-update mobs with the current rates (never upward); unloaded mobs keep migrating as chunks load |
| `/function moblevel:uninstall` | Strips MobLevel data from loaded mobs, stops the pack. Re-run in other areas, then remove the datapack |
| `/function moblevel:purge` | Removes all MobLevel scoreboard objectives (after uninstall) |

## Limitations (vs the Forge mod)
- **No real aggression** — datapacks can't add AI goals. "Aggression" is
  contact damage: a level-150 mob hurts a player who stands within ~1.6 blocks.
  It does not chase.
- **No spyglass level scanner** — nameplate rendering is client-side.
- **No level-scaled loot/XP on death** — the dead mob's level can't be read in
  the kill trigger, so the reward is flat.
- **Passive mobs deal no melee** — handled via the contact-damage script instead.
- **Breeding parents** are inferred by proximity (nearest two leveled adults).
  Reliable for a normal pair; a crowded pen can pick the wrong parent.
- **Name-tagged mobs**: the pack can't detect a player renaming a mob, so the
  despawn sweep may remove far-away renamed hostiles unless they also have
  PersistenceRequired.

## Versions
Targets 1.21.2+ attribute names (`minecraft:max_health`, etc.) and uses
`/random`, macros, `return`, and modern particle SNBT. `supported_formats` is
left open so newer releases keep loading it; bump `pack_format` if a version
refuses to load.
