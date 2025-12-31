package com.moblevel;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Probabilidad de que salga un mob "Élite" (Rango alto)
    public static final ModConfigSpec.DoubleValue ELITE_CHANCE = BUILDER
            .comment("Probabilidad (0.0 a 1.0) de que un mob spawnee en el rango máximo (130-150).",
                    "0.005 = 0.5% de probabilidad.")
            .defineInRange("eliteChance", 0.005, 0.0, 1.0);

    // El exponente solo afectará a los mobs normales (no élites)
    public static final ModConfigSpec.DoubleValue LEVEL_RARITY_EXPONENT = BUILDER
            .comment("Exponente para la curva de niveles normales (1-129). Más alto = Mobs altos más raros.")
            .defineInRange("levelRarityExponent", 5.0, 1.0, 10.0); // Cambiado de 3.0 a 5.0

    public static final ModConfigSpec.IntValue MAX_LEVEL = BUILDER
            .defineInRange("maxLevel", 150, 1, 1000);

    // ... (Mantén ITEM_STRINGS y el resto igual) ...

    static final ModConfigSpec SPEC = BUILDER.build();

    // ... (Mantén validateItemName) ...
    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
    }
}