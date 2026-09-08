# Damage multiplier * 100 into ml_dm. Mirrors the mod: 2% per level above 20,
# and below 20 it follows the same 0.5x -> 1.0x ramp the other stats use.
execute if score @s ml_level matches ..19 run scoreboard players operation @s ml_dm = @s ml_m
execute if score @s ml_level matches 20.. run scoreboard players operation @s ml_dm = @s ml_level
execute if score @s ml_level matches 20.. run scoreboard players remove @s ml_dm 20
execute if score @s ml_level matches 20.. run scoreboard players operation @s ml_dm *= #2 ml_m
execute if score @s ml_level matches 20.. run scoreboard players add @s ml_dm 100
