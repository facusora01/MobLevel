package com.moblevel;

import net.minecraft.network.FriendlyByteBuf;

// Server -> client: "entity <id> has level <level>". Sent when a player starts
// tracking a leveled mob, and re-sent to trackers when a level is reassigned.
public record LevelSyncPayload(int entityId, int level) {

    public LevelSyncPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(level);
    }
}
