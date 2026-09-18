package com.moblevel;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.minecraft.resources.ResourceLocation;

public class ModMessages {
    // Bumped to 2: clients without LevelSyncPayload cannot join and get a clear
    // version-mismatch screen instead of a mid-game packet error.
    // Bumped to 2: clients without LevelSyncPayload cannot join and get a clear
    // version-mismatch screen instead of a mid-game packet error.
    private static final int PROTOCOL_VERSION = 2;
    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(new ResourceLocation(MobLevel.MODID, "messages"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .simpleChannel();
    private static int packetId = 0;

    public static void register() {
        INSTANCE.messageBuilder(TotemAnimationPayload.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TotemAnimationPayload::toBytes)
                .decoder(TotemAnimationPayload::new)
                .consumerMainThread(ModMessages::handleTotemAnimation)
                .add();
        INSTANCE.messageBuilder(LevelSyncPayload.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LevelSyncPayload::toBytes)
                .decoder(LevelSyncPayload::new)
                .consumerMainThread(ModMessages::handleLevelSync)
                .add();
    }

    private static void handleLevelSync(LevelSyncPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientLevelCache.put(payload.entityId(), payload.level()))
        );
        context.setPacketHandled(true);
    }

    public static int id() {
        return packetId++;
    }

    private static void handleTotemAnimation(TotemAnimationPayload payload, CustomPayloadEvent.Context context) {
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

    public static void sendToPlayer(TotemAnimationPayload msg, PacketDistributor.PacketTarget target) {
        INSTANCE.send(msg, target);
    }
}
