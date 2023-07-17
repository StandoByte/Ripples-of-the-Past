package com.github.standobyte.jojo.capability.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil.HamonAttackProperties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;

public class ProjectileHamonChargeCap {
    @Nonnull private final Entity projectile;
    private float hamonBaseDmg;
    private int maxChargeTicks;
    
    private boolean multiplyWithUserStrength;
    private float spentEnergy;
    
    public ProjectileHamonChargeCap(Entity projectile) {
        this.projectile = projectile;
    }
    
    public void setBaseDmg(float damage) {
        this.hamonBaseDmg = damage;
    }
    
    public void setMaxChargeTicks(int ticks) {
        this.maxChargeTicks = ticks;
    }
    
    public void setInfiniteChargeTime() {
        this.maxChargeTicks = -1;
    }
    
    public void setSpentEnergy(float spentEnergy) {
        this.spentEnergy = spentEnergy;
    }
    
    public void setMultiplyWithUserStrength(boolean doMultipy) {
        this.multiplyWithUserStrength = doMultipy;
    }
    
    
    
    public void tick() {
        if (hamonBaseDmg > 0 && projectile.canUpdate()) {
            HamonPowerType.createHamonSparkParticles(projectile.level, null, 
                    projectile.getX(), projectile.getY(0.5), projectile.getZ(), getHamonDamage());
        }
    }
    
    private float getHamonDamage() {
        float chargeRatio = maxChargeTicks >= 0 ? MathHelper.clamp((float) (maxChargeTicks - projectile.tickCount) / (float) maxChargeTicks, 0F, 1F) : 1F;
        return hamonBaseDmg * chargeRatio;
    }
    
    public void addHamonDamageToProjectile(EntityRayTraceResult target) {
        if (!projectile.level.isClientSide() && hamonBaseDmg > 0) {
            Entity owner = getProjectileOwner();
            if (multiplyWithUserStrength) {
                if (owner instanceof LivingEntity) {
                    LivingEntity hamonUser = (LivingEntity) owner;
                    INonStandPower.getNonStandPowerOptional(hamonUser).ifPresent(power -> {
                        power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                            DamageUtil.dealHamonDamage(target.getEntity(), getHamonDamage(), projectile, owner);
                            hamon.hamonPointsFromAction(HamonStat.STRENGTH, spentEnergy);
                        });
                    });
                    return;
                }
            }
            
            DamageUtil.dealHamonDamage(target.getEntity(), getHamonDamage(), projectile, owner, 
                    HamonAttackProperties::noSrcEntityHamonMultiplier);
        }
    }
    
    @Nullable
    private Entity getProjectileOwner() {
        if (projectile instanceof ProjectileEntity) {
            return ((ProjectileEntity) projectile).getOwner();
        }
        return null;
    }
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("HamonDamage", hamonBaseDmg);
        nbt.putInt("ChargeTicks", maxChargeTicks);
        nbt.putFloat("SpentEnergy", spentEnergy);
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        this.hamonBaseDmg = nbt.getFloat("HamonDamage");
        this.maxChargeTicks = nbt.getInt("ChargeTicks");
        this.spentEnergy = nbt.getFloat("SpentEnergy");
    }
}
