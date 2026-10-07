package com.moblevel.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;

/**
 * What the shared code needs from the mod loader. Each loader ships one
 * implementation, found through {@link Services}.
 */
public interface Platform {
    /** The loader name as Modrinth tags it, for the update check and bug reports. */
    String loaderName();

    /** The installed version of this mod, as the loader read it from the jar. */
    String modVersion();

    Item totemNecklace();

    /** Sets uninstall mode and writes it to the config file, so it survives a restart. */
    void setUninstallMode(boolean on);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToTracking(Entity entity, CustomPacketPayload payload);
}
