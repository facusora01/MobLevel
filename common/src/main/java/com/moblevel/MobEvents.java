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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import java.util.Collection;
import com.moblevel.platform.Services;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

// What MobLevel does to mobs. Each loader calls these from its own events or mixins,
// passing plain vanilla objects, so the behavior is identical on every loader.
public class MobEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new Random();
    // Level health scaling, as a permanent max-health modifier saved with the mob.
    private static final ResourceLocation HEALTH_ID =
        ResourceLocation.fromNamespaceAndPath(MobLevel.MODID, "level_health");
    // Mount speed and jump scaling, permanent modifiers like the health one.
    private static final ResourceLocation MOUNT_SPEED_ID =
        ResourceLocation.fromNamespaceAndPath(MobLevel.MODID, "level_speed");
    private static final ResourceLocation MOUNT_JUMP_ID =
        ResourceLocation.fromNamespaceAndPath(MobLevel.MODID, "level_jump");
    private static final ResourceLocation SPEED_BOOST_ID =
        ResourceLocation.fromNamespaceAndPath(MobLevel.MODID, "speed_boost");
    // Version marker: mobs leveled by 1.2.2+ carry this tag and are never migrated.
    static final String VERSION_TAG = "ml2";
    // Per-world scoreboard objective that turns the one-time level migration on.
    static final String MIGRATION_MARKER = "ml_restart_done";
    // Set by onBabySpawn on a child that already carries its level tag, so onEntityJoinLevel
    // still treats it as a fresh spawn and heals it up to its scaled maximum. Consumed on join,
    // so it never survives into the save file.
    static final String NEWBORN_TAG = "ml_newborn";

    // Taming a level 150 mount succeeds this many times less often than vanilla.
    static final int LEVEL_150_TAME_DIFFICULTY = 5;

    private static final DustParticleOptions PARTICLE =
        new DustParticleOptions(new org.joml.Vector3f(0.6f, 0.0f, 1.0f), 0.7f);

    public static void onEntityJoinLevel(Mob mob) {
        if (mob.level().isClientSide()) return;

        // Uninstall mode: strip MobLevel data instead of applying it, so the world
        // can be returned to vanilla before the jar is removed.
        if (Config.uninstallMode) {
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
            currentLevel = calculateLevel(mob);
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
    public static void onStartTracking(Mob mob, ServerPlayer player) {
        int level = getLevelFromEntity(mob);
        if (level <= 0) return;

        Services.PLATFORM.sendToPlayer(player, new LevelSyncPayload(mob.getId(), level));
    }

    public static void onBabySpawn(Mob parentA, Mob parentB, Mob child) {
        if (Config.uninstallMode) return;
        if (child == null) return;
        if (child.level().isClientSide()) return;

        int levelA = (parentA != null) ? DropsCalculator.getLevelFromTags(parentA.getTags()) : 0;
        int levelB = (parentB != null) ? DropsCalculator.getLevelFromTags(parentB.getTags()) : 0;

        int minBonus = Config.breedingMutationMinBonus;
        int maxBonus = Config.breedingMutationMaxBonus;
        int bonus = minBonus + RANDOM.nextInt(Math.max(1, maxBonus - minBonus + 1));
        int childLevel = BreedingCalculator.calculateChildLevel(
            levelA, levelB, RANDOM.nextDouble(),
            Config.breedingMutationChance, bonus, Config.maxLevel);

        // Tag set here so onEntityJoinLevel respects it instead of rolling a random level
        child.addTag("lvl:" + childLevel);
        child.addTag(NEWBORN_TAG);

        // LOGGER.info("onBabySpawn: parents {}+{} -> child level {}", levelA, levelB, childLevel);
    }

    // Returns the damage after scaling it by the attacking mob's level.
    public static float modifyDamage(DamageSource source, float damage) {
        if (source.getEntity() instanceof Mob attacker) {
            int level = getLevelFromEntity(attacker);
            if (level > 0) {
                return damage * DropsCalculator.getDamageMultiplier(level);
            }
        }
        return damage;
    }

    // Returns the XP the entity drops on death, scaled by its level.
    public static int modifyExperience(LivingEntity entity, int originalXp) {
        if (!(entity instanceof Mob mob)) return originalXp;
        int level = getLevelFromEntity(mob);
        return level > 0 ? DropsCalculator.calculateExperienceDrop(originalXp, level) : originalXp;
    }

    // Edits the death drops in place: thins them out below vanilla level, multiplies them above.
    public static void modifyDrops(LivingEntity entity, Collection<ItemEntity> drops) {
        if (!(entity instanceof Mob mob)) return;
        int level = getLevelFromEntity(mob);

        if (level > 0) {
            if (level < DropsCalculator.VANILLA_LEVEL) {
                // Sub-vanilla: each item rolls a drop chance; if it drops, count = 1.
                double dropChance = DropsCalculator.getDropChance(level);
                java.util.Iterator<ItemEntity> it = drops.iterator();
                while (it.hasNext()) {
                    ItemEntity itemEntity = it.next();
                    if (RANDOM.nextFloat() >= dropChance) {
                        it.remove();
                    } else {
                        itemEntity.getItem().setCount(1);
                    }
                }
            } else {
                for (ItemEntity itemEntity : drops) {
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
                ItemStack necklaceDrop = new ItemStack(Services.PLATFORM.totemNecklace());
                ItemEntity dropEntity = new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), necklaceDrop);
                drops.add(dropEntity);
            }
        }
    }

    // Returns true when a totem necklace saved the entity, so the loader cancels the death.
    public static boolean tryTotemSave(LivingEntity entity) {
        if (entity.level().isClientSide()) return false;

        ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack mainHand = entity.getMainHandItem();
        ItemStack offHand = entity.getOffhandItem();

        boolean hasTotemTag = entity.getTags().contains("HasTotemNecklace");
        boolean hasTotemItem = false;
        try {
            hasTotemItem = headItem.is(Services.PLATFORM.totemNecklace()) ||
                    mainHand.is(Services.PLATFORM.totemNecklace()) ||
                    offHand.is(Services.PLATFORM.totemNecklace());
        } catch (NullPointerException e) {
            LOGGER.warn("TOTEM_NECKLACE not registered yet, skipping death check");
            return false;
        }

        if (hasTotemTag || hasTotemItem) {
            if (hasTotemTag) {
                // Mob carries the totem as a tag (no visible item), consume it on save.
                entity.removeTag("HasTotemNecklace");
            } else if (headItem.is(Services.PLATFORM.totemNecklace())) {
                entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            } else if (mainHand.is(Services.PLATFORM.totemNecklace())) {
                entity.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            } else if (offHand.is(Services.PLATFORM.totemNecklace())) {
                entity.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }

            entity.setHealth(entity.getMaxHealth());
            entity.removeAllEffects();

            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));

            ItemStack visualStack = new ItemStack(Services.PLATFORM.totemNecklace());
            TotemAnimationPayload payload = new TotemAnimationPayload(entity.getId(), visualStack);

            if (entity instanceof ServerPlayer serverPlayer) {
                Services.PLATFORM.sendToPlayer(serverPlayer, payload);
            }

            Services.PLATFORM.sendToTracking(entity, payload);
            return true;
        }
        return false;
    }

    public static void onMobTick(Mob mob) {        // Do nothing on the client: names are synced automatically, particles are server-driven.
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
            hostile ? Config.hostileHighLevelChance : Config.highLevelChance,
            hostile ? Config.hostileLevelRarityExponent : Config.levelRarityExponent,
            Config.commonLevelSkew,
            Config.maxLevel);
    }

    private static void applyLevelStats(Mob mob, int level, boolean freshSpawn) {
        AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            if (!freshSpawn && !maxHealth.hasModifier(HEALTH_ID)) {
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
            maxHealth.addOrReplacePermanentModifier(new AttributeModifier(HEALTH_ID,
                target / base - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            if (freshSpawn) {
                mob.setHealth(mob.getMaxHealth());
            }
        }

        if (mob instanceof Creeper creeper) {
            double bonus = (level / 150.0) * 9.0;
            int newRadius = 3 + (int) bonus;
            if (newRadius > 12) newRadius = 12;

            creeper.explosionRadius = newRadius;
        }

        if (mob instanceof AbstractHorse) {
            // Mounts: vanilla genetics roll and inherit the base values, the level scales them.
            // Low levels make worse mounts, high levels better ones, and a horse can still be
            // fast but weak, or a good jumper but slow, exactly as its genes say.
            AttributeModifier.Operation op = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            double mult = DropsCalculator.getMountMultiplier(level);
            AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                // Low levels slow a mount down, but never below the slowest vanilla horse,
                // unless it is that slow by nature (camels).
                double base = speed.getBaseValue();
                double target = Math.max(base * mult, Math.min(base, DropsCalculator.MOUNT_MIN_SPEED));
                speed.addOrReplacePermanentModifier(new AttributeModifier(MOUNT_SPEED_ID, target / base - 1.0, op));
            }
            AttributeInstance jump = mob.getAttribute(Attributes.JUMP_STRENGTH);
            if (jump != null) jump.addOrReplacePermanentModifier(new AttributeModifier(MOUNT_JUMP_ID, mult - 1.0, op));
        }

        if (level >= 150) {
            // Move 1.5x faster. Transient modifier so it doesn't compound across world reloads.
            // Mounts skip it: their level curve above already covers speed.
            AttributeInstance moveSpeed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (moveSpeed != null && !(mob instanceof AbstractHorse) && moveSpeed.getModifier(SPEED_BOOST_ID) == null) {
                moveSpeed.addTransientModifier(new AttributeModifier(
                    SPEED_BOOST_ID, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
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
            improveOffers(merchant, merchant.getOffers(), 0);
        }
        // Clients tracking this mob already cached the old level; push the new one.
        Services.PLATFORM.sendToTracking(mob, new LevelSyncPayload(mob.getId(), level));
    }

    private static boolean isMigrationEnabled(Mob mob) {
        MinecraftServer server = mob.level().getServer();
        return server != null && server.getScoreboard().getObjective(MIGRATION_MARKER) != null;
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
            if (!maxHealth.removeModifier(HEALTH_ID) && lvlTag != null) {
                // Leveled by a version that scaled the base value itself.
                maxHealth.setBaseValue(getVanillaMaxHealth(mob, maxHealth.getBaseValue()));
            }
            if (mob.getHealth() > mob.getMaxHealth()) {
                mob.setHealth(mob.getMaxHealth());
            }
        }

        AttributeInstance moveSpeed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeed != null) {
            moveSpeed.removeModifier(SPEED_BOOST_ID);
            moveSpeed.removeModifier(MOUNT_SPEED_ID);
        }
        AttributeInstance jump = mob.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump != null) {
            jump.removeModifier(MOUNT_JUMP_ID);
        }

        if (mob instanceof Creeper creeper) {
            creeper.explosionRadius = 3;
        }
    }

    // Vanilla default max health for this entity type, used only to undo the base-value
    // scaling of older versions. Falls back to the current base if the lookup fails.
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

    // How many times harder than vanilla this mob is to tame: level 150 mounts are wild.
    public static int tameDifficulty(Mob mob) {
        return mob instanceof AbstractHorse && getLevelFromEntity(mob) >= 150 ? LEVEL_150_TAME_DIFFICULTY : 1;
    }

    // For loaders that report a taming vanilla already allowed (NeoForge's AnimalTameEvent):
    // true to refuse it, so only 1 in tameDifficulty of vanilla's successes go through.
    public static boolean vetoTame(Mob mob) {
        int difficulty = tameDifficulty(mob);
        return difficulty > 1 && mob.getRandom().nextInt(difficulty) != 0;
    }

    // /summon with NBT (tags included) skips vanilla's spawn setup, so a horse, donkey, mule or
    // llama keeps its type's placeholder stats (53 health) instead of rolled genes. Roll them
    // now, as a natural spawn would have. A rolled health never equals the placeholder.
    private static void rollUnrolledMount(Mob mob) {
        if ((mob instanceof Horse || mob instanceof AbstractChestedHorse)
                && mob.getAttributeBaseValue(Attributes.MAX_HEALTH) == getVanillaMaxHealth(mob, -1)) {
            ((AbstractHorse) mob).randomizeAttributes(mob.getRandom());
        }
    }

    private static boolean isTamedMount(Mob mob) {
        return mob instanceof AbstractHorse horse && horse.isTamed();
    }

    // Rebuilds the offers a villager or wandering trader just generated (from index `from`)
    // so they reflect its level: cheaper, bigger, longer-lasting and better enchanted the
    // higher it is, worse below level 20. Runs once per offer, when it is created; offers are
    // saved with the merchant, so they never get scaled twice.
    public static void improveOffers(AbstractVillager merchant, MerchantOffers offers, int from) {
        int level = getLevelFromEntity(merchant);
        if (level <= 0) return;
        // An offer that already has its tag was improved before: never scale it twice.
        java.util.Set<Integer> done = new java.util.HashSet<>();
        for (String tag : merchant.getTags()) done.add(TradeCalculator.offerIndex(tag));
        for (int i = from; i < offers.size(); i++) {
            if (done.contains(i)) continue;
            MerchantOffer original = offers.get(i);
            merchant.addTag(TradeCalculator.encodeOffer(i, vanillaNumbers(original), vanillaEnchantments(original)));
            offers.set(i, improveOffer(original, level, merchant.getRandom()));
        }
    }

    // costA, costB (0 without one), result count and max uses.
    private static int[] vanillaNumbers(MerchantOffer offer) {
        return new int[]{offer.getItemCostA().count(), offer.getItemCostB().map(ItemCost::count).orElse(0),
            offer.getResult().getCount(), offer.getMaxUses()};
    }

    private static java.util.Map<String, Integer> vanillaEnchantments(MerchantOffer offer) {
        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(offer.getResult());
        java.util.Map<String, Integer> levels = new java.util.LinkedHashMap<>();
        for (var enchantment : enchantments.keySet()) {
            levels.put(enchantment.getRegisteredName(), enchantments.getLevel(enchantment));
        }
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
            EnchantmentHelper.updateEnchantments(result, enchantments -> {
                for (var enchantment : java.util.List.copyOf(enchantments.keySet())) {
                    Integer level = levels.get(enchantment.getRegisteredName());
                    if (level != null) enchantments.set(enchantment, level);
                }
            });
            offers.set(index, rebuild(offer,
                new ItemCost(offer.getItemCostA().item(), v[0], offer.getItemCostA().components()),
                offer.getItemCostB().map(cost -> new ItemCost(cost.item(), v[1], cost.components())),
                result, v[3]));
        }
    }

    // Same offer with new costs, result and max uses; uses, demand and discounts carry over.
    private static MerchantOffer rebuild(MerchantOffer offer, ItemCost costA, java.util.Optional<ItemCost> costB,
                                         ItemStack result, int maxUses) {
        MerchantOffer rebuilt = new MerchantOffer(costA, costB, result, Math.min(offer.getUses(), maxUses),
            maxUses, offer.getXp(), offer.getPriceMultiplier(), offer.getDemand());
        rebuilt.setSpecialPriceDiff(offer.getSpecialPriceDiff());
        return rebuilt;
    }

    private static MerchantOffer improveOffer(MerchantOffer offer, int level, net.minecraft.util.RandomSource random) {
        double price = TradeCalculator.getPriceMultiplier(level);
        ItemCost costA = scaleCost(offer.getItemCostA(), price, random);
        java.util.Optional<ItemCost> costB = offer.getItemCostB().map(cost -> scaleCost(cost, price, random));

        ItemStack result = offer.getResult().copy();
        result.setCount(TradeCalculator.scaleCount(result.getCount(),
            TradeCalculator.getResultMultiplier(level), result.getMaxStackSize(), random.nextDouble()));
        int bonus = TradeCalculator.getEnchantmentBonus(level);
        if (bonus != 0) {
            EnchantmentHelper.updateEnchantments(result, enchantments -> {
                for (var enchantment : java.util.List.copyOf(enchantments.keySet())) {
                    int max = enchantment.value().getMaxLevel();
                    enchantments.set(enchantment, Math.max(1, Math.min(max, enchantments.getLevel(enchantment) + bonus)));
                }
            });
        }

        int maxUses = Math.max(1, (int) Math.round(offer.getMaxUses() * TradeCalculator.getUsesMultiplier(level)));
        return rebuild(offer, costA, costB, result, maxUses);
    }

    private static ItemCost scaleCost(ItemCost cost, double multiplier, net.minecraft.util.RandomSource random) {
        int count = TradeCalculator.scaleCount(cost.count(), multiplier, cost.itemStack().getMaxStackSize(), random.nextDouble());
        return new ItemCost(cost.item(), count, cost.components());
    }

    // Trading screen title: "[Lv87] Librarian".
    public static Component tradingTitle(AbstractVillager merchant, Component title) {
        int level = getLevelFromEntity(merchant);
        return level > 0 ? levelPrefix(level).append(title) : title;
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
