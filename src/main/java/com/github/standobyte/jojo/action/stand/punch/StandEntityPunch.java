package com.github.standobyte.jojo.action.stand.punch;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.action.actions.StandEntityHeavyAttack;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;
import com.github.standobyte.jojo.util.utils.JojoModUtil;
import com.github.standobyte.jojo.util.utils.MathUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;

public class StandEntityPunch implements IPunch {
    public final StandEntity stand;
    public final Entity target;
    public final StandEntityDamageSource dmgSource;
    private boolean targetHit;
    protected float damage;
    protected float addCombo;
    protected float knockback = 1.0F;
    protected float knockbackYRot = 0;
    protected float knockbackXRot = 0;
    protected float armorPiercing = 0;
    protected float disableBlockingChance = 0;
    protected float parryTiming = 0;
    protected Vector3d sweepingAabb;
    protected float sweepingDamage;
    protected int standInvulTime = 0;
    protected Supplier<SoundEvent> punchSound = () -> null;
    
    public StandEntityPunch(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        this.stand = stand;
        this.target = target;
        this.dmgSource = dmgSource;
    }
    
    public StandEntityPunch damage(float damage) {
        this.damage = Math.max(damage, 0);
        return this;
    }
    
    public StandEntityPunch addCombo(float combo) {
        this.addCombo = combo;
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
    
    public StandEntityPunch parryTiming(float parryTiming) {
        this.parryTiming = MathHelper.clamp(parryTiming, 0, 1);
        return this;
    }
    
    public StandEntityPunch sweepingAttack(double x, double y, double z, float damage) {
        x = Math.max(x, 0);
        y = Math.max(y, 0);
        z = Math.max(z, 0);
        if ((x > 0 || y > 0 || z > 0) && damage > 0) {
            this.sweepingAabb = new Vector3d(x, y, z);
            this.sweepingDamage = damage;
        }
        return this;
    }
    
    public StandEntityPunch setStandInvulTime(int ticks) {
        this.standInvulTime = ticks;
        return this;
    }
    
    public StandEntityPunch setPunchSound(Supplier<SoundEvent> sound) {
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
    
    private boolean canParryHeavyAttack() {
        return parryTiming > 0;
    }
    
    private boolean isSweepingAttack() {
        return sweepingAabb != null && sweepingDamage > 0;
    }
    
    private AxisAlignedBB sweepingAttackAabb(AxisAlignedBB targetAabb) {
        return targetAabb.inflate(sweepingAabb.x, sweepingAabb.y, sweepingAabb.z);
    }
    
    

    @Override
    public boolean hit(StandEntity standEntity, StandEntityTask task) {
        if (stand.level.isClientSide()) return false;
        targetHit = attackEntity(standEntity, target, dmgSource, task);
        return targetHit;
    }

    @Override
    public boolean targetWasHit() {
        return targetHit;
    }

    @Override
    public SoundEvent getSound() {
        return punchSound != null ? punchSound.get() : null;
    }
    
    @Override
    public Vector3d getSoundPos() {
        return target.getBoundingBox().getCenter();
    }
    
    
    
    private boolean attackEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, StandEntityTask task) {
        boolean attacked = doAttack(stand, target, dmgSource, damage);
        afterAttack(stand, target, dmgSource, task, attacked, !target.isAlive());
        
        if (attacked) {
            if (isSweepingAttack()) {
                for (LivingEntity sweepingTarget : stand.level.getEntitiesOfClass(LivingEntity.class, sweepingAttackAabb(target.getBoundingBox()), 
                        e -> !e.isSpectator() && e.isPickable()
                        && JojoModUtil.getDistance(stand, e.getBoundingBox()) < stand.getAttributeValue(ForgeMod.REACH_DISTANCE.get()) && stand.canHarm(e))) {
                    doAttack(stand, sweepingTarget, dmgSource, sweepingDamage);
                }
            }
            
            stand.addComboMeter(addCombo, StandEntity.COMBO_TICKS);
        }
        return attacked;
    }

    protected void afterAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, StandEntityTask task, boolean hurt, boolean killed) {}
    
    public boolean doAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, float damage) {
        if (reducesKnockback()) {
            dmgSource.setKnockbackReduction(getKnockbackReduction());
        }

        LivingEntity targetLiving = null;
        if (target instanceof LivingEntity) {
            targetLiving = (LivingEntity) target;
            
            dmgSource.setStandInvulTicks(standInvulTime);
            
            if (target instanceof StandEntity) {
                StandEntity targetStand = (StandEntity) target;
                
                if ((canParryHeavyAttack() || disablesBlocking())) {
                    if (canParryHeavyAttack()) {
                        if (targetStand.getCurrentTaskAction() instanceof StandEntityHeavyAttack
                                && targetStand.getCurrentTaskPhase().get() == StandEntityAction.Phase.WINDUP
                                && targetStand.canBlockOrParryFromAngle(dmgSource.getSourcePosition())
                                && 1F - targetStand.getCurrentTaskCompletion(0) < parryTiming) {
                            targetStand.parryHeavyAttack();
                            return false;
                        }
                    }
                    
                    if (disablesBlocking() && stand.getRandom().nextFloat() < disableBlockingChance) {
                        targetStand.breakStandBlocking(StandStatFormulas.getBlockingBreakTicks(targetStand.getDurability()));
                    }
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
            }
        }
        
        return hurt;
    }
}
