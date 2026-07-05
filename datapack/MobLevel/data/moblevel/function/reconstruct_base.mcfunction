# vanilla base = current scaled base * 100 / mult(old level)
tag @s add ml_baseset
execute if score @s ml_level matches 20.. run function moblevel:mult_high
execute if score @s ml_level matches ..19 run function moblevel:mult_low
execute store result score @s ml_basehp run attribute @s minecraft:max_health base get 1
scoreboard players operation @s ml_basehp *= #100 ml_m
scoreboard players operation @s ml_basehp /= @s ml_m
execute store result score @s ml_basespd run attribute @s minecraft:movement_speed base get 1000
