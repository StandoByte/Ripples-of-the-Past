package com.github.standobyte.jojo.entity.stand;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.action.stand.IHasStandPunch;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.action.stand.punch.IPunch;
import com.github.standobyte.jojo.action.stand.punch.StandBlockPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.action.stand.punch.StandMissedPunch;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.BarrageSwingsHolder;
import com.github.standobyte.jojo.client.sound.BarrageHitSoundHandler;
import com.github.standobyte.jojo.entity.damaging.DamagingEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.init.ModDataSerializers;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandTaskTargetPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandManifestation;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.IModdedDamageSource;
import com.github.standobyte.jojo.util.mc.damage.IStandDamageSource;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;
import com.github.standobyte.jojo.util.mc.damage.StandLinkDamageSource;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

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
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
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
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class StandEntity extends LivingEntity implements IStandManifestation, IEntityAdditionalSpawnData {
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
    private StandRelativeOffset offsetDefault = StandRelativeOffset.withYOffset(-0.75, 0.2, -0.75);
    private StandRelativeOffset offsetDefaultArmsOnly = StandRelativeOffset.withYOffset(0, 0, 0.15);

    private static final DataParameter<Boolean> SWING_OFF_HAND = EntityDataManager.defineId(StandEntity.class, DataSerializers.BOOLEAN);
    private boolean alternateAdditionalSwing;
    private int lastSwingTick = -2;
    private static final DataParameter<Integer> BARRAGE_CLASH_OPPONENT_ID = EntityDataManager.defineId(StandEntity.class, DataSerializers.INT);
    public final BarrageHandler barrageHandler = new BarrageHandler(this);
    
    private float blockDamage = 0;
    private float prevBlockDamage = 0;
    
    private static final DataParameter<Float> FINISHER_VALUE = EntityDataManager.defineId(StandEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> LAST_HEAVY_FINISHER_VALUE = EntityDataManager.defineId(StandEntity.class, DataSerializers.FLOAT);
    private int noFinisherDecayTicks;
    public static final int FINISHER_NO_DECAY_TICKS = 40;
    private static final float FINISHER_DECAY = 0.025F;
    
    private static final DataParameter<Optional<StandEntityTask>> CURRENT_TASK = EntityDataManager.defineId(StandEntity.class, 
            (IDataSerializer<Optional<StandEntityTask>>) ModDataSerializers.STAND_ENTITY_TASK.get().getSerializer());
    // scheduled stand task
//    @Nullable
//    private StandEntityTask scheduledTask;
    private StandEntityAction inputBuffer;
    private Optional<StandEntityTask> lastTask = Optional.empty();
    
    static final DataParameter<Byte> MANUAL_MOVEMENT_LOCK = EntityDataManager.defineId(StandEntity.class, DataSerializers.BYTE);
    private ManualStandMovementLock manualMovementLocks = new ManualStandMovementLock(this);
    
    protected StandPose standPose = StandPose.SUMMON;
    public int gradualSummonWeaknessTicks;
    public int unsummonTicks;
    public StandRelativeOffset unsummonOffset = offsetDefault.copy();
    public int summonLockTicks;
    private static final DataParameter<Integer> NO_BLOCKING_TICKS = EntityDataManager.defineId(StandEntity.class, DataSerializers.INT);
    
    public int overlayTickCount = 0;
    private int alphaTicks;

    private IPunch lastPunch;
    private BarrageSwingsHolder<?, ?> barrageSwings;
    private final BarrageHitSoundHandler barrageSounds;

    public float lastMotionTiltTick = -1;
//    public Vector3d motionVec = Vector3d.ZERO;
//    public double motionDist = 0;
//    public double prevMotionDist = 0;
    
    public Vector3d prevTiltVec = Vector3d.ZERO;
    public Vector3d tiltVec = Vector3d.ZERO;
    
    public static final DataParameter<Optional<ResourceLocation>> DATA_PARAM_STAND_SKIN = EntityDataManager.defineId(StandEntity.class, 
            (IDataSerializer<Optional<ResourceLocation>>) ModDataSerializers.OPTIONAL_RES_LOC.get().getSerializer());
    
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
            this.barrageSounds = initBarrageHitSoundHandler();
        }
        else {
            this.summonPoseRandomByte = random.nextInt(128);
            this.barrageSounds = null;
        }
        init(this);
    }
    
    private <T extends StandEntity> void init(T thisEntity) {
        if (level.isClientSide()) {
            this.barrageSwings = new BarrageSwingsHolder<T, StandEntityModel<T>>();
        }
        else {
            this.barrageSwings = null;
        }
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(USER_ID, -1);
        entityData.define(STAND_FLAGS, (byte) 0);
        entityData.define(ARMS_ONLY_MODE, (byte) 0);
        entityData.define(SWING_OFF_HAND, false);
        entityData.define(BARRAGE_CLASH_OPPONENT_ID, -1);
        entityData.define(FINISHER_VALUE, 0F);
        entityData.define(LAST_HEAVY_FINISHER_VALUE, 0F);
        entityData.define(NO_BLOCKING_TICKS, 0);
        entityData.define(CURRENT_TASK, Optional.empty());
        entityData.define(MANUAL_MOVEMENT_LOCK, (byte) 0);
        entityData.define(DATA_PARAM_STAND_SKIN, Optional.empty());
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> dataParameter) {
        super.onSyncedDataUpdated(dataParameter);
        if (STAND_FLAGS.equals(dataParameter)) {
            noPhysics = getStandFlag(StandFlag.NO_PHYSICS);
        }
        else if (USER_ID.equals(dataParameter)) {
            updateUserFromNetwork(entityData.get(USER_ID));
        }
        else if (CURRENT_TASK.equals(dataParameter)) {
            Optional<StandEntityTask> taskOptional = getCurrentTask();
            
            taskOptional.ifPresent(task -> {
                task.resolveEntityTarget(level);
                StandEntityAction action = task.getAction();
                StandEntityAction.Phase phase = task.getPhase();
                action.playSound(this, userPower, phase, task);
                action.onTaskSet(level, this, userPower, phase, task, task.getTicksLeft());
                setStandPose(action.getStandPose(userPower, this, task));
            });
            if (!taskOptional.isPresent()) {
                if (getStandPose() != StandPose.SUMMON) {
                    setStandPose(StandPose.IDLE);
                }
            }
            
            lastTask.ifPresent(task -> task.getAction().taskStopped(level, this, userPower, task, taskOptional.map(StandEntityTask::getAction).orElse(null)));
            lastTask = taskOptional;
            
            taskOptional.ifPresent(task -> {
                task.phaseTransition(this, userPower, null, task.getPhase(), task.getTicksLeft());
            });
        }
        else if (SWING_OFF_HAND.equals(dataParameter)) {
            swingingArm = entityData.get(SWING_OFF_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;
        }
        else if (ARMS_ONLY_MODE.equals(dataParameter)) {
            onArmsOnlyModeUpdated();
        }
        else if (BARRAGE_CLASH_OPPONENT_ID.equals(dataParameter)) {
            barrageHandler.clashOpponent = Optional.ofNullable(level.getEntity(entityData.get(BARRAGE_CLASH_OPPONENT_ID)));
        }
        else if (MANUAL_MOVEMENT_LOCK.equals(dataParameter)) {
            manualMovementLocks.onEntityDataUpdated(this);
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

    protected void initStandAttributes(StandStats stats) {
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(stats.getBasePower());
        getAttribute(Attributes.ATTACK_SPEED).setBaseValue(stats.getBaseAttackSpeed());
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(stats.getBaseMovementSpeed());
        getAttribute(ForgeMod.REACH_DISTANCE.get()).setBaseValue(getDefaultMeleeAttackRange());
        getAttribute(ModEntityAttributes.STAND_DURABILITY.get()).setBaseValue(stats.getBaseDurability());
        getAttribute(ModEntityAttributes.STAND_PRECISION.get()).setBaseValue(stats.getBasePrecision());
        getAttribute(Attributes.ARMOR).setBaseValue(stats.getArmor());
        getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(stats.getArmorToughness());
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
            attributeInstance.addTransientModifier(new AttributeModifier(modifierId, name, value, operation));
        }
    }
    
    private static final AttributeModifier ATTACK_DAMAGE_ARMS_ONLY = new AttributeModifier(
            UUID.fromString("aaa82f0e-f1a7-47d1-9066-e1a025be02df"), "Stand attack damage with only arms", -0.25, Operation.MULTIPLY_TOTAL);
//    private static final AttributeModifier ATTACK_SPEED_ARMS_ONLY = new AttributeModifier(
//            UUID.fromString("5b26b3d1-405c-402b-aee6-d5a0657386fe"), "Stand attack speed with only arms", -0.25, Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier DURABILITY_ARMS_ONLY = new AttributeModifier(
            UUID.fromString("244dd41c-aa40-4604-91f9-a788a40227ca"), "Stand durability with only arms", -0.25, Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier PRECISION_ARMS_ONLY = new AttributeModifier(
            UUID.fromString("f2d493e2-830a-4891-b756-65cc2be6f14f"), "Stand precision with only arms", -1, Operation.MULTIPLY_TOTAL);
    
    private void addArmsOnlyModifiers() {
        addModifier(getAttribute(Attributes.ATTACK_DAMAGE), ATTACK_DAMAGE_ARMS_ONLY);
//        addModifier(getAttribute(Attributes.ATTACK_SPEED), ATTACK_SPEED_ARMS_ONLY);
        addModifier(getAttribute(ModEntityAttributes.STAND_DURABILITY.get()), DURABILITY_ARMS_ONLY);
        addModifier(getAttribute(ModEntityAttributes.STAND_PRECISION.get()), PRECISION_ARMS_ONLY);
    }
    
    private void removeArmsOnlyModifiers() {
        updateModifier(getAttribute(Attributes.ATTACK_DAMAGE), ATTACK_DAMAGE_ARMS_ONLY, false);
//        updateModifier(getAttribute(Attributes.ATTACK_SPEED), ATTACK_SPEED_ARMS_ONLY, false);
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
        return damage * getStandEfficiency();
    }
    
    public double getAttackSpeed() {
        double speed = getAttributeValue(Attributes.ATTACK_SPEED);
        return speed * getStandEfficiency();
    }
    
    public double getAttackKnockback() {
        double damage = getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        return damage * getStandEfficiency();
    }
    
    public double getDurability() {
        double durability = getAttributeValue(ModEntityAttributes.STAND_DURABILITY.get());
        if (ModPowers.VAMPIRISM.get().isHighOnBlood(getUser())) {
            durability *= 2;
        }
        return durability * getStandEfficiency();
    }
    
    public double getPrecision() {
        double precision = getAttributeValue(ModEntityAttributes.STAND_PRECISION.get());
        return precision * getStandEfficiency();
    }
    
    public double getAttackRange() {
        return getAttributeValue(ForgeMod.REACH_DISTANCE.get());
    }
    
    public double getProximityRatio(Entity target) {
        double attackRange = getAttackRange();
        return attackRange > 0 ? 1 - JojoModUtil.getDistance(this, target.getBoundingBox()) / attackRange : 0;
    }
    
    public float getLeapStrength() {
        return StandStatFormulas.getLeapStrength(leapBaseStrength() * getStandEfficiency());
    }
    
    // FIXME ATTACK_DAMAGE is not syncable, therefore the client doesn't know about the strength stat lvling
    protected double leapBaseStrength() {
        return getAttributeValue(Attributes.ATTACK_DAMAGE);
    }
    
    public double getStaminaCondition() {
        return staminaCondition;
    }
    
    public double getStandEfficiency() {
        return staminaCondition * rangeEfficiency;
    }
    

    
    public void setArmsOnlyMode() {
        setArmsOnlyMode(true, true);
    }
    
    public void addToArmsOnly(Hand arm) {
        if (arm != null && !level.isClientSide() && isArmsOnlyMode()) {
            byte b = entityData.get(ARMS_ONLY_MODE);
            switch (arm) {
            case MAIN_HAND:
                b |= 4;
                break;
            case OFF_HAND:
                b |= 8;
                break;
            }
            entityData.set(ARMS_ONLY_MODE, b);
        }
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
            getCurrentTask().ifPresent(task -> {
                StandEntityAction action = task.getAction();
                if (action == ModStandsInit.UNSUMMON_STAND_ENTITY.get()) {
                    stopTask();
                }
            });
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
            }
        }
    }
    
    protected void addSummonParticles() {}



    public boolean requiresUser() {
        return true;
    }
    
    @Override
    public void setUserAndPower(LivingEntity user, IStandPower power) {
        if (!level.isClientSide()) {
            entityData.set(USER_ID, user.getId());
        }

        this.userPower = power;
        if (!level.isClientSide() && power != null) {
            modifiersFromResolveLevel(power.getStatsDevelopment());
            setStandSkin(power.getStandInstance().flatMap(StandInstance::getSelectedSkin));
        }
    }
    
    public void onStandSummonServerSide() {
        LivingEntity user = getUser();
        if (user != null) {
            for (Effect effect : SHARED_EFFECTS_FROM_USER) {
                EffectInstance userEffectInstance = user.getEffect(effect);
                if (userEffectInstance != null) {
                    addEffect(new EffectInstance(userEffectInstance));
                }
            }
        }
    }
    
    private void updateUserFromNetwork(int userId) {
        userRef = lookupUser(userId);
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
    private WeakReference<LivingEntity> lookupUser(int userId) {
        Entity user = level.getEntity(userId);
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
        if (this.standPose != pose) {
            if (level.isClientSide() && pose == StandPose.BARRAGE) {
                getBarrageSwingsHolder().resetSwingTime();
            }
            this.standPose = pose;
        }
    }

    public StandPose getStandPose() {
        return standPose;
    }

    public int getSummonPoseRandomByte() {
        return summonPoseRandomByte;
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
        return !isVisibleForAll() && !StandUtil.clStandEntityVisibleTo(player) || !player.isSpectator() && underInvisibilityEffect();
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
        playSound(sound, volume, pitch, player, position());
    }
    
    public void playSound(SoundEvent sound, float volume, float pitch, @Nullable PlayerEntity player, Vector3d pos) {
        if (!this.isSilent()) {
            if (!isVisibleForAll()) {
                MCUtil.playSound(level, player, pos.x, pos.y, pos.z, sound, getSoundSource(), volume, pitch, StandUtil::playerCanHearStands);
            }
            else {
                level.playSound(player, pos.x, pos.y, pos.z, sound, getSoundSource(), volume, pitch);
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
    


    private boolean wasDamageBlocked;
    @Override
    public boolean hurt(DamageSource dmgSource, float dmgAmount) {
        wasDamageBlocked = false;
        if (!level.isClientSide() && dmgSource instanceof StandEntityDamageSource) {
            dmgAmount = barrageClashParryPunches((StandEntityDamageSource) dmgSource, dmgAmount);
            if (dmgAmount <= 0) {
                return false;
            }
        }
        return super.hurt(dmgSource, dmgAmount);
    }
    
    protected float barrageClashParryPunches(StandEntityDamageSource dmgSource, float dmgAmount) {
        if (barrageHandler.parryCount > 0 && !isDeadOrDying() 
                && canBlockDamage(dmgSource) && canBlockOrParryFromAngle(dmgSource.getSourcePosition())) {
            int punchesIncoming = dmgSource.getBarrageHitsCount();
            if (punchesIncoming > 0) {
                float parriableProportion = Math.min(StandStatFormulas.getMaxBarrageParryTickDamage(getDurability()) / dmgAmount, 1);
                int punchesCanParry = MathHelper.floor(parriableProportion * barrageHandler.parryCount);

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
                    if (attacker instanceof StandEntity) {
                        ((StandEntity) attacker).playPunchSound = true;
                    }

                    punchesCanParry = Math.min(punchesCanParry, punchesIncoming);
                    dmgSource.setBarrageHitsCount(punchesIncoming - punchesCanParry);
                    barrageHandler.parryCount -= punchesCanParry;
                    addFinisherMeter(0.0125F, FINISHER_NO_DECAY_TICKS);
                    setBarrageClashOpponent(attacker);

                    if (punchesCanParry == punchesIncoming) {
                        StandUtil.addResolve(dmgSource.getStandPower(), this, dmgAmount);
                        return 0;
                    }
                    else {
                        float damageParried = dmgAmount * (float) punchesCanParry / (float) punchesIncoming;
                        dmgAmount -= damageParried;
                        StandUtil.addResolve(dmgSource.getStandPower(), this, damageParried);
                        return dmgAmount;
                    }
                }
            }
        }
        
        return dmgAmount;
    }
    
    public Optional<Entity> barrageClashOpponent() {
        return barrageHandler.clashOpponent;
    }
    
    public void barrageClashStopped() {
        setBarrageClashOpponent(null);
    }
    
    private void setBarrageClashOpponent(@Nullable Entity opponent) {
        Entity prevOpponent = barrageClashOpponent().orElse(null);
        
        entityData.set(BARRAGE_CLASH_OPPONENT_ID, opponent != null ? opponent.getId() : -1);
        
        if (prevOpponent instanceof StandEntity) {
            StandEntity prevStandOpponent = (StandEntity) prevOpponent;
            if (opponent == null) {
                if (prevStandOpponent.barrageClashOpponent().isPresent()) {
                    prevStandOpponent.setBarrageClashOpponent(null);
                }
            }
            else {
                if (prevStandOpponent.barrageClashOpponent().orElse(null) != this) {
                    prevStandOpponent.setBarrageClashOpponent(this);
                }
            }
        }
    }
    
    @Override
    protected void actuallyHurt(DamageSource dmgSource, float damageAmount) {
        boolean blockableAngle = canBlockOrParryFromAngle(dmgSource.getSourcePosition());
        if (!isManuallyControlled() && canBlockDamage(dmgSource) && blockableAngle && !getCurrentTask().isPresent() && canStartBlocking()) {
            // FIXME extend the task if it's already blocking
            setTask(ModStandsInit.BLOCK_STAND_ENTITY.get(), 5, StandEntityAction.Phase.PERFORM, ActionTarget.EMPTY);
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
                    if (absorbedDamage > 0.0F && absorbedDamage < Float.MAX_VALUE / 10F && dmgSource.getEntity() instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) dmgSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(absorbedDamage * 10.0F));
                    }
                    damageAmount = damageAfterAbsorption;
                    
                    damageAmount = ForgeHooks.onLivingDamage(this, dmgSource, damageAmount);
                    if (damageAmount != 0.0F) {
                        if (wasDamageBlocked) {
                            blockDamage += damageAmount;
                            // FIXME cancel user hurt sound
                        }
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
    protected float getDamageAfterMagicAbsorb(DamageSource dmgSource, float dmgAmount) {
        dmgAmount = super.getDamageAfterMagicAbsorb(dmgSource, dmgAmount);

        if (dmgSource.isBypassMagic()) return dmgAmount;
        LivingEntity user = getUser();
        if (user == null || user.is(this)) return dmgAmount;

        if (user.hasEffect(Effects.DAMAGE_RESISTANCE) && dmgSource != DamageSource.OUT_OF_WORLD) {
            int j = 25 - (user.getEffect(Effects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
            float f = dmgAmount * (float)j;
            float f1 = dmgAmount;
            dmgAmount = Math.max(f / 25.0F, 0.0F);
            float f2 = f1 - dmgAmount;
            if (f2 > 0.0F && f2 < Float.MAX_VALUE) {
                if (user instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) user).awardStat(Stats.DAMAGE_RESISTED, Math.round(f2 * 10.0F));
                } else if (dmgSource.getEntity() instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) dmgSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f2 * 10.0F));
                }
            }
        }

        return dmgAmount;
    }

    public float guardCounter() {
        return Math.min((isStandBlocking() ? blockDamage : prevBlockDamage) / 5F, 1F);
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
            double durabilityStat = getDurability();
            strength *= StandStatFormulas.getBlockingKnockbackMult(durabilityStat);
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
        if (!canUpdate() || entityData.get(NO_BLOCKING_TICKS) > 0) {
            return false;
        }
        return getCurrentTask().map(task -> task.getAction().canBeCanceled(userPower, 
                this, task.getPhase(), ModStandsInit.BLOCK_STAND_ENTITY.get())).orElse(true);
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
                    if (!userPower.consumeStamina(staminaCost)) {
                        blockedRatio = stamina / staminaCost;
                        standCrash();
                    }
                }
            }
            if (blockedRatio == 1) {
                wasDamageBlocked = true;
//                if (damageSrc.getEntity() instanceof StandEntity) {
//                    ((StandEntity) damageSrc.getEntity()).playPunchSound = false;
//                }
            }
            return damageAmount * (1 - getPhysicalResistance(blockedRatio, damageAmount));
        }
        return damageAmount;
    }
    
    @Override
    public SoundEvent getHurtSound(DamageSource damageSrc) {
        if (wasDamageBlocked) {
            return getAttackBlockSound();
        }
        return super.getHurtSound(damageSrc);
    }

    @Override
    public float getSoundVolume() {
        return super.getSoundVolume();
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch();
    }
    
    protected float getPhysicalResistance(float blockedRatio, float damageDealt) {
        return StandStatFormulas.getPhysicalResistance(getDurability(), getAttackDamage(), blockedRatio, damageDealt);
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
        if (damageSrc == DamageSource.OUT_OF_WORLD) {
            return false;
        }
        if (this.is(damageSrc.getEntity())
                || getUser() != null && getUser().is(damageSrc.getEntity())
                || getUser() instanceof PlayerEntity && ((PlayerEntity) getUser()).abilities.invulnerable && !damageSrc.isBypassInvul()
                || damageSrc.isFire() && !level.getGameRules().getBoolean(GameRules.RULE_FIRE_DAMAGE)) {
            return true;
        }
        
        if (canTakeDamageFrom(damageSrc)) {
            return isInvulnerable();
        }
        
        return true;
    }
    
    public boolean canTakeDamageFrom(DamageSource damageSrc) {
        return damageSrc instanceof IStandDamageSource
                || damageSrc instanceof IModdedDamageSource && ((IModdedDamageSource) damageSrc).canHurtStands()
                || damageSrc.getMsgId().contains("stand")
                || damageSrc == DamageSource.ON_FIRE;
    }
    
    public boolean canBlockDamage(DamageSource dmgSource) {
        return dmgSource.getDirectEntity() != null && !dmgSource.isBypassArmor() && !ModStatusEffects.isStunned(this);
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
    protected float tickHeadTurn(float yRot, float animStep) {
        if (getCurrentTask().isPresent() || getStandPose() == StandPose.SUMMON) {
            yBodyRot = this.yRot;
            return animStep;
        }
        else {
            if (isFollowingUser()) {
                LivingEntity user = getUser();
                if (user != null) {
                    this.yBodyRot = user.yBodyRot;
                    return animStep;
                }
            }
            
            return super.tickHeadTurn(yRot, animStep);
        }
    }

    @Override
    public void setRot(float yRot, float xRot) {
        super.setRot(yRot, xRot);
    }
    
    public void defaultRotation() {
        LivingEntity user = getUser();
        if (user != null && !isManuallyControlled() && !isRemotePositionFixed()) {
            setRot(user.yRot, user.xRot);
        }
        setYHeadRot(this.yRot);
    }
    
    @Override
    public void tick() {
        super.tick();
        barrageHandler.tick();
        LivingEntity user = getUser();
        
        checkInputBuffer();
        
        Optional<StandEntityTask> currentTask = getCurrentTask();
        
        GeneralUtil.ifPresentOrElse(currentTask, 
                task -> task.rotateStand(this), 
                () -> defaultRotation());
        
        updatePosition();
        updateStrengthMultipliers();
        
        if (!level.isClientSide()) {
            if (noFinisherDecayTicks > 0) {
                noFinisherDecayTicks--;
            }
            else {
                StandEntityAction currentAction = getCurrentTaskAction();
                if (currentAction == null || !currentAction.noFinisherDecay()) {
                    float decay = FINISHER_DECAY;
                    float value = entityData.get(FINISHER_VALUE);
                    if (value < 0.5F) {
                        decay *= 0.5F;
                    }
                    if (user != null && user.hasEffect(ModStatusEffects.RESOLVE.get())) {
                        decay *= 0.5F;
                    }
                    setFinisherMeter(Math.max(value - decay, 0));
                }
            }
            
            int noBlockingTicks = entityData.get(NO_BLOCKING_TICKS);
            if (noBlockingTicks > 0) {
                entityData.set(NO_BLOCKING_TICKS, noBlockingTicks - 1);
            }
        }
        
        if (barrageHandler.clashOpponent.map(stand -> {
            return !stand.isAlive() || !this.isTargetInReach(new ActionTarget(stand));
        }).orElse(false)) {
            setBarrageClashOpponent(null);
        }
        
        if (summonLockTicks > 0) {
            summonLockTicks--;
        }
        else {
            boolean stun = ModStatusEffects.isStunned(this);
            currentTask.ifPresent(task -> {
                if (!stun || task.getAction().ignoresPerformerStun()) {
                    task.tick(userPower, this);
                }
            });
            
            if (!stun && gradualSummonWeaknessTicks > 0) {
                gradualSummonWeaknessTicks--;
                
                if (gradualSummonWeaknessTicks == 0 && !level.isClientSide()) {
                    removeArmsOnlyModifiers();
                }
            }
        }

        if (!level.isClientSide()) {
            if (requiresUser() && (user == null || user.removed)) {
                remove();
                return;
            }
            else if (user != null) {
                setHealth(user.isAlive() ? user.getHealth() : 0);
            }
        }
        else {
            if (user != null && isManuallyControlled() && !noPhysics && isInsideViewBlockingBlock(position())) {
                Vector3d vecToUser = user.position().subtract(position());
                if (vecToUser.lengthSqr() > 1) {
                    vecToUser = vecToUser.normalize();
                }
                moveWithoutCollision(vecToUser);
            }
            
            overlayTickCount++;
        }
        
        if (user != null) {
            deathTime = user.deathTime;
        }
    }



    public void updateStrengthMultipliers() {
        LivingEntity user = getUser();
                
        rangeEfficiency = user != null ? StandStatFormulas.rangeStrengthFactor(rangeEffective, getMaxRange(), distanceTo(user)) : 1;
        
        if (userPower != null) {
            staminaCondition = StandUtil.standIgnoresStaminaDebuff(userPower) ? 1
                    : 0.25 + Math.min((double) (userPower.getStamina() / userPower.getMaxStamina()) * 1.5, 0.75);
        }
    }

    public void updatePosition() {
        if (ModStatusEffects.isStunned(this)) return;
        LivingEntity user = getUser();
        if (user == null) return;
        
        if (isFollowingUser()) {
            StandRelativeOffset relativeOffset = getOffsetFromUser();
            if (relativeOffset != null) {
                Optional<StandEntityTask> currentTask = getCurrentTask();
                Vector3d pos = user.position().add(taskOffset(user, relativeOffset, currentTask));
                if (!isArmsOnlyMode()) {
                    pos = collideNextPos(pos);
                }

                setPos(pos.x, pos.y, pos.z);
            }
        }
        else if (isBeingRetracted()) {
            if (!isCloseToUser()) {
                setDeltaMovement(user.position().add(taskOffset(user, getDefaultOffsetFromUser(), 
                        Optional.empty())).subtract(position())
                        .normalize().scale(getAttributeValue(Attributes.MOVEMENT_SPEED)));
            }
            else {
                setDeltaMovement(Vector3d.ZERO);
                setStandFlag(StandFlag.BEING_RETRACTED, false);
            }
        }
    }
    
    public Vector3d collideNextPos(Vector3d pos) {
        if (noPhysics) {
            return pos;
        }
        AxisAlignedBB collisionBox = getBoundingBox();
        double height = collisionBox.getYsize();
        double width = collisionBox.getXsize();
        if (height > width) {
            collisionBox = new AxisAlignedBB(
                    collisionBox.minX, 
                    collisionBox.maxY - Math.max(height * 0.5, width), 
                    collisionBox.minZ, 
                    collisionBox.maxX, 
                    collisionBox.maxY, 
                    collisionBox.maxZ);
        }
        return position().add(MCUtil.collide(this, collisionBox, pos.subtract(position())));
    }
    
    private Vector3d taskOffset(LivingEntity user, StandRelativeOffset relativeOffset, Optional<StandEntityTask> currentTask) {
        ActionTarget target = currentTask.map(StandEntityTask::getTarget).orElse(ActionTarget.EMPTY);
        float yRot;
        float xRot;
        Vector3d targetPos = target.getTargetPos(true);
        if (targetPos != null) {
            Vector3d vecToTarget = targetPos.subtract(user.getEyePosition(1.0F));
            yRot = MathUtil.yRotDegFromVec(vecToTarget);
            xRot = MathUtil.xRotDegFromVec(vecToTarget);
        }
        else {
            yRot = currentTask.map(task -> task.getAction().yRotForOffset(user, task)).orElse(user.yRot);
            xRot = user.xRot;
        }
            
        Vector3d offset = relativeOffset.getAbsoluteVec(getDefaultOffsetFromUser(), yRot, xRot, this, user);
        if (user.isShiftKeyDown()) {
            offset = new Vector3d(offset.x, 0, offset.z);
        }
        
        return offset;
    }
    
    @Nullable
    public StandRelativeOffset getOffsetFromUser() {
        if (Optional.ofNullable(getCurrentTaskAction()).map(action -> action.noAdheringToUserOffset(userPower, this)).orElse(false)) {
            return null;
        }
        return getCurrentTask().map(task -> {
            StandRelativeOffset taskOffset = task.getOffsetFromUser(userPower, this);
            if (taskOffset != null) {
                return taskOffset;
            }
            return getDefaultOffsetFromUser();
        }).orElse(getDefaultOffsetFromUser());
    }
    
    public StandRelativeOffset getDefaultOffsetFromUser() {
        return isArmsOnlyMode() ? offsetDefaultArmsOnly : offsetDefault;
    }
    
    public void setDefaultOffsetFromUser(StandRelativeOffset offset) {
        this.offsetDefault = offset;
        if (unsummonTicks == 0) {
            this.unsummonOffset = offset;
        }
    }
    
//    public void addBarrageOffset() {
//        if (!isArmsOnlyMode()) {
//            StandEntityTask currentTask = getCurrentTask();
//            if (currentTask != null) {
//                StandRelativeOffset offset = currentTask.getOffsetFromUser();
//                double currentOffset = offset.getForward();
//                double newOffset = Math.min(currentOffset + 0.025, rangeEffective);
//                if (newOffset != currentOffset) {
//                    offset = offset.copy(null, null, newOffset);
//                    setTaskPosOffset(offset, true);
//                }
//            }
//        }
//    }

    private boolean isInsideViewBlockingBlock(Vector3d pos) {
        BlockPos.Mutable blockPos$mutable = new BlockPos.Mutable();
        for (int i = 0; i < 8; ++i) {
            double x = pos.x + (double)(((float)((i >> 0) % 2) - 0.5F) * getBbWidth() * 0.8F);
            double y = pos.y + getEyeHeight() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double z = pos.z + (double)(((float)((i >> 2) % 2) - 0.5F) * getBbWidth() * 0.8F);
            blockPos$mutable.set(x, y, z);
            BlockState blockState = level.getBlockState(blockPos$mutable);
            if (blockState.getRenderShape() != BlockRenderType.INVISIBLE && blockState.isSuffocating(level, blockPos$mutable)) {
                return true;
            }
        }
        return false;
    }



    public boolean setTask(StandEntityAction action, int ticks, StandEntityAction.Phase phase, ActionTarget taskTarget) {
        return setTask(StandEntityTask.makeServerSideTask(this, userPower, action, ticks, phase, isArmsOnlyMode(), taskTarget));
    }

    protected boolean setTask(StandEntityTask task) {
        if (!level.isClientSide()) {
            Optional<StandEntityTask> previousTask = getCurrentTask();
            if (stopTask(task, false)) {
                previousTask.ifPresent(prevTask -> {
                    if (prevTask.getTarget().getType() != TargetType.EMPTY
                            && task.getTarget().getType() == TargetType.EMPTY) {
                        task.setTarget(this, prevTask.getTarget(), userPower);
                    }
                    
                    if (task.getAction().transfersPreviousOffset(userPower, this, prevTask)) {
                        StandRelativeOffset offset = prevTask.getOffsetFromUser(userPower, this);
                        if (offset != null) {
                            task.overrideOffsetFromUser(offset);
                        }
                    }
                });
                
                entityData.set(CURRENT_TASK, Optional.of(task));
                if (task.getAction().enablePhysics) {
                    setNoPhysics(false);
                }
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
        stopTask(false);
    }
    
    protected void stopTask(boolean stopNonCancelable) {
        stopTask(null, stopNonCancelable);
    }
    
    private boolean stopTask(@Nullable StandEntityTask newTask, boolean stopNonCancelable) {
        if (!level.isClientSide()) {
            return getCurrentTask().map(task -> {
                StandEntityAction action = newTask != null ? newTask.getAction() : null;
                if (task.getTicksLeft() <= 0 || stopNonCancelable
                        || task.getAction().canBeCanceled(userPower, this, task.getPhase(), action)) {
                    clearTask(task, action);
                    return true;
                }
                return false;
            }).orElse(true);
        }
        return false;
    }
    
    protected void clearTask(StandEntityTask clearedTask, @Nullable StandEntityAction newAction) {
        StandEntityAction oldAction = clearedTask.getAction();

        barrageHandler.reset();
        if (blockDamage > 0) {
            prevBlockDamage += blockDamage;
            blockDamage = 0;
        }
        else {
            prevBlockDamage = 0;
        }
        
        updateNoPhysics();
        setStandPose(StandPose.IDLE);
        
        if (newAction == null && !checkInputBuffer()) {
            if (isArmsOnlyMode()) {
                StandEntityAction unsummon = ModStandsInit.UNSUMMON_STAND_ENTITY.get();
                setTask(StandEntityTask.makeServerSideTask(this, userPower, unsummon, unsummon.getStandActionTicks(userPower, this), 
                        StandEntityAction.Phase.PERFORM, isArmsOnlyMode(), ActionTarget.EMPTY));
            }
            else {
                boolean retractStand = oldAction.standRetractsAfterTask(getUserPower(), this) && getUser() != null && !isCloseToUser() && isFollowingUser();
                entityData.set(CURRENT_TASK, Optional.empty());
                if (retractStand) {
                    retractStand(false);
                }
            }
        }
    }
    
    public void stopTaskWithRecovery() {
        getCurrentTask().ifPresent(task -> {
            task.moveToPhase(StandEntityAction.Phase.RECOVERY, userPower, this);
        });
    }
    
    public Optional<StandEntityTask> getCurrentTask() {
        return entityData.get(CURRENT_TASK);
    }
    
    public Optional<StandEntityAction> getCurrentTaskActionOptional() {
        return getCurrentTask().map(StandEntityTask::getAction);
    }
    
    public StandEntityAction getCurrentTaskAction() {
        return getCurrentTaskActionOptional().orElse(null);
    }
    
    public Optional<StandEntityAction.Phase> getCurrentTaskPhase() {
        return getCurrentTask().map(StandEntityTask::getPhase);
    }
    
    public float getCurrentTaskPhaseCompletion(float partialTick) {
        return getCurrentTask().map(task -> task.getPhaseCompletion(partialTick)).orElse(0F);
    }

    public float getUserWalkSpeed() {
        return getCurrentTask().map(task -> task.getAction().getUserWalkSpeed(getUserPower(), this, task)).orElse(1F);
    }
    
    public void queueNextAction(StandEntityAction action) {
        if (!level.isClientSide()) {
            this.inputBuffer = action;
        }
    }
    
    public boolean checkInputBuffer() {
        if (inputBuffer != null) {
            LivingEntity user = getUser();
            if (userPower.clickAction(inputBuffer, user != null && user.isShiftKeyDown(), ActionTarget.EMPTY)) {
                inputBuffer = null;
                return true;
            }
        }
        return false;
    }


    
    /**
     * @deprecated use {@link StandEntity#aimWithThisOrUser(double, ActionTarget)}, which returns an {@link ActionTarget} instance
     */
    @Deprecated
    public RayTraceResult aimWithStandOrUser(double reachDistance, ActionTarget currentTarget) {
        RayTraceResult aim;
        if (!isManuallyControlled()) {
            LivingEntity user = getUser();
            if (user != null && currentTarget.getType() != TargetType.ENTITY) {
                aim = precisionRayTrace(user, reachDistance);
                if (JojoModUtil.isAnotherEntityTargeted(aim, this)
                        || currentTarget.getType() == TargetType.EMPTY && aim.getType() != RayTraceResult.Type.MISS) {
                    Vector3d targetPos = ActionTarget.fromRayTraceResult(aim).getTargetPos(true);
                    if (targetPos != null) {
                        MCUtil.rotateTowards(this, targetPos, (float) getAttackSpeed() / 16F * 18F);
                    }
                }
            }
        }
        aim = precisionRayTrace(this, reachDistance);
        return aim;
    }
    
    public ActionTarget aimWithThisOrUser(double reachDistance, ActionTarget currentTarget) {
        ActionTarget target;
        if (currentTarget.getType() == TargetType.ENTITY && isTargetInReach(currentTarget)) {
            target = currentTarget;
        }
        else {
            RayTraceResult aim = null;
            if (!isManuallyControlled()) {
                LivingEntity user = getUser();
                if (user != null) {
                    aim = precisionRayTrace(user, reachDistance);
                }
            }
            if (aim == null) {
                aim = precisionRayTrace(this, reachDistance);
            }
            
            target = ActionTarget.fromRayTraceResult(aim);
        }
        
        if (target.getEntity() != this) {
            Vector3d targetPos = target.getTargetPos(true);
            if (targetPos != null) {
                MCUtil.rotateTowards(this, targetPos, (float) getAttackSpeed() / 16F * 18F);
            }
        }
        
        return target;
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
        return entity -> !entity.is(this) && !entity.is(getUser()) && entity.isAlive()
                && !(entity instanceof ProjectileEntity && this.is(((ProjectileEntity) entity).getOwner()));
    }
    
    public RayTraceResult precisionRayTrace(Entity aimingEntity, double reachDistance) {
        return precisionRayTrace(aimingEntity, reachDistance, 0);
    }
    
    public RayTraceResult precisionRayTrace(Entity aimingEntity, double reachDistance, double rayTraceInflate) {
        RayTraceResult[] targets = JojoModUtil.rayTraceMultipleEntities(aimingEntity, 
                reachDistance, canTarget(), rayTraceInflate, getPrecision());
        if (targets.length == 1) {
            return targets[0];
        }

        /* get the closest targets in each category, with categories given different priorities
         *   0 - players
         *   1 - hostile mobs
         *   2 - other entities
         *   3 - blocks
         */
        RayTraceResult[] closestWithPriority = new RayTraceResult[4];
        int priority = 3;
        for (RayTraceResult target : targets) {
            if (target instanceof EntityRayTraceResult) {
                Entity targetEntity = ((EntityRayTraceResult) target).getEntity();
                if (targetEntity instanceof LivingEntity) {
                    if (targetEntity instanceof PlayerEntity || targetEntity instanceof StandEntity) {
                        priority = 0;
                    }
                    else if (StandUtil.attackingTargetGivesResolve(targetEntity)) {
                        priority = 1;
                    }
                }
                else {
                    priority = 2;
                }
            }
            
            setIfNull(closestWithPriority, priority, target);
        }
        for (RayTraceResult target : closestWithPriority) {
            if (target != null) return target;
        }
        
        return targets[0];
    }
    
    private static <T> void setIfNull(T[] array, int index, T value) {
        if (array[index] == null) array[index] = value;
    }
    
    public boolean canAttackMelee() {
        return getAttackSpeed() > 0 && getAttributeValue(ForgeMod.REACH_DISTANCE.get()) > 0 ;
    }

    public boolean punch(StandEntityTask task, IHasStandPunch punch, ActionTarget target) {
        if (!level.isClientSide()) {
            ActionTarget finalTarget = aimWithThisOrUser(getAimDistance(getUser()), target);
            target = finalTarget.getType() != TargetType.EMPTY && isTargetInReach(finalTarget) ? finalTarget : ActionTarget.EMPTY;
            setTaskTarget(target);
        }
        
        return attackTarget(target, punch, task);
    }
    
    public int barrageHits;
    public void setBarrageHitsThisTick(int hits) {
        this.barrageHits = hits;
        barrageHandler.addParryCount(hits);
    }
    
    public Boolean playPunchSound = null;
    public boolean attackTarget(ActionTarget target, IHasStandPunch punchAction, StandEntityTask task) {
        IPunch punchInstance;
        playPunchSound = null;
        switch (target.getType()) {
        case BLOCK:
            BlockPos blockPos = target.getBlockPos();
            StandBlockPunch blockPunchInstance = punchAction.punchBlock(this, blockPos, level.getBlockState(blockPos));
            punchInstance = blockPunchInstance;
            break;
        case ENTITY:
            Entity entity = target.getEntity();
            StandEntityPunch entityPunchInstance = punchAction.punchEntity(this, entity, getDamageSource());
            punchInstance = entityPunchInstance;
            break;
        default:
            StandMissedPunch emptyPunchInstance = punchAction.punchMissed(this);
            punchInstance = emptyPunchInstance;
            break;
        }
        
        onTargetHit(CallOrder.BEFORE, punchInstance);
        punchInstance.doHit(task);
        onTargetHit(CallOrder.AFTER, punchInstance);
        lastPunch = punchInstance;
        if (!level.isClientSide()) {
            punchAction.playPunchImpactSound(punchInstance, target.getType(), playPunchSound == null || playPunchSound, playPunchSound != null && playPunchSound);
        }
        return punchInstance.targetWasHit();
    }
    
    @Deprecated
    protected void onTargetHit(CallOrder called, IPunch punch) {}
    
    protected enum CallOrder {
        BEFORE,
        AFTER
    }
    
    @Nullable
    public IPunch getLastPunch() {
        return lastPunch;
    }
    
    public boolean hitBlock(BlockPos blockPos, BlockState blockState, StandBlockPunch punch, StandEntityTask task) {
        return !level.isClientSide() ? punch.doHit(task) : false;
    }
    
    public StandEntityDamageSource getDamageSource() {
        return new StandEntityDamageSource("stand", this, getUserPower());
    }
    
    public boolean attackEntity(Supplier<Boolean> doAttack, StandEntityPunch punch, StandEntityTask task) {
        if (level.isClientSide() || !canHarm(punch.target)) {
            return false;
        }
        boolean attacked = doAttack.get();
        if (attacked && !isManuallyControlled()) {
            setLastHurtMob(punch.target);
        }
        return attacked;
    }

    public boolean hurtTarget(Entity target, DamageSource dmgSource, float damage) {
        boolean hurt = DamageUtil.hurtThroughInvulTicks(target, DamageUtil.enderDragonDamageHack(dmgSource, target), damage
                * JojoModConfig.getCommonConfigInstance(false).standDamageMultiplier.get().floatValue());
        if (hurt) {
            if (target instanceof LivingEntity) {
                LivingEntity targetLiving = (LivingEntity) target;
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
            }
            doEnchantDamageEffects(this, target);
        }
        return hurt;
    }
    
    protected SoundEvent getAttackBlockSound() {
        return ModSounds.STAND_DAMAGE_BLOCK.get();
    }
    
    public double getDistanceToTarget(ActionTarget target) {
        return target.getBoundingBox(level).map(aabb -> JojoModUtil.getDistance(this, aabb)).orElse(0D);
    }
    
    public boolean isTargetInReach(ActionTarget target) {
        return getDistanceToTarget(target) <= getAttributeValue(ForgeMod.REACH_DISTANCE.get());
    }
    
    public void setTaskTarget(ActionTarget target) {
        if (target != null) {
            getCurrentTask().ifPresent(task -> {
                boolean sendTarget = task.setTarget(this, target, userPower);
                if (!level.isClientSide()) {
                    if (sendTarget) {
                        PacketManager.sendToClientsTracking(new TrStandTaskTargetPacket(getId(), target), this);
                    }
                }
            });
        }
    }
    
    @Override
    public boolean canAttack(LivingEntity entity) {
        if (entity.is(this) || !super.canAttack(entity)) return false;
        
        LivingEntity user = getUser();
        if (user != null) {
            return !entity.is(user) && user.canAttack(entity)
                    && !(entity instanceof AnimalEntity && entity.isPassengerOfSameVehicle(user))
                    && !(user instanceof PlayerEntity && entity instanceof PlayerEntity
                            && !((PlayerEntity) user).canHarmPlayer((PlayerEntity) entity));
        }
        
        return true;
    }
    
    public boolean canHarm(Entity target) {
        if (target instanceof LivingEntity) {
            return canAttack((LivingEntity) target);
        }
        LivingEntity user = getUser();
        if (target instanceof ProjectileEntity) {
            Entity owner = ((ProjectileEntity) target).getOwner();
            if (owner != null && (owner.is(this) || owner.is(user))) {
                return target instanceof DamagingEntity && ((DamagingEntity) target).canHitOwner();
            }
        }
        if (user != null && target.getControllingPassenger() == user) {
            return false;
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
    public HandSide getMainArm() {
        LivingEntity user = getUser();
        return user != null ? user.getMainArm() : HandSide.RIGHT;
    }
    
    public HandSide getArm(Hand arm) {
        return MCUtil.getHandSide(this, arm);
    }
    
    public HandSide getPunchingHand() {
        return getArm(swingingArm);
    }
    
    public float getFinisherMeter() {
        if (userPower != null && !StandUtil.isFinisherMechanicUnlocked(userPower)) {
            return 0;
        }
        return entityData.get(FINISHER_VALUE);
    }
    
    public void addFinisherMeter(float value, int noDecayTicks) {
        if (value > 0 && getUser() != null && getUser().hasEffect(ModStatusEffects.RESOLVE.get())) {
            value *= 2F;
        }
        setFinisherMeter(getFinisherMeter() + value);
        this.noFinisherDecayTicks = Math.max(this.noFinisherDecayTicks, noDecayTicks);
    }
    
    protected void setFinisherMeter(float value) {
        entityData.set(FINISHER_VALUE, MathHelper.clamp(value, 0F, 1F));
    }
    
    public void setHeavyPunchFinisher() {
        entityData.set(LAST_HEAVY_FINISHER_VALUE, getFinisherMeter());
    }
    
    public float getLastHeavyFinisherValue() {
        return entityData.get(LAST_HEAVY_FINISHER_VALUE);
    }

    public boolean willHeavyPunchBeFinisher() {
        return getFinisherMeter() >= 0.5F;
    }

    public boolean isCurrentHeavyPunchFinisher() {
        return getLastHeavyFinisherValue() >= 0.5F;
    }
    
    public int getNoFinisherDecayTicks() {
        return noFinisherDecayTicks;
    }
    
    public void breakStandBlocking(int lockTicks) {
        if (!level.isClientSide() && isStandBlocking()) {
            entityData.set(NO_BLOCKING_TICKS, lockTicks);
            stopTask(true);
            playSound(ModSounds.STAND_PARRY.get(), 1.0F, 1.0F);
        }
    }
    
    public void standCrash() {
        if (!level.isClientSide()) {
            stopTask(true);
            addEffect(new EffectInstance(ModStatusEffects.STUN.get(), 40));
        }
    }

    public boolean breakBlock(BlockPos blockPos, BlockState blockState, boolean canDropItems) {
        return breakBlock(blockPos, blockState, canDropItems, null);
    }
    
    public boolean breakBlockWithExternalDrops(BlockPos blockPos, BlockState blockState, List<ItemStack> createdDrops) {
        return breakBlock(blockPos, blockState, false, createdDrops);
    }
    
    protected boolean breakBlock(BlockPos blockPos, BlockState blockState, boolean dropLootTableItems, @Nullable List<ItemStack> createdDrops) {
        if (level.isClientSide() || !JojoModUtil.canEntityDestroy((ServerWorld) level, blockPos, blockState, this)) {
            return false;
        }
        
        if (canBreakBlock(blockPos, blockState)) {
            LivingEntity user = getUser();
            PlayerEntity playerUser = user instanceof PlayerEntity ? (PlayerEntity) user : null;
            boolean dropItem = dropLootTableItems;
            if (playerUser != null) {
                blockState.getBlock().playerWillDestroy(level, blockPos, blockState, playerUser);
                dropItem &= !playerUser.abilities.instabuild;
            }
            if (!dropItem) {
                CrazyDiamondRestoreTerrain.rememberBrokenBlock(level, blockPos, blockState, 
                        Optional.ofNullable(level.getBlockEntity(blockPos)), 
                        createdDrops != null ? createdDrops : Collections.emptyList());
            }
            if (level.destroyBlock(blockPos, dropItem, this)) {
                blockState.getBlock().destroy(level, blockPos, blockState);
                return true;
            }
        }
        else {
            SoundType soundType = blockState.getSoundType(level, blockPos, this);
            level.playSound(null, blockPos, soundType.getHitSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
        }
        return false;
    }
    
    public boolean canBreakBlock(BlockPos blockPos, BlockState blockState) {
        float blockHardness = blockState.getDestroySpeed(level, blockPos);
        return blockHardness >= 0 && canBreakBlock(blockHardness, blockState.getHarvestLevel());
    }

    public boolean canBreakBlock(float blockHardness, int blockHarvestLevel) {
        return StandStatFormulas.isBlockBreakable(getAttackDamage(), blockHardness, blockHarvestLevel);
    }

    public double getMaxRange() {
        return rangeMax;
    }

    public double getMaxEffectiveRange() {
        return rangeEffective;
    }
    
    public double getRangeEfficiency() {
        return rangeEfficiency;
    }
    

    public BarrageSwingsHolder<?, ?> getBarrageSwingsHolder() {
        if (!level.isClientSide()) {
            throw new IllegalStateException("Barrage swing animating class is only available on the client!");
        }
        return this.barrageSwings;
    }
    
    protected BarrageHitSoundHandler initBarrageHitSoundHandler() {
        return new BarrageHitSoundHandler();
    }
    
    public BarrageHitSoundHandler getBarrageHitSoundsHandler() {
        if (!level.isClientSide()) {
            throw new IllegalStateException("Barrage sound handling class is only available on the client!");
        }
        return this.barrageSounds;
    }
    
    
    
    public void addProjectile(DamagingEntity projectile) {
        if (!level.isClientSide() && !projectile.isAddedToWorld()) {
            projectile.setDamageFactor(projectile.getDamageFactor() * (float) getAttackDamage() / 8);
            projectile.setSpeedFactor(projectile.getSpeedFactor() * (float) getAttackSpeed() / 8);
            level.addFreshEntity(projectile);
        }
    }
    
    public void shootProjectile(ModdedProjectileEntity projectile, float velocity, float inaccuracy) {
        if (!level.isClientSide() && !projectile.isAddedToWorld()) {
            projectile.shootFromRotation(this, velocity, getProjectileInaccuracy(inaccuracy));
            projectile.withStandSkin(getStandSkin());
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
            if (!(toUnsummon && isFollowingUser())) {
                setStandFlag(StandFlag.BEING_RETRACTED, true);
            }
            if (toUnsummon && userPower.getHeldAction() == null) {
                startStandUnsummon();
            }
        }
    }
    
    public void stopRetraction() {
        setStandFlag(StandFlag.BEING_RETRACTED, false);
        getCurrentTask().ifPresent(task -> {
            if (task.getAction() == ModStandsInit.UNSUMMON_STAND_ENTITY.get()) {
                this.stopTask();
            }
        });
    }
    
    private void startStandUnsummon() {
        StandEntityAction unsummon = ModStandsInit.UNSUMMON_STAND_ENTITY.get();
        setTask(unsummon, unsummon.getStandActionTicks(userPower, this), StandEntityAction.Phase.PERFORM, ActionTarget.EMPTY);
    }
    
    public boolean isBeingRetracted() {
        return getStandFlag(StandFlag.BEING_RETRACTED);
    }

    public boolean isCloseToUser() {
        LivingEntity user = getUser();
        return user != null ? distanceToSqr(user) < 2.25 : false;
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
            if (!isCloseToUser() && !GeneralUtil.orElseFalse(getCurrentTask(), 
                    task -> task.getAction().noAdheringToUserOffset(userPower, this))) {
                retractStand(false);
            }
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
        if (noPhysics || standCanHaveNoPhysics()) {
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

    private boolean prevTickInput = false;
    public void moveStandManually(float strafe, float forward, boolean jumping, boolean sneaking) {
        if (isManuallyControlled() && canMoveManually()) {
            strafe = manualMovementLocks.strafe(strafe);
            forward = manualMovementLocks.forward(forward);
            jumping = manualMovementLocks.up(jumping);
            sneaking = manualMovementLocks.down(sneaking);
            boolean input = jumping || sneaking || forward != 0 || strafe != 0;
            if (input) {
                double speed = getAttributeValue(Attributes.MOVEMENT_SPEED);
                double y = jumping ? speed : 0;
                if (sneaking) {
                    y -= speed;
                    strafe *= 0.5;
                    forward *= 0.5;
                }
                setDeltaMovement(getAbsoluteMotion(new Vector3d((double)strafe, y, (double)forward), speed, this.yRot).scale(getUserWalkSpeed()));
                
                if (!prevTickInput) {
                    setDeltaMovement(Vector3d.ZERO);
                }
            }
            else if (prevTickInput) {
                setDeltaMovement(Vector3d.ZERO);
            }
            prevTickInput = input;
        }
    }
    
    public boolean hadInput() {
        return prevTickInput;
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
    
    protected boolean canMoveManually() {
        return getCurrentTaskActionOptional().map(action -> !action.lockStandManualMovement(getUserPower(), this)).orElse(true);
    }
    
    public ManualStandMovementLock getManualMovementLocks() {
        return manualMovementLocks;
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
    
    public void makeStuckInBlock(BlockState blockState, Vector3d stuckSpeedMultiplier) {}
    
    private void moveWithoutCollision(Vector3d vec) {
        setBoundingBox(getBoundingBox().move(vec));
        setLocationFromBoundingbox();
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }
    
    

    @Override
    protected void doPush(Entity entity) {
        if (!entity.is(getUser())) {
            super.doPush(entity);
        }
    }
    
    @Override
    public boolean isPickable() {
        if (level.isClientSide()) {
            return !ClientUtil.getClientPlayer().is(getUser()) && ClientUtil.canSeeStands();
        }
        return super.isPickable();
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
        alpha *= getCurrentTask().map(
                task -> task.getAction().getStandAlpha(this, task.getTicksLeft(), partialTick)).orElse(1F);
        alpha *= rangeEfficiency;
        
//        LivingEntity user = getUser();
//        if (user != null && !user.hasEffect(ModEffects.RESOLVE.get())) {
//            alpha *= Math.min(user.getHealth() / 5F, 1F);
//        }
            
        return MathHelper.clamp(alpha, 0F, 1F);
    }
    
    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        onClientRenderTick(ClientUtil.getPartialTick());
        return super.shouldRender(cameraX, cameraY, cameraZ);
    }
    
    protected void onClientRenderTick(float partialTick) {
        this.barrageSounds.playSound(this, tickCount + partialTick);
    }
    
    
    
    private static final List<Effect> SHARED_EFFECTS_FROM_USER = new ArrayList<>();
    private static final List<Effect> SHARED_EFFECTS_FROM_STAND = new ArrayList<>();
    public static void addSharedEffectsFromUser(Effect... effects) {
        Collections.addAll(SHARED_EFFECTS_FROM_USER, effects);
    }
    
    public static void addSharedEffectsFromStand(Effect... effects) {
        Collections.addAll(SHARED_EFFECTS_FROM_USER, effects);
    }
    
    public boolean isEffectSharedFromUser(Effect effect) {
        return SHARED_EFFECTS_FROM_USER.contains(effect);
    }
    
    @Override
    protected void onEffectAdded(EffectInstance effectInstance) {
        super.onEffectAdded(effectInstance);
        if (!level.isClientSide()) {
            LivingEntity user = getUser();
            if (user instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) user).connection.send(new SPlayEntityEffectPacket(this.getId(), effectInstance));
            }
            if (SHARED_EFFECTS_FROM_STAND.contains(effectInstance.getEffect())) {
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
            if (SHARED_EFFECTS_FROM_STAND.contains(effectInstance.getEffect())) {
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

    

    private ItemStack mainHandItem = ItemStack.EMPTY;
    private ItemStack offHandItem = ItemStack.EMPTY;
    @Override
    public ItemStack getItemBySlot(EquipmentSlotType slot) {
        switch (slot) {
        case MAINHAND:
            return mainHandItem;
        case OFFHAND:
            return offHandItem;
        default:
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemSlot(EquipmentSlotType slot, ItemStack stack) {
        switch (slot) {
        case MAINHAND:
            this.mainHandItem = stack;
            break;
        case OFFHAND:
            this.offHandItem = stack;
            break;
        default:
            break;
        }
    }
    
    public void takeItem(EquipmentSlotType slot, ItemStack item, boolean dropPrev, @Nullable LivingEntity dropPrevTo) {
        if (!level.isClientSide() && !item.isEmpty()) {
            ItemStack heldItem = getItemBySlot(slot);
            if (!heldItem.isEmpty()) {
                if (heldItem.sameItem(item) && ItemStack.tagMatches(heldItem, item)) {
                    int toMove = Math.min(item.getCount(), heldItem.getMaxStackSize() - heldItem.getCount());
                    item.shrink(toMove);
                    heldItem.grow(toMove);
                }
                else if (dropPrev) {
                    if (dropPrevTo != null) {
                        dropItemTo(slot, dropPrevTo);
                    }
                    else {
                        dropItem(slot);
                    }
                    setItemSlot(slot, item);
                }
            }
            else {
                setItemSlot(slot, item);
            }
        }
    }
    
    public void dropItemTo(EquipmentSlotType slot, @Nullable LivingEntity entity) {
        if (!level.isClientSide()) {
            if (entity == null) {
                dropItem(slot);
            }
            else {
                ItemStack heldItem = getItemBySlot(slot);
                if (!heldItem.isEmpty()) {
                    setItemSlot(slot, ItemStack.EMPTY);
                    MCUtil.giveItemTo(entity, heldItem, true);
                }
            }
        }
    }
    
    public void dropItem(EquipmentSlotType slot) {
        if (!level.isClientSide()) {
            ItemStack item = getItemBySlot(slot);
            if (!item.isEmpty()) {
                Vector3d itemPos = position();
                Hand hand = slot == EquipmentSlotType.MAINHAND ? Hand.MAIN_HAND
                          : slot == EquipmentSlotType.OFFHAND ? Hand.OFF_HAND
                          : null;
                if (hand != null) {
                    itemPos = itemPos.add(new Vector3d(getBbWidth() * 0.5 * (getArm(hand) == HandSide.LEFT ? -1 : 1), 
                            getBbHeight() * 0.4, 0).yRot((180 - yRot) * MathUtil.DEG_TO_RAD));
                }
                ItemEntity itemEntity = new ItemEntity(level, itemPos.x, itemPos.y, itemPos.z, item.copy());
                setItemSlot(slot, ItemStack.EMPTY);
                level.addFreshEntity(itemEntity);
            }
        }
    }
    
    private void dropHeldItems() {
        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            dropItem(slot);
        }
    }
    
    public void onKnivesThrow(World world, PlayerEntity playerUser, ItemStack knivesStack, int knivesThrown) {}
    
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (!level.isClientSide()) {
            dropHeldItems();
        }
        else if (isManuallyControlled() && getUser() == ClientUtil.getClientPlayer()) {
            StandUtil.setManualControl(ClientUtil.getClientPlayer(), false, false);
        }
    }
    
    public final EquipmentSlotType handItemSlot(Hand hand) {
        if (hand == null) return null;
        switch (hand) {
        case MAIN_HAND:
            return EquipmentSlotType.MAINHAND;
        case OFF_HAND:
            return EquipmentSlotType.OFFHAND;
        }
        return null;
    }
    // FIXME save the items in nbt
    
    
    
    private boolean noFireAnimFrame = false;
    @Override
    public boolean displayFireAnimation() {
        if (noFireAnimFrame) {
            noFireAnimFrame = false;
            return false;
        }
        
        return super.displayFireAnimation();
    }
    
    public void setNoFireAnimFrame() {
        this.noFireAnimFrame = true;
    }



    @Override
    public void startSeenByPlayer(ServerPlayerEntity player) {
        super.startSeenByPlayer(player);
        if (player.is(getUser()) && player.isAlive()) {
            StandUtil.setManualControl(player, false, false);
        }
    }
    
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(entityData.get(USER_ID));
        buffer.writeVarInt(summonPoseRandomByte);
        buffer.writeInt(tickCount);
    }
    
    protected void beforeClientSpawn(FMLPlayMessages.SpawnEntity packet, World world) {
        int userId = packet.getAdditionalData().readInt();
        entityData.set(USER_ID, userId);
    }
    
    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        summonPoseRandomByte = additionalData.readVarInt();
        tickCount = additionalData.readInt();
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
    
    @Override
    public boolean saveAsPassenger(CompoundNBT pCompound) {
        return false;
    }
//    
//    @Override
//    public CompoundNBT serializeNBT() {
//        return super.serializeNBT();
//    }
    
    

    @Override
    public void swing(Hand hand) {
        if (tickCount - lastSwingTick > 1) {
            lastSwingTick = tickCount;
            super.swing(hand);
        }
        else {
//            swings.addSwing(hand == Hand.MAIN_HAND ? getMainArm() : getOppositeToMainArm());
        }
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
//        double attackSpeed = getAttackSpeed();
//        return attackSpeed < 10D ? (int) (20D / attackSpeed) : 2;
        return 2;
    }
    
    
    
    public void setStandSkin(Optional<ResourceLocation> skinLocation) {
        entityData.set(DATA_PARAM_STAND_SKIN, skinLocation);
    }
    
    public Optional<ResourceLocation> getStandSkin() {
        return entityData.get(DATA_PARAM_STAND_SKIN);
    }
    
    

    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    @Override
    public Iterable<ItemStack> getArmorSlots() { return armorItems; }

    @Override
    public boolean isAffectedByPotions() { return false; }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier) { return false; }

    @Override
    public boolean onClimbable() { return false; }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    public boolean startRiding(Entity entity, boolean force) { return false; }
    
}
