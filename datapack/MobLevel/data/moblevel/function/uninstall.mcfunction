# /function moblevel:uninstall
# Strips MobLevel data from every loaded mob and disables the pack's tick work.
# After running this, visit your remaining areas, then remove the datapack.
execute as @e[tag=ml_leveled] run function moblevel:strip
scoreboard objectives add ml_off dummy
tellraw @a [{"text":"[MobLevel] Uninstall: cleaned all loaded mobs and stopped. ","color":"green"},{"text":"Visit remaining areas and re-run this function there, then remove the datapack. Objectives can be purged with /function moblevel:purge","color":"gray"}]
