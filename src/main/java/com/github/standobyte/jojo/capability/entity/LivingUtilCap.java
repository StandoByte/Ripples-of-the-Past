package com.github.standobyte.jojo.capability.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.capability.entity.living.LivingWallClimbing;
import com.github.standobyte.jojo.entity.AfterimageEntity;
import com.github.standobyte.jojo.entity.HamonSendoOverdriveEntity;
import com.github.standobyte.jojo.entity.ai.LookAtEntityWithoutMovingGoal;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrCosmeticItemsPacket;
import com.github.standobyte.jojo.potion.HamonSpreadEffect;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.CollideBlocks;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.IModdedDamageSource;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.ForgeMod;

public class LivingUtilCap {
    private final LivingEntity entity;
    
    private IStandPower lastHurtByStand;
    private int lastHurtByStandTicks;
    public float lastStandDamage;
    public int standInvulnerableTime;
    
//    private int hurtThroughInvulTime;
    private int hurtArmorTime;
    
    private boolean reduceKnockback;
    private float futureKnockbackFactor;
    @Nullable private Explosion latestExplosion;
    
    public boolean didStackKnockbackInstead;
    @Nullable private Vector3d blockImpactKbVec;
    private double blockImpactMultiplier;
    
    private int noGravityTicks = 0;
    
    private List<StandEffectInstance> standEffectsTargetedBy = new LinkedList<>();
    
    public boolean hasUsedTimeStopToday = false;
    private int noLerpTicks = 0;
    private int hurtTimeSaved;
    
    private HamonSendoOverdriveEntity hurtFromSendoOverdrive;
    private int sendoOverdriveWaveTicks;
    
    private float receivedHamonDamage = 0;
    @Nullable private UUID preHypnosisOwner = null;
    
    private final List<AfterimageEntity> afterimages = new ArrayList<>();
    private boolean usedZoomPunch = false;
    private boolean gotScarf = false;
    
    private LivingWallClimbing wallClimb;
    
    private DyeColor[] ladybugBroochesColored = new DyeColor[3];
    
    public LivingUtilCap(LivingEntity entity) {
        this.entity = entity;
        this.wallClimb = new LivingWallClimbing(entity);
    }
    
    public void tick() {
        lastHurtByStandTick();
        tickNoLerp();
        tickHurtAnim();
        tickDownHamonDamage(); 
        
        if (!entity.level.isClientSide()) {
            tickSendoOverdriveHurtTimer();
            tickHypnosisProcess();
            tickKnockbackBlockImpact();
            tickNoGravityModifier();
        }
        
        Iterator<AfterimageEntity> it = afterimages.iterator();
        while (it.hasNext()) {
            AfterimageEntity afterimage = it.next();
            if (!afterimage.isAlive()) {
                it.remove();
            }
        }
    }
    
    
    public float onStandAttack(float damage) {
        this.lastStandDamage = damage;
        return standInvulnerableTime > 0 ? Math.max(damage - lastStandDamage, 0) : damage;
    }
    
    public void setLastHurtByStand(IStandPower stand, float damage, int invulTicks) {
        this.lastHurtByStand = stand;
        this.lastHurtByStandTicks = 100;
        if (invulTicks > 0) {
            this.standInvulnerableTime = invulTicks;
        }
    }
    
    @Nullable
    public IStandPower getLastHurtByStand() {
        return lastHurtByStand;
    }
    
    public void lastHurtByStandTick() {
        if (lastHurtByStandTicks > 0 && --lastHurtByStandTicks == 0) {
            lastHurtByStand = null;
        }
        
        if (standInvulnerableTime > 0) --standInvulnerableTime;
        if (hurtArmorTime > 0) --hurtArmorTime;
    }
    
    public void setFutureKnockbackFactor(float factor) {
        this.futureKnockbackFactor = MathHelper.clamp(factor, 0, 1);
        this.reduceKnockback = true;
    }
    
    public boolean shouldReduceKnockback() {
        return reduceKnockback;
    }
    
    public float getKnockbackFactorOneTime() {
        reduceKnockback = false;
        return futureKnockbackFactor;
    }
    
    public void setLatestExplosion(Explosion explosion) {
        this.latestExplosion = explosion;
    }
    
    @Nullable
    public Explosion getSourceExplosion(DamageSource damageSource) {
        if (latestExplosion != null && damageSource == latestExplosion.getDamageSource()) {
            return latestExplosion;
        }
        return null;
    }
    
    public void onHurtThroughInvul(IModdedDamageSource dmgSource) {
        if (hurtArmorTime > 0) {
            dmgSource.setPreventDamagingArmor();
        }
        else {
            hurtArmorTime = 5;
        }
    }
    
    
    public void setKnockbackBlockImpact(Vector3d knockbackVec) {
        blockImpactKbVec = knockbackVec;
        blockImpactMultiplier = 1;
    }
    
    private void tickKnockbackBlockImpact() {
        if (blockImpactKbVec != null) {
            Vector3d speedVec = entity.getDeltaMovement()
//                    .subtract(0, entity.getAttributeValue(ForgeMod.ENTITY_GRAVITY.get()), 0)
                    .multiply(1, 0, 1);
            
            if (Math.abs(speedVec.x) < 1.0E-7 && Math.abs(speedVec.z) < 1.0E-7) {
                blockImpactKbVec = null;
                return;
            }
            
            blockImpactMultiplier = Math.min(blockImpactMultiplier, speedVec.normalize().dot(blockImpactKbVec.normalize() /*we can cache this*/));
            if (blockImpactMultiplier < 0) {
                blockImpactKbVec = null;
                return;
            }
            
            Collection<BlockPos> blocksCollision = CollideBlocks.getBlocksOutlineTowards(
                    entity.getBoundingBox(), speedVec, entity.level, true);
            if (!blocksCollision.isEmpty()) {
                blocksCollision.forEach(blockPos -> {
//                  BlockState blockState = entity.level.getBlockState(blockPos);
                  entity.level.destroyBlock(blockPos, true);
                });
                // and ig we reduce blockImpactMultiplier?
                
//                if (entity.isOnGround()) {
//                    blockImpactKbVec = null;
//                }
            }
        }
    }
    
    private static final AttributeModifier NO_GRAVITY_MODIFIER = new AttributeModifier(
            UUID.fromString("4167f685-15f5-4dc6-8b8a-14adfbc05453"), "No gravity when being attacked", -1, Operation.MULTIPLY_TOTAL);
    public void setNoGravityFor(int ticks) {
        boolean addModifier = this.noGravityTicks <= 0;
        this.noGravityTicks = ticks;
        if (addModifier) {
            Vector3d motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, Math.max(motion.y, 0), motion.z);
            ModifiableAttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
            gravity.addTransientModifier(NO_GRAVITY_MODIFIER);
        }
    }
    
    private void tickNoGravityModifier() {
        if (noGravityTicks > 0 && --noGravityTicks == 0) {
            ModifiableAttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
            gravity.removeModifier(NO_GRAVITY_MODIFIER);
        }
    }
    
    
    
    public void addEffectTargetedBy(StandEffectInstance instance) {
        this.standEffectsTargetedBy.add(instance);
    }
    
    public void removeEffectTargetedBy(StandEffectInstance instance) {
        this.standEffectsTargetedBy.remove(instance);
    }
    
    
    
    public void addAfterimages(int count, int lifespan) {
        if (!entity.level.isClientSide()) {
            int i = 0;
            for (AfterimageEntity afterimage : afterimages) {
                if (afterimage.isAlive()) {
                    afterimage.setLifeSpan(lifespan < 0 ? Integer.MAX_VALUE : afterimage.tickCount + lifespan);
                    i++;
                }
            }
            double minSpeed = entity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
            double speed = entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
            for (; i < count; i++) {
                AfterimageEntity afterimage = new AfterimageEntity(entity.level, entity, i + 1);
                afterimage.setLifeSpan(lifespan < 0 ? Integer.MAX_VALUE :lifespan);
                afterimage.setMinSpeed(minSpeed + (speed - minSpeed) * (double) (i + 1) / (double) count);
                afterimages.add(afterimage);
                entity.level.addFreshEntity(afterimage);
            }
        }
    }
    
    
    
    private static final float[] DAMAGE_FOR_HAMON_SPREAD_EFFECT = new float[] { 5, 12.5F, 25, 50, 125 };
    public void hamonSpread(float damageReceived) {
        receivedHamonDamage += damageReceived;
        for (int i = DAMAGE_FOR_HAMON_SPREAD_EFFECT.length - 1; i >= 0; i--) {
            if (receivedHamonDamage >= DAMAGE_FOR_HAMON_SPREAD_EFFECT[i]) {
                int duration = 60 + 40 * i;
                HamonSpreadEffect.giveEffectTo(entity, duration, i);
                break;
            }
        }
    }
    
    private void tickDownHamonDamage() {
        receivedHamonDamage = Math.max(receivedHamonDamage - 0.1F, 0);
    }
    
    
    
    public void setNoLerpTicks(int ticks) {
        this.noLerpTicks = ticks;
    }
    
    private void tickNoLerp() {
        if (noLerpTicks > 0 && CommonReflection.getLerpSteps(entity) > 1) {
            CommonReflection.setLerpSteps(entity, 1);
            noLerpTicks--;
        }
    }
    
    
    
    private void tickHurtAnim() {
        if (!entity.canUpdate()) {
            if (entity.hurtTime > 0) {
                hurtTimeSaved = entity.hurtTime;
                entity.hurtTime = 0;
            }
        }
        else if (hurtTimeSaved > 0) {
            entity.hurtTime = hurtTimeSaved;
            hurtTimeSaved = 0;
        }
    }
    
    
    
    public boolean tryHurtFromSendoOverdrive(HamonSendoOverdriveEntity overdrive, int otherWavesImmuneTicks) {
        boolean canBeHurt = hurtFromSendoOverdrive == null || hurtFromSendoOverdrive == overdrive;
        if (canBeHurt) {
            this.hurtFromSendoOverdrive = overdrive;
            this.sendoOverdriveWaveTicks = otherWavesImmuneTicks;
        }
        return canBeHurt;
    }
    
    private void tickSendoOverdriveHurtTimer() {
        if (sendoOverdriveWaveTicks > 0 && --sendoOverdriveWaveTicks == 0) {
            hurtFromSendoOverdrive = null;
        }
    }
    
    
    
    public void setUsingZoomPunch(boolean zoomPunch) {
        this.usedZoomPunch = zoomPunch;
    }
    
    public boolean isUsingZoomPunch() {
        return usedZoomPunch;
    }
    
    public boolean onScarfPerk() {
        boolean canGetScarf = !gotScarf;
        gotScarf = true;
        return canGetScarf;
    }
    
  
    
    public static HypnosisTargetCheck canBeHypnotized(LivingEntity entity, LivingEntity hypnotizer) {
        if (hypnotizer instanceof PlayerEntity) {
            if (entity instanceof TameableEntity) {
                TameableEntity tameable = (TameableEntity) entity;
                return !hypnotizer.getUUID().equals(tameable.getOwnerUUID()) ? 
                        HypnosisTargetCheck.CORRECT : HypnosisTargetCheck.ALREADY_TAMED_BY_USER;
            }
            if (entity instanceof AbstractHorseEntity) {
                AbstractHorseEntity horse = (AbstractHorseEntity) entity;
                return !hypnotizer.getUUID().equals(horse.getOwnerUUID()) ? 
                        HypnosisTargetCheck.CORRECT : HypnosisTargetCheck.ALREADY_TAMED_BY_USER;
            }
        }
        return HypnosisTargetCheck.INVALID;
    }
    
    public static enum HypnosisTargetCheck {
        CORRECT,
        INVALID,
        ALREADY_TAMED_BY_USER
    }
    
    public void hypnotizeEntity(LivingEntity hypnotizer, int duration) {
        if (!entity.level.isClientSide()) {
            boolean giveEffect = false;
            
            if (hypnotizer instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) hypnotizer;
                if (entity instanceof TameableEntity) {
                    TameableEntity tameable = (TameableEntity) entity;
                    preHypnosisOwner = tameable.getOwnerUUID();
                    tameable.tame(player);
                    giveEffect = true;
                }
                
                else if (entity instanceof AbstractHorseEntity) {
                    AbstractHorseEntity horse = (AbstractHorseEntity) entity;
                    preHypnosisOwner = horse.getOwnerUUID();
                    horse.tameWithName(player);
                    giveEffect = true;
                }
                
                entity.level.broadcastEntityEvent(entity, (byte) 7); // spawn tame heart particles
            }
            
            if (giveEffect) {
                entity.addEffect(new EffectInstance(ModStatusEffects.HYPNOSIS.get(), duration, 0, false, false, true));
            }
        }
    }
    
    public void relieveHypnosis() {
        if (!entity.level.isClientSide()) {
            if (entity instanceof TameableEntity) {
                TameableEntity tameable = (TameableEntity) entity;
                tameable.setOrderedToSit(false);
                tameable.setTame(preHypnosisOwner != null);
                tameable.setOwnerUUID(preHypnosisOwner);
            }
            
            else if (entity instanceof AbstractHorseEntity) {
                AbstractHorseEntity horse = (AbstractHorseEntity) entity;
                horse.setTamed(preHypnosisOwner != null);
                horse.setOwnerUUID(preHypnosisOwner);
                preHypnosisOwner = null;
                horse.makeMad();
            }
            
            entity.level.broadcastEntityEvent(entity, (byte) 6); // spawn smoke particles
        }
    }
    
    private Goal lookAtHypnotizerGoal;
    private int resetLookGoalTicks = 0;
    public void startedHypnosisProcess(LivingEntity hypnotizer) {
        if (entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;
            lookAtHypnotizerGoal = new LookAtEntityWithoutMovingGoal(mob, hypnotizer);
            mob.goalSelector.addGoal(0, lookAtHypnotizerGoal);
            resetLookGoalTicks = 3;
        }
    }
    
    private void tickHypnosisProcess() {
        if (resetLookGoalTicks > 0 && --resetLookGoalTicks == 0
                && lookAtHypnotizerGoal != null && entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;
            mob.goalSelector.removeGoal(lookAtHypnotizerGoal);
            lookAtHypnotizerGoal = null;
        }
    }
    
    
    
    public LivingWallClimbing getWallClimbHandler() {
        return wallClimb;
    }
    
    public void limitPlayerHeadRot() {
        wallClimb.climbLimitPlayerHeadRot();
    }
    
    
    
    public void onClone(LivingUtilCap old, boolean wasDeath) {
        hasUsedTimeStopToday = old.hasUsedTimeStopToday;
        gotScarf = old.gotScarf;
    }
    
    public boolean addLadybugBrooch(DyeColor color) {
        for (int i = 0; i < ladybugBroochesColored.length; i++) {
            if (ladybugBroochesColored[i] == null) {
                setBrooch(i, color);
                return true;
            }
        }
        
        return false;
    }
    
    public boolean canConsumeBrooch() {
        for (int i = 0; i < ladybugBroochesColored.length; i++) {
            if (ladybugBroochesColored[i] != null) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean consumeBrooch() {
        for (int i = ladybugBroochesColored.length - 1; i >= 0; i--) {
            if (ladybugBroochesColored[i] != null) {
                setBrooch(i, null);
                return true;
            }
        }
        
        return false;
    }
    
    private void setBrooch(int index, @Nullable DyeColor color) {
        if (!entity.level.isClientSide()) {
            ladybugBroochesColored[index] = color;
            PacketManager.sendToClientsTrackingAndSelf(TrCosmeticItemsPacket.ladybugBrooch(entity.getId(), ladybugBroochesColored), entity);
        }
    }
    
    public void clSetBrooches(DyeColor[] colors) {
        for (int i = 0; i < colors.length && i < ladybugBroochesColored.length; i++) {
            ladybugBroochesColored[i] = colors[i];
        }
    }
    
    @Nullable
    public DyeColor getBroochWorn(int index) {
        return ladybugBroochesColored[index];
    }
    
    
    
    public void onTracking(ServerPlayerEntity tracking) {
        if (canConsumeBrooch()) {
            PacketManager.sendToClient(TrCosmeticItemsPacket.ladybugBrooch(entity.getId(), 
                    ladybugBroochesColored), tracking);
        }
        wallClimb.syncToPlayer(tracking);
    }
    
    public void syncWithClient() {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            if (canConsumeBrooch()) {
                PacketManager.sendToClient(TrCosmeticItemsPacket.ladybugBrooch(entity.getId(), 
                        ladybugBroochesColored), player);
            }
            wallClimb.syncToPlayer(player);
        }
    }
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("HamonSpread", receivedHamonDamage);
        MCUtil.nbtPutVec3d(nbt, "BlockImpactVec", blockImpactKbVec);
        nbt.putBoolean("UsedTimeStop", hasUsedTimeStopToday);
        if (preHypnosisOwner != null) {
            nbt.putUUID("PreHypnosisOwner", preHypnosisOwner);
        }
        nbt.putBoolean("GotScarf", gotScarf);
        MCUtil.nbtPutEnumArray(nbt, "Brooches", ladybugBroochesColored);
        nbt.put("WallClimb", wallClimb.serializeNBT());
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        receivedHamonDamage = nbt.getFloat("HamonSpread");
        blockImpactKbVec = MCUtil.nbtGetVec3d(nbt, "BlockImpactVec");
        hasUsedTimeStopToday = nbt.getBoolean("UsedTimeStop");
        if (nbt.hasUUID("PreHypnosisOwner")) {
            preHypnosisOwner = nbt.getUUID("PreHypnosisOwner");
        }
        gotScarf = nbt.getBoolean("GotScarf");
        ladybugBroochesColored = MCUtil.nbtGetEnumArray(nbt, "Brooches", DyeColor.class);
        MCUtil.nbtGetCompoundOptional(nbt, "WallClimb").ifPresent(wallClimb::deserializeNBT);
    }
    
}
