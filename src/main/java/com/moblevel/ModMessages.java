package com.moblevel;

import com.moblevel.client.ClientPayloadHandler;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MobLevel.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModMessages {
    // Bumped to 2: clients without LevelSyncPayload cannot join and get a clear
    // version-mismatch screen instead of a mid-game packet error.
    private static final String PROTOCOL_VERSION = "2";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
                TotemAnimationPayload.TYPE,
                TotemAnimationPayload.STREAM_CODEC,
                ModMessages::handleTotemAnimation);
        registrar.playToClient(
                LevelSyncPayload.TYPE,
                LevelSyncPayload.STREAM_CODEC,
                ModMessages::handleLevelSync);
    }

    // Body kept in its own method so ClientPayloadHandler is only class-loaded when the
    // handler actually runs, i.e. never on a dedicated server.
    private static void handleTotemAnimation(TotemAnimationPayload payload, IPayloadContext context) {
        ClientPayloadHandler.handleTotemAnimation(payload, context);
    }

    // ClientLevelCache holds no client-only types, so it is safe to touch from here.
    private static void handleLevelSync(LevelSyncPayload payload, IPayloadContext context) {
        ClientLevelCache.put(payload.entityId(), payload.level());
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToTracking(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }
}
