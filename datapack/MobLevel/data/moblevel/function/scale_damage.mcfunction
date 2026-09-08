# Scaled attack damage = vanilla base * dmg mult / 100, kept in 1/10ths.
function moblevel:dmg_mult
scoreboard players operation @s ml_dmg = @s ml_basedmg
scoreboard players operation @s ml_dmg *= @s ml_dm
scoreboard players operation @s ml_dmg /= #100 ml_m
execute if score @s ml_dmg matches ..0 run scoreboard players set @s ml_dmg 1
execute store result storage moblevel:v dmg double 0.1 run scoreboard players get @s ml_dmg
function moblevel:set_damage with storage moblevel:v
