# Objectives
scoreboard objectives add ml_level dummy
scoreboard objectives add ml_rand dummy
scoreboard objectives add ml_age dummy
scoreboard objectives add ml_m dummy
scoreboard objectives add ml_hp dummy
scoreboard objectives add ml_a dummy
scoreboard objectives add ml_b dummy
scoreboard objectives add ml_cd dummy
scoreboard objectives add ml_dmg dummy

# Constants
scoreboard players set #100 ml_m 100
scoreboard players set #50 ml_m 50
scoreboard players set #19 ml_m 19
scoreboard players set #5 ml_m 5
scoreboard players set #2 ml_m 2
scoreboard players set #25 ml_m 25

tellraw @a {"text":"[MobLevel] datapack loaded","color":"dark_purple"}
