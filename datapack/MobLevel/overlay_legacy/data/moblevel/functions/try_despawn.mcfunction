# Honor real persistence; kill only mobs whose sole anchor is our level label
execute if data entity @s {PersistenceRequired:1b} run return 0
kill @s
