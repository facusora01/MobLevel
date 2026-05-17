package com.moblevel;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue ELITE_CHANCE = BUILDER
            .comment("Probabilidad (0.0 a 1.0) de que un mob spawnee en el rango máximo (130-150).",
                    "0.005 = 0.5% de probabilidad.")
            .defineInRange("eliteChance", 0.005, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue LEVEL_RARITY_EXPONENT = BUILDER
            .comment("Exponente para la curva de niveles normales (1-129). Más alto = Mobs altos más raros.")
            .defineInRange("levelRarityExponent", 5.0, 1.0, 10.0);

    public static final ForgeConfigSpec.IntValue MAX_LEVEL = BUILDER
            .defineInRange("maxLevel", 150, 1, 1000);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
