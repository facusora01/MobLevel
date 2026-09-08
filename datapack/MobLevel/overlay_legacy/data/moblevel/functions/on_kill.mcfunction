# Runs as the killing player. Flat bonus reward.
# NOTE: datapacks can't read the dead mob's level here, so this reward is flat,
# not level-scaled (limitation vs the Forge mod).
advancement revoke @s only moblevel:kill_leveled
xp add @s 5 points
