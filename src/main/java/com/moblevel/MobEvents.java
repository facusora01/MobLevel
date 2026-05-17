package com.moblevel;

import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
        boolean isFreshSpawn = true;

        for (String tag : mob.getTags()) {
            if (tag.startsWith("lvl:")) {
                try {
                    currentLevel = Integer.parseInt(tag.substring(4));
                    isFreshSpawn = false;
                } catch (NumberFormatException e) {
                }
                break;
            }
        }

        if (currentLevel == 0) {
            currentLevel = calculateLevel(mob.getRandom());
            mob.addTag("lvl:" + currentLevel);
        }

        if (isFreshSpawn) {
            applyLevelStats(mob, currentLevel);
            updateMobName(mob, currentLevel);
        }
    }

    @SubscribeEvent
    static void onDamageCalculation(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Mob attacker) {
            int level = getLevelFromEntity(attacker);
            if (level > 0) {
                float damagePerLevel = 0.02f;
                float multiplier = 1.0f + (level * damagePerLevel);
                event.setAmount(event.getAmount() * multiplier);
            }
        }
    }

    @SubscribeEvent
    static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        int level = getLevelFromEntity(mob);
        if (level <= 1) return;

        double lootMultiplierPerLevel = 0.02;

        for (ItemEntity itemEntity : event.getDrops()) {
            ItemStack stack = itemEntity.getItem();
            int originalCount = stack.getCount();
            float multiplier = 1.0f + (level * (float) lootMultiplierPerLevel);

            if (level >= 150) {
                multiplier += 3.0f;
            }

            int newCount = Math.round(originalCount * multiplier);

            if (newCount > originalCount) {
                stack.setCount(newCount);
                itemEntity.setPickUpDelay(10);
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

    @SubscribeEvent
    static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity entity = event.getEntity();

        ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack mainHand = entity.getMainHandItem();
        ItemStack offHand = entity.getOffhandItem();

        boolean hasTotem = headItem.is(ModItems.TOTEM_NECKLACE.get()) ||
                mainHand.is(ModItems.TOTEM_NECKLACE.get()) ||
                offHand.is(ModItems.TOTEM_NECKLACE.get());

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
        double eliteChance = Config.ELITE_CHANCE.get();
        int maxLevel = Config.MAX_LEVEL.get();

        if (random.nextDouble() < eliteChance) {
            int minElite = 130;
            return random.nextInt((maxLevel - minElite) + 1) + minElite;
        }

        int normalMax = 129;
        double exponent = Config.LEVEL_RARITY_EXPONENT.get();
        double randomVal = random.nextDouble();
        double weightedVal = Math.pow(randomVal, exponent);
        return (int) (weightedVal * (normalMax - 1)) + 1;
    }

    private static void applyLevelStats(Mob mob, int level) {
        double healthPerLevel = 0.05;
        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            double baseValue = maxHealth.getBaseValue();
            double newValue = baseValue * (1.0 + (level * healthPerLevel));
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

        if (level >= 150) {
            ItemStack totemNecklaceStack = new ItemStack(ModItems.TOTEM_NECKLACE.get());
            mob.setItemSlot(EquipmentSlot.HEAD, totemNecklaceStack);
            mob.setDropChance(EquipmentSlot.HEAD, 0.0f);
            mob.addTag("HasTotemNecklace");
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
        for (String tag : entity.getTags()) {
            if (tag.startsWith("lvl:")) {
                try {
                    return Integer.parseInt(tag.substring(4));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }
}
