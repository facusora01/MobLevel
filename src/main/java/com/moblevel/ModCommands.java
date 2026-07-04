package com.moblevel;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MobLevel.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {

    @SubscribeEvent
    static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("uninstall")
                .then(Commands.literal("MobLevel")
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> {
                        Config.UNINSTALL_MODE.set(true);

                        int cleaned = 0;
                        for (ServerLevel level : ctx.getSource().getServer().getAllLevels()) {
                            for (Entity entity : level.getAllEntities()) {
                                if (entity instanceof Mob mob) {
                                    MobEvents.stripModData(mob);
                                    cleaned++;
                                }
                            }
                        }

                        final int total = cleaned;
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "[MobLevel] Uninstall mode ON. Cleaned " + total + " loaded mobs. ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(
                                "Visit your remaining areas so their chunks get cleaned, then remove the jar. "
                                + "Run '/uninstall MobLevel cancel' to revert.")
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
                        }))));
    }
}
