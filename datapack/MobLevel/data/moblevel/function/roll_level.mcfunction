# Natural spawn: 93.5% common band (1-20), 6.5% high band (21-150, higher rarer)
execute store result score @s ml_rand run random value 1..1000
execute if score @s ml_rand matches 1..935 run execute store result score @s ml_level run random value 1..20
execute if score @s ml_rand matches 936..1000 run function moblevel:roll_high
