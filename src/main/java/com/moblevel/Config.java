package com.moblevel;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Two curves: hostiles (MobCategory.MONSTER) despawn and are replaced constantly, so a
    // generous tail there is self-limiting. Everything else - farm animals above all - never
    // despawns, so every high roll is permanent and they pile up over hours of exploring.
    // Percentages in these comments are CUMULATIVE (share of all spawns at or above a level),
    // which is what a player actually meets, not the odds of one exact level.

    public static final ModConfigSpec.DoubleValue HIGH_LEVEL_CHANCE = BUILDER
            .comment("Chance (0.0 to 1.0) that a PASSIVE mob spawns ABOVE level 20.",
                    "0.020 = 2%. The rest spawn in the common band 1-20.",
                    "With exponent 8.0 this yields: 50+ ~0.35%, 100+ ~0.12% (one in 810),",
                    "130+ ~0.045%, exactly 150 ~0.002%.")
            .defineInRange("highLevelChance", 0.020, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue LEVEL_RARITY_EXPONENT = BUILDER
            .comment("Rarity curve exponent for PASSIVE mobs within the high band (21-150).",
                    "Higher = high levels rarer. Raise this to thin out the top of the band",
                    "without changing how many mobs enter it at all.")
            .defineInRange("levelRarityExponent", 8.0, 1.0, 10.0);

    public static final ModConfigSpec.DoubleValue HOSTILE_HIGH_LEVEL_CHANCE = BUILDER
            .comment("Same as highLevelChance, but for HOSTILE mobs (MobCategory.MONSTER).",
                    "0.045 = 4.5%. With hostileLevelRarityExponent 6.0 this yields:",
                    "50+ ~1.00%, 100+ ~0.36% (one in 278), 130+ ~0.13%, exactly 150 ~0.006%.",
                    "That is about 2.9x more likely than a passive, which reads as a rare",
                    "encounter rather than a constant threat.")
            .defineInRange("hostileHighLevelChance", 0.045, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue HOSTILE_LEVEL_RARITY_EXPONENT = BUILDER
            .comment("Rarity curve exponent for HOSTILE mobs within the high band (21-150).",
                    "Cumulative share of hostile spawns at level 100 or above, at chance 0.045:",
                    "5.0 -> 0.43%, 6.0 -> 0.36% (one in 278), 8.0 -> 0.27%, 10.0 -> 0.22%.")
            .defineInRange("hostileLevelRarityExponent", 6.0, 1.0, 10.0);

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
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
