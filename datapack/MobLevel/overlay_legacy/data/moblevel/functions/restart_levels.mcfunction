# /function moblevel:restart_levels
# One-time per world (persistent marker). Re-rolls loaded pre-update mobs now,
# and moblevel:sweep keeps migrating the rest as their chunks load.
execute store success score #ok ml_m run scoreboard players add #probe ml_migrate 0
execute if score #ok ml_m matches 1 run tellraw @s {"text":"[MobLevel] restart_levels was already used in this world. Old mobs keep migrating as their chunks load.","color":"red"}
execute if score #ok ml_m matches 1 run return 0

scoreboard objectives add ml_migrate dummy
execute as @e[tag=ml_leveled,tag=!ml2] run function moblevel:migrate
tellraw @s [{"text":"[MobLevel] Migration enabled. Re-rolled loaded pre-update mobs. ","color":"green"},{"text":"Re-rolls never raise a level; unloaded mobs migrate as their chunks load.","color":"gray"}]
