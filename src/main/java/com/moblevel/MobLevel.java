package com.moblevel;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.config.ModConfig;

@Mod(MobLevel.MODID)
public class MobLevel {
    public static final String MODID = "moblevel";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static IEventBus MOD_EVENT_BUS;

    public MobLevel() {
        this(null);
    }

    private MobLevel(IEventBus modEventBus) {
        if (modEventBus != null) {
            MOD_EVENT_BUS = modEventBus;
            ModItems.register(modEventBus);
            ModCreativeModeTabs.register(modEventBus);
            LOGGER.info("Items and Creative Tabs registered via IEventBus");
        }

        try {
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        } catch (Exception e) {
            LOGGER.error("Failed to register config", e);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventHandler {
        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            // Register items if not already registered
            if (MOD_EVENT_BUS == null) {
                LOGGER.warn("MOD_EVENT_BUS not set during setup!");
            } else {
                if (!ModItems.isRegistered()) {
                    ModItems.register(MOD_EVENT_BUS);
                    ModCreativeModeTabs.register(MOD_EVENT_BUS);
                    LOGGER.info("Items registered via FMLCommonSetupEvent");
                }
            }

            ModMessages.register();
            LOGGER.info("MobLevel Setup completado.");
        }
    }
}
