package com.moblevel;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = MobLevel.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class MobLevelClient {
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        MobLevel.LOGGER.info("MobLevel client setup complete.");
    }
}
