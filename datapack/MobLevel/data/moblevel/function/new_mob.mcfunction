# Runs once per mob (as the mob). Marks it and assigns a level.
tag @s add ml_leveled

# Babies have a negative Age; adults/monsters default to 0 (no Age tag)
scoreboard players set @s ml_age 0
execute store result score @s ml_age run data get entity @s Age 1

# Baby -> inherit from parents; otherwise -> natural spawn roll
execute if score @s ml_age matches ..-1 run function moblevel:breeding
execute if score @s ml_age matches 0.. run function moblevel:roll_level

# Bosses are clamped to a sane range (mirror of the mod's 20-80)
execute if entity @s[type=#moblevel:boss] run function moblevel:boss_clamp

# Version marker: mobs leveled by this version are never migrated
tag @s add ml2

function moblevel:apply
