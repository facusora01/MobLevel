package com.moblevel;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server -> client: "entity {@code entityId} has level {@code level}". Sent when a player
 * starts tracking a leveled mob, and re-sent to trackers when a level is reassigned.
 */
public record LevelSyncPayload(int entityId, int level) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<LevelSyncPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MobLevel.MODID, "level_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LevelSyncPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, LevelSyncPayload::entityId,
            ByteBufCodecs.VAR_INT, LevelSyncPayload::level,
            LevelSyncPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
