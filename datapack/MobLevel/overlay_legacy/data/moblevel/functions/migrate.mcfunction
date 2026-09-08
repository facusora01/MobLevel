# One-time re-roll of a pre-update mob (as the mob). Downgrade-only: the new
# level is capped at the old one, so migration can never be farmed upward.
scoreboard players operation @s ml_a = @s ml_level

# Pre-update mobs never captured baselines; reconstruct vanilla base from the
# scaled value so re-applying does not compound.
execute unless entity @s[tag=ml_baseset] run function moblevel:reconstruct_base

function moblevel:roll_level
execute if entity @s[type=#moblevel:boss] run function moblevel:boss_clamp
execute if score @s ml_level > @s ml_a run scoreboard players operation @s ml_level = @s ml_a

tag @s add ml2
function moblevel:apply
