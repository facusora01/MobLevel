package com.moblevel.neoforge;

import com.moblevel.ClientLevelCache;
import com.moblevel.LevelSyncPayload;
import com.moblevel.MobLevel;
import com.moblevel.TotemAnimationPayload;
import com.moblevel.client.ClientPayloadHandler;
import com.moblevel.platform.Platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MobLevel.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NeoForgePlatform implements Platform {
    // Bumped to 2: clients without LevelSyncPayload cannot join and get a clear
    // version-mismatch screen instead of a mid-game packet error.
    private static final String PROTOCOL_VERSION = "2";

    @Override
    public String loaderName() {
        return "neoforge";
    }

    @Override
    public String modVersion() {
        return ModList.get().getModContainerById(MobLevel.MODID)
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse("0");
    }

    @Override
    public Item totemNecklace() {
        return MobLevelNeoForge.TOTEM_NECKLACE.get();
    }

    @Override
    public void setUninstallMode(boolean on) {
        NeoForgeConfig.UNINSTALL_MODE.set(on);
        NeoForgeConfig.UNINSTALL_MODE.save();
        com.moblevel.Config.uninstallMode = on;
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToTracking(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    @SubscribeEvent
    static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
                TotemAnimationPayload.TYPE,
                TotemAnimationPayload.STREAM_CODEC,
                NeoForgePlatform::handleTotemAnimation);
        registrar.playToClient(
                LevelSyncPayload.TYPE,
                LevelSyncPayload.STREAM_CODEC,
                NeoForgePlatform::handleLevelSync);
    }

    // Body kept in its own method so ClientPayloadHandler is only class-loaded when the
    // handler actually runs, i.e. never on a dedicated server.
    private static void handleTotemAnimation(TotemAnimationPayload payload, IPayloadContext context) {
        ClientPayloadHandler.handleTotemAnimation(payload);
    }

    // ClientLevelCache holds no client-only types, so it is safe to touch from here.
    private static void handleLevelSync(LevelSyncPayload payload, IPayloadContext context) {
        ClientLevelCache.put(payload.entityId(), payload.level());
    }
}
