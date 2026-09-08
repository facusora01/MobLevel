package com.moblevel;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue HIGH_LEVEL_CHANCE = BUILDER
            .comment("Chance (0.0 to 1.0) that a mob spawns ABOVE level 20.",
                    "0.065 = 6.5%. The rest spawn in the common band 1-20.",
                    "Percentages below are CUMULATIVE (share of all spawns at or above a level),",
                    "which is what you actually meet in game. With exponent 5.0 this yields:",
                    "50+ ~1.69%, 100+ ~0.61% (one in 164), 130+ ~0.22%, exactly 150 ~0.010%.")
            .defineInRange("highLevelChance", 0.065, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue LEVEL_RARITY_EXPONENT = BUILDER
            .comment("Rarity curve exponent within the high band (21-150). Higher = high levels rarer.",
                    "Raise this to thin out the top of the band without changing how many mobs",
                    "enter it at all. Cumulative share of all spawns at level 100 or above:",
                    "1.5 -> 1.84% (one in 54), 3.0 -> 0.99%, 5.0 -> 0.61%, 10.0 -> 0.31%.",
                    "Passive animals never despawn, so every high-level one ever spawned stays",
                    "in the world; a gentle curve accumulates them over hours of exploring.")
            .defineInRange("levelRarityExponent", 5.0, 1.0, 10.0);

    public static final ModConfigSpec.DoubleValue COMMON_LEVEL_SKEW = BUILDER
            .comment("Shape of the common band (1-20). 1.0 = flat, 5% per level. Below 1.0 nudges",
                    "the band upward so the flimsiest mobs get rarer, without ever exceeding 20.",
                    "Share of common-band mobs landing on level 1, and on levels 1-5:",
                    "1.00 -> 5.0% and 25.0% | 0.75 -> 1.8% and 15.7% | 0.50 -> 0.3% and 6.3%.")
            .defineInRange("commonLevelSkew", 0.75, 0.25, 1.0);

    public static final ModConfigSpec.IntValue MAX_LEVEL = BUILDER
            .defineInRange("maxLevel", 150, 1, 1000);

    public static final ModConfigSpec.DoubleValue BREEDING_MUTATION_CHANCE = BUILDER
            .comment("Chance (0.0 to 1.0) that a child mutates above the parents' average level.",
                    "0.01 = 1%. Leveling up through breeding is rare (shiny-style).")
            .defineInRange("breedingMutationChance", 0.01, 0.0, 1.0);

    public static final ModConfigSpec.IntValue BREEDING_MUTATION_MIN_BONUS = BUILDER
            .comment("Minimum level bonus on a breeding mutation.")
            .defineInRange("breedingMutationMinBonus", 3, 1, 1000);

    public static final ModConfigSpec.IntValue BREEDING_MUTATION_MAX_BONUS = BUILDER
            .comment("Maximum level bonus on a breeding mutation.")
            .defineInRange("breedingMutationMaxBonus", 8, 1, 1000);

    public static final ModConfigSpec.BooleanValue UNINSTALL_MODE = BUILDER
            .comment("Set to true before removing the mod: instead of applying levels, MobLevel will",
                    "strip all of its data (names, tags, health scaling, creeper radius) from every",
                    "entity as its chunk loads. Let the world run / visit your areas, then remove the jar.",
                    "Only chunks that load while this is on get cleaned.")
            .define("uninstallMode", false);

    public static final ModConfigSpec.BooleanValue BOSS_MOBS_HAVE_LEVEL_LIMITS = BUILDER
            .comment("Apply level limits to boss mobs such as Ender Dragon and Wither?")
            .define("bossHaveLevelLimits", true);

    public static final ModConfigSpec.IntValue BOSS_MIN_LEVEL = BUILDER
            .comment("Minimum level for boss mobs (Dragon, Wither, etc.)")
            .defineInRange("bossMinLevel", 20, 1, 1000);

    public static final ModConfigSpec.IntValue BOSS_MAX_LEVEL = BUILDER
            .comment("Maximum level for boss mobs (Dragon, Wither, etc.)")
            .defineInRange("bossMaxLevel", 80, 1, 1000);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
    }
}
