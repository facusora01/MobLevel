# Runs as a level-150 mob with a player within 1.6 blocks and cooldown ready.
# Contact damage stands in for real melee AI (datapacks can't add attack goals).
scoreboard players operation @s ml_dmg = @s ml_level
scoreboard players operation @s ml_dmg /= #25 ml_m
scoreboard players add @s ml_dmg 2
execute store result storage moblevel:v dmg int 1 run scoreboard players get @s ml_dmg
function moblevel:do_damage with storage moblevel:v
scoreboard players set @s ml_cd 20
