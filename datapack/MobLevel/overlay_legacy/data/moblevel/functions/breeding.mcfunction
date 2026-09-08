# Baby (@s): inherit the average level of the two nearest leveled adults.
# Only same species breed, so the two nearest leveled adults are the parents.
# Selectors use "positioned as @s" because tick functions run at world origin.
tag @s add ml_baby
scoreboard players set @s ml_a 1
scoreboard players set @s ml_b 1

# Parent A = nearest leveled adult
execute positioned as @s as @e[tag=ml_leveled,tag=!ml_baby,sort=nearest,limit=1,distance=0.1..8] run tag @s add ml_p1
execute store result score @s ml_a run scoreboard players get @e[tag=ml_p1,limit=1] ml_level

# Parent B = next nearest (exclude A)
execute positioned as @s store result score @s ml_b run scoreboard players get @e[tag=ml_leveled,tag=!ml_p1,tag=!ml_baby,sort=nearest,limit=1,distance=0.1..8] ml_level
tag @e[tag=ml_p1] remove ml_p1

# Average
scoreboard players operation @s ml_level = @s ml_a
scoreboard players operation @s ml_level += @s ml_b
scoreboard players operation @s ml_level /= #2 ml_m

# Rare mutation (~1%): +3..8
execute store result score @s ml_rand run random value 1..100
execute if score @s ml_rand matches 1..1 run function moblevel:mutate

# Clamp 1..150
execute if score @s ml_level matches ..0 run scoreboard players set @s ml_level 1
execute if score @s ml_level matches 151.. run scoreboard players set @s ml_level 150
