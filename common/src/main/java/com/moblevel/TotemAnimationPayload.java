package com.moblevel;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Tells clients to play the totem save animation for the entity that was rescued. */
public record TotemAnimationPayload(int entityId, ItemStack stack) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TotemAnimationPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MobLevel.MODID, "totem_animation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TotemAnimationPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TotemAnimationPayload::entityId,
            ItemStack.OPTIONAL_STREAM_CODEC, TotemAnimationPayload::stack,
            TotemAnimationPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
