# Level 150 perks: contact damage, sun immunity, 1.5x movement speed
tag @s add ml_aggro
scoreboard players set @s ml_cd 0
execute if entity @s[type=#moblevel:burns_in_sun] run tag @s add ml_sun

# Speed applied once (guarded so re-apply can't stack)
execute if entity @s[tag=ml_spd] run return 0
tag @s add ml_spd
scoreboard players operation @s ml_hp = @s ml_basespd
scoreboard players operation @s ml_hp *= #3 ml_m
scoreboard players operation @s ml_hp /= #2 ml_m
execute store result storage moblevel:v spd double 0.001 run scoreboard players get @s ml_hp
function moblevel:set_speed with storage moblevel:v
