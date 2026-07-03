# High band, tiered so higher levels stay rare
execute store result score @s ml_rand run random value 1..100
execute if score @s ml_rand matches 1..70 run execute store result score @s ml_level run random value 21..50
execute if score @s ml_rand matches 71..95 run execute store result score @s ml_level run random value 51..100
execute if score @s ml_rand matches 96..100 run execute store result score @s ml_level run random value 101..150
