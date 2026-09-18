package com.moblevel;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.resources.ResourceLocation;

public class ModMessages {
    // Bumped to 2: clients without LevelSyncPayload cannot join and get a clear
    // version-mismatch screen instead of a mid-game packet error.
    private static final String PROTOCOL_VERSION = "2";
    public static final SimpleChannel INSTANCE = net.minecraftforge.network.NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MobLevel.MODID, "messages"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(
                id(),
                TotemAnimationPayload.class,
                (msg, buf) -> msg.toBytes(buf),
                TotemAnimationPayload::new,
                (msg, ctx) -> handleTotemAnimation(msg, ctx.get()),
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                id(),
                LevelSyncPayload.class,
                (msg, buf) -> msg.toBytes(buf),
                LevelSyncPayload::new,
                (msg, ctx) -> handleLevelSync(msg, ctx.get()),
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    private static void handleLevelSync(LevelSyncPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientLevelCache.put(payload.entityId(), payload.level()))
        );
        context.setPacketHandled(true);
    }

    public static int id() {
        return packetId++;
    }

    private static void handleTotemAnimation(TotemAnimationPayload payload, NetworkEvent.Context context) {
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    Entity entity = Minecraft.getInstance().level.getEntity(payload.entityId());

                    if (entity != null) {
                        if (entity.getId() == Minecraft.getInstance().player.getId()) {
                            Minecraft.getInstance().gameRenderer.displayItemActivation(payload.stack());
                        }

                        Minecraft.getInstance().level.playLocalSound(
                                entity.getX(), entity.getY(), entity.getZ(),
                                SoundEvents.TOTEM_USE,
                                SoundSource.PLAYERS,
                                1.0f, 1.0f, false
                        );

                        Minecraft.getInstance().particleEngine.createTrackingEmitter(
                                entity,
                                ParticleTypes.TOTEM_OF_UNDYING,
                                30
                        );
                    }
                })
        );
        context.setPacketHandled(true);
    }

    public static void sendToServer(Object msg) {
        INSTANCE.sendToServer(msg);
    }

    public static void sendToPlayer(TotemAnimationPayload msg, PacketDistributor.PacketTarget target) {
        INSTANCE.send(target, msg);
    }
}
