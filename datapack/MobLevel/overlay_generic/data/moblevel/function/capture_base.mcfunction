# Store vanilla attribute baselines (health as int, speed in 1/1000ths)
tag @s add ml_baseset
execute store result score @s ml_basehp run attribute @s minecraft:generic.max_health base get 1
execute store result score @s ml_basespd run attribute @s minecraft:generic.movement_speed base get 1000
