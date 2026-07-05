# Ray found a leveled mob: whisper its level tag to the scoping player only.
scoreboard players set @s ml_cd 60
execute as @e[tag=ml_leveled,distance=..1.0,limit=1,sort=nearest] run tag @s add ml_scan
tellraw @s [{"text":"[MobLevel] ","color":"dark_purple"},{"text":"Target: ","color":"gray"},{"selector":"@e[tag=ml_scan,limit=1]"}]
tag @e[tag=ml_scan] remove ml_scan

# End the ray
scoreboard players set @s ml_rand 0
