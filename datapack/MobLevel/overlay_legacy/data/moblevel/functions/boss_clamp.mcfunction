# Boss level limits (mirror of the mod defaults: 20-80)
execute if score @s ml_level matches ..19 run scoreboard players set @s ml_level 20
execute if score @s ml_level matches 81.. run scoreboard players set @s ml_level 80
