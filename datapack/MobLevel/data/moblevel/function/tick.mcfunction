# Kill switch: after moblevel:uninstall ran, do nothing until the pack is removed
execute store success score #off ml_m run scoreboard players add #probe ml_off 0
execute if score #off ml_m matches 1 run return 0

# Assign a level to any newly-spawned leveled mob
execute as @e[type=#moblevel:leveled,tag=!ml_leveled] run function moblevel:new_mob

# Per-tick behaviours for level-150 mobs
scoreboard players remove @e[tag=ml_aggro] ml_cd 1
execute as @e[tag=ml_aggro] at @s if entity @a[distance=..1.6] if score @s ml_cd matches ..0 run function moblevel:contact_damage

execute as @e[tag=ml_sun] run effect give @s minecraft:fire_resistance 2 0 true
execute as @e[tag=ml_aggro] at @s run particle minecraft:dust{color:[0.6,0.0,1.0],scale:0.7} ~ ~1 ~ 0.3 0.5 0.3 0.02 4

# Every 10s: despawn far hostiles + migrate pre-update mobs (if enabled)
scoreboard players add #t ml_timer 1
execute if score #t ml_timer matches 200.. run function moblevel:sweep
