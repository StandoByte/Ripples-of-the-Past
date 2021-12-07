package com.github.standobyte.jojo.entity.stand;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.renderer.entity.stand.AdditionalArmSwing;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.entity.stand.task.BlockTask;
import com.github.standobyte.jojo.entity.stand.task.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.task.SummonLockTask;
import com.github.standobyte.jojo.entity.stand.task.UnsummonTask;
import com.github.standobyte.jojo.init.ModDataSerializers;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandSoundPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandSoundPacket.StandSoundType;
import com.github.standobyte.jojo.power.stand.IStandManifestation;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.util.EntityDistanceRayTraceResult;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.damage.IStandDamageSource;
import com.github.standobyte.jojo.util.damage.ModDamageSources;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;
import com.github.standobyte.jojo.util.damage.StandLinkDamageSource;
import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.network.play.server.SAnimateHandPacket;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.network.play.server.SRemoveEntityEffectPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

abstract public class StandEntity extends LivingEntity implements IStandManifestation, IEntityAdditionalSpawnData {
    private static List<Effect> SHARED_EFFECTS = new ArrayList<>();

    protected static final DataParameter<Byte> STAND_FLAGS = EntityDataManager.defineId(StandEntity.class, DataSerializers.BYTE);
    private static final DataParameter<Float> USER_MOVEMENT_FACTOR = EntityDataManager.defineId(StandEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<StandPose> STAND_POSE = EntityDataManager.defineId(StandEntity.class, (IDataSerializer<StandPose>) ModDataSerializers.STAND_POSE.get().getSerializer());

    private static final DataParameter<Float> ALPHA = EntityDataManager.defineId(StandEntity.class, DataSerializers.FLOAT);
    private float alphaOld;
    private float alpha;
    
    private final StandEntityType<?> type;
    private final double maxRange;
    private double basePrecision;

    private int summonPoseRandomByte;
    private boolean armsOnlyMode;
    public boolean showMainArm;
    public boolean showOffArm;
    
    private static final DataParameter<Integer> USER_ID = EntityDataManager.defineId(StandEntity.class, DataSerializers.INT);
    private WeakReference<LivingEntity> userRef = new WeakReference<LivingEntity>(null);
    private IStandPower userPower;
    protected StandRelativeOffset relativePos = new StandRelativeOffset(-0.5, -0.2, 0.2);

    private boolean accumulateTickParry;
    private int parryCount;
    private boolean alternateSwing;
    private boolean alternateAdditionalSwing;
    private int lastSwingTick = -2;
    private ArmSwings swings = new ArmSwings();

    private static final DataParameter<ActionTarget> TASK_TARGET = (DataParameter<ActionTarget>) EntityDataManager.defineId(StandEntity.class, (IDataSerializer<ActionTarget>) ModDataSerializers.TASK_TARGET.get().getSerializer());
    private StandEntityTask currentTask;
    private StandEntityTask scheduledTask;

    public StandEntity(StandEntityType<? extends StandEntity> type, World world) {
        super(type, world);
        this.type = type;
        setNoGravity(standHasNoGravity());
        setNoPhysics(standCanHaveNoPhysics());
        StandEntityStats stats = type.getStats();
        initStandAttributes(stats);
        this.maxRange = stats.getMaxRange();
        this.basePrecision = stats.getPrecision();
        this.summonPoseRandomByte = random.nextInt(128);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(USER_ID, -1);
        entityData.define(ALPHA, 1F);
        entityData.define(USER_MOVEMENT_FACTOR, 1F);
        entityData.define(STAND_FLAGS, (byte) 0);
        entityData.define(STAND_POSE, StandPose.SUMMON);
        entityData.define(TASK_TARGET, ActionTarget.EMPTY);
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> dataParameter) {
        super.onSyncedDataUpdated(dataParameter);
        if (STAND_FLAGS.equals(dataParameter)) {
            noPhysics = getStandFlag(StandFlag.NO_PHYSICS);
        }
        else if (USER_ID.equals(dataParameter)) {
            userRef = lookupUser();
            if (level.isClientSide()) {
                LivingEntity user = getUser();
                if (user != null) {
                    IStandPower standPower = IStandPower.getStandPowerOptional(user).resolve().get();
                    if (standPower.getStandManifestation() != this) {
                        standPower.setStandManifestation(this);
                    }
                }
            }
        }
    }



    public boolean isVisibleForAll() {
        return false;
    }

    protected boolean transfersDamage() {
        return true;
    }

    protected boolean standCanHaveNoPhysics() {
        return true;
    }

    protected boolean standHasNoGravity() {
        return true;
    }



    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.ATTACK_SPEED);
    }

    private void initStandAttributes(StandEntityStats stats) {
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(stats.getDamage());
        getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(stats.getAttackKnockback());
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(stats.getMovementSpeed());
        getAttribute(Attributes.ATTACK_SPEED).setBaseValue(stats.getAttackSpeed());
        getAttribute(Attributes.ARMOR).setBaseValue(stats.getArmor());
        getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(stats.getArmorToughness());
        getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(stats.getKnockbackResistance());
    }

    @Override
    public StandEntityType<?> getType() {
        return type;
    }


    public void setArmsOnlyMode() {
        setArmsOnlyMode(true, true);
    }

    public void setArmsOnlyMode(boolean showMainArm, boolean showOffArm) {
        if (level.isClientSide() || !isAddedToWorld()) {
            this.armsOnlyMode = true;
            this.showMainArm = showMainArm;
            this.showOffArm = showOffArm;
            scheduledTask = new UnsummonTask(this, getUser());
        }
    }

    public boolean isArmsOnlyMode() {
        return armsOnlyMode;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!level.isClientSide()) {
            if (!isArmsOnlyMode()) {
                setTask(new SummonLockTask(this));
            }
        }
    }



    public boolean requiresUser() {
        return true;
    }
    
    @Override
    public void setUser(LivingEntity user) {
        if (!level.isClientSide()) {
            entityData.set(USER_ID, user.getId());
        }
    }
    
    @Override
    public void setUserPower(IStandPower power) {
        this.userPower = power;
    }
    
    @Nullable
    public IStandPower getUserPower() {
        return userPower;
    }

    protected final boolean hasUser() {
        return entityData.get(USER_ID) >= 0;
    }

    @Nullable
    public LivingEntity getUser() {
        if (hasUser()) {
            return userRef == null ? null : userRef.get();
        }
        return null;
    }

    @Nullable
    private WeakReference<LivingEntity> lookupUser() {
        Entity user = level.getEntity(entityData.get(USER_ID));
        if (user instanceof LivingEntity) {
            return new WeakReference<LivingEntity>((LivingEntity) user);
        }
        return null;
    }

    @Override
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        LivingEntity user = getUser();
        PacketManager.sendToClient(new TrSetStandEntityPacket(user != null ? user.getId() : -1, getId()), player);
    }

    @Override
    public Team getTeam() {
        if (hasUser()) {
            LivingEntity user = getUser();
            if (user != null) {
                return user.getTeam();
            }
        }
        return super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (hasUser()) {
            LivingEntity user = getUser();
            if (entity == user) {
                return true;
            }
            if (user != null) {
                return user.isAlliedTo(entity);
            }
        }
        return super.isAlliedTo(entity);
    }



    public void setStandPose(StandPose pose) {
        entityData.set(STAND_POSE, pose);
    }

    public StandPose getStandPose() {
        return entityData.get(STAND_POSE);
    }

    public int getSummonPoseRandomByte() {
        return summonPoseRandomByte;
    }

    public static enum StandPose {
        NONE,
        SUMMON,
        BLOCK,
        RANGED_ATTACK,
        ABILITY
    }



    @Override
    public boolean isInvisible() {
        return !isVisibleForAll() || underInvisibilityEffect();
    }

    public boolean underInvisibilityEffect() {
        return super.isInvisible();
    }
    
    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        return !player.isSpectator() && (!isVisibleForAll() && !ClientUtil.shouldStandsRender(player) || underInvisibilityEffect());
    }

    @Override
    public double getVisibilityPercent(@Nullable Entity entity) {
        double percent = isVisibleForAll() || entity instanceof IMobStandUser ? 1.0 : 0.0;
        if (underInvisibilityEffect()) {
            percent *= 0.07;
        }
        return ForgeHooks.getEntityVisibilityMultiplier(this, entity, percent);
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        playSound(sound, volume, pitch, null);
    }
    
    private void playSound(SoundEvent sound, float volume, float pitch, @Nullable PlayerEntity player) {
        if (!this.isSilent()) {
            if (!isVisibleForAll()) {
                JojoModUtil.playSound(level, player, getX(), getY(), getZ(), sound, getSoundSource(), volume, pitch, StandUtil::isPlayerStandUser);
            }
            else {
                level.playSound(player, getX(), getY(), getZ(), sound, getSoundSource(), volume, pitch);
            }
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        if (isVisibleForAll()) {
            super.playStepSound(pos, blockIn);
        }
    }

    @Override
    protected void playSwimSound(float volume) {
        if (isVisibleForAll()) {
            super.playSwimSound(volume);
        }
    }
    
    public void playStandSound(StandSoundType soundType) {
        if (!level.isClientSide()) {
            PacketManager.sendToClientsTracking(new TrStandSoundPacket(getId(), soundType), this);
        }
        else {
            clientHandleStandSound(soundType, false);
        }
    }
    
    public void stopStandSound(StandSoundType soundType) {
        if (!level.isClientSide()) {
            PacketManager.sendToClientsTracking(TrStandSoundPacket.stopSound(getId(), soundType), this);
        }
        else {
            clientHandleStandSound(soundType, true);
        }
    }
    
    private final Set<StandSoundType> soundsToStop = EnumSet.noneOf(StandSoundType.class);
    protected void clientHandleStandSound(StandSoundType soundType, boolean stopTheSound) {
        if (stopTheSound) {
            soundsToStop.add(soundType);
        }
        else {
            soundsToStop.remove(soundType);
            SoundEvent sound = getType().getSound(soundType);
            if (sound != null) {
                ClientTickingSoundsHelper.playStandEntitySound(this, sound, soundType, 1.0F, 1.0F);
            }
        }
    }
    
    public boolean checkSoundStop(StandSoundType soundType) {
        return soundsToStop.remove(soundType);
    }



    @Override
    protected void actuallyHurt(DamageSource damageSrc, float damageAmount) {
        if (!damageSrc.isBypassArmor() && canBlockOrParryFromAngle(damageSrc)) {
            if (parryCount > 0) {
                parryCount--;
                return;
            }
            if (currentTask == null) {
                setTask(new BlockTask(5, this));
            }
        }
        if (transfersDamage() && hasUser()) {
            LivingEntity user = getUser();
            if (user != null && user.isAlive()) {
                if (!isInvulnerableTo(damageSrc)) {
                    damageAmount = ForgeHooks.onLivingHurt(this, damageSrc, damageAmount);
                    if (damageAmount <= 0) return;
                    damageAmount = getDamageAfterArmorAbsorb(damageSrc, damageAmount);
                    damageAmount = getDamageAfterMagicAbsorb(damageSrc, damageAmount);
                    if (isStandBlocking()) {
                        damageAmount = onBlockedAttack(damageAmount);
                    }
                    float damageAfterAbsorption = Math.max(damageAmount - getAbsorptionAmount(), 0.0F);
                    setAbsorptionAmount(getAbsorptionAmount() - (damageAmount - damageAfterAbsorption));
                    float absorbedDamage = damageAmount - damageAfterAbsorption;
                    if (absorbedDamage > 0.0F && absorbedDamage < 3.4028235E37F && damageSrc.getEntity() instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) damageSrc.getEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(absorbedDamage * 10.0F));
                    }
                    damageAfterAbsorption = ForgeHooks.onLivingDamage(this, damageSrc, damageAfterAbsorption);
                    if (damageAfterAbsorption != 0.0F) {
                        ModDamageSources.hurtThroughInvulTicks(user, new StandLinkDamageSource(this, damageSrc), damageAmount);
                    }
                }
            }
        }
        else {
            super.actuallyHurt(damageSrc, damageAmount);
        }
    }

    @Override
    public void knockback(float strength, double xRatio, double zRatio) {
        super.knockback(strength, xRatio, zRatio);
        LivingEntity user = getUser();
        if (user != null && user.isAlive()) {
            user.knockback(strength, xRatio, zRatio);
        }
    }



    public boolean canStartBlocking() {
        return currentTask == null || currentTask.canClearMidway();
    }

    public void blockTaskManual() {
        if (canStartBlocking()) {
            setTask(new BlockTask(Integer.MAX_VALUE, this));
            if (isArmsOnlyMode()) {
                setRelativePos(0, 0.1);
                setRelativeY(0);
            }
            else {
                setRelativePos(0, 0.3);
            }
        }
    }

    public boolean isStandBlocking() {
        return StandPose.BLOCK.equals(getStandPose());
    }

    protected float onBlockedAttack(float initialDamageAmount) {
        boolean reduceDamage = true;
        if (userPower != null) {
            reduceDamage = userPower.consumeMana(initialDamageAmount * type.getStats().getBlockStaminaCostForDmgPoint());
        }
        return reduceDamage ? initialDamageAmount * type.getStats().getBlockDmgFactor() : initialDamageAmount;
    }

    protected boolean canBlockOrParryFromAngle(DamageSource damageSrc) {
        if (!canUpdate()) {
            return false;
        }
        Vector3d dmgPosition = damageSrc.getSourcePosition();
        if (dmgPosition == null) {
            return false;
        }
        Vector3d viewVec = getViewVector(1.0F);
        Vector3d diffVec = dmgPosition.subtract(position()).normalize();
        return diffVec.dot(viewVec) > 0.866D;
    }



    @Override
    public boolean isInvulnerable() {
        if (!super.isInvulnerable()) {
            LivingEntity user = getUser();
            if (user != null) {
                return user.isInvulnerable();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSrc) {
        return this.is(damageSrc.getEntity()) || damageSrc != DamageSource.OUT_OF_WORLD && (isInvulnerable() 
                || !(damageSrc instanceof IStandDamageSource) && damageSrc != DamageSource.ON_FIRE 
                || damageSrc.isFire() && !level.getGameRules().getBoolean(GameRules.RULE_FIRE_DAMAGE));
    }

    @Override
    public void thunderHit(ServerWorld world, LightningBoltEntity lightningBolt) {}

    @Override
    protected void lavaHurt() {}

    @Override
    public void setSecondsOnFire(int seconds) {
        setRemainingFireTicks(-1);
    }

    public void setFireFromStand(int seconds) {
        super.setSecondsOnFire(seconds);
    }



    @Override
    public void tick() {
        super.tick();
        accumulateTickParry = false;
        LivingEntity user = getUser();

        ActionTarget target = getTaskTarget();
        if (target.getType() == TargetType.ENTITY) {
            Entity targetEntity = target.getEntity(level);
            if (targetEntity == null || !targetEntity.isAlive()) {
                setTaskTarget(ActionTarget.EMPTY);
            }
        }
        target = getTaskTarget();
        if (target.getType() != TargetType.EMPTY && !isManuallyControlled()) {
            JojoModUtil.rotateTowards(this, target.getTargetPos());
            if (target.getType() == TargetType.BLOCK) {
                setTaskTarget(ActionTarget.EMPTY);
            }
        }
        else if (user != null && !isRemotePositionFixed()) {
            float yRotSet = user.yRot;
            setRot(yRotSet, user.xRot);
            setYHeadRot(yRotSet);
        }

        if (!level.isClientSide()) {
            if (user != null) {
                setHealth(user.isAlive() ? user.getHealth() : 0);
            }
            else if (requiresUser()) {
                setHealth(0);
            }
            
            updatePosition(user);
            if (currentTask != null) {
                currentTask.onEntityTick();
            }
            swings.broadcastSwings(this);
        }
        else {
            alphaOld = alpha;
            alpha = entityData.get(ALPHA);
        }
    }




    public void updatePosition(LivingEntity user) {
        if (user != null) {
            if (isFollowingUser()) {
                Vector3d offset = relativePos.getAbsoluteVec(yRot);
                setPos(user.getX() + offset.x, 
                        user.getY() + (user.isShiftKeyDown() ? 0 : offset.y), 
                        user.getZ() + offset.z);
            }
            else if (isBeingRetracted()) {
                if (!isCloseToEntity(user)) {
                    setDeltaMovement(new Vector3d(user.getX() - getX(), user.getY() - getY(), user.getZ() - getZ())
                            .normalize().scale(getAttributeValue(Attributes.MOVEMENT_SPEED)));
                }
                else {
                    setStandFlag(StandFlag.BEING_RETRACTED, false);
                }
            }
        }
    }
    
    public void setRelativePos(double left, double front) {
        relativePos.left = left;
        relativePos.forward = front;
    }

    public void setRelativeY(double y) {
        relativePos.y = y;
    }



    public void setTask(StandEntityTask task) {
        if (task.getTicks() > 0) {
            if (getStandPose() == StandPose.SUMMON) {
                setStandPose(StandPose.NONE);
            }
            if (clearTask(false) && currentTask == null) {
                this.currentTask = task;
                task.afterInit();
            }
            else {
                this.scheduledTask = task;
            }
        }
    }
    
    public void clearTask() {
        clearTask(true);
    }

    protected boolean clearTask(boolean resetPos) {
        if (currentTask == null) {
            return true;
        }
        if (currentTask.clear()) {
            parryCount = 0;
            setTaskTarget(ActionTarget.EMPTY);
            updateNoPhysics();
            if (currentTask.resetPoseOnClear()) {
                setStandPose(StandPose.NONE);
            }
            if (resetPos && !isArmsOnlyMode()) {
                relativePos.reset();
            }
            currentTask = null;
            if (scheduledTask != null) {
                StandEntityTask nextTask = scheduledTask;
                scheduledTask = null;
                setTask(nextTask);
            }
            return true;
        }
        return false;
    }



    public void setTaskTarget(ActionTarget target) {
        if (target.getType() != TargetType.ENTITY || target.getEntity(level) != this) {
            entityData.set(TASK_TARGET, target);
        }
    }

    protected ActionTarget getTaskTarget() {
        return entityData.get(TASK_TARGET);
    }



    public void setUserMovementFactor(float userMovementFactor) {
        entityData.set(USER_MOVEMENT_FACTOR, MathHelper.clamp(userMovementFactor, 0F, 1F));
    }

    public float getUserMovementFactor() {
        return entityData.get(USER_MOVEMENT_FACTOR);
    }



    public boolean canAttackMelee() {
        return getAttackSpeed() > 0 && getMeleeAttackRange() > 0;
    }

    public double getTicksForSinglePunch() {
        return 20D / getAttackSpeed();
    }

    public void punch(boolean singlePunch) {
        if (!accumulateTickParry) {
            accumulateTickParry = true;
            parryCount = 1;
        }
        else {
            parryCount++;
        }
        RayTraceResult target = JojoModUtil.rayTrace(this, getMeleeAttackRange(), entity -> !(entity instanceof LivingEntity) || canAttack((LivingEntity) entity), getPrecision() * 0.5);
        switch (target.getType()) {
        case BLOCK:
            breakBlock(((BlockRayTraceResult) target).getBlockPos());
            break;
        case ENTITY:
            EntityDistanceRayTraceResult result = (EntityDistanceRayTraceResult) target;
            attackEntity(result.getEntity(), singlePunch, result.getTargetAABBDistance());
            break;
        default:
            break;
        }
    }
    
    @Override
    public boolean canAttack(LivingEntity entity) {
        LivingEntity user = getUser();
        return super.canAttack(entity) && !entity.is(this) && ((user == null || !entity.is(user) && user.canAttack(entity)));
    }
    
    public boolean canHarm(Entity target) {
        LivingEntity user = getUser();
        if (target instanceof LivingEntity) {
            if (!user.canAttack((LivingEntity) target)) {
                return false;
            }
            if (user instanceof PlayerEntity && target instanceof PlayerEntity && !((PlayerEntity) user).canHarmPlayer((PlayerEntity) target)) {
                return false;
            }
        }
        return true;
    }

    public double getMeleeAttackRange() {
        return 3.0D;
    }
    
    public void swingAlternateHands() {
        Hand hand = Hand.MAIN_HAND;
        if (tickCount - lastSwingTick > 1) {
            if (alternateSwing) {
                hand = Hand.OFF_HAND;
            }
            alternateSwing = !alternateSwing;
        }
        else {
            if (alternateAdditionalSwing) {
                hand = Hand.OFF_HAND;
            }
            alternateAdditionalSwing = !alternateAdditionalSwing;
        }
        swing(hand);
    }

    @Override
    public void swing(Hand hand) {
        if (tickCount - lastSwingTick > 1) {
            lastSwingTick = tickCount;
            super.swing(hand);
        }
        else if (!level.isClientSide()) {
            swings.addSwing(hand == Hand.MAIN_HAND ? getMainArm() : getOppositeToMainArm());
        }
    }
    
    public ArmSwings getAdditionalSwings() {
        return swings;
    }
    
    private List<AdditionalArmSwing> swingsWithOffsets = new LinkedList<>();
    public void clUpdateSwings(float timeDelta) {
        if (!swingsWithOffsets.isEmpty()) {
            Iterator<AdditionalArmSwing> iter = swingsWithOffsets.iterator();
            while (iter.hasNext()) {
                AdditionalArmSwing swing = iter.next();
                float anim = swing.addDelta(timeDelta);
                if (anim > 2F) {
                    iter.remove();
                }
            }
        }
        int count = swings.getSwingsCount();
        int bits = swings.getHandSideBits();
        swings.reset();
        for (int i = 0; i < count; i++) {
            swingsWithOffsets.add(new AdditionalArmSwing((float) i / (float) count, (bits & 1) == 1 ? HandSide.RIGHT : HandSide.LEFT, this));
            bits >>= 1;
        }
    }
    
    public List<AdditionalArmSwing> getSwingsWithOffsets() {
        return swingsWithOffsets;
    }

    @Override
    public HandSide getMainArm() {
        return HandSide.RIGHT;
    }

    private HandSide getOppositeToMainArm() {
        return getMainArm() == HandSide.RIGHT ? HandSide.LEFT : HandSide.RIGHT;
    }

    @Override
    public void aiStep() {
        updateSwingTime();
        super.aiStep();
    }

    @Override
    public void swing(Hand hand, boolean sendPacketToSelf) { // copypasted because can't override LivingEntity#getCurrentSwingDuration()
        if (!this.swinging || this.swingTime < 0 || this.swingTime >= getCurrentSwingDuration() / 2) {
            this.swingTime = -1;
            this.swinging = true;
            this.swingingArm = hand;
            if (this.level instanceof ServerWorld) {
                SAnimateHandPacket sanimatehandpacket = new SAnimateHandPacket(this, hand == Hand.MAIN_HAND ? 0 : 3);
                ServerChunkProvider serverchunkprovider = ((ServerWorld)this.level).getChunkSource();
                if (sendPacketToSelf) {
                    serverchunkprovider.broadcastAndSend(this, sanimatehandpacket);
                } else {
                    serverchunkprovider.broadcast(this, sanimatehandpacket);
                }
            }
        }
    }

    @Override   
    protected void updateSwingTime() {
        int i = getCurrentSwingDuration() + 1;
        if (this.swinging) {
            ++this.swingTime;
            if (this.swingTime >= i) {
                this.swingTime = 0;
                this.swinging = false;
            }
        }
        else {
            this.swingTime = 0;
        }
        this.attackAnim = (float)this.swingTime / (float)i;
    }
    
    @Override
    public float getAttackAnim(float partialTick) {
        float f = this.attackAnim - this.oAttackAnim;
        if (f < 0.0F) {
            ++f;
        }
        return this.oAttackAnim + f * partialTick;
    }

    private int getCurrentSwingDuration() {
        double attackSpeed = getAttackSpeed();
        return attackSpeed < 10D ? (int) (20D / attackSpeed) : 2;
    }

    public boolean attackEntity(Entity target, boolean strongAttack, double attackDistance) {
        if (!canHarm(target)) {
            return false;
        }
        float rangeFactor = rangeEfficiencyFactor();
        double precision = getPrecision();
        double damage = getAttackDamage(target, strongAttack, rangeFactor, attackDistance, precision);
        double additionalKnockback = getAttributeValue(Attributes.ATTACK_KNOCKBACK) * rangeFactor;
        StandEntityDamageSource dmgSource = new StandEntityDamageSource("player", this, getUserPower());
        if (strongAttack) {
            additionalKnockback *= 1 + damage / 5;
        }
        else {
            additionalKnockback = 0;
            dmgSource.setKnockbackReduction(0.1F);
        }
        boolean attacked = hurtTarget(target, dmgSource, (float) damage);
        if (attacked) {
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                if (additionalKnockback > 0.0F) {
                    livingTarget.knockback((float) additionalKnockback * 0.5F, (double) MathHelper.sin(yRot * ((float)Math.PI / 180F)), (double) (-MathHelper.cos(yRot * ((float)Math.PI / 180F))));
                }
                if (strongAttack && livingTarget.getUseItem().isShield(livingTarget) && target instanceof PlayerEntity) {
                    ((PlayerEntity) livingTarget).disableShield(precision > 0.5 && attackDistance < getMeleeAttackRange() * 0.4);
                }
                LivingEntity user = getUser();
                if (user != null) {
                    if (user.getType() == EntityType.PLAYER) {
                        livingTarget.setLastHurtByPlayer((PlayerEntity) user);
                        livingTarget.lastHurtByPlayerTime = 100;
                    }
                    else {
                        livingTarget.setLastHurtByMob(user);
                    }
                }
            }
            doEnchantDamageEffects(this, target);
            setLastHurtMob(target);
        }
        return attacked;
    }
    
    protected double getAttackDamage(Entity target, boolean strongAttack, double rangeFactor, double attackDistance, double precision) {
        double damage = getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (strongAttack) {
            double targetProximityRatio = Math.max(attackDistance / getMeleeAttackRange(), 0.2);
            if (targetProximityRatio < 0.4) {
                if (!isArmsOnlyMode()) {
                    damage *= 0.4 / targetProximityRatio;
                }
            }
            else if (targetProximityRatio > 0.6) {
                damage *= 0.6 / targetProximityRatio;
            }
        }
        else {
            damage *= 0.04;
            if (random.nextDouble() > precision) {
                damage *= 1 - (random.nextDouble() * (1 - precision));
            }
        }
        return damage * rangeFactor;
    }
    
    public double getPrecision() {
        if (isArmsOnlyMode()) {
            return 0;
        }
        return basePrecision * rangeEfficiencyFactor();
    }
    
    protected boolean hurtTarget(Entity target, DamageSource dmgSource, float damage) {
        return ModDamageSources.hurtThroughInvulTicks(target, dmgSource, (float) damage);
    }

    protected boolean breakBlock(BlockPos blockPos) {
        if (!JojoModUtil.canEntityDestroy(level, blockPos, this)) {
            return false;
        }
        BlockState blockState = level.getBlockState(blockPos);
        if (canBreakBlock(blockPos, blockState)) {
            LivingEntity user = getUser();
            level.destroyBlock(blockPos, !(user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild));
            return true;
        }
        else {
            SoundType soundType = blockState.getSoundType(level, blockPos, this);
            level.playSound(null, blockPos, soundType.getHitSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
            return false;
        }
    }
    
    protected boolean canBreakBlock(BlockPos blockPos, BlockState blockState) {
        float blockHardness = blockState.getDestroySpeed(level, blockPos);
        return blockHardness >= 0 && canBreakBlock(blockHardness, blockState.getHarvestLevel());
    }
    
    protected boolean canBreakBlock(float blockHardness, int blockHarvestLevel) {
        double blockBreakTier = getAttributeValue(Attributes.ATTACK_DAMAGE) / 2; // Star Platinum has 10 Attack Damage
        /* damage:
         * 2                                4                                   6                                   8                       10
         * 
         * hardness:
         * e^0 = 1                          e^1 ~ 2.718                         e^2 ~ 7.389                         e^3 ~ 20.086            e^4 ~ 54.598
         * 
         * 0.3:  glass, glowstone           1.25: terracota, basalt             2.8:  blue ice                      10:   hardened glass    22.5: ender chest
         * 0.4:  netherrack                 1.5:  stone                         3.5:  furnace                                               30:   ancient debris
         * 0.5:  dirt, ice, sand, hay       1.8:  concrete                      3:    ores, gold/lapis block                                50:   obsidian, netherite
         * 0.6:  clay, gravel               2:    bricks, cobblestone, wood     4.5:  deepslate ores
         * 0.8:  quartz, sandstone, wool    2.5:  chest                         5:    diamond/iron/redstone block
         * 1:    melon, mob head
         * 
         * harvest level:
         * -1: leaves/grass                 0: dirt/wood/stone                  1: iron                             2: diamond/gold         3: obsidian/netherite
         */
        return blockHardness <= Math.exp(blockBreakTier * rangeEfficiencyFactor() - 1) && blockHarvestLevel + 2 <= blockBreakTier;
    }
    
    public double getAttackSpeed() {
        double speed = getAttributeValue(Attributes.ATTACK_SPEED);
        if (isArmsOnlyMode()) {
            return speed * 0.5F;
        }
        return speed * rangeEfficiencyFactor();
    }

    public float rangeEfficiencyFactor() {
        LivingEntity user = getUser();
        if (user != null) {
            double distanceSq = distanceToSqr(user);
            double rangeSq = getMaxRange();
            rangeSq *= rangeSq;
            if (distanceSq > rangeSq / 4) {
                return (float) (rangeSq / distanceSq / 4);
            }
        }
        return 1.0F;
    }
    
    protected double getMaxRange() {
        return maxRange;
    }



    public boolean canAttackRanged() {
        return true;
    }

    public void rangedAttackTick(int ticks, boolean shift) {}

    public int rangedAttackDuration(boolean shift) {
        return 1;
    }



    protected void setStandFlag(StandFlag flag, boolean value) {
        byte i = entityData.get(STAND_FLAGS);
        if (value) {
            i |= flag.bit;
        } else {
            i &= ~flag.bit;
        }
        entityData.set(STAND_FLAGS, i);
    }

    protected boolean getStandFlag(StandFlag flag) {
        return (entityData.get(STAND_FLAGS) & flag.bit) != 0;
    }
    
    public static enum StandFlag {
        MANUAL_CONTROL,
        FIXED_REMOTE_POSITION,
        BEING_RETRACTED,
        NO_PHYSICS;

        private final byte bit;
        private StandFlag() {
            bit = (byte) (1 << ordinal());
        }
    }

    public void retractStand(boolean toUnsummon) {
        LivingEntity user = getUser();
        if (user != null) {
            setStandFlag(StandFlag.BEING_RETRACTED, true);
            if (toUnsummon && userPower.getHeldAction() == null) {
                setTask(new UnsummonTask(this, user));
            }
        }
    }

    public boolean isBeingRetracted() {
        return getStandFlag(StandFlag.BEING_RETRACTED);
    }

    public boolean isCloseToEntity(Entity entity) {
        return distanceToSqr(entity) < 3D;
    }

    public boolean isFollowingUser() {
        return !isManuallyControlled() && !isRemotePositionFixed() && !isBeingRetracted();
    }

    protected boolean shouldHaveNoPhysics() {
        return standCanHaveNoPhysics() && !isManuallyControlled() && !isRemotePositionFixed();
    }

    public void setManualControl(boolean manualControl, boolean fixRemotePosition) {
        setStandFlag(StandFlag.MANUAL_CONTROL, manualControl);
        fixRemotePosition = !manualControl && fixRemotePosition;
        setStandFlag(StandFlag.FIXED_REMOTE_POSITION, fixRemotePosition);
        if (!manualControl && !fixRemotePosition) {
            retractStand(false);
        }
        else {
            setStandFlag(StandFlag.BEING_RETRACTED, false);
        }
        updateNoPhysics();
    }

    public boolean isManuallyControlled() {
        return getStandFlag(StandFlag.MANUAL_CONTROL);
    }

    public boolean isRemotePositionFixed() {
        return getStandFlag(StandFlag.FIXED_REMOTE_POSITION);
    }

    public void updateNoPhysics() {
        setNoPhysics(shouldHaveNoPhysics());
    }

    public void setNoPhysics(boolean noPhysics) {
        setStandFlag(StandFlag.NO_PHYSICS, noPhysics);
    }



    @Override
    public boolean isControlledByLocalInstance() {
        if (isManuallyControlled()) {
            Entity user = getUser();
            if (user instanceof PlayerEntity) {
                return ((PlayerEntity) user).isLocalPlayer();
            }
        }
        return isManuallyControlled();
    }

    public void moveStandManually(float strafe, float forward, boolean jumping, boolean sneaking) {
        if (isManuallyControlled()) {
            double speed = getAttributeValue(Attributes.MOVEMENT_SPEED);
            double y = jumping ? speed : 0;
            if (sneaking) {
                y -= speed;
                strafe *= 0.5;
                forward *= 0.5;
            }
            setDeltaMovement(getAbsoluteMotion(new Vector3d((double)strafe, y, (double)forward), speed, this.yRot).scale(getUserMovementFactor()));
        }
    }

    private static Vector3d getAbsoluteMotion(Vector3d relative, double speed, float facingYRot) {
        double d0 = relative.lengthSqr();
        if (d0 < 1.0E-7D) {
            return Vector3d.ZERO;
        } else {
            Vector3d vec3d = relative.normalize().scale(speed);
            float yRotSin = MathHelper.sin(facingYRot * ((float)Math.PI / 180F));
            float yRotCos = MathHelper.cos(facingYRot * ((float)Math.PI / 180F));
            return new Vector3d(vec3d.x * (double)yRotCos - vec3d.z * (double)yRotSin, vec3d.y, vec3d.z * (double)yRotCos + vec3d.x * (double)yRotSin);
        }
    }

    @Override
    public void move(MoverType type, Vector3d vec) { // TODO allow stand to only phase through 1-block wide walls
        super.move(type, vec);
        LivingEntity user = getUser();
        if (user != null) {
            double distanceSqr = distanceToSqr(user);
            double rangeSq = getMaxRange();
            rangeSq *= rangeSq;
            if (distanceSqr > rangeSq) {
                super.move(MoverType.SELF, user.position().subtract(position()).scale(1 - rangeSq / distanceSqr));
            }
            if (!level.isClientSide() && isManuallyControlled() && distanceSqr > 728 && user instanceof PlayerEntity) {
                double horizontalDistSqr = distanceSqr - Math.pow(getY() - user.getY(), 2);
                int warningDistance = ((ServerWorld) level).getServer().getPlayerList().getViewDistance() * 16 - 4;
                if (horizontalDistSqr > warningDistance * warningDistance) {
                    ((PlayerEntity) user).getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                        cap.sendNotification(OneTimeNotification.HIGH_STAND_RANGE, new TranslationTextComponent("jojo.chat.message.view_distance_stand"));
                    });
                }
            }
        }
    }



    @Override
    protected void doPush(Entity entity) {
        if (!entity.is(getUser())) {
            super.doPush(entity);
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }
    


    public void setAlpha(float alpha) {
        entityData.set(ALPHA, MathHelper.clamp(alpha, 0, 1));
    }

    public float getAlpha(float partialTick) {
        float alphaLerp = MathHelper.lerp(partialTick, alphaOld, alpha);
        if (!isFollowingUser()) {
            alphaLerp *= rangeEfficiencyFactor(); 
        }
        return alphaLerp;
    }



    public static void addSharedEffects(Effect... effects) {
        Collections.addAll(SHARED_EFFECTS, effects);
    }
    
    public List<Effect> getEffectsSharedToStand() {
        return ImmutableList.copyOf(SHARED_EFFECTS);
    }

    @Override
    protected void onEffectAdded(EffectInstance effectInstance) {
        super.onEffectAdded(effectInstance);
        if (!level.isClientSide()) {
            LivingEntity user = getUser();
            if (user instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) user).connection.send(new SPlayEntityEffectPacket(this.getId(), effectInstance));
            }
            if (effectInstance.getEffect() == ModEffects.STUN.get()) {
                user.addEffect(new EffectInstance(effectInstance));
            }
        }
    }

    @Override
    protected void onEffectUpdated(EffectInstance effectInstance, boolean reapply) {
        super.onEffectUpdated(effectInstance, reapply);
        if (!level.isClientSide()) {
            LivingEntity user = getUser();
            if (user instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) user).connection.send(new SPlayEntityEffectPacket(this.getId(), effectInstance));
            }
            if (effectInstance.getEffect() == ModEffects.STUN.get()) {
                user.addEffect(new EffectInstance(effectInstance));
            }
        }
    }

    @Override
    protected void onEffectRemoved(EffectInstance effectInstance) {
        super.onEffectRemoved(effectInstance);
        if (!level.isClientSide()) {
            LivingEntity user = getUser();
            if (user instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) user).connection.send(new SRemoveEntityEffectPacket(this.getId(), effectInstance.getEffect()));
            }
        }
    }



    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        byte flags = 0;
        if (armsOnlyMode) {
            flags |= 1;
            if (showMainArm) {
                flags |= 2;
            }
            if (showOffArm) {
                flags |= 4;
            }
        }
        buffer.writeByte(flags);
        if (!armsOnlyMode) {
            buffer.writeVarInt(summonPoseRandomByte);
        }
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        byte flags = additionalData.readByte();
        if ((flags & 1) > 0) {
            setArmsOnlyMode();
            showMainArm = (flags & 2) > 0;
            showOffArm = (flags & 4) > 0;
        }
        if (!armsOnlyMode) {
            summonPoseRandomByte = additionalData.readVarInt();
        }
        
        playStandSound(StandSoundType.SUMMON);
    }

    // TODO nbt write/read for stands which will have save enabled
    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
    }

    private static final List<ItemStack> EMPTY_EQUIPMENT = Collections.emptyList();
    @Override
    public Iterable<ItemStack> getArmorSlots() { return EMPTY_EQUIPMENT; }

    @Override
    public boolean isAffectedByPotions() { return false; }

    @Override
    public ItemStack getItemBySlot(EquipmentSlotType slot) { return ItemStack.EMPTY; }

    @Override
    public void setItemSlot(EquipmentSlotType slot, ItemStack stack) {}

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier) { return false; }

    @Override
    public boolean onClimbable() { return false; }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    public boolean startRiding(Entity entity, boolean force) { return false; }
}
