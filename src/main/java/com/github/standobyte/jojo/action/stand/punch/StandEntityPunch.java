package com.github.standobyte.jojo.action.stand.punch;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ForgeEventFactory;

public class StandEntityPunch implements IPunch {
    public final StandEntity stand;
    public final Entity target;
    public final StandEntityDamageSource dmgSource;
    private boolean targetHit;
    private float damageDealtToLiving;
    
    protected float damage;
    protected float addFinisher;
    protected float knockback = 1.0F;
    protected float knockbackYRot = 0;
    protected float knockbackXRot = 0;
    protected float armorPiercing = 0;
    protected float disableBlockingChance = 0;
    protected Vector3d sweepingAabb;
    protected float sweepingDamage;
    protected int standInvulTime = 0;
    protected Supplier<SoundEvent> punchSound = () -> null;
    
    public StandEntityPunch(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        this.stand = stand;
        this.target = target;
        this.dmgSource = dmgSource;
    }
    
    @Override
    public TargetType getType() {
        return TargetType.ENTITY;
    }
    
    public StandEntityPunch copyProperties(StandEntityPunch original) {
        return this
        .damage(original.damage)
        .addFinisher(original.addFinisher)
        .knockbackVal(original.knockback)
        .knockbackYRotDeg(original.knockbackXRot)
        .knockbackXRot(original.knockbackXRot)
        .armorPiercing(original.armorPiercing)
        .disableBlocking(original.disableBlockingChance)
        .sweepingAttack(original.sweepingAabb, original.sweepingDamage)
        .setStandInvulTime(original.standInvulTime)
        .impactSound(original.punchSound);
    }
    
    public StandEntityPunch damage(float damage) {
        this.damage = Math.max(damage, 0);
        return this;
    }
    
    public StandEntityPunch addFinisher(float value) {
        this.addFinisher = value;
        return this;
    }
    
    public StandEntityPunch reduceKnockback(float knockback) {
        this.knockback = MathHelper.clamp(knockback, 0, 1);
        return this;
    }
    
    public StandEntityPunch addKnockback(float knockback) {
        this.knockback = 1 + knockback;
        return this;
    }
    
    public StandEntityPunch multiplyAddKnockback(float multiplier) {
        return addKnockback((this.knockback - 1) * multiplier);
    }
    
    private StandEntityPunch knockbackVal(float knockback) {
        this.knockback = knockback;
        return this;
    }
    
    public StandEntityPunch knockbackYRotDeg(float knockbackYRot) {
        this.knockbackYRot = knockbackYRot;
        return this;
    }
    
    public StandEntityPunch knockbackXRot(float knockbackXRot) {
        this.knockbackXRot = MathHelper.clamp(knockbackXRot, -90F, 90F);
        return this;
    }
    
    public StandEntityPunch armorPiercing(float armorPiercing) {
        this.armorPiercing = MathHelper.clamp(armorPiercing, 0, 1);
        return this;
    }
    
    public StandEntityPunch disableBlocking(float chance) {
        this.disableBlockingChance = MathHelper.clamp(chance, 0, 1);
        return this;
    }
    
    public StandEntityPunch sweepingAttack(double x, double y, double z, float damage) {
        if ((x > 0 || y > 0 || z > 0) && damage > 0) {
            return sweepingAttack(new Vector3d(Math.max(x, 0), Math.max(y, 0), Math.max(z, 0)), damage);
        }
        return this;
    }
    
    public StandEntityPunch sweepingAttack(Vector3d aabbRange, float damage) {
        this.sweepingAabb = aabbRange;
        this.sweepingDamage = damage;
        return this;
    }
    
    public StandEntityPunch setStandInvulTime(int ticks) {
        this.standInvulTime = ticks;
        return this;
    }
    
    public StandEntityPunch impactSound(Supplier<SoundEvent> sound) {
        this.punchSound = sound;
        return this;
    }


    
    public float getDamage() {
        return damage;
    }
    
    

    private boolean reducesKnockback() {
        return knockback < 1;
    }

    private float getKnockbackReduction() {
        return Math.min(knockback, 1);
    }
    
    private float getAdditionalKnockback() {
        return Math.max(knockback - 1, 0);
    }
    
    private boolean disablesBlocking() {
        return disableBlockingChance > 0;
    }
    
    private boolean isSweepingAttack() {
        return sweepingAabb != null && sweepingDamage > 0;
    }
    
    private AxisAlignedBB sweepingAttackAabb(AxisAlignedBB targetAabb) {
        return targetAabb.inflate(sweepingAabb.x, sweepingAabb.y, sweepingAabb.z);
    }
    

    @Override
    public boolean targetWasHit() {
        return targetHit;
    }
    
    @Override
    public StandEntity getStand() {
        return stand;
    }

    @Override
    public SoundEvent getImpactSound() {
        return punchSound != null ? punchSound.get() : null;
    }
    
    @Override
    public Vector3d getImpactSoundPos() {
        return target.getBoundingBox().getCenter();
    }
    
    public float getDamageDealtToLiving() {
        return damageDealtToLiving;
    }
    
    
    
    @Override
    public boolean doHit(StandEntityTask task) {
        if (stand.level.isClientSide()) return false;
        
        targetHit = stand.attackEntity(() -> doAttack(stand, target, dmgSource, damage), this, task);
        afterAttack(stand, target, dmgSource, task, targetHit, !target.isAlive());
        
        if (isSweepingAttack()) {
            for (LivingEntity sweepingTarget : stand.level.getEntitiesOfClass(LivingEntity.class, sweepingAttackAabb(target.getBoundingBox()), 
                    e -> !e.isSpectator() && e.isPickable() && e != target
                    && JojoModUtil.getDistance(stand, e.getBoundingBox()) < stand.getAttributeValue(ForgeMod.REACH_DISTANCE.get()) && stand.canHarm(e))) {
                doAttack(stand, sweepingTarget, dmgSource, sweepingDamage);
            }
        }
        
        if (!targetHit && target instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) target;
            boolean isTargetBlocking = targetLiving.isBlocking();
            if (isTargetBlocking) {
                ItemStack targetShield = targetLiving.getUseItem();
                if (targetShield.isShield(targetLiving) && damage < 3.0F && targetLiving instanceof PlayerEntity) {
                    int shieldItemDamage = MathUtil.fractionRandomInc(damage * 0.5F);
                    targetShield.hurtAndBreak(shieldItemDamage, targetLiving, e -> {
                        e.broadcastBreakEvent(targetLiving.getUsedItemHand());
                        ForgeEventFactory.onPlayerDestroyItem((PlayerEntity) targetLiving, 
                                targetShield, targetLiving.getUsedItemHand());
                    });
                }
            }
        }
        
        stand.addFinisherMeter(addFinisher, StandEntity.FINISHER_NO_DECAY_TICKS);
        
        return targetHit;
    }

    protected void afterAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, StandEntityTask task, boolean hurt, boolean killed) {}
    
    protected boolean doAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, float damage) {
        if (reducesKnockback()) {
            dmgSource.setKnockbackReduction(getKnockbackReduction());
        }

        LivingEntity targetLiving = null;
        float hp = 0;
        if (target instanceof LivingEntity) {
            targetLiving = (LivingEntity) target;
            hp = targetLiving.getHealth();
            
            dmgSource.setStandInvulTicks(standInvulTime);
            
            if (target instanceof StandEntity) {
                StandEntity targetStand = (StandEntity) target;
                
                if (disablesBlocking() && stand.getRandom().nextFloat() < disableBlockingChance) {
                    targetStand.breakStandBlocking(StandStatFormulas.getBlockingBreakTicks(targetStand.getDurability()));
                }
            }
            
            damage = DamageUtil.addArmorPiercing(damage, armorPiercing, targetLiving);
            
            final float dmg = damage;
            damage = targetLiving.getCapability(LivingUtilCapProvider.CAPABILITY).map(cap -> {
                return cap.onStandAttack(dmg);
            }).orElse(damage);
        }
        
        if (damage <= 0) {
            return false;
        }

        boolean hurt = stand.hurtTarget(target, dmgSource, damage);
        
        if (hurt) {
            if (targetLiving != null) {
                if (getAdditionalKnockback() > 0) {
                    Vector3d vecToTarget = target.position().subtract(stand.position());
                    float knockbackYRot = (float) -MathHelper.atan2(vecToTarget.x, vecToTarget.z) * MathUtil.RAD_TO_DEG + this.knockbackYRot;
                    float knockbackStrength = getAdditionalKnockback() * 0.5F;
                    if (Math.abs(knockbackXRot) < 90) {
                        DamageUtil.knockback(targetLiving, knockbackStrength * MathHelper.cos(knockbackXRot * MathUtil.DEG_TO_RAD), knockbackYRot);
                    }
                    if (knockbackXRot != 0) {
                        DamageUtil.upwardsKnockback(targetLiving, -knockbackStrength * MathHelper.sin(knockbackXRot * MathUtil.DEG_TO_RAD));
                    }
                }

                if (disablesBlocking() && 
                        targetLiving.getUseItem().isShield(targetLiving) && targetLiving instanceof PlayerEntity) {
                    DamageUtil.disableShield((PlayerEntity) targetLiving, disableBlockingChance);
                }
                
                damageDealtToLiving = hp - targetLiving.getHealth();
            }
        }
        
        return hurt;
    }
}
