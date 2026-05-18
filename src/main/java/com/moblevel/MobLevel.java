package com.moblevel;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.config.ModConfig;

@Mod(MobLevel.MODID)
public class MobLevel {
    public static final String MODID = "moblevel";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MobLevel() {
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
            ModMessages.register();
            LOGGER.info("MobLevel Setup completado.");
        }
    }
}
