package com.moblevel;

import java.lang.reflect.Field; // <--- Necesario para el truco de la llave maestra
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Random;

@EventBusSubscriber(modid = MobLevel.MODID)
public class MobEvents {

    private static final Random RANDOM = new Random();

    // ----------------------------------------------------------------
    // 1. AL SPAWNEAR (Nivel, Vida, Daño y Nombre)
    // ----------------------------------------------------------------
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        int currentLevel = mob.getData(MobLevel.MOB_LEVEL_DATA);
        boolean isFreshSpawn = (currentLevel == 0);

        // TRUCO: DETECTAR COMANDO /summon ... {Tags:["lvl:XX"]}
        if (isFreshSpawn) {
            for (String tag : mob.getTags()) {
                if (tag.startsWith("lvl:")) {
                    try {
                        String numero = tag.substring(4);
                        currentLevel = Integer.parseInt(numero);
                        mob.setData(MobLevel.MOB_LEVEL_DATA, currentLevel);
                        mob.removeTag(tag);
                        break;
                    } catch (NumberFormatException e) {
                        // Ignorar formato incorrecto
                    }
                }
            }
        }

        if (currentLevel == 0) {
            currentLevel = calculateLevel();
            mob.setData(MobLevel.MOB_LEVEL_DATA, currentLevel);
        }

        if (isFreshSpawn) {
            applyLevelStats(mob, currentLevel);
            updateMobName(mob, currentLevel);
        }
    }

    // ----------------------------------------------------------------
    // 2. ACTUALIZAR VIDA AL RECIBIR DAÑO
    // ----------------------------------------------------------------
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getEntity() instanceof Mob mob) {
            int level = mob.getData(MobLevel.MOB_LEVEL_DATA);
            if (level > 0) {
                float currentHealth = mob.getHealth();
                float damageTaken = event.getNewDamage();
                float healthRemaining = Math.max(0, currentHealth - damageTaken);

                updateMobName(mob, level, healthRemaining);
            }
        }
    }

    // ----------------------------------------------------------------
    // 3. CONTROL DE DAÑO REAL (Esqueletos, Magia, Creeper)
    // ----------------------------------------------------------------
    @SubscribeEvent
    public static void onDamageCalculation(LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof Mob attacker) {
            int level = attacker.getData(MobLevel.MOB_LEVEL_DATA);
            if (level > 0) {
                float damagePerLevel = 0.02f; // 2% por nivel
                float originalDamage = event.getOriginalDamage();
                float multiplier = 1.0f + (level * damagePerLevel);
                event.setNewDamage(originalDamage * multiplier);
            }
        }
    }

    // ----------------------------------------------------------------
    // 4. AL MORIR (Multiplicar Loot)
    // ----------------------------------------------------------------
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        int level = mob.getData(MobLevel.MOB_LEVEL_DATA);
        if (level <= 1) return;

        double lootMultiplierPerLevel = 0.01;

        for (ItemEntity itemEntity : event.getDrops()) {
            ItemStack stack = itemEntity.getItem();
            int originalCount = stack.getCount();
            float multiplier = 1.0f + (level * (float) lootMultiplierPerLevel);
            int newCount = Math.round(originalCount * multiplier);

            if (newCount > originalCount) {
                stack.setCount(newCount);
                itemEntity.setPickUpDelay(10);
            }
        }
    }

    // ----------------------------------------------------------------
    // 5. EFECTO VISUAL (Aura Dorada)
    // ----------------------------------------------------------------
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        Component name = mob.getCustomName();
        if (name != null && name.getString().contains("Lv1") && name.getString().contains("§6")) {
            if (RANDOM.nextInt(10) == 0) {
                double x = mob.getX() + (RANDOM.nextDouble() - 0.5) * mob.getBbWidth();
                double y = mob.getY() + RANDOM.nextDouble() * mob.getBbHeight();
                double z = mob.getZ() + (RANDOM.nextDouble() - 0.5) * mob.getBbWidth();
                mob.level().addParticle(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 0, 0.05, 0);
            }
        }
    }

    // ----------------------------------------------------------------
    // MÉTODOS AUXILIARES
    // ----------------------------------------------------------------

    private static int calculateLevel() {
        double eliteChance = Config.ELITE_CHANCE.get();
        int maxLevel = Config.MAX_LEVEL.get();

        if (RANDOM.nextDouble() < eliteChance) {
            int minElite = 130;
            return RANDOM.nextInt((maxLevel - minElite) + 1) + minElite;
        }

        int normalMax = 129;
        double exponent = Config.LEVEL_RARITY_EXPONENT.get();
        double randomVal = RANDOM.nextDouble();
        double weightedVal = Math.pow(randomVal, exponent);
        return (int) (weightedVal * (normalMax - 1)) + 1;
    }

    // --- AQUÍ ESTABA TU ERROR: AHORA SOLO HAY UN MÉTODO applyLevelStats ---

    // Este debe ser el ÚNICO método applyLevelStats en el archivo
    private static void applyLevelStats(Mob mob, int level) {
        // 1. VIDA (Igual que antes)
        double healthPerLevel = 0.05;

        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            double baseValue = maxHealth.getBaseValue();
            double newValue = baseValue * (1.0 + (level * healthPerLevel));

            maxHealth.setBaseValue(newValue);
            mob.setHealth((float) newValue);
        }

        // 2. CREEPERS (Versión "Llave Maestra" - Reflexión)
        if (mob instanceof Creeper creeper) {
            double bonus = (level / 150.0) * 9.0;
            int newRadius = 3 + (int) bonus;
            if (newRadius > 12) newRadius = 12;

            try {
                // Buscamos la variable privada "explosionRadius" dentro del código del Creeper
                // Nota: "explosionRadius" es el nombre en entorno de desarrollo.
                Field field = Creeper.class.getDeclaredField("explosionRadius");

                // Quitamos el candado (private)
                field.setAccessible(true);

                // Inyectamos el nuevo valor
                field.setInt(creeper, newRadius);

            } catch (Exception e) {
                // Si falla (no debería), imprimimos el error en la consola pero no crasheamos el juego
                System.out.println("Error al modificar Creeper: " + e.getMessage());
            }
        }
    }

    // --- MÉTODOS DE NOMBRE (Versiones corta y larga) ---

    private static void updateMobName(Mob mob, int level) {
        updateMobName(mob, level, mob.getHealth());
    }

    private static void updateMobName(Mob mob, int level, float currentHealthVal) {
        Component originalName = mob.getType().getDescription();

        ChatFormatting color = ChatFormatting.GREEN;
        if (level >= 50) color = ChatFormatting.YELLOW;
        if (level >= 100) color = ChatFormatting.RED;
        if (level >= 130) color = ChatFormatting.GOLD;

        String prefix = "[" + "Lv" + level + "] ";

        int currentHp = (int) Math.ceil(currentHealthVal);
        int maxHp = (int) mob.getMaxHealth();

        Component fullName = Component.literal(prefix).withStyle(color)
                .append(originalName.copy().withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" ❤ " + currentHp + "/" + maxHp).withStyle(ChatFormatting.RED));

        mob.setCustomName(fullName);
        mob.setCustomNameVisible(true);
    }
}