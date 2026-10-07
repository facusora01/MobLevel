package com.moblevel.fabric;

import com.moblevel.Config;
import com.moblevel.MobLevel;
import com.moblevel.platform.Platform;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;

public class FabricPlatform implements Platform {
    @Override
    public String loaderName() {
        return "fabric";
    }

    @Override
    public String modVersion() {
        return FabricLoader.getInstance().getModContainer(MobLevel.MODID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("0");
    }

    @Override
    public Item totemNecklace() {
        return MobLevelFabric.totemNecklace;
    }

    @Override
    public void setUninstallMode(boolean on) {
        Config.uninstallMode = on;
        FabricConfig.save();
    }

    // Players without the mod (vanilla clients) can't receive these, so they are skipped.
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        if (ServerPlayNetworking.canSend(player, payload.type())) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendToTracking(Entity entity, CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            sendToPlayer(player, payload);
        }
    }
}
