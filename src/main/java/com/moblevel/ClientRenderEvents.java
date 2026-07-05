package com.moblevel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.TickEvent;
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

    @SubscribeEvent
    static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        // Scoped target: force the label regardless of distance.
        if (scopedMobId != -1 && mob.getId() == scopedMobId) {
            event.setResult(Event.Result.ALLOW);
            return;
        }

        double distSq = Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(mob);
        double limitSq = mob.isDiscrete() ? RANGE_DISCRETE_SQ : RANGE_SQ;
        if (distSq > limitSq) {
            event.setResult(Event.Result.DENY);
        }
    }
}
