# One raycast step (as the scoping player, positioned along the ray).
execute if entity @e[tag=ml_leveled,distance=..1.0,limit=1] run function moblevel:scan_hit

scoreboard players remove @s ml_rand 1
execute if score @s ml_rand matches ..0 run return 0

# Walls stop the ray: only advance through scannable blocks (air/water)
execute unless block ~ ~ ~ #moblevel:scan_through run return 0
execute positioned ^ ^ ^0.5 run function moblevel:scan_step
