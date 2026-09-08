# Vanilla attack damage in 1/10ths. Captured under its own tag so mobs that were
# already leveled by an earlier version get it too: their damage was never scaled,
# so whatever they carry now IS the vanilla base.
# Mobs without the attribute (cows, sheep) leave the score at 0 and are skipped.
tag @s add ml_dmgset
scoreboard players set @s ml_basedmg 0
execute store result score @s ml_basedmg run attribute @s minecraft:generic.attack_damage base get 10
