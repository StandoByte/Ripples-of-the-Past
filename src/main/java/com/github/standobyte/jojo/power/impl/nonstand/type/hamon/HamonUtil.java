package com.github.standobyte.jojo.power.impl.nonstand.type.hamon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.non_stand.HamonOrganismInfusion;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCap;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer.BarType;
import com.github.standobyte.jojo.entity.CrimsonBubbleEntity;
import com.github.standobyte.jojo.entity.HamonBlockChargeEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SnakeMufflerEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonParticlesPacket;
import com.github.standobyte.jojo.power.bowcharge.BowChargeEffectInstance;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion.CustomExplosionType;
import com.github.standobyte.jojo.util.mc.damage.explosion.HamonBlastExplosion;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.TripWireBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.Property;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class HamonUtil {

    
    public static boolean ropeTrap(LivingEntity user, BlockPos pos, BlockState blockState, World world, INonStandPower power, HamonData hamon) {
        if (hamon.isSkillLearned(ModHamonSkills.ROPE_TRAP.get())) {
            createChargedCobweb(user, pos, blockState, world, 64, null, power, 
                    40 + (int) ((float) (160 * hamon.getHamonStrengthLevel()) / (float) HamonData.MAX_STAT_LEVEL * hamon.getActionEfficiency(STRING_CHARGE_COST, false)), 
                    0.02F * hamon.getHamonDamageMultiplier() * hamon.getActionEfficiency(STRING_CHARGE_COST, false), hamon);
            return true;
        }
        return false;
    }
    
    private static final Map<BooleanProperty, Direction> PROPERTY_TO_DIRECTION = ImmutableMap.of(
            TripWireBlock.EAST, Direction.EAST, 
            TripWireBlock.SOUTH, Direction.SOUTH, 
            TripWireBlock.WEST, Direction.WEST, 
            TripWireBlock.NORTH, Direction.NORTH);
    private static final float STRING_CHARGE_COST = 10;
    private static void createChargedCobweb(LivingEntity user, BlockPos pos, BlockState blockState, World world, 
            int range, @Nullable Direction from, INonStandPower power, int chargeTicks, float charge, HamonData hamon) {
        if (range > 0 && blockState.getBlock() == Blocks.TRIPWIRE && power.consumeEnergy(STRING_CHARGE_COST)) {
            hamon.hamonPointsFromAction(HamonStat.CONTROL, STRING_CHARGE_COST / 2);
            Map<Property<?>, Comparable<?>> values = blockState.getValues();
            List<Direction> directions = new ArrayList<Direction>();
            for (Map.Entry<BooleanProperty, Direction> entry: PROPERTY_TO_DIRECTION.entrySet()) {
                if (entry.getValue() != from && (Boolean) values.get(entry.getKey())) {
                    directions.add(entry.getValue());
                }
            }
            world.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
            HamonBlockChargeEntity chargeEntity = new HamonBlockChargeEntity(world, pos);
            chargeEntity.setCharge(charge, chargeTicks, user, STRING_CHARGE_COST / 2);
            world.addFreshEntity(chargeEntity);
            for (Direction direction : directions) {
                BlockPos nextPos = pos.relative(direction);
                createChargedCobweb(user, nextPos, world.getBlockState(nextPos), world, 
                        range - 1, direction.getOpposite(), power, chargeTicks, charge, hamon);
            }
        }
    }
    
    public static void updateCheatDeathEffect(LivingEntity user) {
        user.addEffect(new EffectInstance(ModStatusEffects.CHEAT_DEATH.get(), 120000, 0, false, false, true));
    }
    
    public static boolean snakeMuffler(LivingEntity target, DamageSource dmgSource, float dmgAmount) {
        if (!target.level.isClientSide() && target.canUpdate() && target.isOnGround()) {
            Entity attacker = dmgSource.getEntity();
            if (attacker != null && dmgSource.getDirectEntity() == attacker && attacker instanceof LivingEntity
                    && target instanceof PlayerEntity && target.getItemBySlot(EquipmentSlotType.HEAD).getItem() == ModItems.SATIPOROJA_SCARF.get()) {
                LivingEntity livingAttacker = (LivingEntity) attacker;
                PlayerEntity playerTarget = (PlayerEntity) target;
                if (!playerTarget.getCooldowns().isOnCooldown(ModItems.SATIPOROJA_SCARF.get())) {
                    INonStandPower power = INonStandPower.getPlayerNonStandPower(playerTarget);
                    float energyCost = 500F;
                    if (power.hasEnergy(energyCost)) {
                        if (power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
                            if (hamon.isSkillLearned(ModHamonSkills.SNAKE_MUFFLER.get())) {
                                playerTarget.getCooldowns().addCooldown(ModItems.SATIPOROJA_SCARF.get(), 80);
                                float efficiency = hamon.getActionEfficiency(energyCost, false);
                                if (efficiency == 1 || efficiency >= dmgAmount / target.getMaxHealth()) {
                                    JojoModUtil.sayVoiceLine(target, ModSounds.LISA_LISA_SNAKE_MUFFLER.get());
                                    power.consumeEnergy(energyCost);
                                    DamageUtil.dealHamonDamage(attacker, 0.75F, target, null);
                                    livingAttacker.addEffect(new EffectInstance(Effects.GLOWING, 200));
                                    SnakeMufflerEntity snakeMuffler = new SnakeMufflerEntity(target.level, target);
                                    snakeMuffler.setEntityToJumpOver(attacker);
                                    target.level.addFreshEntity(snakeMuffler);
                                    snakeMuffler.attachToBlockPos(target.blockPosition());
                                    return true;
                                }
                            }
                            return false;
                        }).orElse(false)) return true;
                    }
                }
            }
        }
        return false;
    }
    

    @Nullable
    public static Set<AbstractHamonSkill> nearbyTeachersSkills(LivingEntity learner) {
        Set<AbstractHamonSkill> skills = new HashSet<>();
        if (MCUtil.entitiesAround(LivingEntity.class, learner, 3D, false, 
                entity -> INonStandPower.getNonStandPowerOptional(entity).map(power -> 
                power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
                    hamon.getLearnedSkills().forEach(skill -> {
                        if (skill.requiresTeacher()) {
                            skills.add(skill);
                        }
                    });
                    return true;
                }).orElse(false)).orElse(false)).isEmpty()) {
            return null;
        }
        return skills;
    }
    
    public static void interactWithHamonTeacher(World world, PlayerEntity player, LivingEntity teacher, HamonData teacherHamon) {
        INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
            Optional<HamonData> hamonOptional = power.getTypeSpecificData(ModPowers.HAMON.get());
            if (!hamonOptional.isPresent() && !world.isClientSide()) {
                if (teacher instanceof PlayerEntity) {
                    teacherHamon.addNewPlayerLearner(player);
                }
                else {
                    startLearningHamon(world, player, power, teacher, teacherHamon);
                }
            }
            hamonOptional.ifPresent(hamon -> {
                if (world.isClientSide()) {
                    ClientUtil.openHamonTeacherUi();
                }
                else {
                    if (player.abilities.instabuild) {
                        hamon.setBreathingLevel(HamonData.MAX_BREATHING_LEVEL);
                        hamon.setHamonStatPoints(HamonStat.STRENGTH, HamonData.MAX_HAMON_POINTS, true, true);
                        hamon.setHamonStatPoints(HamonStat.CONTROL, HamonData.MAX_HAMON_POINTS, true, true);
                        hamon.tcsa(false);
                    }
                }
            });
        });
    }
    
    public static void startLearningHamon(World world, PlayerEntity player, INonStandPower playerPower, LivingEntity teacher, HamonData teacherHamon) {
        if (playerPower.canGetPower(ModPowers.HAMON.get()) && teacherHamon.characterIs(ModHamonSkills.CHARACTER_ZEPPELI.get())) {
            JojoModUtil.sayVoiceLine(teacher, ModSounds.ZEPPELI_FORCE_BREATH.get());
            teacher.swing(Hand.MAIN_HAND, true);
            if (player.getRandom().nextFloat() <= 0.01F) {
                player.hurt(DamageSource.GENERIC, 4.0F);
                player.setAirSupply(0);
                return;
            }
            else {
                player.hurt(DamageSource.GENERIC, 0.1F);
            }
        } 
        if (playerPower.givePower(ModPowers.HAMON.get())) {
            playerPower.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                if (player.abilities.instabuild) {
                    hamon.setBreathingLevel(HamonData.MAX_BREATHING_LEVEL);
                    hamon.setHamonStatPoints(HamonStat.STRENGTH, HamonData.MAX_HAMON_POINTS, true, true);
                    hamon.setHamonStatPoints(HamonStat.CONTROL, HamonData.MAX_HAMON_POINTS, true, true);
                    hamon.tcsa(false);
                }
                player.sendMessage(new TranslationTextComponent("jojo.chat.message.learnt_hamon"), Util.NIL_UUID);
                PlayerUtilCap utilCap = player.getCapability(PlayerUtilCapProvider.CAPABILITY).orElseThrow(() -> new IllegalStateException());
                utilCap.sendNotification(OneTimeNotification.HAMON_WINDOW, 
                        new TranslationTextComponent("jojo.chat.message.hamon_window_hint", new KeybindTextComponent("jojo.key.hamon_skills_window")));
            });
        }
        else {
            player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.cant_learn_hamon"), true);
        }
        return;
    }

    public static void hamonPerksOnDeath(LivingEntity dead) {
        if (JojoModConfig.getCommonConfigInstance(false).keepHamonOnDeath.get() && !dead.level.getLevelData().isHardcore()) return;
        INonStandPower.getNonStandPowerOptional(dead).ifPresent(power -> {
            power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                if (hamon.isSkillLearned(ModHamonSkills.CRIMSON_BUBBLE.get())) {
                    CrimsonBubbleEntity bubble = new CrimsonBubbleEntity(dead.level);
                    ItemStack heldItem = dead.getMainHandItem();
                    if (!heldItem.isEmpty()) {
                        dead.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                        ItemEntity item = new ItemEntity(dead.level, dead.getX(), dead.getEyeY() - 0.3D, dead.getZ(), heldItem);
                        item.setPickUpDelay(2);
                        dead.level.addFreshEntity(item);
                        bubble.putItem(item);
                    }
                    dead.level.playSound(null, dead.getX(), dead.getY(), dead.getZ(), 
                            ModSounds.CAESAR_LAST_HAMON.get(), dead.getSoundSource(), 1.0F, 1.0F);
                    bubble.moveTo(dead.getX(), dead.getEyeY(), dead.getZ(), dead.yRot, dead.xRot);
                    bubble.setHamonPoints(hamon.getHamonStrengthPoints(), hamon.getHamonControlPoints());
                    dead.level.addFreshEntity(bubble);
                }
                else if (hamon.isSkillLearned(ModHamonSkills.DEEP_PASS.get())) {
                    PlayerEntity closestHamonUser = MCUtil.entitiesAround(PlayerEntity.class, dead, 8, false, player -> 
                    INonStandPower.getNonStandPowerOptional(player).map(pwr -> pwr.getType() == ModPowers.HAMON.get()).orElse(false))
                            .stream()
                            .min(Comparator.comparingDouble(player -> player.distanceToSqr(dead)))
                            .orElse(null);
                    if (closestHamonUser != null) {
                        dead.level.playSound(null, dead.getX(), dead.getY(), dead.getZ(), 
                                ModSounds.ZEPPELI_DEEP_PASS.get(), dead.getSoundSource(), 1.0F, 1.0F);
                        HamonData receiverHamon = INonStandPower.getPlayerNonStandPower(closestHamonUser).getTypeSpecificData(ModPowers.HAMON.get()).get();
                        if (receiverHamon.characterIs(ModHamonSkills.CHARACTER_JONATHAN.get())) {
                            JojoModUtil.sayVoiceLine(closestHamonUser, ModSounds.JONATHAN_DEEP_PASS_REACTION.get());
                        }
                        receiverHamon.setHamonStatPoints(HamonStat.STRENGTH, 
                                receiverHamon.getHamonStrengthPoints() + hamon.getHamonStrengthPoints(), true, false);
                        receiverHamon.setHamonStatPoints(HamonStat.CONTROL, 
                                receiverHamon.getHamonControlPoints() + hamon.getHamonControlPoints(), true, false);
                        if (closestHamonUser instanceof ServerPlayerEntity) {
                            ModCriteriaTriggers.LAST_HAMON.get().trigger((ServerPlayerEntity) closestHamonUser, dead);
                        }
                        createHamonSparkParticlesEmitter(closestHamonUser, 1.0F);
                    }
                }
            });
        });
    }
    
    public static void hamonExplosion(World world, @Nullable Entity source, @Nullable Entity hamonUser, 
            Vector3d position, float radius, float damage) {
        HamonBlastExplosion hamonBlast = new HamonBlastExplosion(world, source, null, 
                position.x, position.y, position.z, radius);
        hamonBlast.setHamonDamage(damage);
        CustomExplosion.explodePreCreated(hamonBlast, world, CustomExplosionType.HAMON);
    }
    

    
    public static boolean cancelDamageFromBlock(LivingEntity entity, DamageSource dmgSource, float dmgAmount) {
        DamagingBlockType type = DamagingBlockType.getType(dmgSource);
        if (type != null) {
            World world = entity.level;
            boolean protectedFromDamage = true;
            boolean fromBlocks = false;
            
            AxisAlignedBB hitbox = entity.getBoundingBox();
            BlockPos posMin = new BlockPos(hitbox.minX + 0.001D, hitbox.minY + 0.001D, hitbox.minZ + 0.001D);
            BlockPos posMax = new BlockPos(hitbox.maxX - 0.001D, hitbox.maxY - 0.001D, hitbox.maxZ - 0.001D);
            BlockPos.Mutable blockPos = new BlockPos.Mutable();
            if (world.hasChunksAt(posMin, posMax)) {
                for (int x = posMin.getX(); x <= posMax.getX() && protectedFromDamage; ++x) {
                    for (int y = posMin.getY(); y <= posMax.getY() && protectedFromDamage; ++y) {
                        for (int z = posMin.getZ(); z <= posMax.getZ() && protectedFromDamage; ++z) {
                            blockPos.set(x, y, z);
                            BlockState blockState = world.getBlockState(blockPos);
                            if (type.rightBlock(blockState)) {
                                protectedFromDamage &= preventBlockDamage(entity, world, blockPos, blockState, dmgSource, dmgAmount);
                                fromBlocks = true;
                            }
                        }
                    }
                }
            }
            
            if (!fromBlocks) {
                protectedFromDamage = preventBlockDamage(entity, world, null, null, dmgSource, dmgAmount);
            }
            
            return protectedFromDamage;
        }
        
        return false;
    }
    
    private static enum DamagingBlockType {
        CACTUS {
            @Override public boolean rightBlock(BlockState blockState) {
                return blockState.getBlock() instanceof CactusBlock;
            }
        },
        BERRY_BUSH {
            @Override public boolean rightBlock(BlockState blockState) {
                return blockState.getBlock() instanceof SweetBerryBushBlock;
            }
        };
        
        public abstract boolean rightBlock(BlockState blockState);
        
        @Nullable public static DamagingBlockType getType(DamageSource dmgSource) {
            if (dmgSource == DamageSource.CACTUS) {
                return CACTUS;
            }
            else if (dmgSource == DamageSource.SWEET_BERRY_BUSH) {
                return BERRY_BUSH;
            }
            return null;
        }
    }
    
    public static boolean preventBlockDamage(LivingEntity entity, World world, 
            @Nullable BlockPos blockPos, @Nullable BlockState blockState, DamageSource dmgSource, float dmgAmount) {
        if (world.isClientSide()) {
            return false;
        }
        
        boolean damagePrevented = GeneralUtil.orElseFalse(INonStandPower.getNonStandPowerOptional(entity), power -> {
            if (power.getType() == ModPowers.HAMON.get()) {
                if (entity.getType() == ModEntityTypes.HAMON_MASTER.get()) {
                    return true;
                }

                float energyCost = dmgAmount * 0.5F;
                if (power.getEnergy() >= energyCost) {
                    power.consumeEnergy(energyCost);
                    return true;
                }
                else {
                    power.consumeEnergy(power.getEnergy());
                }
            }
            return false;
            
        }) || GeneralUtil.orElseFalse(entity.getCapability(EntityHamonChargeCapProvider.CAPABILITY), cap -> {
            if (cap.hasHamonCharge()) {
                cap.getHamonCharge().decreaseTicks(Math.max((int) dmgAmount, 1));
                return true;
            }
            return false;
        });
        
        if (damagePrevented) {
            Vector3d sparkPos = null;
            if (blockPos != null && blockState != null) {
                AxisAlignedBB entityHitbox = entity.getBoundingBox();
                VoxelShape blockShape = blockState.getCollisionShape(world, blockPos);
                if (!blockShape.isEmpty()) {
                    AxisAlignedBB blockAABB = blockShape.bounds().move(blockPos);
                    AxisAlignedBB intersection = entityHitbox.intersect(blockAABB);
                    sparkPos = new Vector3d(
                            MathHelper.lerp(Math.random(), intersection.minX, intersection.maxX), 
                            MathHelper.lerp(Math.random(), intersection.minY, intersection.maxY), 
                            MathHelper.lerp(Math.random(), intersection.minZ, intersection.maxZ));
                }
            }
            
            if (sparkPos == null) {
                sparkPos = dmgSource.getSourcePosition();
            }
            
            if (sparkPos != null) {
                PacketManager.sendToClientsTrackingAndSelf(TrHamonParticlesPacket.shortSpark(
                        entity.getId(), sparkPos, false, 
                        Math.max((int) (dmgAmount * 0.5F), 1), Math.min(dmgAmount * 0.25F, 1)), entity);
            }
            return true;
        }
        return false;
    }
    
    
    
    // TODO fix not being able to walk on liquid on shift (PlayerEntity#maybeBackOffFromEdge (989))
    public static boolean liquidWalking(PlayerEntity player) {
        World world = player.level;
        if (player.abilities.flying || player.isInWater()) {
            return false;
        }
        boolean doubleShift = player.isShiftKeyDown() && player.getCapability(PlayerUtilCapProvider.CAPABILITY).map(
                cap -> cap.getDoubleShiftPress()).orElse(false);
        if (doubleShift) {
            return false;
        }
        
        return INonStandPower.getNonStandPowerOptional(player).map(power -> {
            return power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
                boolean liquidWalking = hamon.isSkillLearned(ModHamonSkills.LIQUID_WALKING.get());
                if (liquidWalking) {
                    BlockPos blockPos = new BlockPos(player.position().add(0, -0.3, 0));
                    FluidState fluidBelow = world.getBlockState(blockPos).getFluidState();
                    Fluid fluidType = fluidBelow.getType();
                    if (!fluidBelow.isEmpty() && 
                            !(fluidType.is(FluidTags.WATER) && player.isOnFire())) {
                        if (power.getEnergy() > 0) {
                            player.setOnGround(true);
                            if (!world.isClientSide() || player.isLocalPlayer()) {
                                Vector3d deltaMovement = player.getDeltaMovement();
                                if (player.isShiftKeyDown()) {
                                    deltaMovement = new Vector3d(deltaMovement.x, 0, deltaMovement.z);
                                }
                                else {
                                    deltaMovement = new Vector3d(deltaMovement.x, Math.max(deltaMovement.y, 0), deltaMovement.z);
                                }
                                player.setDeltaMovement(deltaMovement);
                                player.fallDistance = 0;
                            }
    
                            if (!player.level.isClientSide()) {
                                if (fluidType.is(FluidTags.LAVA) 
                                        && !player.fireImmune() && !EnchantmentHelper.hasFrostWalker(player)) {
                                    player.hurt(DamageSource.HOT_FLOOR, 1.0F);
                                }
                                boolean wasWaterWalking = player.getCapability(PlayerUtilCapProvider.CAPABILITY)
                                        .map(cap -> cap.isWaterWalking()).orElse(false);
                                power.consumeEnergy(wasWaterWalking ? 1.0F : 50.0F);
                            }
                            return true;
                        }
                        else if (player.isLocalPlayer()) {
                            BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).triggerRedHighlight(19);
                        }
                    }
                }
                return false;
            }).orElse(false);
        }).orElse(false);
    }
    
    
    
    public static void chargeShotProjectile(Entity entity, World world) {
        if (!world.isClientSide()) {
            if (entity instanceof ProjectileEntity) {
                ProjectileEntity projectile = (ProjectileEntity) entity;
                if (projectile.getOwner() instanceof LivingEntity) {
                    LivingEntity shooter = (LivingEntity) projectile.getOwner();
                    if (projectile instanceof AbstractArrowEntity) {
                        // TODO stand effects on arrows
                        IStandPower.getStandPowerOptional(shooter).ifPresent(stand -> {
                            BowChargeEffectInstance<?, ?> bowCharge = stand.getBowChargeEffect();
                            if (bowCharge != null) {
                                bowCharge.onArrowShot((AbstractArrowEntity) projectile);
                            }
                        });
                    }
                    
                    ProjectileChargeProperties hamonChargeProperties = ProjectileChargeProperties.getChargeProperties(projectile);
                    if (hamonChargeProperties != null) {
                        projectile.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(projCharge -> {
                            
                            // projectiles charged by a hamon user
                            INonStandPower.getNonStandPowerOptional(shooter).ifPresent(power -> {
                                if (power.getEnergy() > 0) {
                                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                                        AbstractHamonSkill skillRequired = projectile instanceof AbstractArrowEntity
                                                ? ModHamonSkills.ARROW_INFUSION.get() : ModHamonSkills.THROWABLES_INFUSION.get();
                                        if (hamon.isSkillLearned(skillRequired)) {
                                            hamon.consumeHamonEnergyTo(efficiency -> {
                                                hamonChargeProperties.applyCharge(projCharge, efficiency, power);
                                                projCharge.setMultiplyWithUserStrength(true);
                                                return null;
                                            }, hamonChargeProperties.energyRequired);
                                        }
                                    });
                                }
                            });

                            // projectiles charged by an infused entity
                            shooter.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
                                if (cap.hasHamonCharge()) {
                                    HamonCharge hamonCharge = cap.getHamonCharge();
                                    hamonCharge.decreaseTicks((int) (hamonCharge.getInitialTicks() * hamonChargeProperties.energyRequired / 1000F));
                                    hamonChargeProperties.applyCharge(projCharge, hamonCharge.getTickDamage() * 5, null);
                                    projCharge.setMultiplyWithUserStrength(false);
                                }
                            });
                        });
                    }
                }
            }
            
            // charge chicken coming out of a charged egg
            if (entity instanceof ChickenEntity) {
                world.getCapability(WorldUtilCapProvider.CAPABILITY).resolve()
                .flatMap(worldCap -> worldCap.eggChargingChicken(entity)).ifPresent(eggEntity -> {
                    eggEntity.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(eggCharge -> {
                        entity.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(chickenCharge -> {
                            Entity eggThrower = eggEntity.getOwner();
                            LivingEntity throwerLiving = eggThrower instanceof LivingEntity ? (LivingEntity) eggThrower : null;
                            Optional<HamonData> userHamon = throwerLiving != null ? INonStandPower.getNonStandPowerOptional(throwerLiving)
                                    .map(power -> power.getTypeSpecificData(ModPowers.HAMON.get())).map(Optional::get)
                                    : Optional.empty();
                            chickenCharge.setHamonCharge(
                                    eggCharge.getHamonDamage() * userHamon.map(hamon -> hamon.getHamonDamageMultiplier() * hamon.getBloodstreamEfficiency()).orElse(1F), 
                                    Integer.MAX_VALUE, 
                                    throwerLiving, 0);
                        });
                    });
                });
            }
        }
    }
    
    public static final class ProjectileChargeProperties {
        public static final ProjectileChargeProperties ABSTRACT_ARROW = new ProjectileChargeProperties(1.5F, OptionalInt.of(10), 1000);
        public static final ProjectileChargeProperties SNOWBALL = new ProjectileChargeProperties(0.75F, OptionalInt.of(20), 500);
        public static final ProjectileChargeProperties EGG = new ProjectileChargeProperties(0.75F, OptionalInt.empty(), 200);
        public static final ProjectileChargeProperties WATER_BOTTLE = new ProjectileChargeProperties(1.0F, OptionalInt.of(30), 750);
        
        private final float baseMultiplier;
        private final OptionalInt chargeTicks;
        public final float energyRequired;
        
        private ProjectileChargeProperties(float baseMultiplier, OptionalInt chargeTicks, float energyRequired) {
            this.baseMultiplier = baseMultiplier;
            this.chargeTicks = chargeTicks;
            this.energyRequired = energyRequired;
        }
        
        public static boolean canBeChargedWithHamon(Entity entity) {
            return entity.getType() == EntityType.POTION // potion item hasn't been set yet, so we can't check if it's a water bottle
                    || ProjectileChargeProperties.getChargeProperties(entity) != null;
        }
        
        @Nullable 
        public static ProjectileChargeProperties getChargeProperties(Entity projectile) {
            if (projectile instanceof AbstractArrowEntity && !isChargedInOtherWay(projectile)) {
                return ABSTRACT_ARROW;
            }
            EntityType<?> type = projectile.getType();
            if (type == EntityType.SNOWBALL) {
                return SNOWBALL;
            }
            else if (type == EntityType.EGG) {
                return EGG;
            }
            else if (type == EntityType.POTION && MCUtil.isPotionWaterBottle((PotionEntity) projectile)) {
                return WATER_BOTTLE;
            }
            return null;
        }
        
        public void applyCharge(ProjectileHamonChargeCap chargeCap, float damageMultiplier, @Nullable INonStandPower spendingEnergy) {
            chargeCap.setBaseDmg(this.baseMultiplier * damageMultiplier);
            if (chargeTicks.isPresent()) {
                chargeCap.setMaxChargeTicks(chargeTicks.getAsInt());
            }
            else {
                chargeCap.setInfiniteChargeTime();
            }
            if (spendingEnergy != null) {
                chargeCap.setSpentEnergy(Math.min(spendingEnergy.getEnergy(), energyRequired));
            }
        }
    }
    
    private static boolean isChargedInOtherWay(Entity projectile) {
        return projectile.getType() == ModEntityTypes.CLACKERS.get();
    }
    
    public static void onProjectileImpact(Entity entity, RayTraceResult target) {
        entity.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.onTargetHit(target);
        });
    }
    
    
    public static void hamonChargedCreeperBlast(Explosion explosion, World world) {
        if (!world.isClientSide() && !(explosion instanceof HamonBlastExplosion)) {
            Entity exploder = explosion.getExploder();
            if (exploder != null) {
                exploder.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
                    if (cap.hasHamonCharge()) {
                        HamonCharge hamonCharge = cap.getHamonCharge();
                        float radius = CommonReflection.getRadius(explosion);
                        HamonUtil.hamonExplosion(exploder.level, exploder, 
                                hamonCharge.getUser((ServerWorld) world), explosion.getPosition(),
                                radius, hamonCharge.getTickDamage());
                    }
                });
            }
        }
    }
    
    
    public static void chargeItemEntity(PlayerEntity throwerPlayer, ItemEntity itemEntity) {
        if (!throwerPlayer.level.isClientSide()) {
            INonStandPower.getNonStandPowerOptional(throwerPlayer).ifPresent(power -> {
                if (power.getEnergy() > 0) {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        if (hamon.isSkillLearned(ModHamonSkills.PLANT_ITEM_INFUSION.get()) && isItemLivingMatter(itemEntity.getItem())) {
                            hamon.consumeHamonEnergyTo(hamonEfficiency -> {
                                int chargeTicks = 100 + MathHelper.floor((float) (1100 * hamon.getHamonStrengthLevel())
                                        / (float) HamonData.MAX_STAT_LEVEL * hamonEfficiency * hamonEfficiency);
                                
                                itemEntity.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> 
                                cap.setHamonCharge(0.1F * hamon.getHamonDamageMultiplier() * hamonEfficiency, chargeTicks, throwerPlayer, 200));
                                
                                return null;
                            }, 200);
                        }
                    });
                }
            });
        }
    }
    
    private static boolean isItemLivingMatter(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        
        Item item = itemStack.getItem();
        if (item instanceof BlockItem) {
            return HamonOrganismInfusion.isBlockLiving(((BlockItem) item).getBlock().defaultBlockState());
        }
        
        return item instanceof EggItem;
    }
    
    
    
    // one-time particle emit (like crit particles) + 'generic electricity sound'
    public static void emitHamonSparkParticles(World world, @Nullable PlayerEntity clientHandled, 
            double x, double y, double z, float intensity, @Nullable SoundEvent hamonSound) {
        if (intensity > 0) {
            intensity = Math.min(intensity, 4F);
            int count = Math.max((int) (intensity * 16.5F), 1);
            if (!world.isClientSide()) {
                ((ServerWorld) world).sendParticles(ModParticles.HAMON_SPARK.get(), x, y, z, count, 0.05, 0.05, 0.05, 0.25);
            }
            else if (clientHandled == ClientUtil.getClientPlayer()) {
                CustomParticlesHelper.createHamonSparkParticles(null, x, y, z, count);
            }
            if (hamonSound != null) {
                float volume = Math.min(intensity * 2, 1.0F);
                world.playSound(clientHandled, x, y, z, hamonSound, 
                        SoundCategory.AMBIENT, volume, 1.0F + (world.random.nextFloat() - 0.5F) * 0.15F);
            }
        }
    }
    
    public static void emitHamonSparkParticles(World world, @Nullable PlayerEntity clientHandled, double x, double y, double z, float intensity) {
        emitHamonSparkParticles(world, clientHandled, x, y, z, intensity, ModSounds.HAMON_SPARK.get());
    }
    
    public static void emitHamonSparkParticles(World world, @Nullable PlayerEntity clientHandled, Vector3d vec, float intensity) {
        emitHamonSparkParticles(world, clientHandled, vec.x, vec.y, vec.z, intensity);
    }
    
    public static void emitHamonSparkParticles(World world, @Nullable PlayerEntity clientHandled, Vector3d vec, float intensity, @Nullable SoundEvent hamonSound) {
        emitHamonSparkParticles(world, clientHandled, vec.x, vec.y, vec.z, intensity, hamonSound);
    }
    
    
    public static void createHamonSparkParticlesEmitter(Entity entity, float intensity) {
        createHamonSparkParticlesEmitter(entity, intensity, 1, ModParticles.HAMON_SPARK.get());
    }

    // particles emitter accompanied by 'generic electricity sound but longer'
    public static void createHamonSparkParticlesEmitter(Entity entity, float intensity, float soundVolumeMultiplier, IParticleData hamonParticle) {
        if (intensity > 0) {
            intensity = Math.min(intensity, 4F);
            World world = entity.level;
            if (!world.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(TrHamonParticlesPacket.emitter(entity.getId(), intensity, soundVolumeMultiplier, 
                        hamonParticle != ModParticles.HAMON_SPARK.get() ? hamonParticle : null), entity);
            }
            else {
                float volume = intensity * 2 * soundVolumeMultiplier;
                for (int i = (int) (intensity * 9.5F); i >= 0; i--) {
                    CustomParticlesHelper.createParticlesEmitter(entity, hamonParticle, Math.max(1, (int) (intensity * 9.5) - i));
                    if (i % 2 == 0 && i < 4) {
                        ClientTickingSoundsHelper.playHamonSparksSound(entity, Math.min(volume, 1.0F), 1.0F + (world.random.nextFloat() - 0.5F) * 0.15F);
                    }
                }
            }
        }
    }
}
