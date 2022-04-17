package com.github.standobyte.jojo.entity.stand;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.action.actions.StandEntityHeavyAttack;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.renderer.entity.stand.AdditionalArmSwing;
import com.github.standobyte.jojo.entity.damaging.DamagingEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModDataSerializers;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandOffsetPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncStandTargetPacket;
import com.github.standobyte.jojo.power.stand.IStandManifestation;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.stats.StandStats;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.damage.IStandDamageSource;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;
import com.github.standobyte.jojo.util.damage.StandLinkDamageSource;
import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
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
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

abstract public class StandEntity extends LivingEntity implements IStandManifestation, IEntityAdditionalSpawnData {
    private static final List<Effect> SHARED_EFFECTS = new ArrayList<>();

    protected static final DataParameter<Byte> STAND_FLAGS = EntityDataManager.defineId(StandEntity.class, DataSerializers.BYTE);

    private final StandEntityType<?> type;
    private final double rangeEffective;
    private final double rangeMax;
    private double rangeEfficiency = 1;
    private double staminaCondition = 1;

    private int summonPoseRandomByte;
    private static final DataParameter<Byte> ARMS_ONLY_MODE = EntityDataManager.defineId(StandEntity.class, DataSerializers.BYTE);
    
    private static final DataParameter<Integer> USER_ID = EntityDataManager.defineId(StandEntity.class, DataSerializers.INT);
    private WeakReference<LivingEntity> userRef = new WeakReference<LivingEntity>(null);
    private IStandPower userPower;
    private StandRelativeOffset offsetDefault = StandRelativeOffset.withYOffset(-1, 0.2, -0.5);
    private StandRelativeOffset offsetDefaultArmsOnly = StandRelativeOffset.withYOffset(0, 0, 0.15);

    private static final DataParameter<Boolean> SWING_OFF_HAND = EntityDataManager.defineId(StandEntity.class, DataSerializers.BOOLEAN);
    private boolean alternateAdditionalSwing;
    private int lastSwingTick = -2;
    private final ArmSwings swings = new ArmSwings();
    public boolean barragePunchDelayed = false;
    public int barrageDelayedPunches = 0;
    private boolean accumulateBarrageTickParry;
    private int barrageParryCount;
    
    private static final DataParameter<Float> PUNCHES_COMBO = EntityDataManager.defineId(StandEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> LAST_HEAVY_PUNCH_COMBO = EntityDataManager.defineId(StandEntity.class, DataSerializers.FLOAT);
    private int noComboDecayTicks;
    private static final int COMBO_TICKS = 40;
    private static final float COMBO_DECAY = 0.025F;
    
    private static final DataParameter<Optional<StandEntityTask>> CURRENT_TASK = EntityDataManager.defineId(StandEntity.class, 
            (IDataSerializer<Optional<StandEntityTask>>) ModDataSerializers.STAND_ENTITY_TASK.get().getSerializer());
    // scheduled stand task
//    @Nullable
//    private StandEntityTask scheduledTask;
    public boolean rotatedTowardsTarget;
    
    protected StandPose standPose = StandPose.SUMMON;
    public int gradualSummonWeaknessTicks;
    public int unsummonTicks;
    public StandRelativeOffset unsummonOffset = offsetDefault.copy();
    public int summonLockTicks;
    private static final DataParameter<Integer> NO_BLOCKING_TICKS = EntityDataManager.defineId(StandEntity.class, DataSerializers.INT);
    
    public int overlayTickCount = 0;
    private int alphaTicks;

    public StandEntity(StandEntityType<? extends StandEntity> type, World world) {
        super(type, world);
        this.type = type;
        setNoGravity(standHasNoGravity());
        setNoPhysics(standCanHaveNoPhysics());
        StandStats stats = type.getStats();
        initStandAttributes(stats);
        this.rangeEffective = stats.getEffectiveRange();
        this.rangeMax = stats.getMaxRange();
        this.summonLockTicks = StandStatFormulas.getSummonLockTicks(stats.getBaseAttackSpeed());
        if (level.isClientSide()) {
            this.alphaTicks = this.summonLockTicks;
        }
        else {
            this.summonPoseRandomByte = random.nextInt(128);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(USER_ID, -1);
        entityData.define(STAND_FLAGS, (byte) 0);
        entityData.define(ARMS_ONLY_MODE, (byte) 0);
        entityData.define(SWING_OFF_HAND, false);
        entityData.define(PUNCHES_COMBO, 0F);
        entityData.define(LAST_HEAVY_PUNCH_COMBO, 0F);
        entityData.define(NO_BLOCKING_TICKS, 0);
        entityData.define(CURRENT_TASK, Optional.empty());
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
        else if (CURRENT_TASK.equals(dataParameter)) {
            StandEntityAction action = getCurrentTaskAction();
            if (action != null) {
                StandEntityAction.Phase phase = getCurrentTaskPhase();
                action.playSound(this, userPower, phase);
                action.onTaskSet(level, this, userPower, phase, this.getCurrentTask().getTicksLeft());
            }
            if (action != null || getStandPose() != StandPose.SUMMON) {
                setStandPose(action != null ? action.getStandPose(userPower, this) : StandPose.IDLE);
            }
        }
        else if (SWING_OFF_HAND.equals(dataParameter)) {
            swingingArm = entityData.get(SWING_OFF_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;
        }
        else if (ARMS_ONLY_MODE.equals(dataParameter)) {
            onArmsOnlyModeUpdated();
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
                .add(Attributes.ATTACK_SPEED)
                .add(ForgeMod.REACH_DISTANCE.get())
                .add(ModEntityAttributes.STAND_DURABILITY.get())
                .add(ModEntityAttributes.STAND_PRECISION.get());
    }

    private void initStandAttributes(StandStats stats) {
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(stats.getBasePower());
        getAttribute(Attributes.ATTACK_SPEED).setBaseValue(stats.getBaseAttackSpeed());
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(stats.getBaseMovementSpeed());
        getAttribute(ForgeMod.REACH_DISTANCE.get()).setBaseValue(getDefaultMeleeAttackRange());
        getAttribute(ModEntityAttributes.STAND_DURABILITY.get()).setBaseValue(stats.getBaseDurability());
        getAttribute(ModEntityAttributes.STAND_PRECISION.get()).setBaseValue(stats.getBasePrecision());
    }

    protected double getDefaultMeleeAttackRange() {
        return 2.5D;
    }
    
    public void modifiersFromResolveLevel(float ratio) {
        if (!level.isClientSide()) {
            StandStats stats = type.getStats();
            applyAttributeModifier(Attributes.ATTACK_DAMAGE, UUID.fromString("532a6cb6-0df0-44ea-a769-dba2db506545"), 
                    "Stand attack damage from experience", stats.getDevPower(ratio), Operation.ADDITION);
            
            applyAttributeModifier(Attributes.ATTACK_SPEED, UUID.fromString("51f253cd-b440-48b1-aa60-89913963df51"), 
                    "Stand attack speed from experience", stats.getDevAttackSpeed(ratio), Operation.ADDITION);
            
            applyAttributeModifier(Attributes.MOVEMENT_SPEED, UUID.fromString("cbb892a6-3390-4e75-b0a3-04cd253033e3"), 
                    "Stand movement speed from experience", stats.getDevMovementSpeed(ratio), Operation.ADDITION);
            
            applyAttributeModifier(ModEntityAttributes.STAND_DURABILITY.get(), UUID.fromString("d6979cd9-1481-49a0-bd5d-6922049448b4"), 
                    "Stand durability from experience", stats.getDevDurability(ratio), Operation.ADDITION);
            
            applyAttributeModifier(ModEntityAttributes.STAND_PRECISION.get(), UUID.fromString("1dcdb98e-00fa-42d0-8867-242165e49832"), 
                    "Stand precision from experience", stats.getDevPrecision(ratio), Operation.ADDITION);
        }
    }
    
    private void applyAttributeModifier(Attribute attribute, UUID modifierId, String name, double value, Operation operation) {
        ModifiableAttributeInstance attributeInstance = getAttribute(attribute);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(modifierId);
            if (value > 0) {
                attributeInstance.addTransientModifier(new AttributeModifier(modifierId, name, value, operation));
            }
        }
    }
    
    private static final AttributeModifier ATTACK_DAMAGE_ARMS_ONLY = new AttributeModifier(
            UUID.fromString("aaa82f0e-f1a7-47d1-9066-e1a025be02df"), "Stand attack damage with only arms", -0.25, Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier ATTACK_SPEED_ARMS_ONLY = new AttributeModifier(
            UUID.fromString("5b26b3d1-405c-402b-aee6-d5a0657386fe"), "Stand attack speed with only arms", -0.25, Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier DURABILITY_ARMS_ONLY = new AttributeModifier(
            UUID.fromString("244dd41c-aa40-4604-91f9-a788a40227ca"), "Stand durability with only arms", -0.5, Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier PRECISION_ARMS_ONLY = new AttributeModifier(
            UUID.fromString("f2d493e2-830a-4891-b756-65cc2be6f14f"), "Stand precision with only arms", -1, Operation.MULTIPLY_TOTAL);
    
    private void addArmsOnlyModifiers() {
        addModifier(getAttribute(Attributes.ATTACK_DAMAGE), ATTACK_DAMAGE_ARMS_ONLY);
        addModifier(getAttribute(Attributes.ATTACK_SPEED), ATTACK_SPEED_ARMS_ONLY);
        addModifier(getAttribute(ModEntityAttributes.STAND_DURABILITY.get()), DURABILITY_ARMS_ONLY);
        addModifier(getAttribute(ModEntityAttributes.STAND_PRECISION.get()), PRECISION_ARMS_ONLY);
    }
    
    private void removeArmsOnlyModifiers() {
        updateModifier(getAttribute(Attributes.ATTACK_DAMAGE), ATTACK_DAMAGE_ARMS_ONLY, false);
        updateModifier(getAttribute(Attributes.ATTACK_SPEED), ATTACK_SPEED_ARMS_ONLY, false);
        updateModifier(getAttribute(ModEntityAttributes.STAND_DURABILITY.get()), DURABILITY_ARMS_ONLY, false);
        updateModifier(getAttribute(ModEntityAttributes.STAND_PRECISION.get()), PRECISION_ARMS_ONLY, false);
    }
    
    protected final void addModifier(ModifiableAttributeInstance attribute, AttributeModifier modifier) {
        if (!attribute.hasModifier(modifier)) {
            attribute.addPermanentModifier(modifier);
        }
    }
    
    protected final void updateModifier(ModifiableAttributeInstance attribute, AttributeModifier modifier, boolean setModifier) {
        if (attribute.hasModifier(modifier)) {
            attribute.removeModifier(modifier);
        }
        if (setModifier) {
            attribute.addPermanentModifier(modifier);
        }
    }
    
    
    
    public double getAttackDamage() {
        double damage = getAttributeValue(Attributes.ATTACK_DAMAGE);
        return damage * rangeEfficiency * staminaCondition;
    }
    
    public double getAttackSpeed() {
        double speed = getAttributeValue(Attributes.ATTACK_SPEED);
        return speed * rangeEfficiency * staminaCondition;
    }
    
    public double getAttackKnockback() {
        double damage = getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        return damage * rangeEfficiency * staminaCondition;
    }
    
    public double getDurability() {
        double durability = getAttributeValue(ModEntityAttributes.STAND_DURABILITY.get());
        return durability * rangeEfficiency;
    }
    
    public double getPrecision() {
        double precision = getAttributeValue(ModEntityAttributes.STAND_PRECISION.get());
        return precision * rangeEfficiency * staminaCondition;
    }
    
    public float getLeapStrength() {
        return StandStatFormulas.getLeapStrength(leapBaseStrength() * rangeEfficiency * staminaCondition);
    }
    
    protected double leapBaseStrength() {
        return getAttributeValue(Attributes.ATTACK_DAMAGE);
    }
    

    
    public void setArmsOnlyMode() {
        setArmsOnlyMode(true, true);
    }

    public void setArmsOnlyMode(boolean showMainArm, boolean showOffArm) {
        if (!level.isClientSide()) {
            byte b = 3;
            if (showMainArm) {
                b |= 4;
            }
            if (showOffArm) {
                b |= 8;
            }
            entityData.set(ARMS_ONLY_MODE, b);
            
            addArmsOnlyModifiers();
        }
    }

    public boolean isArmsOnlyMode() {
        return (entityData.get(ARMS_ONLY_MODE) & 1) > 0;
    }
    
    public boolean wasSummonedAsArms() {
        return (entityData.get(ARMS_ONLY_MODE) & 2) > 0;
    }
    
    public boolean showArm(Hand hand) {
        switch (hand) {
        case MAIN_HAND:
            return (entityData.get(ARMS_ONLY_MODE) & 4) > 0;
        case OFF_HAND:
            return (entityData.get(ARMS_ONLY_MODE) & 8) > 0;
        default:
            return false;
        }
    }
    
    public void fullSummonFromArms() {
        if (!level.isClientSide() && isArmsOnlyMode()) {
            entityData.set(ARMS_ONLY_MODE, (byte) 2);
            // scheduled stand task
//            scheduledTask = null;
            StandEntityTask currentTask = getCurrentTask();
            if (currentTask != null) {
                StandEntityAction action = currentTask.getAction();
                if (action == ModActions.STAND_ENTITY_UNSUMMON.get()) {
                    stopTask();
                }
            }
        }
    }
    
    private void onArmsOnlyModeUpdated() {
        if (isArmsOnlyMode()) {
            summonLockTicks = 0;
        }
        else {
            if (level.isClientSide()) {
                overlayTickCount = 0;
                addSummonParticles();
            }
            if ((entityData.get(ARMS_ONLY_MODE) & 2) > 0) {
                gradualSummonWeaknessTicks = StandStatFormulas.getSummonLockTicks(getAttackSpeed());
                if (level.isClientSide()) {
                    alphaTicks = gradualSummonWeaknessTicks;
                }
                else if (gradualSummonWeaknessTicks == 0) {
                    removeArmsOnlyModifiers();
                }
                StandEntityTask currentTask = getCurrentTask();
                if (currentTask != null) {
                    StandEntityAction action = currentTask.getAction();
                    if (action != ModActions.STAND_ENTITY_UNSUMMON.get() || !hasEffect(ModEffects.STUN.get())) {
                        currentTask.setOffsetFromUser(action.getOffsetFromUser(this));
                    }
                }
            }
        }
    }
    
    protected void addSummonParticles() {}



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
        if (power != null) {
            modifiersFromResolveLevel(power.getStatsDevelopment());
        }
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
        this.standPose = pose;
    }

    public StandPose getStandPose() {
        return standPose;
    }

    public int getSummonPoseRandomByte() {
        return summonPoseRandomByte;
    }

    public static class StandPose {
        private final String name;
        
        public StandPose(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
        
        public static final StandPose IDLE = new StandPose("IDLE");
        public static final StandPose SUMMON = new StandPose("SUMMON");
        public static final StandPose BLOCK = new StandPose("BLOCK");
        public static final StandPose LIGHT_ATTACK = new StandPose("LIGHT_ATTACK");
        public static final StandPose HEAVY_ATTACK = new StandPose("HEAVY_ATTACK");
        public static final StandPose HEAVY_ATTACK_COMBO = new StandPose("HEAVY_ATTACK_COMBO");
        public static final StandPose RANGED_ATTACK = new StandPose("RANGED_ATTACK");
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
    
    public void playSound(SoundEvent sound, float volume, float pitch, @Nullable PlayerEntity player) {
        if (!this.isSilent()) {
            if (!isVisibleForAll()) {
                JojoModUtil.playSound(level, player, getX(), getY(), getZ(), sound, getSoundSource(), volume, pitch, StandUtil::isEntityStandUser);
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
    
    public void playStandSummonSound() {
        if (!level.isClientSide()) {
            SoundEvent sound = type.getSummonSound();
            if (sound != null) {
                playSound(sound, 1.0F, 1.0F);
            }
        }
    }
    
    public SoundEvent getStandUnsummonSound() {
        return type.getUnsummonSound();
    }
    

    
    @Override
    public boolean hurt(DamageSource dmgSource, float dmgAmount) {
        if (!level.isClientSide() && barrageParryCount > 0
                && !isInvulnerableTo(dmgSource) && !isDeadOrDying() 
                && !(dmgSource.isFire() && hasEffect(Effects.FIRE_RESISTANCE))
                && canBlockDamage(dmgSource) && canBlockOrParryFromAngle(dmgSource.getSourcePosition())
                && dmgSource instanceof StandEntityDamageSource) {
            int punchesIncoming = ((StandEntityDamageSource) dmgSource).getBarrageHitsCount();
            if (punchesIncoming > 0) {
                float parriableProportion = Math.min(StandStatFormulas.getMaxBarrageParryTickDamage(getDurability()) / dmgAmount, 1);
                int punchesCanParry = MathHelper.floor(parriableProportion * barrageParryCount);
                
                if (punchesCanParry > 0) {
                    Vector3d attackPos = this.getEyePosition(1.0F);
                    Entity attacker = dmgSource.getDirectEntity();
                    if (attacker != null) {
                        attackPos = attackPos.scale(0.5).add(attacker.getEyePosition(1.0F).scale(0.5));
                    }
                    else {
                        attackPos = attackPos.add(this.getLookAngle().scale(1.0));
                    }
                    ((ServerWorld) level).sendParticles(ParticleTypes.CRIT, 
                            attackPos.x, attackPos.y, attackPos.z, 1, 0.5D, 0.25D, 0.5D, 0.2D);
                    attacker.playSound(SoundEvents.GENERIC_HURT, 1.0F, 1.0F);
                    
                    if (punchesCanParry >= punchesIncoming) {
                        barrageParryCount -= punchesIncoming;
                        return false;
                    }
                    else {
                        dmgAmount *= (1 - (float) punchesCanParry / (float) punchesIncoming);
                        barrageParryCount -= punchesCanParry;
                    }
                }
            }
        }
        return super.hurt(dmgSource, dmgAmount);
    }
    
    @Override
    protected void actuallyHurt(DamageSource dmgSource, float damageAmount) {
        boolean blockableAngle = canBlockOrParryFromAngle(dmgSource.getSourcePosition());
        if (canBlockDamage(dmgSource) && blockableAngle && getCurrentTask() == null && canStartBlocking()) {
            setTask(ModActions.STAND_ENTITY_BLOCK.get(), 5, StandEntityAction.Phase.PERFORM, ActionTarget.EMPTY);
        }
        if (transfersDamage() && hasUser()) {
            LivingEntity user = getUser();
            if (user != null && user.isAlive()) {
                if (!isInvulnerableTo(dmgSource)) {
                    damageAmount = ForgeHooks.onLivingHurt(this, dmgSource, damageAmount);
                    if (damageAmount <= 0) return;
                    damageAmount = getDamageAfterArmorAbsorb(dmgSource, damageAmount);
                    damageAmount = getDamageAfterMagicAbsorb(dmgSource, damageAmount);
                    damageAmount = standDamageResistance(dmgSource, damageAmount, blockableAngle);
                    float damageAfterAbsorption = Math.max(damageAmount - getAbsorptionAmount(), 0.0F);
                    setAbsorptionAmount(getAbsorptionAmount() - (damageAmount - damageAfterAbsorption));
                    float absorbedDamage = damageAmount - damageAfterAbsorption;
                    if (absorbedDamage > 0.0F && absorbedDamage < 3.4028235E37F && dmgSource.getEntity() instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) dmgSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(absorbedDamage * 10.0F));
                    }
                    damageAfterAbsorption = ForgeHooks.onLivingDamage(this, dmgSource, damageAfterAbsorption);
                    if (damageAfterAbsorption != 0.0F) {
                        DamageUtil.hurtThroughInvulTicks(user, new StandLinkDamageSource(this, dmgSource), damageAmount);
                    }
                }
            }
        }
        else {
            super.actuallyHurt(dmgSource, damageAmount);
        }
    }

    @Override
    public void knockback(float strength, double xRatio, double zRatio) {
        LivingKnockBackEvent event = ForgeHooks.onLivingKnockBack(this, strength, xRatio, zRatio);
        if (event.isCanceled()) return;
        strength = event.getStrength();
        xRatio = event.getRatioX();
        zRatio = event.getRatioZ();
        strength *= 1.0F - (float) getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        if (isStandBlocking() && canBlockOrParryFromAngle(position().add(new Vector3d(xRatio, 0, zRatio)))) {
            strength *= 0.2F;
        }
        
        if (strength > 0) {
            hasImpulse = true;
            Vector3d motionVec = getDeltaMovement();
            Vector3d knockbackVec = new Vector3d(xRatio, 0.0D, zRatio).normalize().scale(strength);
            setDeltaMovement(
                    motionVec.x / 2.0D - knockbackVec.x, 
                    onGround ? Math.min(0.4D, motionVec.y / 2.0D + (double) strength) : motionVec.y, 
                    motionVec.z / 2.0D - knockbackVec.z);
        }

        LivingEntity user = getUser();
        if (user != null && user.isAlive()) {
            user.knockback(strength, xRatio, zRatio);
        }
    }



    public boolean canStartBlocking() {
        if (entityData.get(NO_BLOCKING_TICKS) > 0) {
            return false;
        }
        StandEntityTask currentTask = getCurrentTask();
        return currentTask == null || currentTask.getAction().isCancelable(userPower, this, currentTask.getPhase(), null);
    }

    public boolean isStandBlocking() {
        return StandPose.BLOCK.equals(getStandPose());
    }
    
    protected float standDamageResistance(DamageSource damageSrc, float damageAmount, boolean blockableAngle) {
        if (canBlockDamage(damageSrc)) {
            float blockedRatio = 0;
            if (blockableAngle && isStandBlocking() && userPower != null) {
                blockedRatio = 1F;
                if (userPower.usesStamina()) {
                    float staminaCost = StandStatFormulas.getBlockStaminaCost(damageAmount);
                    float stamina = userPower.getStamina();
                    if (!userPower.consumeStamina(staminaCost) && !StandUtil.standIgnoresStaminaDebuff(getUser())) {
                        blockedRatio = stamina / staminaCost;
                        standCrash();
                    }
                }
            }
            return damageAmount * (1 - getPhysicalResistance(blockedRatio));
        }
        return damageAmount;
    }
    
    protected float getPhysicalResistance(float blockedRatio) {
        return StandStatFormulas.getPhysicalResistance(getDurability(), getAttackDamage(), blockedRatio);
    }

    public boolean canBlockOrParryFromAngle(Vector3d dmgPosition) {
        if (!canUpdate()) {
            return false;
        }
        if (dmgPosition == null) {
            return false;
        }
        Vector3d viewVec = getViewVector(1.0F);
        Vector3d diffVec = dmgPosition.subtract(position()).normalize();
        return diffVec.dot(viewVec) > 0.7071D;
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
        return this.is(damageSrc.getEntity()) || damageSrc != DamageSource.OUT_OF_WORLD 
                && !damageSrc.getMsgId().contains("stand")
                && (isInvulnerable() 
                || !(damageSrc instanceof IStandDamageSource) && damageSrc != DamageSource.ON_FIRE 
                || damageSrc.isFire() && !level.getGameRules().getBoolean(GameRules.RULE_FIRE_DAMAGE))
                || getUser() instanceof PlayerEntity && ((PlayerEntity) getUser()).abilities.invulnerable && !damageSrc.isBypassInvul();
    }
    
    public boolean canBlockDamage(DamageSource dmgSource) {
        return dmgSource.getDirectEntity() != null && !dmgSource.isBypassArmor() && canUpdate() && !hasEffect(ModEffects.STUN.get());
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
        accumulateBarrageTickParry = false;
        LivingEntity user = getUser();
        
        if (user != null && !isRemotePositionFixed() && !rotatedTowardsTarget) {
            float yRotSet = user.yRot;
            setRot(yRotSet, user.xRot);
            setYHeadRot(yRotSet);
        }
        rotatedTowardsTarget = false;
        
        rangeEfficiency = user != null ? 
                StandStatFormulas.rangeStrengthFactor(rangeEffective, getMaxRange(), distanceTo(user))
                : 1;
        
        staminaCondition = StandUtil.standIgnoresStaminaDebuff(user) ? 
                        1
                        : 0.25 + Math.min((double) (userPower.getStamina() / userPower.getMaxStamina()) * 1.5, 0.75);

        updatePosition(user);
        
        if (isManuallyControlled()) {
            Vector3d manualMovementVec = getManualMovement();
            if (!Vector3d.ZERO.equals(manualMovementVec)) {
                move(MoverType.PLAYER, manualMovementVec);
                setDeltaMovement(Vector3d.ZERO);
            }
        }
        
        if (!level.isClientSide()) {
            swings.broadcastSwings(this);
            
            if (noComboDecayTicks > 0) {
                noComboDecayTicks--;
            }
            else {
                StandEntityAction currentAction = getCurrentTaskAction();
                if (currentAction == null || !currentAction.isCombatAction()) {
                    float decay = COMBO_DECAY;
                    float combo = entityData.get(PUNCHES_COMBO);
                    if (combo < 0.5F) {
                        decay *= 0.5F;
                    }
                    if (user != null && user.hasEffect(ModEffects.RESOLVE.get())) {
                        decay *= 0.5F;
                    }
                    setComboMeter(Math.max(combo - decay, 0));
                }
            }
            
            int noBlockingTicks = entityData.get(NO_BLOCKING_TICKS);
            if (noBlockingTicks > 0) {
                entityData.set(NO_BLOCKING_TICKS, noBlockingTicks - 1);
            }
        }
        
        if (summonLockTicks > 0) {
            summonLockTicks--;
        }
        else {
            boolean stun = hasEffect(ModEffects.STUN.get());
            StandEntityTask currentTask = getCurrentTask();
            if (currentTask != null && (!stun || currentTask.getAction().ignoresPerformerStun())) {
                currentTask.tick(userPower, this);
            }
            
            if (!stun && gradualSummonWeaknessTicks > 0) {
                gradualSummonWeaknessTicks--;
                
                if (gradualSummonWeaknessTicks == 0 && !level.isClientSide()) {
                    removeArmsOnlyModifiers();
                }
            }
        }


        if (!level.isClientSide()) {
            if (user != null) {
                setHealth(user.isAlive() ? user.getHealth() : 0);
            }
            else if (requiresUser()) {
                setHealth(0);
            }
        }
        else {
            if (user != null && isManuallyControlled() && !noPhysics && isInsideViewBlockingBlock()) {
                Vector3d vecToUser = user.position().subtract(position());
                if (vecToUser.lengthSqr() > 1) {
                    vecToUser = vecToUser.normalize();
                }
                moveWithoutCollision(vecToUser);
            }
            
            overlayTickCount++;
        }
    }




    private void updatePosition(LivingEntity user) {
        if (user != null/* && (!level.isClientSide() || isArmsOnlyMode())*/) {
            if (isFollowingUser()) {
                StandRelativeOffset relativeOffset = getOffsetFromUser();
                if (relativeOffset != null) {
                    Vector3d offset = relativeOffset.getAbsoluteVec(getDefaultOffsetFromUser(), yRot);
                    setPos(user.getX() + offset.x, 
                            user.getY() + (user.isShiftKeyDown() ? 0 : offset.y), 
                            user.getZ() + offset.z);
                }
            }
            else if (isBeingRetracted()) {
                if (!isCloseToEntity(user)) {
                    setDeltaMovement(new Vector3d(user.getX() - getX(), user.getY() - getY(), user.getZ() - getZ())
                            .normalize().scale(getAttributeValue(Attributes.MOVEMENT_SPEED)));
                }
                else {
                    setDeltaMovement(Vector3d.ZERO);
                    setStandFlag(StandFlag.BEING_RETRACTED, false);
                }
            }
        }
    }
    
    @Nullable
    protected StandRelativeOffset getOffsetFromUser() {
        if (Optional.ofNullable(getCurrentTaskAction()).map(action -> action.useDeltaMovement(userPower, this)).orElse(false)) {
            return null;
        }
        StandRelativeOffset offset = getDefaultOffsetFromUser();
        StandEntityTask currentTask = getCurrentTask();
        if (currentTask != null) {
            StandRelativeOffset taskOffset = currentTask.getOffsetFromUser();
            if (taskOffset != null) {
                offset = taskOffset;
            }
        }
        return offset;
    }
    
    public StandRelativeOffset getDefaultOffsetFromUser() {
        return isArmsOnlyMode() ? offsetDefaultArmsOnly : offsetDefault;
    }
    
    public void addBarrageOffset() {
        if (!isArmsOnlyMode()) {
            StandEntityTask currentTask = getCurrentTask();
            if (currentTask != null) {
                StandRelativeOffset offset = currentTask.getOffsetFromUser();
                double currentOffset = offset.getForward();
                double newOffset = Math.min(currentOffset + 0.025, rangeEffective);
                if (newOffset != currentOffset) {
                    offset = offset.copy(null, null, newOffset);
                    setTaskPosOffset(offset, true);
                }
            }
        }
    }
    
    public void setTaskPosOffset(double left, double forward) {
        setTaskPosOffset(StandRelativeOffset.noYOffset(left, forward), false);
    }
    
    public void setTaskPosOffset(double left, double y, double forward) {
        setTaskPosOffset(StandRelativeOffset.withYOffset(left, y, forward), false);
    }
    
    public void setTaskPosOffset(StandRelativeOffset offset, boolean sync) {
        StandEntityTask currentTask = getCurrentTask();
        if (currentTask != null) {
            currentTask.setOffsetFromUser(offset);
            if (sync && !level.isClientSide()) {
                PacketManager.sendToClientsTracking(new TrSetStandOffsetPacket(getId(), currentTask.getOffsetFromUser()), this);
            }
        }
    }

    private boolean isInsideViewBlockingBlock() {
        BlockPos.Mutable blockPos$mutable = new BlockPos.Mutable();
        for (int i = 0; i < 8; ++i) {
            double x = getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * getBbWidth() * 0.8F);
            double y = getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double z = getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * getBbWidth() * 0.8F);
            blockPos$mutable.set(x, y, z);
            BlockState blockState = level.getBlockState(blockPos$mutable);
            if (blockState.getRenderShape() != BlockRenderType.INVISIBLE && blockState.isViewBlocking(level, blockPos$mutable)) {
                return true;
            }
        }
        return false;
    }



    public boolean setTask(StandEntityAction action, int ticks, StandEntityAction.Phase phase, ActionTarget taskTarget) {
        return setTask(new StandEntityTask(this, action, ticks, phase, isArmsOnlyMode(), taskTarget));
    }

    protected boolean setTask(StandEntityTask task) {
        if (!level.isClientSide()) {
            Optional<StandRelativeOffset> offset = entityData.get(CURRENT_TASK).map(StandEntityTask::getOffsetFromUser);
            if (stopTask(task.getAction(), false) && getCurrentTask() == null) {
                offset.ifPresent(offsetPrevious -> task.setOffsetFromUser(offsetPrevious));
                entityData.set(CURRENT_TASK, Optional.of(task));
                if (task.getAction().enablePhysics) {
                    setNoPhysics(false);
                }
                userPower.consumeStamina(task.getAction().getStaminaCost(userPower));
                return true;
            }
            // scheduled stand task
//            else if (task.getAction().canBeScheduled(userPower, this)) {
//                this.scheduledTask = task;
//            }
        }
        return false;
    }
    
    public void stopTask() {
        stopTask(null, false);
    }
    
    protected void stopTask(boolean stopNonCancelable) {
        stopTask(null, stopNonCancelable);
    }
    
    private boolean stopTask(@Nullable StandEntityAction newAction, boolean stopNonCancelable) {
        if (!level.isClientSide()) {
            StandEntityTask currentTask = getCurrentTask();
            if (currentTask == null) {
                return true;
            }
            if (currentTask.getTicksLeft() <= 0 || stopNonCancelable
                    || currentTask.getAction().isCancelable(userPower, this, currentTask.getPhase(), newAction)) {
                clearTask(currentTask, newAction);
                return true;
            }
        }
        return false;
    }
    
    protected void clearTask(StandEntityTask clearedTask, @Nullable StandEntityAction newAction) {
        barrageParryCount = 0;
        updateNoPhysics();
        setStandPose(StandPose.IDLE);
        clearedTask.getAction().onClear(userPower, this);
        entityData.set(CURRENT_TASK, Optional.empty());
        // isn't called on client
        if (userPower.clickQueuedAction()) {
            StandEntityTask nextTask = getCurrentTask();
            if (nextTask != null && clearedTask.getOffsetFromUser() != null) {
                nextTask.setOffsetFromUser(clearedTask.getOffsetFromUser());
            }
        }
        else if (newAction == null) {
            if (isArmsOnlyMode()) {
                StandEntityAction unsummon = ModActions.STAND_ENTITY_UNSUMMON.get();
                setTask(new StandEntityTask(this, unsummon, unsummon.getStandActionTicks(userPower, this), 
                        StandEntityAction.Phase.PERFORM, isArmsOnlyMode(), ActionTarget.EMPTY));
            }
            else if (getUser() != null && !isCloseToEntity(getUser()) && isFollowingUser()) {
                retractStand(false);
            }
        }
    }
    
    public void stopTaskWithRecovery() {
        StandEntityTask currentTask = getCurrentTask();
        if (currentTask != null) {
            currentTask.moveToPhase(StandEntityAction.Phase.RECOVERY, userPower, this);
        }
    }
    
    public boolean hasTask() {
        return getCurrentTask() != null;
    }
    
    @Nullable
    public StandEntityTask getCurrentTask() {
        return entityData.get(CURRENT_TASK).orElse(null);
    }
    
    @Nullable
    public StandEntityAction getCurrentTaskAction() {
        StandEntityTask task = getCurrentTask();
        if (task != null) {
            return task.getAction();
        }
        return null;
    }
    
    @Nullable
    public StandEntityAction.Phase getCurrentTaskPhase() {
        StandEntityTask task = getCurrentTask();
        if (task != null) {
            return task.getPhase();
        }
        return null;
    }
    
    public float getCurrentTaskCompletion(float partialTick) {
        StandEntityTask task = getCurrentTask();
        if (task != null) {
            return task.getTaskCompletion(partialTick);
        }
        return 0;
    }

    public float getUserMovementFactor() {
        StandEntityAction currentAction = getCurrentTaskAction();
        if (currentAction == null) {
            return 1.0F;
        }
        return currentAction.getUserMovementFactor(userPower, this);
    }


    
    public RayTraceResult aimWithStandOrUser(double reachDistance, ActionTarget currentTarget) {
        RayTraceResult aim;
        if (!isManuallyControlled()) {
            LivingEntity user = getUser();
            if (user != null && currentTarget.getType() != TargetType.ENTITY) {
                aim = precisionRayTrace(user, reachDistance);
                if (JojoModUtil.isAnotherEntityTargeted(aim, this)
                        || currentTarget.getType() == TargetType.EMPTY && aim.getType() != RayTraceResult.Type.MISS) {
                    rotateTowards(ActionTarget.fromRayTraceResult(aim).getTargetPos(), true);
                }
            }
        }
        aim = precisionRayTrace(this, reachDistance);
        return aim;
    }
    
    public RayTraceResult precisionRayTrace(Entity aimingEntity) {
        return precisionRayTrace(aimingEntity, getAimDistance(aimingEntity));
    }
    
    public double getAimDistance(@Nullable Entity aimingEntity) {
        double reachDistance = getAttributeValue(ForgeMod.REACH_DISTANCE.get());
        if (!isManuallyControlled() && aimingEntity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) aimingEntity;
            if (livingEntity.getAttribute(ForgeMod.REACH_DISTANCE.get()) != null) {
                reachDistance = livingEntity.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
            }
        }
        return reachDistance;
    }

    public Predicate<Entity> canTarget() {
        return entity -> !entity.is(this) && !entity.is(getUser());
    }
    
    public RayTraceResult precisionRayTrace(Entity aimingEntity, double reachDistance) {
        return JojoModUtil.rayTrace(aimingEntity, 
                reachDistance, canTarget(), 0.25, getPrecision());
    }
    
    public boolean canAttackMelee() {
        return getAttackSpeed() > 0 && getAttributeValue(ForgeMod.REACH_DISTANCE.get()) > 0 ;
    }

    public boolean punch(PunchType punch, ActionTarget target, StandEntityAction action) {
        return punch(punch, target, action, 1);
    }

    public boolean barrageTickPunches(ActionTarget target, StandEntityAction action, int barrageHits) {
        return punch(PunchType.BARRAGE, target, action, barrageHits);
    }

    public boolean punch(PunchType punch, ActionTarget target, StandEntityAction action, int barrageHits) {
        if (punch == PunchType.BARRAGE) {
            if (!accumulateBarrageTickParry) {
                accumulateBarrageTickParry = true;
                barrageParryCount = barrageHits + 1;
            }
            else {
                barrageParryCount += barrageHits;
            }
        }
        
        ActionTarget finalTarget = ActionTarget.fromRayTraceResult(aimWithStandOrUser(getAimDistance(getUser()), target));
        target = finalTarget.getType() != TargetType.EMPTY && isTargetInReach(finalTarget) ? finalTarget : ActionTarget.EMPTY;
        setTaskTarget(target);
        
        return attackTarget(target, punch, action, barrageHits);
    }
    
    public boolean attackTarget(ActionTarget target, PunchType punch, StandEntityAction action, int barrageHits) {
        boolean punched;
        switch (target.getType()) {
        case BLOCK:
            punched = breakBlock(target.getBlockPos());
            break;
        case ENTITY:
            Entity entity = target.getEntity(level);
            punched = attackEntity(entity, punch, action, barrageHits);
            break;
        default:
            punched = false;
            break;
        }
        return punched;
    }
    
    public void rotateTowards(Vector3d target, boolean limitBySpeed) {
        JojoModUtil.rotateTowards(this, target, limitBySpeed ? (float) getAttackSpeed() / 16F * 18F : 360F);
    }
    
    public double getDistanceToTarget(ActionTarget target) {
        double distance = 0;
        switch (target.getType()) {
        case BLOCK:
            BlockPos blockPos = target.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            VoxelShape blockShape = blockState.getCollisionShape(level, blockPos);
            if (!blockShape.isEmpty()) {
                AxisAlignedBB aabb = blockShape.bounds().move(blockPos);
                distance = JojoModUtil.getDistance(this, aabb);
            }
            break;
        case ENTITY:
            AxisAlignedBB aabb = target.getEntity(level).getBoundingBox();
            distance = JojoModUtil.getDistance(this, aabb);
            break;
        default:
            break;
        }
        return distance;
    }
    
    public boolean isTargetInReach(ActionTarget target) {
        return getDistanceToTarget(target) <= getAttributeValue(ForgeMod.REACH_DISTANCE.get());
    }
    
    public void setTaskTarget(ActionTarget target) {
        if (target != null) {
            StandEntityTask task = getCurrentTask();
            if (task != null) {
                task.setTarget(this, target);
                if (!level.isClientSide()) {
                    PacketManager.sendToClientsTracking(new TrSyncStandTargetPacket(getId(), target), this);
                }
                else {
                    target.cacheEntity(level);
                }
            }
        }
    }
    
    public enum PunchType {
        HEAVY_NO_COMBO,
        HEAVY_COMBO,
        LIGHT,
        BARRAGE
    }
    
    @Override
    public boolean canAttack(LivingEntity entity) {
        LivingEntity user = getUser();
        return super.canAttack(entity) && !entity.is(this) && ((user == null || !entity.is(user) && user.canAttack(entity)));
    }
    
    public boolean canHarm(Entity target) {
        LivingEntity user = getUser();
        if (target instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) target;
            if (!user.canAttack(targetLiving)) {
                return false;
            }
            if (user instanceof PlayerEntity && targetLiving instanceof PlayerEntity && !((PlayerEntity) user).canHarmPlayer((PlayerEntity) targetLiving)) {
                return false;
            }
            return canAttack(targetLiving);
        }
        return true;
    }
    
    public Hand alternateHands() {
        Hand hand = Hand.MAIN_HAND;
        if (tickCount - lastSwingTick > 1) {
            boolean offHand = entityData.get(SWING_OFF_HAND);
            if (offHand) {
                hand = Hand.OFF_HAND;
            }
            entityData.set(SWING_OFF_HAND, !offHand);
        }
        else {
            if (alternateAdditionalSwing) {
                hand = Hand.OFF_HAND;
            }
            alternateAdditionalSwing = !alternateAdditionalSwing;
        }
        return hand;
    }

    @Override
    public void swing(Hand hand) {
        if (tickCount - lastSwingTick > 1) {
            lastSwingTick = tickCount;
            super.swing(hand);
        }
        else {
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
                if (anim > AdditionalArmSwing.MAX_ANIM_DURATION) {
                    iter.remove();
                }
            }
        }
        int count = swings.getSwingsCount();
        long bits = swings.getHandSideBits();
        swings.reset();
        for (int i = 0; i < count; i++) {
            double maxOffset = 0.9 / (getPrecision() / 16 + 1) - 0.9 / 11;
            swingsWithOffsets.add(new AdditionalArmSwing((float) i / (float) count, (bits & 1) == 1 ? HandSide.RIGHT : HandSide.LEFT, this, maxOffset));
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
    
    public HandSide getSwingingHand() {
        return swingingArm == Hand.MAIN_HAND ? getMainArm() : getOppositeToMainArm();
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
    
    public float getComboMeter() {
        if (userPower != null && !userPower.isComboUnlocked()) {
            return 0;
        }
        return entityData.get(PUNCHES_COMBO);
    }
    
    public void addComboMeter(float combo, int noDecayTicks) {
        if (getUser() != null && getUser().hasEffect(ModEffects.RESOLVE.get())) {
            combo *= 2F;
        }
        setComboMeter(getComboMeter() + combo);
        this.noComboDecayTicks = Math.max(this.noComboDecayTicks, noDecayTicks);
    }
    
    protected void setComboMeter(float combo) {
        entityData.set(PUNCHES_COMBO, MathHelper.clamp(combo, 0F, 1F));
    }
    
    public void setHeavyPunchCombo() {
        entityData.set(LAST_HEAVY_PUNCH_COMBO, getComboMeter());
    }
    
    public float getLastHeavyPunchCombo() {
        return entityData.get(LAST_HEAVY_PUNCH_COMBO);
    }
    
    public boolean willHeavyPunchCombo() {
        return getComboMeter() >= 0.5F;
    }
    
    public boolean isHeavyComboPunching() {
        return getLastHeavyPunchCombo() >= 0.5F;
    }
    
    public boolean attackEntity(Entity target, PunchType punch, StandEntityAction action, int barrageHits) {
        return attackEntity(target, punch, action, barrageHits, null);
    }

    public boolean attackEntity(Entity target, PunchType punch, StandEntityAction action, int barrageHits, @Nullable Consumer<StandAttackProperties> attackOverride) {
        if (!canHarm(target)) {
            return false;
        }
        StandEntityDamageSource dmgSource = new StandEntityDamageSource("stand", this, getUserPower());
        
        if (punch == PunchType.BARRAGE) {
            dmgSource.setBarrageHitsCount(barrageHits);
        }

        double strength = getAttackDamage();
        double precision = getPrecision();
        double attackRange = getAttributeValue(ForgeMod.REACH_DISTANCE.get());
        double distance = JojoModUtil.getDistance(this, target.getBoundingBox());
        double knockback = getAttackKnockback();
        
        StandAttackProperties attack = standAttackProperties(punch, target, action, strength, precision, attackRange, distance, knockback, barrageHits);
        
        if (attackOverride != null) {
            attackOverride.accept(attack);
        }

        boolean attacked = doAttack(target, attack, dmgSource, StandAttackProperties::getDamage);
        
        if (attacked) {
            if (attack.isSweepingAttack()) {
                for (LivingEntity sweepingTarget : level.getEntitiesOfClass(LivingEntity.class, attack.sweepingAttackAabb(target.getBoundingBox()), 
                        e -> !e.isSpectator() && e.isPickable()
                        && JojoModUtil.getDistance(this, e.getBoundingBox()) < getAttributeValue(ForgeMod.REACH_DISTANCE.get()) && this.canHarm(e))) {
                    doAttack(sweepingTarget, attack, dmgSource, StandAttackProperties::getSweepingDamage);
                }
            }
            
            addComboMeter(attack.getAddCombo(), COMBO_TICKS);
            if (!isManuallyControlled()) {
                setLastHurtMob(target);
            }
        }
        return attacked;
    }
    
    protected StandAttackProperties standAttackProperties(PunchType punchType, Entity target, StandEntityAction action,
            double strength, double precision, double attackRange, double distance, double knockback, int barrageHits) {
        StandAttackProperties attack = new StandAttackProperties();
        
        switch (punchType) {
        case LIGHT:
            attack
            .damage(StandStatFormulas.getLightAttackDamage(strength))
            .addCombo(0.2F);
            if (getComboMeter() == 0) {
                attack.parryTiming(StandStatFormulas.getParryTiming(precision));
            }
            break;
        case HEAVY_NO_COMBO:
        case HEAVY_COMBO:
            float heavyAttackCombo = getLastHeavyPunchCombo();
//          float targetProximityRatio = 1 - (float) (distance / attackRange);
            
            attack
            .damage(StandStatFormulas.getHeavyAttackDamage(strength, target instanceof LivingEntity ? (LivingEntity) target : null))
            .addKnockback(1 + (float) strength / 4 * heavyAttackCombo)
            .setStandInvulTime(10);
            break;
        case BARRAGE:
            attack
            .damage(StandStatFormulas.getBarrageHitDamage(strength, precision) * barrageHits)
            .addCombo(0.0025F * barrageHits)
            .reduceKnockback(0.1F);
            break;
        }
        return attack;
    }
    
    protected final boolean doAttack(Entity target, StandAttackProperties attack, StandEntityDamageSource dmgSource, Function<StandAttackProperties, Float> damageMethod) {
        if (attack.reducesKnockback()) {
            dmgSource.setKnockbackReduction(attack.getKnockbackReduction());
        }

        float damage = damageMethod.apply(attack);
        LivingEntity targetLiving = null;
        if (target instanceof LivingEntity) {
            targetLiving = (LivingEntity) target;
            
            damage = DamageUtil.addArmorPiercing(damage, attack.getArmorPiercing(), targetLiving);
            
            dmgSource.setStandInvulTicks(attack.getStandInvulTime());
            
            if (target instanceof StandEntity) {
                StandEntity targetStand = (StandEntity) target;
                
                if ((attack.canParryHeavyAttack() || attack.disablesBlocking())) {
                    if (attack.canParryHeavyAttack()) {
                        if (targetStand.getCurrentTaskAction() instanceof StandEntityHeavyAttack
                                && targetStand.getCurrentTaskPhase() == StandEntityAction.Phase.WINDUP
                                && targetStand.canBlockOrParryFromAngle(dmgSource.getSourcePosition())
                                && 1F - targetStand.getCurrentTaskCompletion(0) < attack.getHeavyAttackParryTiming()) {
                            targetStand.parryHeavyAttack();
                            return false;
                        }
                    }
                    
                    if (attack.disablesBlocking() && random.nextFloat() < attack.getDisableBlockingChance()) {
                        targetStand.stopStandBlocking(StandStatFormulas.getBlockingBreakTicks(targetStand.getDurability()));
                    }
                }
            }
            
            final float dmg = damage;
            damage = targetLiving.getCapability(LivingUtilCapProvider.CAPABILITY).map(cap -> {
                return cap.onStandAttack(dmg);
            }).orElse(damage);
        }
        
        if (damage <= 0) {
            return false;
        }
        
        boolean hurt = hurtTarget(target, dmgSource, damage);
        
        if (hurt) {
            if (targetLiving != null) {
                if (attack.getAdditionalKnockback() > 0) {
                    Vector3d vecToTarget = target.position().subtract(this.position());
                    float knockbackYRot = (float) -MathHelper.atan2(vecToTarget.x, vecToTarget.z) * MathUtil.RAD_TO_DEG + attack.getKnockbackYRotDeg();
                    float knockbackXRot = attack.getKnockbackXRot();
                    float knockbackStrength = attack.getAdditionalKnockback() * 0.5F;
                    if (Math.abs(knockbackXRot) < 90) {
                        targetLiving.knockback(
                                knockbackStrength * MathHelper.cos(knockbackXRot * MathUtil.DEG_TO_RAD), 
                                (double) MathHelper.sin(knockbackYRot * MathUtil.DEG_TO_RAD), 
                                (double) (-MathHelper.cos(knockbackYRot * MathUtil.DEG_TO_RAD)));
                    }
                    if (knockbackXRot != 0) {
                        DamageUtil.upwardsKnockback(targetLiving, -knockbackStrength * MathHelper.sin(knockbackXRot * MathUtil.DEG_TO_RAD));
                    }
                }
                
                LivingEntity user = getUser();
                if (user != null) {
                    if (user.getType() == EntityType.PLAYER) {
                        targetLiving.setLastHurtByPlayer((PlayerEntity) user);
                        targetLiving.lastHurtByPlayerTime = 100;
                    }
                    LivingEntity aggroTo = isFollowingUser() || targetLiving.canSee(user) ? user : 
                                StandUtil.isEntityStandUser(targetLiving) ? this : null;
                    if (aggroTo != null) {
                        targetLiving.setLastHurtByMob(aggroTo);
                    }
                }

                if (attack.disablesBlocking() && 
                        targetLiving.getUseItem().isShield(targetLiving) && targetLiving instanceof PlayerEntity) {
                    DamageUtil.disableShield((PlayerEntity) targetLiving, attack.getDisableBlockingChance());
                }
            }
            doEnchantDamageEffects(this, target);
        }
        
        return hurt;
    }

    protected boolean hurtTarget(Entity target, DamageSource dmgSource, float damage) {
        return DamageUtil.hurtThroughInvulTicks(target, dmgSource, damage
                * JojoModConfig.getCommonConfigInstance(false).standDamageMultiplier.get().floatValue());
    }
    
    protected void parryHeavyAttack() {
        if (!level.isClientSide()) {
            stopTask(true);
            addEffect(new EffectInstance(ModEffects.STUN.get(), 20));
            playSound(ModSounds.PARRY.get(), 1.0F, 1.0F);
        }
    }
    
    protected void stopStandBlocking(int lockTicks) {
        if (!level.isClientSide() && isStandBlocking()) {
            entityData.set(NO_BLOCKING_TICKS, lockTicks);
            stopTask(true);
            playSound(ModSounds.PARRY.get(), 1.0F, 1.0F);
        }
    }
    
    protected void standCrash() {
        if (!level.isClientSide()) {
            stopTask(true);
            addEffect(new EffectInstance(ModEffects.STUN.get(), 40));
        }
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
        return StandStatFormulas.isBlockBreakable(getAttackDamage(), blockHardness, blockHarvestLevel);
    }

    public double getMaxRange() {
        return rangeMax;
    }
    
    public double getRangeEfficiency() {
        return rangeEfficiency;
    }

    

    public void addProjectile(DamagingEntity projectile) {
        if (!level.isClientSide() && !projectile.isAddedToWorld()) {
            projectile.setDamageFactor((float) getAttackDamage() / 8);
            level.addFreshEntity(projectile);
        }
    }
    
    public void shootProjectile(ModdedProjectileEntity projectile, float velocity, float inaccuracy) {
        if (!level.isClientSide() && !projectile.isAddedToWorld()) {
            projectile.shootFromRotation(this, velocity, getProjectileInaccuracy(inaccuracy));
            addProjectile(projectile);
        }
    }

    public float getProjectileInaccuracy(float inaccuracyBase) {
        return Math.max((inaccuracyBase + 1) * 8 / Math.max((float) getPrecision(), 4) - 1, 0);
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
                startStandUnsummon();
            }
        }
    }
    
    private void startStandUnsummon() {
        StandEntityAction unsummon = ModActions.STAND_ENTITY_UNSUMMON.get();
        setTask(unsummon, unsummon.getStandActionTicks(userPower, this), StandEntityAction.Phase.PERFORM, ActionTarget.EMPTY);
    }
    
    public void tickUnsummonOffset() {
        if (this.unsummonTicks == 0) {
            unsummonOffset = getOffsetFromUser();
        }
        else {
            Vector3d offsetVec = unsummonOffset.toRelativeVec();
            offsetVec = offsetVec.normalize().scale(Math.max(offsetVec.length() - 0.075, 0));
            unsummonOffset.setFromRelativeVec(offsetVec);
        }
        this.setTaskPosOffset(unsummonOffset, false);
    }

    public boolean isBeingRetracted() {
        return getStandFlag(StandFlag.BEING_RETRACTED);
    }

    public boolean isCloseToEntity(Entity entity) {
        return distanceToSqr(entity) < 2.25;
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
        if (noPhysics == true || standCanHaveNoPhysics()) {
            setStandFlag(StandFlag.NO_PHYSICS, noPhysics);
        }
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
            if (Optional.ofNullable(getCurrentTaskAction()).map(action -> !action.useDeltaMovement(userPower, this)).orElse(true)) {
                double speed = getAttributeValue(Attributes.MOVEMENT_SPEED);
                double y = jumping ? speed : 0;
                if (sneaking) {
                    y -= speed;
                    strafe *= 0.5;
                    forward *= 0.5;
                }
                manualMovement = getAbsoluteMotion(new Vector3d((double)strafe, y, (double)forward), speed, this.yRot).scale(getUserMovementFactor());
            }
            else {
                manualMovement = Vector3d.ZERO;
            }
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
    public void move(MoverType type, Vector3d vec) {
        super.move(type, vec);
        LivingEntity user = getUser();
        if (user != null && user.level == this.level) {
            double distanceSqr = distanceToSqr(user);
            double rangeSq = getMaxRange();
            rangeSq *= rangeSq;
            if (distanceSqr > rangeSq) {
                Vector3d vecToUser = user.position().subtract(position()).scale(1 - rangeSq / distanceSqr);
                moveWithoutCollision(vecToUser);
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
    
    private void moveWithoutCollision(Vector3d vec) {
        setBoundingBox(getBoundingBox().move(vec));
        setLocationFromBoundingbox();
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }
    
    private Vector3d manualMovement = Vector3d.ZERO;
    
    private Vector3d getManualMovement() {
        if (!isManuallyControlled()) {
            return Vector3d.ZERO;
        }
        return manualMovement;
    }



    @Override
    protected void doPush(Entity entity) {
        if (!entity.is(getUser())) {
            super.doPush(entity);
        }
    }
    
    @Override
    public boolean isPickable() {
        return super.isPickable() && 
                (!level.isClientSide() || !ClientUtil.getClientPlayer().is(getUser()));
    }

    @Override
    public boolean isPushable() {
        return false;
    }
    


    public float getAlpha(float partialTick) {
        float alpha = 1F;
        int ticks = summonLockTicks > 0 ? summonLockTicks : gradualSummonWeaknessTicks;
        if (ticks > 0 && alphaTicks > 0) {
            alpha = (float) (alphaTicks - ticks) / (float) alphaTicks;
        }
        else {
            StandEntityTask currentTask = getCurrentTask();
            if (currentTask != null) {
                alpha = currentTask.getAction().getStandAlpha(this, currentTask.getTicksLeft(), partialTick);
            }
        }
        if (!isFollowingUser()) {
            alpha *= rangeEfficiency; 
        }
        return MathHelper.clamp(alpha, 0F, 1F);
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
                stopTask();
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
    public void startSeenByPlayer(ServerPlayerEntity player) {
        super.startSeenByPlayer(player);
        if (player.is(getUser())) {
            StandUtil.setManualControl(player, false, false);
        }
    }
    
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeVarInt(summonPoseRandomByte);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        summonPoseRandomByte = additionalData.readVarInt();
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

    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    @Override
    public Iterable<ItemStack> getArmorSlots() { return armorItems; }

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
