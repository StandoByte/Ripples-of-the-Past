package com.github.standobyte.jojo.entity.stand;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.action.actions.StandEntityHeavyAttack;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.renderer.entity.stand.AdditionalArmSwing;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModDataSerializers;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.power.stand.IStandManifestation;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.stats.StandStats;
import com.github.standobyte.jojo.util.EntityDistanceRayTraceResult;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.damage.IStandDamageSource;
import com.github.standobyte.jojo.util.damage.ModDamageSources;
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
import net.minecraftforge.common.ForgeMod;
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
    protected StandRelativeOffset relativePos = new StandRelativeOffset(-0.75, -0.5, 0.2);

    private static final DataParameter<Boolean> SWING_OFF_HAND = EntityDataManager.defineId(StandEntity.class, DataSerializers.BOOLEAN);
    private boolean alternateAdditionalSwing;
    private int lastSwingTick = -2;
    private final ArmSwings swings = new ArmSwings();
    public boolean barragePunchDelayed = false;
    public int barrageDelayedPunches = 0;
    private boolean accumulateBarrageTickParry;
    private int barrageParryCount;
    private static final DataParameter<Float> PUNCHES_COMBO = EntityDataManager.defineId(StandEntity.class, DataSerializers.FLOAT);
    private int noComboDecayTicks;
    private static final int COMBO_TICKS = 40;
    private static final float COMBO_DECAY = 0.025F;
    
    private static final DataParameter<ActionTarget> TASK_TARGET = (DataParameter<ActionTarget>) EntityDataManager.defineId(StandEntity.class, 
            (IDataSerializer<ActionTarget>) ModDataSerializers.TASK_TARGET.get().getSerializer());
    private static final DataParameter<Optional<StandEntityTask>> CURRENT_TASK = EntityDataManager.defineId(StandEntity.class, 
            (IDataSerializer<Optional<StandEntityTask>>) ModDataSerializers.STAND_ENTITY_TASK.get().getSerializer());
    @Nullable
    private StandEntityTask scheduledTask;
    
    protected StandPose standPose = StandPose.SUMMON;
    public int gradualSummonWeaknessTicks;
    public int unsummonTicks;
    public int summonLockTicks;
    
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
        entityData.define(TASK_TARGET, ActionTarget.EMPTY);
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
                action.onTaskSet(level, this, userPower, phase);
            }
            if (level.isClientSide()) {
                if (action != null || getStandPose() != StandPose.SUMMON) {
                    setStandPose(action != null ? action.standPose : StandPose.NONE);
                }
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
        return 3.0D;
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
    
    // FIXME (!) is it nerfed too heavily?
    private static final AttributeModifier ATTACK_DAMAGE_ARMS_ONLY = new AttributeModifier(
            UUID.fromString("aaa82f0e-f1a7-47d1-9066-e1a025be02df"), "Stand attack damage with only arms", -0.5, Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier ATTACK_SPEED_ARMS_ONLY = new AttributeModifier(
            UUID.fromString("5b26b3d1-405c-402b-aee6-d5a0657386fe"), "Stand attack speed with only arms", -0.5, Operation.MULTIPLY_TOTAL);
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
    
    
    
    public double getAttackDamage(@Nullable LivingEntity target) {
        double damage = getAttributeValue(Attributes.ATTACK_DAMAGE);
        return damage * rangeEfficiency * staminaCondition;
    }
    
    public double getAttackSpeed() {
        double speed = getAttributeValue(Attributes.ATTACK_SPEED);
        return speed * rangeEfficiency * staminaCondition;
    }
    
    public double getAttackKnockback(@Nullable LivingEntity target) {
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
    
    public boolean wasSummonedAsArms() {
        return (entityData.get(ARMS_ONLY_MODE) & 2) > 0;
    }
    
    public void fullSummonFromArms() {
        if (!level.isClientSide() && isArmsOnlyMode()) {
            entityData.set(ARMS_ONLY_MODE, (byte) 2);
            scheduledTask = null;
            if (getCurrentTaskAction() == ModActions.STAND_ENTITY_UNSUMMON.get()) {
                stopTask();
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
            modifiersFromResolveLevel(getStandStatsDev(power));
        }
    }
    
    public static float getStandStatsDev(IStandPower stand) {
        return stand.usesResolve() ? stand.getResolveLevel() / stand.getMaxResolveLevel() : 0;
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
        public static final StandPose NONE = new StandPose();
        public static final StandPose SUMMON = new StandPose();
        public static final StandPose BLOCK = new StandPose();
        public static final StandPose LIGHT_ATTACK = new StandPose();
        public static final StandPose HEAVY_ATTACK = new StandPose();
        public static final StandPose RANGED_ATTACK = new StandPose();
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
        if (!level.isClientSide() && dmgAmount < 1F && !isInvulnerableTo(dmgSource) && !isDeadOrDying() 
                && !(dmgSource.isFire() && hasEffect(Effects.FIRE_RESISTANCE))
                && !dmgSource.isBypassArmor() && canBlockOrParryFromAngle(dmgSource)
                && barrageParryCount > 0) {
            barrageParryCount--;
            Entity attacker = dmgSource.getDirectEntity();
            Vector3d attackPos = this.getEyePosition(1.0F);
            if (attacker != null) {
                attackPos = attackPos.scale(0.5).add(attacker.getEyePosition(1.0F).scale(0.5));
            }
            else {
                attackPos = attackPos.add(this.getLookAngle().scale(1.0));
            }
            ((ServerWorld) level).sendParticles(ParticleTypes.CRIT, 
                    attackPos.x, attackPos.y, attackPos.z, 1, 0.5D, 0.25D, 0.5D, 0.2D);
            return false;
        }
        return super.hurt(dmgSource, dmgAmount);
    }
    
    @Override
    protected void actuallyHurt(DamageSource dmgSource, float damageAmount) {
        boolean blockableAngle = canBlockOrParryFromAngle(dmgSource);
        if (!dmgSource.isBypassArmor() && blockableAngle && getCurrentTask() == null
                && setTask(ModActions.STAND_ENTITY_BLOCK.get(), 5, StandEntityAction.Phase.PERFORM)) {
            setNoPhysics(false);
        }
        if (transfersDamage() && hasUser()) {
            LivingEntity user = getUser();
            if (user != null && user.isAlive()) {
                if (!isInvulnerableTo(dmgSource)) {
                    damageAmount = ForgeHooks.onLivingHurt(this, dmgSource, damageAmount);
                    if (damageAmount <= 0) return;
                    damageAmount = getDamageAfterArmorAbsorb(dmgSource, damageAmount);
                    damageAmount = getDamageAfterMagicAbsorb(dmgSource, damageAmount);
                    damageAmount = damageResistance(dmgSource, damageAmount, blockableAngle);
                    float damageAfterAbsorption = Math.max(damageAmount - getAbsorptionAmount(), 0.0F);
                    setAbsorptionAmount(getAbsorptionAmount() - (damageAmount - damageAfterAbsorption));
                    float absorbedDamage = damageAmount - damageAfterAbsorption;
                    if (absorbedDamage > 0.0F && absorbedDamage < 3.4028235E37F && dmgSource.getEntity() instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) dmgSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(absorbedDamage * 10.0F));
                    }
                    damageAfterAbsorption = ForgeHooks.onLivingDamage(this, dmgSource, damageAfterAbsorption);
                    if (damageAfterAbsorption != 0.0F) {
                        ModDamageSources.hurtThroughInvulTicks(user, new StandLinkDamageSource(this, dmgSource), damageAmount);
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
        super.knockback(strength, xRatio, zRatio);
        LivingEntity user = getUser();
        if (user != null && user.isAlive()) {
            user.knockback(strength, xRatio, zRatio);
        }
    }



    public boolean canStartBlocking() {
        StandEntityTask currentTask = getCurrentTask();
        return currentTask == null || currentTask.getAction().isCancelable(userPower, this, currentTask.getPhase(), null);
    }

    public boolean isStandBlocking() {
        return StandPose.BLOCK.equals(getStandPose());
    }
    
    protected float damageResistance(DamageSource damageSrc, float damageAmount, boolean blockableAngle) {
        if (!damageSrc.isBypassArmor()) {
            float blockedRatio = 0;
            if (blockableAngle && isBlocking() && userPower != null && userPower.usesStamina()) {
                float staminaCost = StandStatFormulas.getBlockStaminaCost(damageAmount);
                if (userPower.consumeStamina(staminaCost)) {
                    blockedRatio = 1F;
                }
                else {
                    blockedRatio = userPower.getStamina() / staminaCost;
                    standCrash();
                }
            }
            return damageAmount * StandStatFormulas.getPhysicalResistance(getDurability(), getAttackDamage(null), blockedRatio);
        }
        return damageAmount;
    }
    
    protected void standCrash() {
        // FIXME (!) a mechanic from that old JoJo fighting game i suck at
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
        accumulateBarrageTickParry = false;
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
//            if (target.getType() == TargetType.BLOCK) {
//                setTaskTarget(ActionTarget.EMPTY);
//            }
        }
        else if (user != null && !isRemotePositionFixed()) {
            float yRotSet = user.yRot;
            setRot(yRotSet, user.xRot);
            setYHeadRot(yRotSet);
        }
        
        rangeEfficiency = user != null ? 
                StandStatFormulas.rangeStrengthFactor(rangeEffective, getMaxRange(), distanceTo(user))
                : 1;
        
        staminaCondition = user != null ? 
                user.hasEffect(ModEffects.RESOLVE.get()) ? 
                        1
                        : 0.1 + Math.min((double) (userPower.getStamina() / userPower.getMaxStamina()) * 1.8, 0.9)
                : 1;
        
        if (summonLockTicks > 0) {
            summonLockTicks--;
        }
        else {
            StandEntityTask currentTask = getCurrentTask();
            if (currentTask != null) {
                currentTask.tick(userPower, this);
            }
            
            if (gradualSummonWeaknessTicks > 0) {
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
            
            updatePosition(user);
            swings.broadcastSwings(this);
            
            if (noComboDecayTicks > 0) {
                noComboDecayTicks--;
            }
            else {
                StandEntityAction currentAction = getCurrentTaskAction();
                if (currentAction == null || !currentAction.isCombatAction()) {
                    setComboMeter(Math.max(getComboMeter() - COMBO_DECAY, 0));
                }
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
    
    public void setRelativePos(double left, double forward) {
        relativePos.left = left;
        relativePos.forward = forward;
    }
    
    public void addRelativePos(double left, double forward) {
        relativePos.left += left;
        relativePos.forward += forward;
    }

    public void setRelativeY(double y) {
        relativePos.y = y;
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



    public boolean setTask(StandEntityAction action, int ticks, StandEntityAction.Phase phase) {
        return setTask(new StandEntityTask(action, ticks, phase));
    }

    protected boolean setTask(StandEntityTask task) {
        if (!level.isClientSide()) {
            if (stopTask(false, task.getAction()) && getCurrentTask() == null) {
                entityData.set(CURRENT_TASK, Optional.of(task));
                return true;
            }
            else {
                this.scheduledTask = task;
            }
        }
        return false;
    }
    
    public void stopTask() {
        stopTask(true, null);
    }
    
    private boolean stopTask(boolean resetPos, @Nullable StandEntityAction newAction) {
        if (!level.isClientSide()) {
            StandEntityTask currentTask = getCurrentTask();
            if (currentTask == null) {
                return true;
            }
            if (currentTask.getTicksLeft() <= 0) {
                clearTask(currentTask, resetPos);
                return true;
            }
            if (currentTask.getAction().isCancelable(userPower, this, currentTask.getPhase(), newAction)) {
                clearTask(currentTask, resetPos);
                return true;
            }
        }
        return false;
    }
    
    protected void clearTask(StandEntityTask clearedTask, boolean resetPos) {
        barrageParryCount = 0;
        setTaskTarget(ActionTarget.EMPTY);
        updateNoPhysics();
        setStandPose(StandPose.NONE);
        if (resetPos && !isArmsOnlyMode()) {
            relativePos.reset();
        }
        clearedTask.getAction().onClear(userPower, this);
        entityData.set(CURRENT_TASK, Optional.empty());
        if (scheduledTask != null) {
            StandEntityTask nextTask = scheduledTask;
            scheduledTask = null;
            setTask(nextTask);
        }
        else if (isArmsOnlyMode()) {
            StandEntityAction unsummon = ModActions.STAND_ENTITY_UNSUMMON.get();
            setTask(new StandEntityTask(unsummon, unsummon.getStandActionTicks(userPower, this), 
                    StandEntityAction.Phase.PERFORM));
        }
    }
    
    @Nullable
    protected StandEntityTask getCurrentTask() {
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

    public void setTaskTarget(ActionTarget target) {
        if (target.getType() != TargetType.ENTITY || target.getEntity(level) != this) {
            entityData.set(TASK_TARGET, target);
        }
    }

    protected ActionTarget getTaskTarget() {
        return entityData.get(TASK_TARGET);
    }

    public float getUserMovementFactor() {
        StandEntityAction currentAction = getCurrentTaskAction();
        if (currentAction == null) {
            return 1.0F;
        }
        return currentAction.userMovementFactor;
    }


    
    public boolean canAttackMelee() {
        return getAttackSpeed() > 0 && getAttributeValue(ForgeMod.REACH_DISTANCE.get()) > 0 ;
    }

    public boolean punch(PunchType punch) {
        if (!accumulateBarrageTickParry) {
            accumulateBarrageTickParry = true;
            barrageParryCount = 1;
        }
        else {
            barrageParryCount++;
        }
        
        if (punch == PunchType.LIGHT) {
            addComboMeter(0.007F);
        }

        // FIXME (!) precision: expand the hitbox of smaller entities more
        RayTraceResult target = JojoModUtil.rayTrace(this, getAttributeValue(ForgeMod.REACH_DISTANCE.get()), 
                entity -> !(entity instanceof LivingEntity) || canAttack((LivingEntity) entity), getAttributeValue(ModEntityAttributes.STAND_PRECISION.get()) * 0.5);
        switch (target.getType()) {
        case BLOCK:
            return breakBlock(((BlockRayTraceResult) target).getBlockPos());
        case ENTITY:
            EntityDistanceRayTraceResult result = (EntityDistanceRayTraceResult) target;
            return attackEntity(result.getEntity(), punch, result.getTargetAABBDistance());
        default:
            return false;
        }
    }
    
    public enum PunchType {
        HEAVY,
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
            if (!user.canAttack((LivingEntity) target)) {
                return false;
            }
            if (user instanceof PlayerEntity && target instanceof PlayerEntity && !((PlayerEntity) user).canHarmPlayer((PlayerEntity) target)) {
                return false;
            }
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
            double maxOffset = 0.9 / (getPrecision() + 1) - 0.9 / 11;
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
        return entityData.get(PUNCHES_COMBO);
    }
    
    protected void addComboMeter(float combo) {
        if (combo > 0) {
            setComboMeter(getComboMeter() + combo);
            noComboDecayTicks = COMBO_TICKS;
        }
    }
    
    protected void setComboMeter(float combo) {
        entityData.set(PUNCHES_COMBO, MathHelper.clamp(combo, 0F, 1F));
    }

    public boolean attackEntity(Entity target, PunchType punch, double attackDistance) {
        if (!canHarm(target)) {
            return false;
        }
        StandEntityDamageSource dmgSource = new StandEntityDamageSource("stand", this, getUserPower());
        
        LivingEntity livingTarget = null;
        if (target instanceof LivingEntity) {
            livingTarget = (LivingEntity) target;
        }
        
        double strength = getAttackDamage(livingTarget);
        double precision = getPrecision();
        double attackRange = getAttributeValue(ForgeMod.REACH_DISTANCE.get());
        double knockback = getAttackKnockback(livingTarget);
        
        float damage;
        float addCombo = 0;
        switch (punch) {
        case HEAVY:
            damage = StandStatFormulas.getHeavyAttackDamage(strength, livingTarget);

            double targetProximityRatio = 1 - attackDistance / attackRange;
            if (targetProximityRatio > 0.75) {
                damage *= targetProximityRatio * 2 - 0.5;
            }
            else if (targetProximityRatio < 0.25) {
                damage *= targetProximityRatio * 2 + 0.5;
            }
            
            knockback += damage / 4 * getComboMeter();
            break;
        case LIGHT:
            damage = StandStatFormulas.getLightAttackDamage(strength);
            break;
        case BARRAGE:
            damage = StandStatFormulas.getBarrageHitDamage(strength, precision, getRandom());
            dmgSource.setKnockbackReduction(0.1F);
            break;
        default:
            damage = 0;
        }
        
        if (punch == PunchType.LIGHT && livingTarget instanceof StandEntity) {
            StandEntity standTarget = (StandEntity) livingTarget;
            if (standTarget.getCurrentTaskAction() instanceof StandEntityHeavyAttack
                    && standTarget.getCurrentTaskPhase() == StandEntityAction.Phase.WINDUP
                    && standTarget.canBlockOrParryFromAngle(dmgSource)
                    && 1F - standTarget.getCurrentTaskCompletion(0) < StandStatFormulas.getParryTiming(precision)) {
                standTarget.parryHeavyAttack();
                return true;
            }
        }
        boolean attacked = hurtTarget(target, dmgSource, damage);
        if (attacked) {
            if (livingTarget != null) {
                if (knockback > 0.0F) {
                    livingTarget.knockback((float) knockback * 0.5F, (double) MathHelper.sin(yRot * ((float)Math.PI / 180F)), (double) (-MathHelper.cos(yRot * ((float)Math.PI / 180F))));
                }
                switch (punch) {
                case HEAVY:
                    if (!level.isClientSide()) {
                        ((ServerWorld) level).sendParticles(ParticleTypes.CRIT, 
                                livingTarget.getX(), livingTarget.getY(0.5), livingTarget.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.0D);
                        if (livingTarget.getUseItem().isShield(livingTarget) && target instanceof PlayerEntity) {
                            ((PlayerEntity) livingTarget).disableShield(precision > 0.5 && attackDistance < attackRange * 0.4);
                        }
                    }
                    break;
                case LIGHT:
                    addCombo = 0.16F;
                    break;
                case BARRAGE:
                    addCombo = 0.005F;
                    break;
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
        addComboMeter(addCombo);
        return attacked;
    }

    protected boolean hurtTarget(Entity target, DamageSource dmgSource, float damage) {
        return ModDamageSources.hurtThroughInvulTicks(target, dmgSource, (float) damage);
    }
    
    protected void parryHeavyAttack() {
        addEffect(new EffectInstance(ModEffects.STUN.get(), 30));
        this.playSound(ModSounds.PARRY.get(), 1.0F, 1.0F);
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
        return StandStatFormulas.isBlockBreakable(getAttackDamage(null), blockHardness, blockHarvestLevel);
    }

    public double getMaxRange() {
        return rangeMax;
    }
    
    public double getRangeEfficiency() {
        return rangeEfficiency;
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
                unsummonStand();
            }
        }
    }
    
    private void unsummonStand() {
        StandEntityAction unsummon = ModActions.STAND_ENTITY_UNSUMMON.get();
        setTask(unsummon, unsummon.getStandActionTicks(userPower, this), StandEntityAction.Phase.PERFORM);
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
