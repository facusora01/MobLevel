# level >= 20: mult*100 = 100 + (level-20)*5
scoreboard players operation @s ml_m = @s ml_level
scoreboard players remove @s ml_m 20
scoreboard players operation @s ml_m *= #5 ml_m
scoreboard players add @s ml_m 100
