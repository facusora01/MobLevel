package com.moblevel;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.config.ModConfig;

@Mod(MobLevel.MODID)
public class MobLevel {
    public static final String MODID = "moblevel";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static boolean registered = false;

    public MobLevel() {
        if (!registered) {
            try {
                java.lang.reflect.Field f = ModLoadingContext.class.getDeclaredField("context");
                f.setAccessible(true);
                ModLoadingContext ctx = (ModLoadingContext) f.get(null);
                java.lang.reflect.Method m = ModLoadingContext.class.getDeclaredMethod("getModEventBus");
                m.setAccessible(true);
                IEventBus modEventBus = (IEventBus) m.invoke(ctx);
                ModItems.register(modEventBus);
                ModCreativeModeTabs.register(modEventBus);
                registered = true;
            } catch (Exception e) {
                LOGGER.error("Failed to register items via reflection", e);
            }
        }
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
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
