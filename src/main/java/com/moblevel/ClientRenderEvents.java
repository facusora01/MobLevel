package com.moblevel;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MobLevel.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientRenderEvents {
    // Vanilla shows mob nameplates up to 64 blocks (32 sneaking); trimmed by 25%.
    private static final double RANGE_SQ = 48.0 * 48.0;
    private static final double RANGE_DISCRETE_SQ = 24.0 * 24.0;
    // Spyglass acts as a level scanner: the mob under the scoped crosshair shows its label this far.
    private static final double SPYGLASS_SCAN_RANGE = 100.0;

    // Entity id of the mob currently under the spyglass crosshair, -1 when not scoping.
    private static int scopedMobId = -1;

    @SubscribeEvent
    static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !player.isScoping()) {
            scopedMobId = -1;
            return;
        }

        // One raycast per tick, only while scoping. Blocks clip the ray first so
        // the spyglass can't reveal levels through walls.
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(SPYGLASS_SCAN_RANGE));

        HitResult blockHit = minecraft.level.clip(new ClipContext(
            eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        AABB searchBox = player.getBoundingBox()
            .expandTowards(look.scale(SPYGLASS_SCAN_RANGE)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            player, eye, end, searchBox,
            entity -> entity instanceof Mob && entity.isAlive(),
            eye.distanceToSqr(end));

        scopedMobId = (entityHit != null) ? entityHit.getEntity().getId() : -1;
    }

    // The [LvN] label is drawn here from the synced level cache instead of living
    // in the entity's CustomName, so vanilla naming/persistence stays untouched.
    @SubscribeEvent
    static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        Integer level = ClientLevelCache.get(mob.getId());
        double distSq = Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(mob);
        double limitSq = mob.isDiscrete() ? RANGE_DISCRETE_SQ : RANGE_SQ;

        if (level == null) {
            // Not synced (vanilla-named mob, or packet not arrived yet): only trim range.
            if (distSq > limitSq) {
                event.setResult(Event.Result.DENY);
            }
            return;
        }

        event.setContent(buildLabel(mob, level));

        // Scoped target: force the label regardless of distance.
        if (scopedMobId != -1 && mob.getId() == scopedMobId) {
            event.setResult(Event.Result.ALLOW);
            return;
        }

        // Same feel as the old CustomName behavior: label shows when the crosshair
        // is on the mob within range. ALLOW is required because unnamed mobs never
        // pass vanilla's shouldShowName check on their own.
        boolean targeted = Minecraft.getInstance().crosshairPickEntity == mob;
        event.setResult((targeted && distSq <= limitSq) ? Event.Result.ALLOW : Event.Result.DENY);
    }

    private static Component buildLabel(Mob mob, int level) {
        ChatFormatting color = ChatFormatting.GREEN;
        if (level >= 50) color = ChatFormatting.AQUA;
        if (level >= 100) color = ChatFormatting.YELLOW;
        if (level >= 130) color = ChatFormatting.RED;
        if (level >= 150) color = ChatFormatting.DARK_PURPLE;

        // A player-given name (name tag) is shown inside the label; otherwise the
        // type name, resolved by this client in its own language.
        Component base = mob.hasCustomName()
            ? mob.getCustomName()
            : mob.getType().getDescription();

        return Component.literal("[Lv" + level + "] ").withStyle(color)
            .append(base.copy().withStyle(ChatFormatting.WHITE));
    }

    // Keep the cache bounded: entries die with their entity and on disconnect.
    @SubscribeEvent
    static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            ClientLevelCache.remove(event.getEntity().getId());
        }
    }

    @SubscribeEvent
    static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientLevelCache.clear();
    }
}
