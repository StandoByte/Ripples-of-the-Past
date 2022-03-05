package com.github.standobyte.jojo.util;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.actions.VampirismFreeze;
import com.github.standobyte.jojo.advancements.criterion.ModCriteriaTriggers;
import com.github.standobyte.jojo.block.StoneMaskBlock;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.ProjectileHamonChargeCapProvider;
import com.github.standobyte.jojo.command.TestBuildCommand;
import com.github.standobyte.jojo.entity.SoulEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.MRCrossfireHurricaneEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.init.ModPaintings;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.item.StoneMaskItem;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.Technique;
import com.github.standobyte.jojo.power.nonstand.type.VampirismPowerType;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.tileentity.StoneMaskTileEntity;
import com.github.standobyte.jojo.util.damage.IStandDamageSource;
import com.github.standobyte.jojo.util.damage.ModDamageSources;
import com.github.standobyte.jojo.util.reflection.CommonReflection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionApplicableEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class GameplayEventHandler {
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingTick(LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!entity.level.isClientSide() && entity.invulnerableTime <= 10) {
            float sunDamage = getSunDamage(entity);
            if (sunDamage > 0) {
                ModDamageSources.dealUltravioletDamage(entity, sunDamage, null, null, true);
            }
        }
        entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.tick();
        });
    }
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        PlayerEntity player = event.player;
        if (player.hasEffect(ModEffects.STUN.get())) {
            player.setSprinting(false);
        }
        if (event.side == LogicalSide.SERVER) {
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.tick();
            });
            if (player.tickCount % 60 == 0 && !player.isInvisible() && player.xxa == 0 && player.zza == 0 && player instanceof ServerPlayerEntity) {
                long timeNotActive = Util.getMillis() - ((ServerPlayerEntity) player).getLastActionTime();
                if (timeNotActive > 60000) {
                    ((ServerWorld) player.level).sendParticles(ModParticles.MENACING.get(), player.getX(), player.getEyeY(), player.getZ(), 
                            0,  MathHelper.cos(player.yRot * MathUtil.DEG_TO_RAD), 0.5D, MathHelper.sin(player.yRot * MathUtil.DEG_TO_RAD), 0.005D);
                }
            }
        }
        INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
            power.tick();
        });
        IStandPower.getStandPowerOptional(player).ifPresent(power -> {
            power.tick();
        }); 
    }

    private static final float MAX_SUN_DAMAGE = 10;
    private static final float MIN_SUN_DAMAGE = 2;
    private static float getSunDamage(LivingEntity entity) {
        if (entity.hasEffect(ModEffects.SUN_RESISTANCE.get())) {
            return 0;
        }
        World world = entity.level;
        if (world.isDay()) {
            float brightness = entity.getBrightness();
            BlockPos blockPos = entity.getVehicle() instanceof BoatEntity ? 
                    (new BlockPos(entity.getX(), (double)Math.round(entity.getY(1.0)), entity.getZ())).above()
                    : new BlockPos(entity.getX(), (double)Math.round(entity.getY(1.0)), entity.getZ());
            if (brightness > 0.5F && world.canSeeSky(blockPos)) {
                int time = (int) (world.getDayTime() % 24000L);
                float damage = MAX_SUN_DAMAGE;
                float diff = MAX_SUN_DAMAGE - MIN_SUN_DAMAGE;
                
                // sunrise
                if (time > 23460) { 
                    time -= 24000;
                }
                if (time <= 60) {
                    damage -= diff * (1F - (float) (time + 540) / 600F);
                }
                
                // sunset
                else if (time > 11940 && time <= 12540) {
                    damage -= diff * (float) (time - 11940) / 600F;
                }
                
                return damage;
            }
        }
        return 0;
    }
    
    @SubscribeEvent
    public static void onWorldTick(WorldTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            ((ServerWorld) event.world).getAllEntities().forEach(entity -> {
                entity.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
                    if (cap.hamonBaseDmg > 0 && entity.canUpdate()) {
                        HamonPowerType.createHamonSparkParticles(entity.level, null, 
                                entity.getX(), entity.getY(0.5), entity.getZ(), cap.getHamonDamage());
                    }
                });
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelStunnedPlayerInteraction(PlayerInteractEvent event) {
        if (event.isCancelable() && event.getPlayer().hasEffect(ModEffects.STUN.get())) {
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (event.getPlayer().hasEffect(ModEffects.STUN.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;
            if (entity.getClassification(false) == EntityClassification.MONSTER) {
                makeMobNeutralToVampirePlayers(mob);
            }
            else if (entity instanceof IronGolemEntity) {
                mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, PlayerEntity.class, 5, false, false, 
                        target -> target instanceof PlayerEntity && JojoModUtil.isPlayerUndead((PlayerEntity) target)));
            }
        }
        else if (entity.getType() == EntityType.PAINTING) {
            cutOutHands((PaintingEntity) event.getEntity());
        }
    }

    private static void makeMobNeutralToVampirePlayers(MobEntity mob) {
        Set<PrioritizedGoal> goals = CommonReflection.getGoalsSet(mob.targetSelector);
        for (PrioritizedGoal prGoal : goals) {
            Goal goal = prGoal.getGoal();
            if (goal instanceof NearestAttackableTargetGoal) {
                NearestAttackableTargetGoal<?> targetGoal = (NearestAttackableTargetGoal<?>) goal;
                Class<? extends LivingEntity> targetClass = CommonReflection.getTargetClass(targetGoal);
                if (targetClass == PlayerEntity.class) {
                    EntityPredicate selector = CommonReflection.getTargetConditions(targetGoal);
                    if (selector != null) {
                        Predicate<LivingEntity> oldPredicate = CommonReflection.getTargetSelector(selector);
                        Predicate<LivingEntity> undeadPredicate = target -> target instanceof PlayerEntity && !JojoModUtil.isPlayerUndead((PlayerEntity) target);
                        CommonReflection.setTargetConditions(targetGoal, new EntityPredicate().range(CommonReflection.getTargetDistance(targetGoal)).selector(
                                oldPredicate != null ? oldPredicate.and(undeadPredicate) : undeadPredicate));
                    }
                }
            }
        }
    }
    
    private static void cutOutHands(PaintingEntity painting) {
        if (!painting.level.isClientSide()) {
            boolean monaLisaFull = painting.motive == ModPaintings.MONA_LISA.get();
            boolean monaLisaHands = painting.motive == ModPaintings.MONA_LISA_HANDS.get();
            if (monaLisaFull || monaLisaHands) {
                List<LivingEntity> KQUsers = painting.level.getEntitiesOfClass(
                        LivingEntity.class, painting.getBoundingBox().expandTowards(painting.getLookAngle().scale(3)).inflate(1), 
                            entity -> IStandPower.getStandPowerOptional(entity).map(
                                    stand -> stand.hasPower() && stand.getType() == ModStandTypes.KILLER_QUEEN.get())
                            .orElse(false));
                if (!KQUsers.isEmpty()) {
                    if (monaLisaFull) {
                        painting.motive = ModPaintings.MONA_LISA_HANDS.get();
                        double x = painting.getX();
                        double z = painting.getZ();
                        if (x - (int) x != 0 && (int) (x + 0.04) != (int) x) {
                            z -= 1;
                        }
                        if (z - (int) z != 0 && (int) (z - 0.04) != (int) z) {
                            x -= 1;
                        }
                        painting.setPos(x, painting.getY(), z);
                    }
                }
                else if (monaLisaHands) {
                    painting.motive = PaintingType.KEBAB;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHeal(LivingHealEvent event) {
        VampirismPowerType.consumeEnergyOnHeal(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurtStart(LivingAttackEvent event) {
        HamonPowerType.cancelCactusDamage(event);
        if (VampirismFreeze.onUserAttacked(event)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingAttack(LivingAttackEvent event) {
        DamageSource damageSrc = event.getSource();
        if (damageSrc instanceof IStandDamageSource) {
            IStandPower attackerStand = ((IStandDamageSource) damageSrc).getStandPower();
            attackerStand.addResolveOnAttack(event.getEntityLiving(), event.getAmount());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void cancelLivingAttack(LivingAttackEvent event) {
        HamonPowerType.snakeMuffler(event);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void resolveOnTakingDamage(LivingDamageEvent event) {
        IStandPower.getStandPowerOptional(event.getEntityLiving()).ifPresent(stand -> {
            stand.addResolveOnTakingDamage(event.getSource(), event.getAmount());
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void reduceDamageFromResolve(LivingDamageEvent event) {
        if (event.getSource() == DamageSource.OUT_OF_WORLD) {
            return;
        }
        float dmgReduction = IStandPower.getStandPowerOptional(event.getEntityLiving()).map(stand -> {
            return stand.getResolveDmgReduction();
        }).orElse(0F);
        if (dmgReduction > 0F) {
            event.setAmount(event.getAmount() * (1 - dmgReduction));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        activateStoneMasks(event);
        StandType.onHurtByStand(event);
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void prepareToReduceKnockback(LivingDamageEvent event) {
        float knockbackReduction = ModDamageSources.knockbackReduction(event.getSource());
        if (knockbackReduction >= 0 && knockbackReduction < 1) {
            event.getEntityLiving().getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(util -> {
                util.setFutureKnockbackFactor(knockbackReduction);
            });
        }
    }

    // sucks that LivingKnockBackEvent doesn't receive DamageSource
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityKnockback(LivingKnockBackEvent event) {
        event.getEntityLiving().getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(util -> {
            if (util.shouldReduceKnockback()) {
                float factor = util.getKnockbackFactorOneTime();
                event.setStrength(event.getStrength() * factor);
            }
        });
    }

    private static void activateStoneMasks(LivingDamageEvent event) {
        DamageSource dmgSource = event.getSource();
        if (event.getAmount() >= 0.98F && 
                (!dmgSource.isBypassArmor() && !dmgSource.isFire() && !dmgSource.isMagic() && !dmgSource.isBypassMagic() || dmgSource == DamageSource.FALL)) {
            LivingEntity target = event.getEntityLiving();
            if (!JojoModUtil.canBleed(target)) {
                return;
            }
            double radius = 2;
            BlockPos entityPos = target.blockPosition();
            World world = target.level;
            List<LivingEntity> entitiesAround = JojoModUtil.entitiesAround(LivingEntity.class, target, radius, true,
                    entity -> entity.getItemBySlot(EquipmentSlotType.HEAD).getItem() instanceof StoneMaskItem);
            for (LivingEntity entity : entitiesAround) {
                applyStoneMask(entity, entity.getItemBySlot(EquipmentSlotType.HEAD));
            }
            BlockPos.betweenClosedStream(entityPos.offset(-radius, -radius, -radius), entityPos.offset(radius, radius, radius))
            .filter(pos -> world.getBlockState(pos).getBlock() == ModBlocks.STONE_MASK.get())
            .forEach(pos -> {
                BlockState blockState = world.getBlockState(pos);
                world.playSound(null, pos, ModSounds.STONE_MASK_ACTIVATION.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                switch (blockState.getValue(HorizontalFaceBlock.FACE)) {
                case FLOOR:
                    TileEntity tileEntity = world.getBlockEntity(pos);
                    if (tileEntity instanceof StoneMaskTileEntity) {
                        ((StoneMaskTileEntity) tileEntity).activate();
                    }
                    break;
                default:
                    Block.popResource(world, pos, StoneMaskBlock.getItemFromBlock(world, pos, blockState));
                    world.removeBlock(pos, false);
                    break;
                }
            });
        }
    }

    public static boolean applyStoneMask(LivingEntity entity, ItemStack headStack) {
        if (entity.level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            INonStandPower power = INonStandPower.getPlayerNonStandPower(player);
            VampirismPowerType vampirism = ModNonStandPowers.VAMPIRISM.get();
            if (power.getTypeSpecificData(vampirism).map(vamp -> !vamp.isVampireAtFullPower()).orElse(false) || power.givePower(vampirism)) {
                entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.STONE_MASK_ACTIVATION_ENTITY.get(), entity.getSoundSource(), 1.0F, 1.0F);
                power.getTypeSpecificData(vampirism).get().setVampireFullPower(true);
                StoneMaskItem.setActivatedArmorTexture(headStack); // TODO light beams on stone mask activation
                headStack.hurtAndBreak(1, entity, stack -> {});
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPotionApply(PotionApplicableEvent event) {
        LivingEntity entity = event.getEntityLiving();
        Effect effect = event.getPotionEffect().getEffect();
        if (JojoModUtil.isUndead(entity)) {
            if (entity instanceof PlayerEntity) {
                if (effect == Effects.POISON || effect == Effects.HUNGER || effect == Effects.REGENERATION) {
                    event.setResult(Result.DENY);
                }
            }
        }
        else if (effect == ModEffects.UNDEAD_REGENERATION.get() || effect == ModEffects.HAMON_SPREAD.get()) {
            event.setResult(Result.DENY);
        }
        if (effect == ModEffects.FREEZE.get() && ModDamageSources.isImmuneToCold(entity)) {
            event.setResult(Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPotionAdded(PotionAddedEvent event) {
        EntityStandType.giveSharedEffectsFromUser(event);
        
        Entity entity = event.getEntity();
        if (!entity.level.isClientSide() && ModEffects.isEffectTracked(event.getPotionEffect().getEffect())) {
            ((ServerChunkProvider) entity.getCommandSenderWorld().getChunkSource()).broadcast(entity, 
                    new SPlayEntityEffectPacket(entity.getId(), event.getPotionEffect()));
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPotionRemoved(PotionRemoveEvent event) {
        VampirismPowerType.cancelVampiricEffectRemoval(event);
    }
    
    @SubscribeEvent
    public static void syncTrackedEffects(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            LivingEntity tracked = (LivingEntity) event.getTarget();
            for (Map.Entry<Effect, EffectInstance> effectEntry : tracked.getActiveEffectsMap().entrySet()) {
                if (ModEffects.isEffectTracked(effectEntry.getKey())) {
                    player.connection.send(new SPlayEntityEffectPacket(tracked.getId(), effectEntry.getValue()));
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getTarget() instanceof LivingEntity) {
            INonStandPower.getNonStandPowerOptional(event.getPlayer()).ifPresent(power -> {
                if (power.getHeldAction() == null) {
                    power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                        HamonPowerType.overdriveAttack(event.getPlayer(), (LivingEntity) event.getTarget(), power, hamon);
                    });
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getCancellationResult() == ActionResultType.PASS && event.getHand() == Hand.MAIN_HAND && !event.getPlayer().isShiftKeyDown()) {
            Entity target = event.getTarget();
            if (target instanceof PlayerEntity) {
                PlayerEntity targetPlayer = (PlayerEntity) target;
                INonStandPower targetPower = INonStandPower.getPlayerNonStandPower(targetPlayer);
                if (targetPower.getType() == ModNonStandPowers.HAMON.get()) {
                    HamonPowerType.interactWithHamonTeacher(target.level, event.getPlayer(), targetPlayer, 
                            targetPower.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get());
                    event.setCanceled(true);
                    event.setCancellationResult(ActionResultType.sidedSuccess(target.level.isClientSide));
                }
                else {
                    INonStandPower.getPlayerNonStandPower(event.getPlayer())
                    .getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.interactWithNewLearner(targetPlayer);
                        event.setCanceled(true);
                        event.setCancellationResult(ActionResultType.sidedSuccess(target.level.isClientSide));
                    });
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == Hand.MAIN_HAND && event.getUseBlock() != Event.Result.DENY) {
            PlayerEntity player = event.getPlayer();
            if (!player.isSpectator() && player.getMainHandItem().isEmpty()) {
                World world = player.level;
                BlockPos pos = event.getHitVec().getBlockPos();
                BlockState blockState = world.getBlockState(pos);
                if (blockState.getBlock() == Blocks.TRIPWIRE) {
                    INonStandPower power = INonStandPower.getPlayerNonStandPower(event.getPlayer());
                    power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                        if (hamon.isSkillLearned(HamonSkill.ROPE_TRAP)) {
                            event.setCanceled(true);
                            event.setCancellationResult(ActionResultType.SUCCESS);
                            if (!world.isClientSide()) {
                                HamonPowerType.ropeTrap(player, pos, blockState, world, power, hamon);
                            }
                        }
                    });
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!event.getEntity().level.isClientSide()) {
            LivingEntity dead = event.getEntityLiving();
            DamageSource dmgSource = event.getSource();
            HamonPowerType.hamonPerksOnDeath(dead);
            summonSoul(dead, dmgSource);
            Entity killer = dmgSource.getEntity();
            if (killer instanceof StandEntity) {
                StandEntity killerStand = (StandEntity) killer;
                if (killerStand.getUser() != null) {
                    killer = killerStand.getUser();
                }
            }
            if (killer instanceof ServerPlayerEntity) {
                ModCriteriaTriggers.PLAYER_KILLED_ENTITY.get().trigger((ServerPlayerEntity) killer, dead, dmgSource);
            }
            if (dead instanceof ServerPlayerEntity) {
                ModCriteriaTriggers.ENTITY_KILLED_PLAYER.get().trigger((ServerPlayerEntity) dead, killer, dmgSource);
            }
        }
    }
    
    private static void summonSoul(LivingEntity entity, DamageSource dmgSource) {
        if (JojoModUtil.isUndead(entity)) {
            return;
        }
        int ticks = IStandPower.getStandPowerOptional(entity)
                .map(power -> getSoulAscensionTicks(entity, power)).orElse(0);
        
        if (dmgSource == TestBuildCommand.SOUL_TEST) ticks = 300;
        
        if (ticks > 0) {
            SoulEntity soulEntity = new SoulEntity(entity.level, entity, ticks);
            entity.level.addFreshEntity(soulEntity);
        }
    }
    
    private static int getSoulAscensionTicks(LivingEntity user, IStandPower stand) {
        if (!stand.usesResolve()) {
            return 0;
        }
        boolean hardcore = user.level.getLevelData().isHardcore();
        if (user instanceof PlayerEntity && (
                JojoModConfig.COMMON.keepStandOnDeath.get() && !hardcore
                || stand.wasProgressionSkipped())) {
            return 0;
        }
        
        int ticks = (int) (60 * stand.getResolveLevel() * (stand.getResolveRatio() + 1));
        if (hardcore) {
            ticks += ticks / 2;
        }
        return ticks;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleCheatDeath(LivingDeathEvent event) {
        if (!event.getEntity().level.isClientSide()) {
            cheatDeath(event);
        }
    }

    private static void cheatDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntityLiving();
        if (event.getSource().getEntity() != null && event.getSource().getEntity() != dead && dead.hasEffect(ModEffects.CHEAT_DEATH.get())) {
            event.setCanceled(true);
            dead.setHealth(dead.getMaxHealth() / 2F);
            dead.removeEffect(ModEffects.CHEAT_DEATH.get());
            dead.clearFire();
            ((ServerWorld) dead.level).sendParticles(ParticleTypes.POOF, dead.getX(), dead.getY(), dead.getZ(), 
                    20, (double) dead.getBbWidth() * 2D - 1D, (double) dead.getBbHeight(), (double) dead.getBbWidth() * 2D - 1D, 0.02D);
            dead.addEffect(new EffectInstance(Effects.INVISIBILITY, 200, 0, false, false, true));
            chorusFruitTeleport(dead);
            INonStandPower.getNonStandPowerOptional(dead).ifPresent(power -> {
                power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                    if (hamon.getTechnique() == Technique.JOSEPH) {
                        if (dead instanceof ServerPlayerEntity) {
                            ServerPlayerEntity joseph = (ServerPlayerEntity) dead;
                            sendMemeDeathMessage(joseph, event.getSource().getLocalizedDeathMessage(dead));
                            sendSoundToOnePlayer(joseph, ModSounds.JOSEPH_GIGGLE.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                        }
                    }
                });
            });
        }
    }

    private static void chorusFruitTeleport(LivingEntity entity) {
        Random random = entity.getRandom();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        for (int i = 0; i < 16; ++i) {
            double xRandom = x + (random.nextDouble() - 0.5D) * 16.0D;
            double yRandom = MathHelper.clamp(y + (double) (random.nextInt(16) - 8), 0.0D, (double)(entity.level.getHeight() - 1));
            double zRandom = z + (random.nextDouble() - 0.5D) * 16.0D;
            if (entity.isPassenger()) {
                entity.stopRiding();
            }
            if (entity.randomTeleport(xRandom, yRandom, zRandom, false)) {
                break;
            }
        }
    }

    private static void sendMemeDeathMessage(ServerPlayerEntity player, ITextComponent deathMessage) {
        if (player.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)) {
            ITextComponent fakeDeathMessage = new TranslationTextComponent("chat.type.text", player.getDisplayName(), deathMessage);
            Team team = player.getTeam();
            if (team != null && team.getDeathMessageVisibility() != Team.Visible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OTHER_TEAMS) {
                    player.server.getPlayerList().broadcastToTeam(player, fakeDeathMessage);
                }
                else if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OWN_TEAM) {
                    player.server.getPlayerList().broadcastToAllExceptTeam(player, fakeDeathMessage);
                }
            } else {
                player.server.getPlayerList().broadcastMessage(fakeDeathMessage, ChatType.CHAT, player.getUUID());
            }
        }
    }

    private static void sendSoundToOnePlayer(ServerPlayerEntity player, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(player, sound, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) return;
        sound = event.getSound();
        category = event.getCategory();
        volume = event.getVolume();
        player.connection.send(new SPlaySoundEffectPacket(sound, category, player.getX(), player.getY(), player.getZ(), volume, pitch));
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onProjectileShot(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (!entity.level.isClientSide()) {
            entity.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(projCap -> {
                if (entity instanceof ProjectileEntity) {
                    ProjectileEntity projectile = (ProjectileEntity) entity;
                    Entity shooter = projectile.getOwner();
                    if (shooter instanceof LivingEntity) {
                        INonStandPower.getNonStandPowerOptional((LivingEntity) shooter).ifPresent(power -> {
                            power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                                float energyCost = -1;
                                HamonSkill requiredSkill;
                                float hamonBaseDmg = 0;
                                int maxChargeTicks = 0;
                                boolean water = false;
                                if (projectile instanceof AbstractArrowEntity && !alreadyHasHamon(projectile)) {
                                    requiredSkill = HamonSkill.ARROW_INFUSION;
                                    energyCost = 1000;
                                    hamonBaseDmg = 0.25F;
                                    maxChargeTicks = 10;
                                    water = projectile.getType() == EntityType.ARROW && ((ArrowEntity) projectile).getColor() != -1;
                                }
                                else {
                                    EntityType<?> type = projectile.getType();
                                    requiredSkill = HamonSkill.THROWABLES_INFUSION;
                                    if (type == EntityType.SNOWBALL) {
                                        energyCost = 600;
                                        hamonBaseDmg = 0.125F;
                                        maxChargeTicks = 25;
                                        water = true;
                                    }
                                    else if (type == EntityType.EGG) {
                                        energyCost = 600;
                                        hamonBaseDmg = 0.125F;
                                        maxChargeTicks = 100;
                                    }
                                    else if (type == EntityType.POTION) {
                                        energyCost = 800;
                                        hamonBaseDmg = 0.15F; 
                                        maxChargeTicks = 20;
                                        water = true;
                                    }
                                }
                                if (energyCost > -1 && hamon.isSkillLearned(requiredSkill) && power.consumeEnergy(energyCost)) {
                                    projCap.hamonBaseDmg = hamonBaseDmg;
                                    projCap.maxChargeTicks = maxChargeTicks;
                                    projCap.water = water;
                                    projCap.spentEnergy = energyCost;
                                }
                            });
                        });
                    }
                }
            });
        }
    }
    
    public static boolean projectileCanBeChargedWithHamon(Entity entity) {
        return entity instanceof AbstractArrowEntity && !alreadyHasHamon(entity) || 
                entity.getType() == EntityType.SNOWBALL || entity.getType() == EntityType.EGG || entity.getType() == EntityType.POTION;
    }
    
    private static boolean alreadyHasHamon(Entity projectile) {
        return projectile.getType() == ModEntityTypes.CLACKERS.get();
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onProjectileHit(ProjectileImpactEvent event) {
        RayTraceResult rayTrace = event.getRayTraceResult();
        if (rayTrace.getType() == RayTraceResult.Type.ENTITY) {
            Entity entity = event.getEntity();
            entity.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
                if (cap.hamonBaseDmg > 0 && entity instanceof ProjectileEntity) {
                    ProjectileEntity projectile = (ProjectileEntity) entity;
                    addHamonDamageToProjectile((EntityRayTraceResult) rayTrace, 
                            projectile.getOwner(), projectile, cap.getHamonDamage(), cap.water, cap.spentEnergy);
                }
            });
        }
    }
    
    private static void addHamonDamageToProjectile(EntityRayTraceResult rayTrace, Entity thrower, Entity thrown, 
            float damage, boolean waterProjectile, float spentEnergy) {
        if (thrown.level.isClientSide() || damage <= 0 || !(thrower instanceof LivingEntity)) {
            return;
        }
        INonStandPower.getNonStandPowerOptional((LivingEntity) thrower).ifPresent(power -> {
            power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                float dmgCheckWater = damage;
                if (waterProjectile && hamon.isSkillLearned(HamonSkill.TURQUOISE_BLUE_OVERDRIVE)) {
                    dmgCheckWater *= 1.25;
                }
                ModDamageSources.dealHamonDamage(rayTrace.getEntity(), dmgCheckWater, thrown, thrower);
                hamon.hamonPointsFromAction(HamonStat.STRENGTH, spentEnergy);
            });
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getDistance() > 4) {
            LivingEntity entity = event.getEntityLiving();
            float leapStrength = Math.max(
                    IStandPower.getStandPowerOptional(entity).map(power -> 
                    power.hasPower() && power.isLeapUnlocked() ? power.leapStrength() : 0).orElse(0F), 
                    INonStandPower.getNonStandPowerOptional(entity).map(power -> 
                    power.hasPower() && power.isLeapUnlocked() ? power.leapStrength() : 0).orElse(0F));
            if (leapStrength > 0) {
                event.setDistance(Math.max(event.getDistance() - leapStrength * 3, 0));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChatMessage(ServerChatEvent event) {
        List<ServerPlayerEntity> josephTechniqueUsers = JojoModUtil.entitiesAround(ServerPlayerEntity.class, event.getPlayer(), 8, false, 
                pl -> INonStandPower.getNonStandPowerOptional(pl).map(power -> 
                power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).map(hamon -> 
                hamon.getTechnique() == Technique.JOSEPH).orElse(false)).orElse(false));
        for (ServerPlayerEntity joseph : josephTechniqueUsers) {
            if (joseph.getChatVisibility() != ChatVisibility.HIDDEN && joseph.getRandom().nextFloat() < 0.05F) {
                String tlKey = "jojo.chat.joseph.next_line." + (joseph.getRandom().nextInt(3) + 1);
                ITextComponent message = new TranslationTextComponent("chat.type.text", joseph.getDisplayName(), 
                        new TranslationTextComponent(tlKey, event.getMessage()));
                LanguageMap map = LanguageMap.getInstance();
                if (map != null) {
                    message = ForgeHooks.onServerChatEvent(joseph.connection, String.format(map.getOrDefault(tlKey), event.getMessage()), message);
                }
                if (message != null) {
                    JojoModUtil.sayVoiceLine(joseph, ModSounds.JOSEPH_GIGGLE.get());
                    joseph.server.getPlayerList().broadcastMessage(message, ChatType.CHAT, joseph.getUUID());
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        if (explosion.getExploder() instanceof MRCrossfireHurricaneEntity) {
            ((MRCrossfireHurricaneEntity) explosion.getExploder())
            .onExplode(event.getAffectedEntities(), event.getAffectedBlocks());
        }
    }
    
    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!event.wakeImmediately() && !event.updateWorld()) {
            IStandPower stand = IStandPower.getPlayerStandPower(event.getPlayer());
            if (stand.hasPower()) {
                stand.setStamina(stand.getMaxStamina());
            }
        }
    }
}
