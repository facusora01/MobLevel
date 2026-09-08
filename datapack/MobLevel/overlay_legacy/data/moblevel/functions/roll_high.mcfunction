# High band, tiered to mirror the mod's exponent-1.5 curve (validated by simulation):
# of mobs above 20 -> 37% land 21-49, 35% 50-99, 17% 100-129, 10.5% 130-149, 0.5% exactly 150
execute store result score @s ml_rand run random value 1..1000
execute if score @s ml_rand matches 1..370 run execute store result score @s ml_level run random value 21..49
execute if score @s ml_rand matches 371..720 run execute store result score @s ml_level run random value 50..99
execute if score @s ml_rand matches 721..890 run execute store result score @s ml_level run random value 100..129
execute if score @s ml_rand matches 891..995 run execute store result score @s ml_level run random value 130..149
execute if score @s ml_rand matches 996..1000 run scoreboard players set @s ml_level 150
