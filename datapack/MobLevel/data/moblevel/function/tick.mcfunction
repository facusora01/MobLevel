# Assign a level to any newly-spawned leveled mob
execute as @e[type=#moblevel:leveled,tag=!ml_leveled] run function moblevel:new_mob

# Per-tick behaviours for level-150 mobs
scoreboard players remove @e[tag=ml_aggro] ml_cd 1
execute as @e[tag=ml_aggro] at @s if entity @a[distance=..1.6] if score @s ml_cd matches ..0 run function moblevel:contact_damage

# Sun immunity + particles run once per second (throttled by ml_cd not needed; light enough)
execute as @e[tag=ml_sun] run effect give @s minecraft:fire_resistance 2 0 true
execute as @e[tag=ml_aggro] at @s run particle minecraft:dust{color:[0.6,0.0,1.0],scale:0.7} ~ ~1 ~ 0.3 0.5 0.3 0.02 4
