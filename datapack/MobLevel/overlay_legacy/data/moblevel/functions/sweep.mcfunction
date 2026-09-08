# Runs every 200 ticks (10s) from tick.mcfunction
scoreboard players set #t ml_timer 0

# Our label makes hostiles persistent forever; re-create vanilla far-despawn.
# PersistenceRequired (e.g. /summon) is honored inside try_despawn.
execute as @e[tag=ml_leveled,type=#moblevel:hostile] at @s unless entity @a[distance=..128] run function moblevel:try_despawn

# One-time migration (enabled by moblevel:restart_levels): re-roll pre-update mobs
execute store success score #mig ml_m run scoreboard players add #probe ml_migrate 0
execute if score #mig ml_m matches 1 as @e[tag=ml_leveled,tag=!ml2] run function moblevel:migrate
