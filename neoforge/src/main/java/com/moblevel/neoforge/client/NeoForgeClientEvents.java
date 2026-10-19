package com.moblevel.neoforge.client;

import com.moblevel.ClientLevelCache;
import com.moblevel.MobLevel;
import com.moblevel.UpdateChecker;
import com.moblevel.client.NameTags;

import java.lang.reflect.Method;
import java.util.Arrays;

import net.minecraft.network.chat.Component;
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

        Boolean visibility = NameTags.visibility(mob);
        if (visibility != null) setCanRender(event, visibility);
    }

    // The TriState parameter is net.neoforged.neoforge.common.util.TriState up to 1.21.4 and
    // net.minecraft.util.TriState from 1.21.5, so the setter is looked up instead of named.
    private static final Method SET_CAN_RENDER = Arrays.stream(RenderNameTagEvent.CanRender.class.getMethods())
        .filter(m -> m.getName().equals("setCanRender")).findFirst().orElse(null);

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setCanRender(RenderNameTagEvent.CanRender event, boolean show) {
        if (SET_CAN_RENDER == null) return;
        try {
            SET_CAN_RENDER.invoke(event, Enum.valueOf((Class<Enum>) SET_CAN_RENDER.getParameterTypes()[0],
                show ? "TRUE" : "FALSE"));
        } catch (ReflectiveOperationException ignored) {
            // name tags fall back to vanilla visibility
        }
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
