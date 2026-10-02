package com.moblevel.neoforge.client;

import com.moblevel.ClientLevelCache;
import com.moblevel.MobLevel;
import com.moblevel.UpdateChecker;
import com.moblevel.client.NameTags;

import net.minecraft.network.chat.Component;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

@EventBusSubscriber(modid = MobLevel.MODID, value = Dist.CLIENT)
public class NeoForgeClientEvents {
    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        NameTags.onClientTick();
    }

    @SubscribeEvent
    static void onRenderNameTag(RenderNameTagEvent.CanRender event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        Component label = NameTags.label(mob);
        if (label != null) event.setContent(label);

        TriState visibility = NameTags.visibility(mob);
        if (visibility != TriState.DEFAULT) event.setCanRender(visibility);
    }

    // Keep the cache bounded: entries die with their entity and on disconnect.
    @SubscribeEvent
    static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            ClientLevelCache.remove(event.getEntity().getId());
        }
    }

    @SubscribeEvent
    static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        UpdateChecker.check();
    }

    @SubscribeEvent
    static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientLevelCache.clear();
    }
}
