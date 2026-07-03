# Apply stats/visuals for the level currently in @s ml_level.

# Stat multiplier * 100 into ml_m
execute if score @s ml_level matches 20.. run function moblevel:mult_high
execute if score @s ml_level matches ..19 run function moblevel:mult_low

# Scaled health = base max_health * mult / 100
execute store result score @s ml_hp run attribute @s minecraft:max_health base get 1
scoreboard players operation @s ml_hp *= @s ml_m
scoreboard players operation @s ml_hp /= #100 ml_m
execute if score @s ml_hp matches ..0 run scoreboard players set @s ml_hp 1
execute store result storage moblevel:v hp int 1 run scoreboard players get @s ml_hp
function moblevel:set_health with storage moblevel:v

# Colored name (CustomNameVisible left false -> shows only when looked at)
function moblevel:set_name

# Level 150 perks
execute if score @s ml_level matches 150.. run tag @s add ml_aggro
execute if score @s ml_level matches 150.. run scoreboard players set @s ml_cd 0
execute if score @s ml_level matches 150.. if entity @s[type=#moblevel:burns_in_sun] run tag @s add ml_sun
