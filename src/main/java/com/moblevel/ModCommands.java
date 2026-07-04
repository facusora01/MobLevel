package com.moblevel;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = MobLevel.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {

    @SubscribeEvent
    static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("moblevel")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("uninstall")
                    .executes(ctx -> {
                        Config.UNINSTALL_MODE.set(true);
                        int total = forEachLoadedMob(ctx.getSource(), MobEvents::stripModData);
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
                        int total = forEachLoadedMob(ctx.getSource(), MobEvents::reassignLevel);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "[MobLevel] Re-rolled levels for " + total + " loaded mobs with the current spawn rates. ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(
                                "Re-rolls never raise a mob's level. Mobs in unloaded chunks keep their old level; "
                                + "run this again in other areas if needed.")
                                .withStyle(ChatFormatting.GRAY)), true);
                        return total;
                    })));
    }

    private static int forEachLoadedMob(CommandSourceStack source, Consumer<Mob> action) {
        int count = 0;
        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Mob mob) {
                    action.accept(mob);
                    count++;
                }
            }
        }
        return count;
    }
}
