package com.moblevel;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue HIGH_LEVEL_CHANCE = BUILDER
            .comment("Probabilidad (0.0 a 1.0) de que un mob spawnee por ENCIMA de lvl 20.",
                    "0.20 = 20%. El resto spawnea en la banda común 1-20.",
                    "Con exponente 1.5 esto da: lvl 50 exacto ~0.17% (similar a oveja rosa vanilla).")
            .defineInRange("highLevelChance", 0.20, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue LEVEL_RARITY_EXPONENT = BUILDER
            .comment("Exponente de la curva dentro de la banda alta (21-150). Más alto = niveles altos más raros.",
                    "1.5 = decae suave (ves muchos 21-40, pocos 100+).")
            .defineInRange("levelRarityExponent", 1.5, 1.0, 10.0);

    public static final ForgeConfigSpec.IntValue MAX_LEVEL = BUILDER
            .defineInRange("maxLevel", 150, 1, 1000);

    // ---------- Breeding (estilo shiny: progreso raro) ----------
    public static final ForgeConfigSpec.DoubleValue BREEDING_MUTATION_CHANCE = BUILDER
            .comment("Probabilidad (0.0 a 1.0) de que un hijo mute a nivel superior al promedio de los padres.",
                    "0.01 = 1%. Subir niveles via breeding es raro (estilo shiny).")
            .defineInRange("breedingMutationChance", 0.01, 0.0, 1.0);

    public static final ForgeConfigSpec.IntValue BREEDING_MUTATION_MIN_BONUS = BUILDER
            .comment("Bonus mínimo de nivel al mutar en breeding.")
            .defineInRange("breedingMutationMinBonus", 3, 1, 1000);

    public static final ForgeConfigSpec.IntValue BREEDING_MUTATION_MAX_BONUS = BUILDER
            .comment("Bonus máximo de nivel al mutar en breeding.")
            .defineInRange("breedingMutationMaxBonus", 8, 1, 1000);

    public static final ForgeConfigSpec.BooleanValue BOSS_MOBS_HAVE_LEVEL_LIMITS = BUILDER
            .comment("¿Aplicar límites de nivel a boss mobs como Dragon y Wither?")
            .define("bossHaveLevelLimits", true);

    public static final ForgeConfigSpec.IntValue BOSS_MIN_LEVEL = BUILDER
            .comment("Nivel mínimo para boss mobs (Dragon, Wither, etc.)")
            .defineInRange("bossMinLevel", 20, 1, 1000);

    public static final ForgeConfigSpec.IntValue BOSS_MAX_LEVEL = BUILDER
            .comment("Nivel máximo para boss mobs (Dragon, Wither, etc.)")
            .defineInRange("bossMaxLevel", 80, 1, 1000);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
