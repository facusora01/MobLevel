package com.moblevel;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MobLevel.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientRenderEvents {
    // Vanilla shows mob nameplates up to 64 blocks (32 sneaking); trimmed by 25%.
    private static final double RANGE_SQ = 48.0 * 48.0;
    private static final double RANGE_DISCRETE_SQ = 24.0 * 24.0;

    @SubscribeEvent
    static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        double distSq = Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(mob);
        double limitSq = mob.isDiscrete() ? RANGE_DISCRETE_SQ : RANGE_SQ;
        if (distSq > limitSq) {
            event.setResult(Event.Result.DENY);
        }
    }
}
