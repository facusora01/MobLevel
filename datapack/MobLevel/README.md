# MobLevel — datapack edition

Every mob gets a level from 1 to 150 that scales its health, its damage and how
dangerous it is. No mod loader needed.

One download covers **Minecraft 1.20.2 through 26.2**. The pack carries version
overlays and picks the right files for whatever server it lands on.

## Install

Drop this zip (or the unpacked folder) into `<world>/datapacks/` and run `/reload`.

To remove it cleanly, run `/function moblevel:uninstall`, let your world load once so
every chunk gets cleaned, then delete it.

## What it does

- **Levels** — every mob in `#moblevel:leveled` rolls a level on spawn. Passives are
  far rarer at the top than hostiles: above level 100 is 1 in 840 for a passive, 1 in
  281 for a hostile. Animals never despawn, so a generous curve would pile them up
  forever; hostiles get replaced constantly, so theirs can afford to be looser.
- **Health scaling** — level 20 is vanilla, level 1 is 0.5x, level 150 is 7.5x.
  Baselines are captured per mob, so re-applying can never compound.
- **Damage scaling** — 2% per level above 20, so a level 150 hits for 3.6x. Mobs that
  have no attack damage attribute at all, like cows, are left alone.
- **Breeding** — a calf inherits the average of its parents, rounded down, with a rare
  mutation that pushes it above them.
- **Colored `[LvN]` label**, shown when you look at the mob.
- **Level 150 perks** — purple particles, sun immunity, 1.5x speed, and contact damage
  to nearby players for hostiles.
- **Spyglass scanner** — scope a leveled mob within 100 blocks and its level is
  whispered to you in chat.

## What the mod does and this cannot

| | mod | datapack | why |
|---|---|---|---|
| Levels, health, damage, breeding | full | full | — |
| **Totem death-save** | yes | **missing** | a datapack cannot cancel a death |
| **Creeper blast radius** | scales with level | **untouched** | — |
| **Kill reward** | drops scale with the level | **flat 5 XP** | the kill trigger cannot read the dead mob's level |
| Level-150 aggression | real AI, passives hunt you down | hostiles only, as contact damage | goals cannot be added to a mob |
| Level label | drawn client-side, entity untouched | written into `CustomName` | no custom rendering |

## Two things to know before installing

**The pack removes mobs to keep them from piling up.** Writing the level into
`CustomName` is what vanilla reads as name-tag persistence, so leveled hostiles would
never despawn. Every 10 seconds the pack removes far-away mobs whose only anchor is its
own label. Mobs you named yourself are left alone, but that check is the only thing
protecting them.

**Never run this together with the MobLevel mod.** Both assign levels independently, so
every mob ends up with two different ones.

## Commands

- `/function moblevel:restart_levels` — one-time re-roll of pre-update mobs
- `/function moblevel:uninstall` — strip every trace and stop working
