package com.moblevel;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import java.util.function.Predicate;

@EventBusSubscriber(modid = MobLevel.MODID)
public class ModCommands {

    @SubscribeEvent
    static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("moblevel")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("uninstall")
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
                    })));
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
