package com.moblevel.fabric.mixin.client;

import com.moblevel.client.NameTags;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The [LvN] name tag. Overrides vanilla's decision the way NeoForge's RenderNameTagEvent.CanRender does:
// TRUE shows the tag regardless of distance, FALSE hides it, DEFAULT keeps vanilla's result.
@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    // In 1.21.11 the name tag decision is part of extractRenderState (26.x moved it to extractNameTags).
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V",
        at = @At("TAIL"))
    private void moblevel$levelNameTag(T entity, S state, float partialTicks, CallbackInfo ci) {
        if (this.entityRenderDispatcher.camera == null || !(entity instanceof Mob mob)) return;

        TriState visibility = NameTags.visibility(mob);
        if (visibility == TriState.FALSE) {
            state.nameTag = null;
        } else if (visibility == TriState.TRUE) {
            Component label = NameTags.label(mob);
            state.nameTag = label != null ? label : entity.getDisplayName();
            state.nameTagAttachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot(partialTicks));
        }
    }
}
