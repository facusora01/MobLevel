package com.moblevel;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
// IMPORTANTE: Este es el import nuevo que necesitas
import com.mojang.serialization.Codec;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod(MobLevel.MODID)
public class MobLevel {
    public static final String MODID = "moblevel";
    public static final Logger LOGGER = LogUtils.getLogger();

    // ... (Tus registros de BLOCKS, ITEMS, TABS si los tienes, déjalos aquí) ...

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    // CORRECCIÓN: Añadimos .fieldOf("level")
    public static final Supplier<AttachmentType<Integer>> MOB_LEVEL_DATA = ATTACHMENT_TYPES.register(
            "mob_level_data",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT.fieldOf("level"))
                    .build()
    );

    public MobLevel(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        // Registros...
        // BLOCKS.register(modEventBus);
        // ITEMS.register(modEventBus);
        // CREATIVE_MODE_TABS.register(modEventBus);

        ATTACHMENT_TYPES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        // Tu código de setup
    }
}