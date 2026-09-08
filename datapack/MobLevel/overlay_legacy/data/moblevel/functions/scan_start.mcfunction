# Fired every tick while a player uses a spyglass (as the player).
advancement revoke @s only moblevel:spyglass_scan

# Chat cooldown so a hit doesn't spam every tick
scoreboard players remove @s ml_cd 1
execute if score @s ml_cd matches 1.. run return 0

# Raycast: 200 steps x 0.5 = 100 blocks, from the eyes, blocked by walls
scoreboard players set @s ml_rand 200
execute at @s anchored eyes positioned ^ ^ ^0.2 run function moblevel:scan_step
