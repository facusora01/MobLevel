# Apply stats/visuals for the level currently in @s ml_level.

# Capture vanilla baselines once per mob: scaling always recomputes from these,
# so re-applying (migration) can never compound the multiplier.
execute unless entity @s[tag=ml_baseset] run function moblevel:capture_base

# Stat multiplier * 100 into ml_m
execute if score @s ml_level matches 20.. run function moblevel:mult_high
execute if score @s ml_level matches ..19 run function moblevel:mult_low

# Scaled health = vanilla base * mult / 100
scoreboard players operation @s ml_hp = @s ml_basehp
scoreboard players operation @s ml_hp *= @s ml_m
scoreboard players operation @s ml_hp /= #100 ml_m
execute if score @s ml_hp matches ..0 run scoreboard players set @s ml_hp 1
execute store result storage moblevel:v hp int 1 run scoreboard players get @s ml_hp
function moblevel:set_health with storage moblevel:v

# Colored name (CustomNameVisible left false -> shows only when looked at)
function moblevel:set_name

# Level 150 perks (reset first so migration downgrades lose them)
tag @s remove ml_apex
tag @s remove ml_aggro
tag @s remove ml_sun
execute if score @s ml_level matches 150.. run function moblevel:apex
