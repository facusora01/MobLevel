package com.moblevel;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue HIGH_LEVEL_CHANCE = BUILDER
            .comment("Chance (0.0 to 1.0) that a mob spawns ABOVE level 20.",
                    "0.065 = 6.5%. The rest spawn in the common band 1-20.",
                    "With exponent 1.5 this yields: exact level 50 ~0.055%, level 150 ~0.03%.")
            .defineInRange("highLevelChance", 0.065, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue LEVEL_RARITY_EXPONENT = BUILDER
            .comment("Rarity curve exponent within the high band (21-150). Higher = high levels rarer.",
                    "1.5 = gentle decay (many 21-40, few 100+).")
            .defineInRange("levelRarityExponent", 1.5, 1.0, 10.0);

    public static final ForgeConfigSpec.IntValue MAX_LEVEL = BUILDER
            .defineInRange("maxLevel", 150, 1, 1000);

    public static final ForgeConfigSpec.DoubleValue BREEDING_MUTATION_CHANCE = BUILDER
            .comment("Chance (0.0 to 1.0) that a child mutates above the parents' average level.",
                    "0.01 = 1%. Leveling up through breeding is rare (shiny-style).")
            .defineInRange("breedingMutationChance", 0.01, 0.0, 1.0);

    public static final ForgeConfigSpec.IntValue BREEDING_MUTATION_MIN_BONUS = BUILDER
            .comment("Minimum level bonus on a breeding mutation.")
            .defineInRange("breedingMutationMinBonus", 3, 1, 1000);

    public static final ForgeConfigSpec.IntValue BREEDING_MUTATION_MAX_BONUS = BUILDER
            .comment("Maximum level bonus on a breeding mutation.")
            .defineInRange("breedingMutationMaxBonus", 8, 1, 1000);

    public static final ForgeConfigSpec.BooleanValue BOSS_MOBS_HAVE_LEVEL_LIMITS = BUILDER
            .comment("Apply level limits to boss mobs such as Ender Dragon and Wither?")
            .define("bossHaveLevelLimits", true);

    public static final ForgeConfigSpec.IntValue BOSS_MIN_LEVEL = BUILDER
            .comment("Minimum level for boss mobs (Dragon, Wither, etc.)")
            .defineInRange("bossMinLevel", 20, 1, 1000);

    public static final ForgeConfigSpec.IntValue BOSS_MAX_LEVEL = BUILDER
            .comment("Maximum level for boss mobs (Dragon, Wither, etc.)")
            .defineInRange("bossMaxLevel", 80, 1, 1000);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
