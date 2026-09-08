# Pick a color by level, then macro-set the CustomName
data modify storage moblevel:v color set value "green"
execute if score @s ml_level matches 50.. run data modify storage moblevel:v color set value "aqua"
execute if score @s ml_level matches 100.. run data modify storage moblevel:v color set value "yellow"
execute if score @s ml_level matches 130.. run data modify storage moblevel:v color set value "red"
execute if score @s ml_level matches 150.. run data modify storage moblevel:v color set value "dark_purple"
execute store result storage moblevel:v lvl int 1 run scoreboard players get @s ml_level
function moblevel:set_name_macro with storage moblevel:v
