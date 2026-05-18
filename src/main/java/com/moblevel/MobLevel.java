package com.moblevel;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MobLevel.MODID)
public class MobLevel {
    public static final String MODID = "moblevel";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MobLevel() {
        // Get mod event bus and register items immediately
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        LOGGER.info("Items and Creative Tabs registered via constructor");

        try {
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        } catch (Exception e) {
            LOGGER.error("Failed to register config", e);
        }

        modEventBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        ModMessages.register();
        LOGGER.info("MobLevel Setup completado.");
    }
}
