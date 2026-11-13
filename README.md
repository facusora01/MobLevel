
Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

Additional Resources: 
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/

Configuration:
==========
Config file: `config/moblevel.properties`.
Changes only affect mobs spawned afterwards. Defaults keep the original behavior.

Scaling (level `vanillaLevel` = vanilla stats):
- `vanillaLevel` (20), `lowLevelStatMultiplier` (0.5), `healthPerLevel` (0.05), `damagePerLevel` (0.02)
- `lowLevelDropChance` (0.25), `lootPerLevel` (0.02), `maxLevelLootBonus` (3.0)
- `xpPerLevel` (0.01), `maxLevelXpBonus` (2.0)

Armor per level (off by default):
- `armorEnabled` (false), `armorPerLevel` (0.1), `armorMax` (20.0)

Level by distance from world spawn (off by default, not applied to bred offspring):
- `distanceScalingEnabled` (false), `blocksPerLevel` (100), `distanceMaxBonus` (50)
