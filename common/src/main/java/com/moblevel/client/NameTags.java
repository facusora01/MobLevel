package com.moblevel.client;

import com.moblevel.ClientLevelCache;
import com.moblevel.MobEvents;

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
// Client-only: the [LvN] nameplate and the spyglass level scanner. Each loader calls
// onClientTick() every client tick and asks label()/visibility() while rendering a name tag.
public class NameTags {
    // Vanilla shows mob nameplates up to 64 blocks (32 sneaking); trimmed by 25%.
    private static final double RANGE_SQ = 48.0 * 48.0;
    private static final double RANGE_DISCRETE_SQ = 24.0 * 24.0;
    // Spyglass acts as a level scanner: the mob under the scoped crosshair shows its label this far.
    private static final double SPYGLASS_SCAN_RANGE = 100.0;

    // Entity id of the mob currently under the spyglass crosshair, -1 when not scoping.
    private static int scopedMobId = -1;

    public static void onClientTick() {
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

    // The [LvN] label is drawn from the synced level cache instead of living in the
    // entity's CustomName, so vanilla naming/persistence stays untouched.
    // Returns null for a mob with no synced level: its vanilla name tag is left alone.
    public static Component label(Mob mob) {
        Integer level = ClientLevelCache.get(mob.getId());
        return level == null ? null : buildLabel(mob, level);
    }

    // Whether the mob's name tag shows. null leaves the decision to vanilla.
    public static Boolean visibility(Mob mob) {
        Integer level = ClientLevelCache.get(mob.getId());
        double distSq = Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(mob);
        double limitSq = mob.isDiscrete() ? RANGE_DISCRETE_SQ : RANGE_SQ;

        if (level == null) {
            // Not synced (vanilla-named mob, or packet not arrived yet): only trim range.
            return distSq > limitSq ? Boolean.FALSE : null;
        }

        // Scoped target: force the label regardless of distance.
        if (scopedMobId != -1 && mob.getId() == scopedMobId) {
            return Boolean.TRUE;
        }

        // Same feel as the old CustomName behavior: label shows when the crosshair
        // is on the mob within range. TRUE is required because unnamed mobs never
        // pass vanilla's shouldShowName check on their own.
        boolean targeted = Minecraft.getInstance().crosshairPickEntity == mob;
        return (targeted && distSq <= limitSq) ? Boolean.TRUE : Boolean.FALSE;
    }

    private static Component buildLabel(Mob mob, int level) {
        // A player-given name (name tag) is shown inside the label; otherwise the
        // type name, resolved by this client in its own language.
        Component base = mob.hasCustomName()
            ? mob.getCustomName()
            : mob.getType().getDescription();

        return MobEvents.levelPrefix(level)
            .append(base.copy().withStyle(ChatFormatting.WHITE));
    }
}
