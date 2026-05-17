package com.moblevel;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class TotemAnimationPayload {
    private final int entityId;
    private final ItemStack stack;

    public TotemAnimationPayload(int entityId, ItemStack stack) {
        this.entityId = entityId;
        this.stack = stack;
    }

    public TotemAnimationPayload(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.stack = buf.readItem();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeItem(this.stack);
    }

    public int entityId() {
        return entityId;
    }

    public ItemStack stack() {
        return stack;
    }

    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
        });
        return true;
    }
}
