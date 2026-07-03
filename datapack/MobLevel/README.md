# MobLevel — Datapack (vanilla server compatible)

A data-driven port of the MobLevel mod. Works on vanilla servers/clients (1.21+),
no Forge required. Because datapacks cannot touch entity AI or add attributes to
passive mobs, some mod features are approximated — see Limitations.

## Install
Drop the `MobLevel` folder into `<world>/datapacks/` and run `/reload`
(or `/datapack enable`).

## What it does
- **Levels** — every mob in `#moblevel:leveled` gets a level on spawn.
  95% land in 1-20; 5% roll the high band (21-150, higher tiers rarer).
- **Health scaling** — level 20 = vanilla. Level 1 = 0.5x, level 150 = 7.5x.
- **Colored name** — `[LvN]` colored by tier, shown when you look at the mob.
- **Level 150 perks** — purple particles, sun immunity (zombies/skeletons),
  and contact damage to nearby players.
- **Breeding** — a baby inherits the average level of the two nearest adults,
  with a ~1% mutation that adds +3..8 (shiny-style climb).
- **Kill reward** — killing a leveled mob grants the player bonus XP.

## Limitations (vs the Forge mod)
- **No real aggression** — datapacks can't add AI goals. "Aggression" is
  contact damage: a level-150 mob hurts a player who stands within ~1.6 blocks.
  It does not chase.
- **No level-scaled loot/XP on death** — the dead mob's level can't be read in
  the kill trigger, so the reward is flat.
- **Passive mobs deal no melee** — handled via the contact-damage script instead.
- **Breeding parents** are inferred by proximity (nearest two leveled adults).
  Reliable for a normal pair; a crowded pen can pick the wrong parent.

## Versions
Targets 1.21.2+ attribute names (`minecraft:max_health`, etc.) and uses
`/random`, macros and modern particle SNBT. `supported_formats` is left open so
newer releases keep loading it; bump `pack_format` if a version refuses to load.
