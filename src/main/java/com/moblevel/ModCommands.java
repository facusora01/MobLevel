package com.moblevel;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

@EventBusSubscriber(modid = MobLevel.MODID)
public class ModCommands {

    // How far ahead of the player to look for the mob a report is about.
    private static final double CROSSHAIR_RANGE = 32.0;

    @SubscribeEvent
    static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("moblevel")
                // The permission sits on the admin subcommands, not on the root: any player
                // has to be able to run "report".
                .then(Commands.literal("uninstall")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(ctx -> {
                        Config.UNINSTALL_MODE.set(true);
                        int total = forEachLoadedMob(ctx.getSource(), mob -> {
                            MobEvents.stripModData(mob);
                            return true;
                        });
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "[MobLevel] Uninstall mode ON. Cleaned " + total + " loaded mobs. ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(
                                "Visit your remaining areas so their chunks get cleaned, then remove the jar. "
                                + "Run '/moblevel uninstall cancel' to revert.")
                                .withStyle(ChatFormatting.GRAY)), true);
                        return total;
                    })
                    .then(Commands.literal("cancel")
                        .executes(ctx -> {
                            Config.UNINSTALL_MODE.set(false);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "[MobLevel] Uninstall mode OFF. Mobs level up again on spawn.")
                                .withStyle(ChatFormatting.YELLOW), true);
                            return 1;
                        })))
                .then(Commands.literal("restartLevels")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(ctx -> {
                        // One-time migration: a persistent per-world marker blocks re-runs, so the
                        // command can't be farmed for re-rolls or used to nerf scary encounters.
                        // Once enabled, pre-1.2.2 mobs also migrate automatically as chunks load.
                        Scoreboard scoreboard = ctx.getSource().getServer().getScoreboard();
                        if (scoreboard.getObjective(MobEvents.MIGRATION_MARKER) != null) {
                            ctx.getSource().sendFailure(Component.literal(
                                "[MobLevel] restartLevels was already used in this world. "
                                + "It is a one-time migration; old mobs keep migrating as their chunks load."));
                            return 0;
                        }
                        scoreboard.addObjective(MobEvents.MIGRATION_MARKER, ObjectiveCriteria.DUMMY,
                            Component.literal("MobLevel migration marker"),
                            ObjectiveCriteria.RenderType.INTEGER, false, null);

                        int total = forEachLoadedMob(ctx.getSource(), mob -> {
                            if (!mob.getTags().contains(MobEvents.VERSION_TAG)) {
                                MobEvents.reassignLevel(mob);
                                return true;
                            }
                            return false;
                        });
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "[MobLevel] Migration enabled. Re-rolled loaded pre-update mobs with the current rates. ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(
                                "Re-rolls never raise a mob's level. Mobs in unloaded chunks migrate "
                                + "automatically when their chunks load.")
                                .withStyle(ChatFormatting.GRAY)), true);
                        return total;
                    }))
                .then(Commands.literal("report")
                    .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> report(ctx.getSource(),
                            StringArgumentType.getString(ctx, "message"))))));
    }

    // Hands the player a prefilled issue link. Nothing leaves the game by itself: the player
    // opens the page and submits it, so there is no endpoint to run, no secret in the jar,
    // and no report is ever filed by someone who did not read it.
    private static int report(CommandSourceStack source, String message) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        String url = ReportLink.build(message, diagnostics(player));

        source.sendSuccess(() -> Component.literal("[MobLevel] ").withStyle(ChatFormatting.GOLD)
            .append(Component.literal("Your report is ready. ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("[Click here to send it]").withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.OpenUrl(URI.create(url)))))
            .append(Component.literal(
                "\nNothing has been sent yet. The page opens with your version and the mob "
                + "you are looking at already filled in.")
                .withStyle(ChatFormatting.GRAY)), false);
        return 1;
    }

    // What turns "it doesn't work" into something actionable.
    private static Map<String, String> diagnostics(ServerPlayer player) {
        Map<String, String> out = new LinkedHashMap<>();
        out.put("MobLevel", ModList.get().getModContainerById(MobLevel.MODID)
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse("unknown"));
        out.put("Minecraft", SharedConstants.getCurrentVersion().name());
        out.put("Loader", "NeoForge");
        MinecraftServer server = player.level().getServer();
        out.put("Playing on", server != null && server.isDedicatedServer()
            ? "a dedicated server" : "singleplayer");
        out.put("Dimension", player.level().dimension().identifier().toString());

        Mob target = mobInCrosshair(player);
        if (target != null) {
            out.put("Mob looked at", target.getType().getDescription().getString());
            out.put("Its level", String.valueOf(DropsCalculator.getLevelFromTags(target.getTags())));
            out.put("Its health", String.format(Locale.ROOT, "%.1f / %.1f",
                target.getHealth(), target.getMaxHealth()));
            out.put("Its tags", target.getTags().isEmpty()
                ? "(none)" : String.join(" ", target.getTags()));
        }
        return out;
    }

    // Same raycast the spyglass scanner uses, run server-side: whatever the player is
    // pointing at is almost always what the report is about.
    private static Mob mobInCrosshair(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F).scale(CROSSHAIR_RANGE);
        Vec3 end = eye.add(look);
        AABB box = player.getBoundingBox().expandTowards(look).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
            player, eye, end, box, entity -> entity instanceof Mob && entity.isAlive(),
            eye.distanceToSqr(end));
        return hit != null ? (Mob) hit.getEntity() : null;
    }

    // Applies the action to every loaded mob; the action returns whether it processed the mob.
    private static int forEachLoadedMob(CommandSourceStack source, Predicate<Mob> action) {
        int count = 0;
        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Mob mob && action.test(mob)) {
                    count++;
                }
            }
        }
        return count;
    }
}
