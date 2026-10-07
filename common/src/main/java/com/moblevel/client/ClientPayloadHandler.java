package com.moblevel.client;

import com.moblevel.TotemAnimationPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/** Client-only: each loader calls it from its client-bound packet handler, never on a dedicated server. */
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {
    }

    public static void handleTotemAnimation(TotemAnimationPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        Entity entity = minecraft.level.getEntity(payload.entityId());
        if (entity == null) return;

        if (minecraft.player != null && entity.getId() == minecraft.player.getId()) {
            minecraft.gameRenderer.displayItemActivation(payload.stack());
        }

        minecraft.level.playLocalSound(
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.TOTEM_USE,
                SoundSource.PLAYERS,
                1.0f, 1.0f, false);

        minecraft.particleEngine.createTrackingEmitter(
                entity,
                ParticleTypes.TOTEM_OF_UNDYING,
                30);
    }
}
