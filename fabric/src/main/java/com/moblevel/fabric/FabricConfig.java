package com.moblevel.fabric;

import com.moblevel.Config;
import com.moblevel.MobLevel;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

// config/moblevel.properties: same settings, defaults and ranges as NeoForge's
// moblevel-common.toml, without needing a config library. Read once at startup.
final class FabricConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("moblevel.properties");

    private FabricConfig() {
    }

    static void load() {
        Properties p = new Properties();
        if (Files.exists(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE)) {
                p.load(reader);
            } catch (IOException | IllegalArgumentException e) {
                MobLevel.LOGGER.error("Could not read {}, using defaults", FILE, e);
            }
        }
        Config.highLevelChance = number(p, "highLevelChance", Config.highLevelChance, 0.0, 1.0);
        Config.levelRarityExponent = number(p, "levelRarityExponent", Config.levelRarityExponent, 1.0, 10.0);
        Config.hostileHighLevelChance = number(p, "hostileHighLevelChance", Config.hostileHighLevelChance, 0.0, 1.0);
        Config.hostileLevelRarityExponent = number(p, "hostileLevelRarityExponent", Config.hostileLevelRarityExponent, 1.0, 10.0);
        Config.commonLevelSkew = number(p, "commonLevelSkew", Config.commonLevelSkew, 0.25, 1.0);
        Config.maxLevel = (int) number(p, "maxLevel", Config.maxLevel, 1, 1000);
        Config.breedingMutationChance = number(p, "breedingMutationChance", Config.breedingMutationChance, 0.0, 1.0);
        Config.breedingMutationMinBonus = (int) number(p, "breedingMutationMinBonus", Config.breedingMutationMinBonus, 1, 1000);
        Config.breedingMutationMaxBonus = (int) number(p, "breedingMutationMaxBonus", Config.breedingMutationMaxBonus, 1, 1000);
        Config.uninstallMode = bool(p, "uninstallMode", Config.uninstallMode);
        Config.bossMobsHaveLevelLimits = bool(p, "bossHaveLevelLimits", Config.bossMobsHaveLevelLimits);
        Config.bossMinLevel = (int) number(p, "bossMinLevel", Config.bossMinLevel, 1, 1000);
        Config.bossMaxLevel = (int) number(p, "bossMaxLevel", Config.bossMaxLevel, 1, 1000);
        // Rewrite so missing keys show up with their comments and bad values get corrected.
        save();
    }

    // Out of range or unreadable values fall back to the default, like NeoForge's config does.
    private static double number(Properties p, String key, double def, double min, double max) {
        try {
            double value = Double.parseDouble(p.getProperty(key, String.valueOf(def)).trim());
            return value >= min && value <= max ? value : def;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        return Boolean.parseBoolean(p.getProperty(key, String.valueOf(def)).trim());
    }

    static void save() {
        String text = """
            # MobLevel config. Same settings as moblevel-common.toml on NeoForge.
            # Changes apply on the next game start.
            # Percentages below are CUMULATIVE (share of all spawns at or above a level).

            # Chance (0.0 to 1.0) that a PASSIVE mob spawns ABOVE level 20. 0.020 = 2%%.
            # The rest spawn in the common band 1-20.
            highLevelChance=%s

            # Rarity curve exponent for PASSIVE mobs within the high band (21-150). Range 1.0 to 10.0.
            # Higher = high levels rarer.
            levelRarityExponent=%s

            # Same as highLevelChance, but for HOSTILE mobs (MobCategory.MONSTER). 0.045 = 4.5%%.
            hostileHighLevelChance=%s

            # Rarity curve exponent for HOSTILE mobs within the high band (21-150). Range 1.0 to 10.0.
            hostileLevelRarityExponent=%s

            # Shape of the common band (1-20). Range 0.25 to 1.0. 1.0 = flat, 5%% per level.
            # Below 1.0 nudges the band upward so the flimsiest mobs get rarer.
            commonLevelSkew=%s

            # Range 1 to 1000.
            maxLevel=%d

            # Chance (0.0 to 1.0) that a child mutates above the average level of its parents. 0.01 = 1%%.
            breedingMutationChance=%s

            # Minimum and maximum level bonus on a breeding mutation. Range 1 to 1000.
            breedingMutationMinBonus=%d
            breedingMutationMaxBonus=%d

            # Set to true before removing the mod: instead of applying levels, MobLevel strips all
            # of its data from every entity as its chunk loads. Let the world run and visit your
            # areas, then remove the jar. Only chunks that load while this is on get cleaned.
            uninstallMode=%s

            # Apply level limits to boss mobs such as Ender Dragon and Wither?
            bossHaveLevelLimits=%s

            # Minimum and maximum level for boss mobs. Range 1 to 1000.
            bossMinLevel=%d
            bossMaxLevel=%d
            """.formatted(
                Config.highLevelChance, Config.levelRarityExponent,
                Config.hostileHighLevelChance, Config.hostileLevelRarityExponent,
                Config.commonLevelSkew, Config.maxLevel,
                Config.breedingMutationChance, Config.breedingMutationMinBonus, Config.breedingMutationMaxBonus,
                Config.uninstallMode, Config.bossMobsHaveLevelLimits, Config.bossMinLevel, Config.bossMaxLevel);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, text);
        } catch (IOException e) {
            MobLevel.LOGGER.error("Could not write {}", FILE, e);
        }
    }
}
