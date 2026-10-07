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
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
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
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
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
    // Level health and mount speed/jump scaling, permanent modifiers saved with the mob.
    private static final UUID HEALTH_UUID = UUID.fromString("5f0c2a8e-6b1d-4c7a-9e3f-1a2b3c4d5e60");
    private static final UUID MOUNT_SPEED_UUID = UUID.fromString("5f0c2a8e-6b1d-4c7a-9e3f-1a2b3c4d5e61");
    private static final UUID MOUNT_JUMP_UUID = UUID.fromString("5f0c2a8e-6b1d-4c7a-9e3f-1a2b3c4d5e62");
    // Taming a level 150 mount succeeds this many times less often than vanilla.
    static final int LEVEL_150_TAME_DIFFICULTY = 5;
    private static final UUID SPEED_BOOST_UUID = UUID.fromString("b7a1f3c2-0d4e-4a8b-9c6d-2e1f5a3b7c90");
    // Version marker: mobs leveled by 1.2.2+ carry this tag and are never migrated.
    static final String VERSION_TAG = "ml2";
    // Per-world scoreboard objective that turns the one-time level migration on.
    static final String MIGRATION_MARKER = "ml_restart_done";
    // Set by onBabySpawn on a child that already carries its level tag, so onEntityJoinLevel
    // still treats it as a fresh spawn and heals it up to its scaled maximum. Consumed on join,
    // so it never survives into the save file.
    static final String NEWBORN_TAG = "ml_newborn";

    // SRG name (f_32272_ = explosionRadius); resolved once, works in dev and in the
    // reobfuscated production jar where the mojmap name does not exist.
    private static final Field CREEPER_EXPLOSION_RADIUS = findCreeperRadiusField();

    private static Field findCreeperRadiusField() {
        try {
            return ObfuscationReflectionHelper.findField(Creeper.class, "f_32272_");
        } catch (Throwable t) {
            LOGGER.warn("Creeper explosionRadius field not found; creeper scaling disabled: {}", t.getMessage());
            return null;
        }
    }
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

        // A mob bred this tick already carries its level tag but has vanilla health,
        // so it needs the fresh-spawn treatment to be healed to its scaled maximum.
        if (mob.getTags().contains(NEWBORN_TAG)) {
            mob.removeTag(NEWBORN_TAG);
            // Bred by this version, so the one-time migration must never re-roll it.
            mob.addTag(VERSION_TAG);
            freshSpawn = true;
        }

        if (currentLevel == 0) {
            currentLevel = bredVillagerLevel(mob);
            if (currentLevel == 0) {
                currentLevel = calculateLevel(mob);
            }
            if (BossMobUtil.isBossMob(mob)) {
                currentLevel = BossMobUtil.getLevelForBossMob(currentLevel);
                // LOGGER.info("  BOSS MOB detected: {}, applying level limits: {}",
                //     mob.getType().getDescription().getString(), currentLevel);
            }
            mob.addTag("lvl:" + currentLevel);
            mob.addTag(VERSION_TAG);
        } else if (!mob.getTags().contains(VERSION_TAG) && isMigrationEnabled(mob)) {
            // Pre-1.2.2 mob and /moblevel restartLevels was run: re-roll it once
            // (downgrade-only) as its chunk loads. reassignLevel adds the version tag.
            reassignLevel(mob);
            return;
        }

        // Apply stats to all mobs with valid level (fresh spawn or /summon with tag).
        applyLevelStats(mob, currentLevel, freshSpawn);
        // Levels used to be baked into CustomName; strip any legacy label so vanilla
        // naming, persistence and despawn rules apply again. The label is now drawn
        // client-side from data synced in onStartTracking.
        stripLevelLabel(mob);
    }

    // Sends the mob's level to a player the moment their client starts tracking it.
    // Fresh spawns are covered too: tracking always starts after EntityJoinLevelEvent.
    @SubscribeEvent
    static void onStartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof Mob mob)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        int level = getLevelFromEntity(mob);
        if (level <= 0) return;

        ModMessages.INSTANCE.send(
            new LevelSyncPayload(mob.getId(), level),
            PacketDistributor.PLAYER.with(player));
    }

    @SubscribeEvent
    static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (Config.UNINSTALL_MODE.get()) return;
        Mob child = event.getChild();
        if (child == null) return;
        if (child.level().isClientSide()) return;

        int childLevel = childLevel(event.getParentA(), event.getParentB());

        // Tag set here so onEntityJoinLevel respects it instead of rolling a random level
        child.addTag("lvl:" + childLevel);
        child.addTag(NEWBORN_TAG);

        // LOGGER.info("onBabySpawn: parents {}+{} -> child level {}", levelA, levelB, childLevel);
    }

    // A baby's level from its parents' levels plus the breeding mutation roll.
    private static int childLevel(Mob parentA, Mob parentB) {
        int levelA = parentA != null ? getLevelFromEntity(parentA) : 0;
        int levelB = parentB != null ? getLevelFromEntity(parentB) : 0;
        int minBonus = Config.BREEDING_MUTATION_MIN_BONUS.get();
        int maxBonus = Config.BREEDING_MUTATION_MAX_BONUS.get();
        int bonus = minBonus + RANDOM.nextInt(Math.max(1, maxBonus - minBonus + 1));
        return BreedingCalculator.calculateChildLevel(
            levelA, levelB, RANDOM.nextDouble(),
            Config.BREEDING_MUTATION_CHANCE.get(), bonus, Config.MAX_LEVEL.get());
    }

    // Villagers breed through VillagerMakeLove, which fires no BabyEntitySpawnEvent. It sets both
    // parents' age to exactly 6000 and the child's to -24000, puts the child on the first parent
    // and adds it to the world in the same call, so on join the parents are the adult villagers
    // next to it whose age is still exactly 6000. Returns 0 when this isn't such a baby.
    private static int bredVillagerLevel(Mob mob) {
        if (!(mob instanceof Villager child) || child.getAge() != -24000) return 0;
        List<Villager> parents = child.level().getEntitiesOfClass(Villager.class,
            child.getBoundingBox().inflate(5), v -> v != child && v.getAge() == 6000);
        return parents.size() >= 2 ? childLevel(parents.get(0), parents.get(1)) : 0;
    }

    // Vanilla 1.20.4 doesn't copy entity tags when a mob converts, so a cured zombie villager
    // (or a villager turned zombie) would roll a new level. Give the new mob the old one's
    // level, totem and saved trade numbers instead.
    @SubscribeEvent
    static void onConversion(LivingConversionEvent.Post event) {
        if (Config.UNINSTALL_MODE.get()) return;
        if (!(event.getEntity() instanceof Mob source) || !(event.getOutcome() instanceof Mob outcome)) return;
        int level = getLevelFromEntity(source);
        if (level <= 0) return;
        stripModData(outcome);
        for (String tag : source.getTags()) {
            if (tag.startsWith("lvl:") || tag.equals(VERSION_TAG) || TradeCalculator.offerIndex(tag) >= 0) {
                outcome.addTag(tag);
            }
        }
        applyLevelStats(outcome, level, true);
        if (!source.getTags().contains("HasTotemNecklace")) outcome.removeTag("HasTotemNecklace");
        ModMessages.INSTANCE.send(
            new LevelSyncPayload(outcome.getId(), level),
            PacketDistributor.TRACKING_ENTITY.with(outcome));
    }

    // Approach without mixins: Forge has no per-merchant trade event, but every trade a player
    // can see is shown through a right click, and this event fires before the merchant opens
    // its screen. So improve every offer not improved yet (new merchants, and trades unlocked
    // by a level-up since the last visit), then show the level in the trading screen title.
    @SubscribeEvent
    static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof AbstractVillager merchant) || merchant.level().isClientSide()) return;
        if (openingTrade) return;
        int level = getLevelFromEntity(merchant);
        if (level <= 0) return;
        improveOffers(merchant);

        // The screen takes its title from the merchant's name, so lend it "[LvN] <name>" for the
        // length of this click: run the whole vanilla interaction now (re-entering this event,
        // which then lets it through) and put the name back before anything is synced or saved.
        // If the click itself renamed the merchant (a name tag), that new name stays.
        Component name = merchant.getCustomName();
        Component titled = levelPrefix(level).append(merchant.getName());
        merchant.setCustomName(titled);
        openingTrade = true;
        InteractionResult result;
        try {
            result = event.getEntity().interactOn(merchant, event.getHand());
        } finally {
            openingTrade = false;
            if (merchant.getCustomName() == titled) merchant.setCustomName(name);
        }
        event.setCancellationResult(result);
        event.setCanceled(true);
    }

    private static boolean openingTrade;

    @SubscribeEvent
    static void onTame(net.minecraftforge.event.entity.living.AnimalTameEvent event) {
        if (vetoTame(event.getAnimal())) event.setCanceled(true);
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
                ModMessages.INSTANCE.send(payload, PacketDistributor.PLAYER.with(serverPlayer));
            }

            ModMessages.INSTANCE.send(payload, PacketDistributor.TRACKING_ENTITY.with(entity));
        }
    }

    @SubscribeEvent
    static void onEntityTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        // Do nothing on the client: names are synced automatically, particles are server-driven.
        if (mob.level().isClientSide()) return;

        // Sun-proof for level 150 burners, checked every tick (fire re-ignites each tick at
        // dawn, so throttling would let damage land). Gates ordered cheapest and most
        // selective first: the 1-in-thousands level check runs before the heightmap lookup
        // in canSeeSky, so a horde of ordinary burning zombies costs almost nothing extra.
        if (mob.isOnFire() && (mob instanceof Zombie || mob instanceof AbstractSkeleton)
                && mob.level().isDay()
                && getLevelFromEntity(mob) >= 150
                && mob.level().canSeeSky(mob.blockPosition())) {
            mob.clearFire();
        }

        // Just tamed: drop the player it was chasing right away instead of on the next search.
        if (isTamedMount(mob) && mob.getTarget() instanceof Player) {
            mob.setTarget(null);
        }

        if (mob.tickCount % 10 != 0) return;

        int level = getLevelFromEntity(mob);
        if (level <= 0) return;

        // Server-broadcast particles for level 150. No per-tick client work.
        if (level >= 150 && mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(PARTICLE,
                mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(),
                4, mob.getBbWidth() * 0.5, mob.getBbHeight() * 0.4, mob.getBbWidth() * 0.5, 0.01);
        }

        // No despawn workaround needed anymore: the level label no longer touches
        // CustomName, so vanilla persistence and despawn rules apply untouched.
    }

    // Hostiles roll on their own, slightly more generous curve. They despawn and are replaced
    // constantly, so their high-level population stays bounded by whatever is near the player,
    // while passives never despawn and would otherwise accumulate forever.
    private static boolean isHostile(Mob mob) {
        return mob.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER;
    }

    private static int calculateLevel(Mob mob) {
        boolean hostile = isHostile(mob);
        return LevelCalculator.rollSpawnLevel(
            mob.getRandom().nextDouble(),
            mob.getRandom().nextDouble(),
            hostile ? Config.HOSTILE_HIGH_LEVEL_CHANCE.get() : Config.HIGH_LEVEL_CHANCE.get(),
            hostile ? Config.HOSTILE_LEVEL_RARITY_EXPONENT.get() : Config.LEVEL_RARITY_EXPONENT.get(),
            Config.COMMON_LEVEL_SKEW.get(),
            Config.MAX_LEVEL.get());
    }

    private static void applyLevelStats(Mob mob, int level, boolean freshSpawn) {
        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            if (!freshSpawn && maxHealth.getModifier(HEALTH_UUID) == null) {
                // Saved by a version that scaled the base value itself: put the type default
                // back first, so the multiplier below is not applied on top of the old one.
                maxHealth.setBaseValue(getVanillaMaxHealth(mob, maxHealth.getBaseValue()));
            }
            rollUnrolledMount(mob);
            // A modifier, not a new base value: it keeps each mob's own base health (horses roll
            // theirs at spawn) and is replaced, never stacked, when this runs again on reload.
            double base = maxHealth.getBaseValue();
            double target = mob instanceof AbstractHorse
                ? Math.min(base * DropsCalculator.getMountHealthMultiplier(level), DropsCalculator.MOUNT_MAX_HEALTH)
                : base * DropsCalculator.getStatMultiplier(level);
            setModifier(maxHealth, HEALTH_UUID, "moblevel_level_health", target / base - 1.0);
            if (freshSpawn) {
                mob.setHealth(mob.getMaxHealth());
            }
        }

        if (mob instanceof AbstractHorse) {
            // Mounts: vanilla genetics roll and inherit the base values, the level scales them.
            // Low levels make worse mounts, high levels better ones, and a horse can still be
            // fast but weak, or a good jumper but slow, exactly as its genes say.
            double mult = DropsCalculator.getMountMultiplier(level);
            AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                // Low levels slow a mount down, but never below the slowest vanilla horse,
                // unless it is that slow by nature.
                double base = speed.getBaseValue();
                double target = Math.max(base * mult, Math.min(base, DropsCalculator.MOUNT_MIN_SPEED));
                setModifier(speed, MOUNT_SPEED_UUID, "moblevel_level_speed", target / base - 1.0);
            }
            AttributeInstance jump = mob.getAttribute(Attributes.JUMP_STRENGTH);
            if (jump != null) setModifier(jump, MOUNT_JUMP_UUID, "moblevel_level_jump", mult - 1.0);
        }

        if (mob instanceof Creeper creeper && CREEPER_EXPLOSION_RADIUS != null) {
            double bonus = (level / 150.0) * 9.0;
            int newRadius = 3 + (int) bonus;
            if (newRadius > 12) newRadius = 12;

            try {
                CREEPER_EXPLOSION_RADIUS.setInt(creeper, newRadius);
            } catch (Exception e) {
                LOGGER.warn("Failed to modify Creeper explosion radius: {}", e.getMessage());
            }
        }

        if (level >= 150) {
            // Move 1.5x faster. Transient modifier so it doesn't compound across world reloads.
            // Mounts skip it: their level curve above already covers speed.
            AttributeInstance moveSpeed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (moveSpeed != null && !(mob instanceof AbstractHorse) && moveSpeed.getModifier(SPEED_BOOST_UUID) == null) {
                moveSpeed.addTransientModifier(new AttributeModifier(
                    SPEED_BOOST_UUID, "moblevel_speed_1_5x", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

            // Aggressive toward players, regardless of mob type (cows included). Villagers and
            // wandering traders are left out: one that attacks you can't be traded with.
            if (mob instanceof PathfinderMob pathMob && !(mob instanceof AbstractVillager)) {
                // Passive mobs flee via PanicGoal when hurt; remove it so they fight instead.
                List<PanicGoal> panicGoals = new ArrayList<>();
                for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
                    if (wrapped.getGoal() instanceof PanicGoal panic) {
                        panicGoals.add(panic);
                    }
                }
                panicGoals.forEach(mob.goalSelector::removeGoal);

                // A tamed mount stays on its player's side: it no longer hunts players and never
                // turns on one that hits it, but still fights back against mobs. A wild one keeps
                // attacking even the player riding it, which is what makes taming it hard.
                mob.targetSelector.addGoal(1, new HurtByTargetGoal(pathMob) {
                    @Override
                    public boolean canUse() {
                        return super.canUse() && !(isTamedMount(mob) && mob.getLastHurtByMob() instanceof Player);
                    }
                });
                mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, Player.class, true,
                    target -> !isTamedMount(mob)));
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
        int level = calculateLevel(mob);
        if (BossMobUtil.isBossMob(mob)) {
            level = BossMobUtil.getLevelForBossMob(level);
        }
        // A re-roll can only lower a level: spamming the command deflates the world
        // instead of slot-machining until a high level lands.
        if (oldLevel > 0 && level > oldLevel) {
            level = oldLevel;
        }
        mob.addTag("lvl:" + level);
        mob.addTag(VERSION_TAG);
        applyLevelStats(mob, level, true);
        // stripModData put the trades back to vanilla; scale them again for the new level.
        if (mob instanceof AbstractVillager merchant) {
            improveOffers(merchant);
        }
        // Clients tracking this mob already cached the old level; push the new one.
        ModMessages.INSTANCE.send(
            new LevelSyncPayload(mob.getId(), level),
            PacketDistributor.TRACKING_ENTITY.with(mob));
    }

    private static boolean isMigrationEnabled(Mob mob) {
        return mob.getServer() != null
            && mob.getServer().getScoreboard().getObjective(MIGRATION_MARKER) != null;
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
        mob.removeTag(VERSION_TAG);
        mob.removeTag(NEWBORN_TAG);
        if (mob instanceof AbstractVillager merchant) {
            restoreOffers(merchant);
        }

        stripLevelLabel(mob);

        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            if (maxHealth.getModifier(HEALTH_UUID) != null) {
                maxHealth.removeModifier(HEALTH_UUID);
            } else if (lvlTag != null) {
                // Leveled by a version that scaled the base value itself.
                maxHealth.setBaseValue(getVanillaMaxHealth(mob, maxHealth.getBaseValue()));
            }
            if (mob.getHealth() > mob.getMaxHealth()) {
                mob.setHealth(mob.getMaxHealth());
            }
        }

        AttributeInstance moveSpeed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeed != null) {
            moveSpeed.removeModifier(SPEED_BOOST_UUID);
            moveSpeed.removeModifier(MOUNT_SPEED_UUID);
        }
        AttributeInstance jump = mob.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump != null) {
            jump.removeModifier(MOUNT_JUMP_UUID);
        }

        if (mob instanceof Creeper creeper && CREEPER_EXPLOSION_RADIUS != null) {
            try {
                CREEPER_EXPLOSION_RADIUS.setInt(creeper, 3);
            } catch (Exception ignored) {
            }
        }
    }

    // Replaces one of our permanent modifiers (this version has no addOrReplacePermanentModifier).
    private static void setModifier(AttributeInstance attribute, UUID id, String name, double amount) {
        attribute.removeModifier(id);
        attribute.addPermanentModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    // /summon with NBT (tags included) skips vanilla's spawn setup, so a horse, donkey, mule or
    // llama keeps its type's placeholder stats (53 health) instead of rolled genes. Roll them
    // now with vanilla's own formulas, as a natural spawn would have.
    private static void rollUnrolledMount(Mob mob) {
        if (!(mob instanceof Horse || mob instanceof AbstractChestedHorse)
                || mob.getAttributeBaseValue(Attributes.MAX_HEALTH) != getVanillaMaxHealth(mob, -1)) {
            return;
        }
        var random = mob.getRandom();
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(15.0 + random.nextInt(8) + random.nextInt(9));
        if (mob instanceof Horse) {
            mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
                (0.45 + random.nextDouble() * 0.3 + random.nextDouble() * 0.3 + random.nextDouble() * 0.3) * 0.25);
            mob.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(
                0.4 + random.nextDouble() * 0.2 + random.nextDouble() * 0.2 + random.nextDouble() * 0.2);
        }
    }

    // How many times harder than vanilla this mob is to tame: level 150 mounts are wild.
    public static int tameDifficulty(Mob mob) {
        return mob instanceof AbstractHorse && getLevelFromEntity(mob) >= 150 ? LEVEL_150_TAME_DIFFICULTY : 1;
    }

    // Called with a taming vanilla already allowed (AnimalTameEvent): true to refuse it,
    // so only 1 in tameDifficulty of vanilla's successes go through.
    public static boolean vetoTame(Mob mob) {
        int difficulty = tameDifficulty(mob);
        return difficulty > 1 && mob.getRandom().nextInt(difficulty) != 0;
    }

    private static boolean isTamedMount(Mob mob) {
        return mob instanceof AbstractHorse horse && horse.isTamed();
    }

    // Vanilla default max health for this entity type, used only to undo the base-value
    // scaling of older versions and to spot unrolled mounts. Falls back to the given value.
    private static double getVanillaMaxHealth(Mob mob, double fallbackBase) {
        try {
            var supplier = net.minecraft.world.entity.ai.attributes.DefaultAttributes
                .getSupplier((net.minecraft.world.entity.EntityType<? extends LivingEntity>) mob.getType());
            return supplier.getBaseValue(Attributes.MAX_HEALTH);
        } catch (Exception e) {
            return fallbackBase;
        }
    }

    // Removes a legacy "[LvN] " CustomName label from worlds saved by versions
    // that baked the level into the entity name. Keeps a player-given name; drops
    // the name entirely (and its unwanted persistence) when it was only ours.
    // Also matches server-resolved type names and leaked raw translation keys.
    static void stripLevelLabel(Mob mob) {
        Component name = mob.getCustomName();
        if (name == null || !name.getString().startsWith("[Lv")) return;

        String raw = name.getString();
        int end = raw.indexOf("] ");
        String base = (end != -1) ? raw.substring(end + 2) : "";

        if (base.isEmpty()
                || base.equals(mob.getType().getDescription().getString())
                || base.equals(mob.getType().getDescriptionId())) {
            mob.setCustomName(null);
        } else {
            mob.setCustomName(Component.literal(base));
        }
        mob.setCustomNameVisible(false);
    }

    // Rebuilds the offers of a villager or wandering trader so they reflect its level: cheaper,
    // bigger, longer-lasting and better enchanted the higher it is, worse below level 20.
    // Each improved offer keeps its vanilla numbers in an ml_offer tag on the merchant; an offer
    // that already has one was improved before and is never scaled twice.
    public static void improveOffers(AbstractVillager merchant) {
        int level = getLevelFromEntity(merchant);
        if (level <= 0) return;
        MerchantOffers offers = merchant.getOffers();
        java.util.Set<Integer> done = new java.util.HashSet<>();
        for (String tag : merchant.getTags()) done.add(TradeCalculator.offerIndex(tag));
        for (int i = 0; i < offers.size(); i++) {
            if (done.contains(i)) continue;
            MerchantOffer original = offers.get(i);
            merchant.addTag(TradeCalculator.encodeOffer(i, vanillaNumbers(original), vanillaEnchantments(original)));
            offers.set(i, improveOffer(original, level, merchant.getRandom()));
        }
    }

    // costA, costB (0 without one), result count and max uses.
    private static int[] vanillaNumbers(MerchantOffer offer) {
        return new int[]{offer.getBaseCostA().getCount(), offer.getCostB().getCount(),
            offer.getResult().getCount(), offer.getMaxUses()};
    }

    private static java.util.Map<String, Integer> vanillaEnchantments(MerchantOffer offer) {
        java.util.Map<String, Integer> levels = new java.util.LinkedHashMap<>();
        EnchantmentHelper.getEnchantments(offer.getResult()).forEach((enchantment, level) ->
            levels.put(String.valueOf(EnchantmentHelper.getEnchantmentId(enchantment)), level));
        return levels;
    }

    // Undoes improveOffers from the tags it left: each offer gets its vanilla numbers back,
    // keeping how many times it has been used. Offers without a tag are already vanilla.
    static void restoreOffers(AbstractVillager merchant) {
        List<String> tags = merchant.getTags().stream()
            .filter(tag -> TradeCalculator.offerIndex(tag) >= 0).toList();
        if (tags.isEmpty()) return;
        MerchantOffers offers = merchant.getOffers();
        for (String tag : tags) {
            merchant.removeTag(tag);
            int index = TradeCalculator.offerIndex(tag);
            if (index >= offers.size()) continue;
            int[] v = TradeCalculator.offerValues(tag);
            MerchantOffer offer = offers.get(index);
            ItemStack result = offer.getResult().copyWithCount(v[2]);
            var levels = TradeCalculator.offerEnchantments(tag);
            if (!levels.isEmpty()) {
                var enchantments = EnchantmentHelper.getEnchantments(result);
                enchantments.replaceAll((enchantment, level) ->
                    levels.getOrDefault(String.valueOf(EnchantmentHelper.getEnchantmentId(enchantment)), level));
                setEnchantments(result, enchantments);
            }
            ItemStack costB = offer.getCostB();
            offers.set(index, rebuild(offer, offer.getBaseCostA().copyWithCount(v[0]),
                costB.isEmpty() ? costB : costB.copyWithCount(v[1]), result, v[3]));
        }
    }

    // EnchantmentHelper.setEnchantments only ever raises a book's stored levels, so clear them first.
    private static void setEnchantments(ItemStack stack, java.util.Map<Enchantment, Integer> enchantments) {
        if (stack.is(Items.ENCHANTED_BOOK)) stack.removeTagKey(EnchantedBookItem.TAG_STORED_ENCHANTMENTS);
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    // Same offer with new costs, result and max uses. Everything else (uses, demand, discounts,
    // xp, rewardExp) carries over through the offer's own NBT.
    private static MerchantOffer rebuild(MerchantOffer offer, ItemStack costA, ItemStack costB,
                                         ItemStack result, int maxUses) {
        CompoundTag tag = offer.createTag();
        tag.put("buy", costA.save(new CompoundTag()));
        tag.put("buyB", costB.save(new CompoundTag()));
        tag.put("sell", result.save(new CompoundTag()));
        tag.putInt("uses", Math.min(offer.getUses(), maxUses));
        tag.putInt("maxUses", maxUses);
        return new MerchantOffer(tag);
    }

    private static MerchantOffer improveOffer(MerchantOffer offer, int level, net.minecraft.util.RandomSource random) {
        double price = TradeCalculator.getPriceMultiplier(level);
        ItemStack costA = scaleCost(offer.getBaseCostA(), price, random);
        ItemStack costB = scaleCost(offer.getCostB(), price, random);

        ItemStack result = offer.getResult().copy();
        result.setCount(TradeCalculator.scaleCount(result.getCount(),
            TradeCalculator.getResultMultiplier(level), result.getMaxStackSize(), random.nextDouble()));
        int bonus = TradeCalculator.getEnchantmentBonus(level);
        var enchantments = EnchantmentHelper.getEnchantments(result);
        if (bonus != 0 && !enchantments.isEmpty()) {
            enchantments.replaceAll((enchantment, current) ->
                Math.max(1, Math.min(enchantment.getMaxLevel(), current + bonus)));
            setEnchantments(result, enchantments);
        }

        int maxUses = Math.max(1, (int) Math.round(offer.getMaxUses() * TradeCalculator.getUsesMultiplier(level)));
        return rebuild(offer, costA, costB, result, maxUses);
    }

    private static ItemStack scaleCost(ItemStack cost, double multiplier, net.minecraft.util.RandomSource random) {
        if (cost.isEmpty()) return cost.copy();
        return cost.copyWithCount(TradeCalculator.scaleCount(cost.getCount(), multiplier, cost.getMaxStackSize(), random.nextDouble()));
    }

    // "[LvN] " in the level's color, shared by name tags and the trading screen.
    public static net.minecraft.network.chat.MutableComponent levelPrefix(int level) {
        ChatFormatting color = ChatFormatting.GREEN;
        if (level >= 50) color = ChatFormatting.AQUA;
        if (level >= 100) color = ChatFormatting.YELLOW;
        if (level >= 130) color = ChatFormatting.RED;
        if (level >= 150) color = ChatFormatting.DARK_PURPLE;
        return Component.literal("[Lv" + level + "] ").withStyle(color);
    }

    private static int getLevelFromEntity(LivingEntity entity) {
        return DropsCalculator.getLevelFromTags(entity.getTags());
    }
}
