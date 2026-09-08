# Shiny-style mutation: add 3..8 levels
execute store result score @s ml_rand run random value 3..8
scoreboard players operation @s ml_level += @s ml_rand
