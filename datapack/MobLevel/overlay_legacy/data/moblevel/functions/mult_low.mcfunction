# level 1-19: mult*100 = 50 + (level-1)*50/19  (0.5x at lvl1 -> 1.0x at lvl20)
scoreboard players operation @s ml_m = @s ml_level
scoreboard players remove @s ml_m 1
scoreboard players operation @s ml_m *= #50 ml_m
scoreboard players operation @s ml_m /= #19 ml_m
scoreboard players add @s ml_m 50
