# MobLevel — datapack edition

Every mob gets a level from 1 to 150 that scales its health, its drops and how
dangerous it is. No mod loader needed: drop the folder into `world/datapacks`
and run `/reload`.

One download covers **Minecraft 1.20.2 through 26.2**. The pack carries version
overlays and picks the right one for the server it lands on.

## What this does not do

The datapack is a reduced version of the MobLevel mod. Some of what the mod does
is simply not reachable from mcfunctions, so the pack either skips it or
approximates it. If you want the full thing, use the mod.

| | mod | datapack | why |
|---|---|---|---|
| Level, health scaling, breeding | full | full | — |
| **Totem death-save** | yes | **missing** | a datapack cannot cancel a death |
| **Creeper blast radius** | scales with level | **untouched** | — |
| **Kill reward** | drops scale with the mob's level | **flat 5 XP** | the kill trigger cannot read the dead mob's level |
| Level-150 aggression | real AI: passives hunt you down | hostiles only, as contact damage | goals cannot be added to a mob |
| Level label | drawn client-side, entity untouched | written into the mob's `CustomName` | no custom rendering |
| Spyglass scanner | label shown up to 100 blocks | level whispered in chat | — |

## Two consequences worth knowing before you install

**The pack kills mobs to keep them from piling up.** Writing the level into
`CustomName` is what vanilla treats as name-tag persistence, so leveled hostiles
would never despawn. Every 10 seconds the pack removes far-away mobs whose only
anchor is its own label. Mobs you named yourself, or that are persistent for any
real reason, are left alone — but that check is the only thing protecting them.

**Never run the datapack and the mod together.** Both assign levels, so every mob
ends up with two different ones: the mod's tags say one number and the pack's
label says another. Pick one.

## Commands

- `/function moblevel:restart_levels` — one-time re-roll of pre-update mobs
- `/function moblevel:uninstall` — strip every trace and stop working, so the pack
  can be removed cleanly. Let your world load once with this on, then delete it.
