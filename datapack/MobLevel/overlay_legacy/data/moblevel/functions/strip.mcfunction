# Revert one mob (as the mob) to vanilla
data remove entity @s CustomName

# Restore vanilla health/speed from captured baselines
execute if entity @s[tag=ml_baseset] store result storage moblevel:v hp int 1 run scoreboard players get @s ml_basehp
execute if entity @s[tag=ml_baseset] run function moblevel:set_health with storage moblevel:v
execute if entity @s[tag=ml_spd] store result storage moblevel:v spd double 0.001 run scoreboard players get @s ml_basespd
execute if entity @s[tag=ml_spd] run function moblevel:set_speed with storage moblevel:v

scoreboard players reset @s
tag @s remove ml_leveled
tag @s remove ml2
tag @s remove ml_apex
tag @s remove ml_aggro
tag @s remove ml_sun
tag @s remove ml_spd
tag @s remove ml_baby
tag @s remove ml_baseset
