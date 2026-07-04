package com.moblevel;

import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = MobLevel.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new Random();
    private static final UUID SPEED_BOOST_UUID = UUID.fromString("b7a1f3c2-0d4e-4a8b-9c6d-2e1f5a3b7c90");
    private static final DustParticleOptions PARTICLE =
        new DustParticleOptions(new org.joml.Vector3f(0.6f, 0.0f, 1.0f), 0.7f);

    @SubscribeEvent
    static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (event.getLevel().isClientSide()) return;

        // Uninstall mode: strip MobLevel data instead of applying it, so the world
        // can be returned to vanilla before the jar is removed.
        if (Config.UNINSTALL_MODE.get()) {
            stripModData(mob);
            return;
        }

        int currentLevel = 0;
        boolean freshSpawn = true;

        // LOGGER.info("onEntityJoinLevel: {} (tags before: {})",
        //     mob.getType().getDescription().getString(), mob.getTags());

        for (String tag : mob.getTags()) {
            if (tag.startsWith("lvl:")) {
                try {
                    currentLevel = Integer.parseInt(tag.substring(4));
                    // Existing tag = chunk reload or /summon with tag, not a fresh spawn.
                    freshSpawn = false;
                } catch (NumberFormatException e) {
                }
                break;
            }
        }

        if (currentLevel == 0) {
            currentLevel = calculateLevel(mob.getRandom());
            if (BossMobUtil.isBossMob(mob)) {
                currentLevel = BossMobUtil.getLevelForBossMob(currentLevel);
                // LOGGER.info("  BOSS MOB detected: {}, applying level limits: {}",
                //     mob.getType().getDescription().getString(), currentLevel);
            }
            mob.addTag("lvl:" + currentLevel);
            // LOGGER.info("  NEW SPAWN - assigned level: {}, tags after: {}",
            //     currentLevel, mob.getTags());
        }

        // Apply stats and name to all mobs with valid level (fresh spawn or /summon with tag)
        applyLevelStats(mob, currentLevel, freshSpawn);
        updateMobName(mob, currentLevel);
        // Migrate old-world mobs saved with always-visible labels back to vanilla look-at.
        if (mob.isCustomNameVisible()) {
            mob.setCustomNameVisible(false);
        }
    }

    @SubscribeEvent
    static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (Config.UNINSTALL_MODE.get()) return;
        Mob child = event.getChild();
        if (child == null) return;
        if (child.level().isClientSide()) return;

        int levelA = (event.getParentA() != null)
            ? DropsCalculator.getLevelFromTags(event.getParentA().getTags()) : 0;
        int levelB = (event.getParentB() != null)
            ? DropsCalculator.getLevelFromTags(event.getParentB().getTags()) : 0;

        int minBonus = Config.BREEDING_MUTATION_MIN_BONUS.get();
        int maxBonus = Config.BREEDING_MUTATION_MAX_BONUS.get();
        int bonus = minBonus + RANDOM.nextInt(Math.max(1, maxBonus - minBonus + 1));
        int childLevel = BreedingCalculator.calculateChildLevel(
            levelA, levelB, RANDOM.nextDouble(),
            Config.BREEDING_MUTATION_CHANCE.get(), bonus, Config.MAX_LEVEL.get());

        // Tag set here so onEntityJoinLevel respects it instead of rolling a random level
        child.addTag("lvl:" + childLevel);

        // LOGGER.info("onBabySpawn: parents {}+{} -> child level {}", levelA, levelB, childLevel);
    }

    @SubscribeEvent
    static void onDamageCalculation(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Mob attacker) {
            int level = getLevelFromEntity(attacker);
            if (level > 0) {
                float multiplier = DropsCalculator.getDamageMultiplier(level);
                event.setAmount(event.getAmount() * multiplier);
            }
        }
    }

    @SubscribeEvent
    static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        int level = getLevelFromEntity(mob);

        if (level > 0) {
            int originalXp = event.getDroppedExperience();
            int newXp = DropsCalculator.calculateExperienceDrop(originalXp, level);
            event.setDroppedExperience(newXp);

            // LOGGER.debug("onExperienceDrop: {} | Level: {} | XP: {} -> {}",
            //     mob.getType().getDescription().getString(), level, originalXp, newXp);
        }
    }

    @SubscribeEvent
    static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        int level = getLevelFromEntity(mob);

        // LOGGER.debug("onLivingDrops: {} | Level: {} | Drops: {}",
        //     mob.getType().getDescription().getString(), level, event.getDrops().size());

        if (level > 0) {
            if (level < DropsCalculator.VANILLA_LEVEL) {
                // Sub-vanilla: each item rolls a drop chance; if it drops, count = 1.
                double dropChance = DropsCalculator.getDropChance(level);
                java.util.Iterator<ItemEntity> it = event.getDrops().iterator();
                while (it.hasNext()) {
                    ItemEntity itemEntity = it.next();
                    if (RANDOM.nextFloat() >= dropChance) {
                        it.remove();
                    } else {
                        itemEntity.getItem().setCount(1);
                    }
                }
            } else {
                for (ItemEntity itemEntity : event.getDrops()) {
                    ItemStack stack = itemEntity.getItem();
                    int originalCount = stack.getCount();
                    int newCount = DropsCalculator.calculateDropCount(originalCount, level);

                    if (DropsCalculator.shouldIncreaseDrops(originalCount, newCount)) {
                        stack.setCount(newCount);
                        itemEntity.setPickUpDelay(10);
                    }
                }
            }

            // Level 150+ mobs have a 10% chance to drop the necklace on real death.
            // Tied to level, not the save tag, since the tag is consumed when the totem is used.
            if (level >= 150 && RANDOM.nextFloat() < 0.1f) {
                ItemStack necklaceDrop = new ItemStack(ModItems.TOTEM_NECKLACE.get());
                ItemEntity dropEntity = new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), necklaceDrop);
                event.getDrops().add(dropEntity);
            }
        }
    }

    @SubscribeEvent
    static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity entity = event.getEntity();

        ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack mainHand = entity.getMainHandItem();
        ItemStack offHand = entity.getOffhandItem();

        boolean hasTotemTag = entity.getTags().contains("HasTotemNecklace");
        boolean hasTotemItem = false;
        try {
            hasTotemItem = headItem.is(ModItems.TOTEM_NECKLACE.get()) ||
                    mainHand.is(ModItems.TOTEM_NECKLACE.get()) ||
                    offHand.is(ModItems.TOTEM_NECKLACE.get());
        } catch (NullPointerException e) {
            LOGGER.warn("TOTEM_NECKLACE not registered yet, skipping death check");
            return;
        }

        if (hasTotemTag || hasTotemItem) {
            event.setCanceled(true);

            if (hasTotemTag) {
                // Mob carries the totem as a tag (no visible item), consume it on save.
                entity.removeTag("HasTotemNecklace");
            } else if (headItem.is(ModItems.TOTEM_NECKLACE.get())) {
                entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            } else if (mainHand.is(ModItems.TOTEM_NECKLACE.get())) {
                entity.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            } else if (offHand.is(ModItems.TOTEM_NECKLACE.get())) {
                entity.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }

            entity.setHealth(entity.getMaxHealth());
            entity.removeAllEffects();

            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));

            ItemStack visualStack = new ItemStack(ModItems.TOTEM_NECKLACE.get());
            TotemAnimationPayload payload = new TotemAnimationPayload(entity.getId(), visualStack);

            if (entity instanceof ServerPlayer serverPlayer) {
                ModMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), payload);
            }

            ModMessages.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), payload);
        }
    }

    // Re-apply the [LvN] prefix only when a player renames a leveled mob with a name tag.
    // One deferred task for that single mob; no global per-tick scanning.
    @SubscribeEvent
    static void onNameTagUse(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getTarget() instanceof Mob mob)) return;
        if (!event.getItemStack().is(Items.NAME_TAG)) return;

        int level = getLevelFromEntity(mob);
        if (level <= 0) return;

        MinecraftServer server = mob.getServer();
        if (server != null) {
            // Defer one tick so vanilla applies the rename first, then we prepend the prefix.
            server.tell(new TickTask(server.getTickCount() + 1, () -> {
                if (mob.isAlive()) {
                    updateMobName(mob, level);
                }
            }));
        }
    }

    @SubscribeEvent
    static void onEntityTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        // Do nothing on the client: names are synced automatically, particles are server-driven.
        if (mob.level().isClientSide()) return;

        // Sun-proof for level 150 burners. isOnFire() is a cheap gate so nothing else runs
        // for the vast majority of mobs that aren't currently burning.
        if (mob.isOnFire() && (mob instanceof Zombie || mob instanceof AbstractSkeleton)
                && mob.level().isDay() && mob.level().canSeeSky(mob.blockPosition())
                && getLevelFromEntity(mob) >= 150) {
            mob.clearFire();
        }

        // Server-broadcast particles for level 150, every 10 ticks. No per-tick client work.
        if (mob.tickCount % 10 == 0 && mob.level() instanceof ServerLevel serverLevel
                && getLevelFromEntity(mob) >= 150) {
            serverLevel.sendParticles(PARTICLE,
                mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(),
                4, mob.getBbWidth() * 0.5, mob.getBbHeight() * 0.4, mob.getBbWidth() * 0.5, 0.01);
        }
    }

    private static int calculateLevel(net.minecraft.util.RandomSource random) {
        return LevelCalculator.rollSpawnLevel(
            random.nextDouble(),
            random.nextDouble(),
            Config.HIGH_LEVEL_CHANCE.get(),
            Config.LEVEL_RARITY_EXPONENT.get(),
            Config.MAX_LEVEL.get());
    }

    private static void applyLevelStats(Mob mob, int level, boolean freshSpawn) {
        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            // Recompute from the entity type's vanilla default, not the current base value:
            // this event also fires on chunk reload, and scaling the already-scaled base
            // would compound the multiplier on every reload.
            double vanillaBase = getVanillaMaxHealth(mob, maxHealth.getBaseValue());
            double newValue = vanillaBase * DropsCalculator.getStatMultiplier(level);
            maxHealth.setBaseValue(newValue);
            if (freshSpawn) {
                mob.setHealth((float) newValue);
            }
        }

        if (mob instanceof Creeper creeper) {
            double bonus = (level / 150.0) * 9.0;
            int newRadius = 3 + (int) bonus;
            if (newRadius > 12) newRadius = 12;

            try {
                Field field = Creeper.class.getDeclaredField("explosionRadius");
                field.setAccessible(true);
                field.setInt(creeper, newRadius);
            } catch (Exception e) {
                LOGGER.warn("Failed to modify Creeper explosion radius: {}", e.getMessage());
            }
        }

        if (level >= 150) {
            // Move 1.5x faster. Transient modifier so it doesn't compound across world reloads.
            AttributeInstance moveSpeed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (moveSpeed != null && moveSpeed.getModifier(SPEED_BOOST_UUID) == null) {
                moveSpeed.addTransientModifier(new AttributeModifier(
                    SPEED_BOOST_UUID, "moblevel_speed_1_5x", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

            // Aggressive toward players, regardless of mob type (cows included).
            if (mob instanceof PathfinderMob pathMob) {
                // Passive mobs flee via PanicGoal when hurt; remove it so they fight instead.
                List<PanicGoal> panicGoals = new ArrayList<>();
                for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
                    if (wrapped.getGoal() instanceof PanicGoal panic) {
                        panicGoals.add(panic);
                    }
                }
                panicGoals.forEach(mob.goalSelector::removeGoal);

                mob.targetSelector.addGoal(1, new HurtByTargetGoal(pathMob));
                mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, Player.class, true));
                mob.goalSelector.addGoal(2, new MeleeAttackGoal(pathMob, 1.2, false));
            }
        }

        // Grant the totem death-save as an invisible tag (not a head item, so nothing renders).
        // Fresh spawns only: re-granting on chunk reload would refill a consumed totem.
        if (level >= 150 && freshSpawn) {
            mob.addTag("HasTotemNecklace");
        }
    }

    // Wipes the mob's old level and rolls a fresh one with the current spawn curve.
    // Used by /moblevel restartLevels to fix worlds bloated by pre-1.2.1 levels.
    static void reassignLevel(Mob mob) {
        int oldLevel = DropsCalculator.getLevelFromTags(mob.getTags());
        stripModData(mob);
        int level = calculateLevel(mob.getRandom());
        if (BossMobUtil.isBossMob(mob)) {
            level = BossMobUtil.getLevelForBossMob(level);
        }
        // A re-roll can only lower a level: spamming the command deflates the world
        // instead of slot-machining until a high level lands.
        if (oldLevel > 0 && level > oldLevel) {
            level = oldLevel;
        }
        mob.addTag("lvl:" + level);
        applyLevelStats(mob, level, true);
        updateMobName(mob, level);
    }

    // Reverts everything MobLevel persisted on this entity back to vanilla.
    static void stripModData(Mob mob) {
        String lvlTag = null;
        for (String tag : mob.getTags()) {
            if (tag.startsWith("lvl:")) {
                lvlTag = tag;
                break;
            }
        }
        if (lvlTag != null) mob.removeTag(lvlTag);
        mob.removeTag("HasTotemNecklace");

        Component name = mob.getCustomName();
        if (name != null && name.getString().startsWith("[Lv")) {
            String raw = name.getString();
            int end = raw.indexOf("] ");
            String base = (end != -1) ? raw.substring(end + 2) : "";
            // Keep a player-given nametag name; drop the name entirely if it was only ours.
            if (base.isEmpty() || base.equals(mob.getType().getDescription().getString())) {
                mob.setCustomName(null);
            } else {
                mob.setCustomName(Component.literal(base));
            }
            mob.setCustomNameVisible(false);
        }

        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(getVanillaMaxHealth(mob, maxHealth.getBaseValue()));
            if (mob.getHealth() > mob.getMaxHealth()) {
                mob.setHealth(mob.getMaxHealth());
            }
        }

        if (mob instanceof Creeper creeper) {
            try {
                Field field = Creeper.class.getDeclaredField("explosionRadius");
                field.setAccessible(true);
                field.setInt(creeper, 3);
            } catch (Exception ignored) {
            }
        }
    }

    // Vanilla default max health for this entity type. Falls back to the current base
    // divided by the level multiplier if the registry lookup fails.
    private static double getVanillaMaxHealth(Mob mob, double fallbackBase) {
        try {
            var supplier = net.minecraft.world.entity.ai.attributes.DefaultAttributes
                .getSupplier((net.minecraft.world.entity.EntityType<? extends LivingEntity>) mob.getType());
            return supplier.getBaseValue(Attributes.MAX_HEALTH);
        } catch (Exception e) {
            return fallbackBase;
        }
    }

    private static void updateMobName(Mob mob, int level) {
        Component currentName = mob.getCustomName();
        String rawName = (currentName != null) ? currentName.getString() : mob.getType().getDescription().getString();

        String baseName = rawName;

        if (rawName.startsWith("[Lv")) {
            int endBracket = rawName.indexOf("] ");
            if (endBracket != -1) {
                baseName = rawName.substring(endBracket + 2);
            }
        }

        if (baseName.contains(" ♥")) {
            baseName = baseName.split(" ♥")[0];
        }

        ChatFormatting color = ChatFormatting.GREEN;
        if (level >= 50) color = ChatFormatting.AQUA;
        if (level >= 100) color = ChatFormatting.YELLOW;
        if (level >= 130) color = ChatFormatting.RED;
        if (level >= 150) color = ChatFormatting.DARK_PURPLE;

        String prefix = "[Lv" + level + "] ";
        Component newName = Component.literal(prefix).withStyle(color)
            .append(Component.literal(baseName).withStyle(ChatFormatting.WHITE));

        String currentString = (currentName != null) ? currentName.getString() : "";
        if (!currentString.equals(newName.getString())) {
            mob.setCustomName(newName);
        }
    }

    private static int getLevelFromEntity(LivingEntity entity) {
        return DropsCalculator.getLevelFromTags(entity.getTags());
    }
}
