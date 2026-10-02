package com.moblevel.neoforge;

import com.moblevel.MobEvents;
import com.moblevel.MobLevel;
import com.moblevel.ModCommands;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

// Wires NeoForge's game events to the shared MobEvents.
@EventBusSubscriber(modid = MobLevel.MODID)
public class NeoForgeEvents {
    @SubscribeEvent
    static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob) MobEvents.onEntityJoinLevel(mob);
    }

    @SubscribeEvent
    static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Mob mob && event.getEntity() instanceof ServerPlayer player) {
            MobEvents.onStartTracking(mob, player);
        }
    }

    @SubscribeEvent
    static void onBabySpawn(BabyEntitySpawnEvent event) {
        MobEvents.onBabySpawn(event.getParentA(), event.getParentB(), event.getChild());
    }

    @SubscribeEvent
    static void onTame(AnimalTameEvent event) {
        if (MobEvents.vetoTame(event.getAnimal())) event.setCanceled(true);
    }

    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Pre event) {
        event.setNewDamage(MobEvents.modifyDamage(event.getSource(), event.getNewDamage()));
    }

    @SubscribeEvent
    static void onExperienceDrop(LivingExperienceDropEvent event) {
        event.setDroppedExperience(MobEvents.modifyExperience(event.getEntity(), event.getDroppedExperience()));
    }

    @SubscribeEvent
    static void onLivingDrops(LivingDropsEvent event) {
        MobEvents.modifyDrops(event.getEntity(), event.getDrops());
    }

    @SubscribeEvent
    static void onLivingDeath(LivingDeathEvent event) {
        if (MobEvents.tryTotemSave(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob mob) MobEvents.onMobTick(mob);
    }

    @SubscribeEvent
    static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}
