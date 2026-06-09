package com.moblevel;

import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = MobLevel.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (event.getLevel().isClientSide()) return;

        int currentLevel = 0;

        LOGGER.info("onEntityJoinLevel: {} (tags before: {})",
            mob.getType().getDescription().getString(), mob.getTags());

        for (String tag : mob.getTags()) {
            if (tag.startsWith("lvl:")) {
                try {
                    currentLevel = Integer.parseInt(tag.substring(4));
                    LOGGER.info("  Found existing level tag: {}", currentLevel);
                } catch (NumberFormatException e) {
                }
                break;
            }
        }

        if (currentLevel == 0) {
            currentLevel = calculateLevel(mob.getRandom());
            if (BossMobUtil.isBossMob(mob)) {
                currentLevel = BossMobUtil.getLevelForBossMob(currentLevel);
                LOGGER.info("  BOSS MOB detected: {}, applying level limits: {}",
                    mob.getType().getDescription().getString(), currentLevel);
            }
            mob.addTag("lvl:" + currentLevel);
            LOGGER.info("  NEW SPAWN - assigned level: {}, tags after: {}",
                currentLevel, mob.getTags());
        }

        // Apply stats and name to all mobs with valid level (fresh spawn or /summon with tag)
        applyLevelStats(mob, currentLevel);
        updateMobName(mob, currentLevel);
    }

    @SubscribeEvent
    static void onBabySpawn(BabyEntitySpawnEvent event) {
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

        LOGGER.info("onBabySpawn: parents {}+{} -> child level {}", levelA, levelB, childLevel);
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

            LOGGER.debug("onExperienceDrop: {} | Level: {} | XP: {} -> {}",
                mob.getType().getDescription().getString(), level, originalXp, newXp);
        }
    }

    @SubscribeEvent
    static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        int level = getLevelFromEntity(mob);

        LOGGER.debug("onLivingDrops: {} | Level: {} | Drops: {}",
            mob.getType().getDescription().getString(), level, event.getDrops().size());

        // Only modify drops if level is found and > 0
        if (level > 0) {
            if (level < DropsCalculator.VANILLA_LEVEL) {
                // Sub-vanilla: drops escasos. Cada item tiene chance de caer; si cae, count = 1.
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

            if (mob.getTags().contains("HasTotemNecklace")) {
                if (RANDOM.nextFloat() < 0.1f) {
                    ItemStack necklaceDrop = new ItemStack(ModItems.TOTEM_NECKLACE.get());
                    ItemEntity dropEntity = new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), necklaceDrop);
                    event.getDrops().add(dropEntity);
                }
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

        boolean hasTotem = false;
        try {
            hasTotem = headItem.is(ModItems.TOTEM_NECKLACE.get()) ||
                    mainHand.is(ModItems.TOTEM_NECKLACE.get()) ||
                    offHand.is(ModItems.TOTEM_NECKLACE.get());
        } catch (NullPointerException e) {
            LOGGER.warn("TOTEM_NECKLACE not registered yet, skipping death check");
            return;
        }

        if (hasTotem) {
            event.setCanceled(true);

            if (headItem.is(ModItems.TOTEM_NECKLACE.get())) {
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

    @SubscribeEvent
    static void onEntityTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        if (!mob.level().isClientSide()) {
            if (mob.tickCount % 20 == 0) {
                int level = getLevelFromEntity(mob);
                if (level > 0) {
                    updateMobName(mob, level);
                }
            }
            return;
        }

        Component name = mob.getCustomName();
        if (name == null) return;

        String nameStr = name.getString();
        if (!nameStr.contains("[Lv")) return;

        int visualLevel = 0;
        try {
            int start = nameStr.indexOf("[Lv") + 3;
            int end = nameStr.indexOf("]");

            if (end > start) {
                String numStr = nameStr.substring(start, end).trim();
                visualLevel = Integer.parseInt(numStr);
            }
        } catch (Exception e) {
            return;
        }

        if (visualLevel < 150) return;

        if (RANDOM.nextFloat() > 0.5f) return;

        double spreadXZ = mob.getBbWidth() * 1.5;
        double spreadY = mob.getBbHeight() * 1.2;
        double x = mob.getX() + (RANDOM.nextDouble() - 0.5) * spreadXZ;
        double y = mob.getY() + (RANDOM.nextDouble() * spreadY);
        double z = mob.getZ() + (RANDOM.nextDouble() - 0.5) * spreadXZ;

        double speed = 0.05;
        double vx = (RANDOM.nextDouble() - 0.5) * speed;
        double vy = (RANDOM.nextDouble() - 0.5) * speed;
        double vz = (RANDOM.nextDouble() - 0.5) * speed;

        DustParticleOptions particle = new DustParticleOptions(new org.joml.Vector3f(0.6f, 0.0f, 1.0f), 0.7f);
        mob.level().addParticle(particle, x, y, z, vx, vy, vz);
    }

    private static int calculateLevel(net.minecraft.util.RandomSource random) {
        return LevelCalculator.rollSpawnLevel(
            random.nextDouble(),
            random.nextDouble(),
            Config.HIGH_LEVEL_CHANCE.get(),
            Config.LEVEL_RARITY_EXPONENT.get(),
            Config.MAX_LEVEL.get());
    }

    private static void applyLevelStats(Mob mob, int level) {
        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            double baseValue = maxHealth.getBaseValue();
            double newValue = baseValue * DropsCalculator.getStatMultiplier(level);
            maxHealth.setBaseValue(newValue);
            mob.setHealth((float) newValue);
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
                System.out.println("Error al modificar Creeper: " + e.getMessage());
            }
        }

        // Only equip totem necklace on hostile mobs that can wear equipment
        try {
            Item totemItem = ModItems.getTotemNecklace();
            if (level >= 150 && mob.canHoldItem(new ItemStack(totemItem))) {
                ItemStack totemNecklaceStack = new ItemStack(totemItem);
                mob.setItemSlot(EquipmentSlot.HEAD, totemNecklaceStack);
                mob.setDropChance(EquipmentSlot.HEAD, 0.0f);
                mob.addTag("HasTotemNecklace");
            }
        } catch (Exception e) {
            LOGGER.debug("Could not equip totem necklace: {}", e.getMessage());
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
            mob.setCustomNameVisible(true);
        }
    }

    private static int getLevelFromEntity(LivingEntity entity) {
        int level = DropsCalculator.getLevelFromTags(entity.getTags());
        if (level == 0) {
            LOGGER.debug("getLevelFromEntity: {} HAS NO LEVEL TAG (tags: {})",
                entity.getType().getDescription().getString(), entity.getTags());
        } else {
            LOGGER.debug("getLevelFromEntity: {} -> level {}", entity.getType().getDescription().getString(), level);
        }
        return level;
    }
}
