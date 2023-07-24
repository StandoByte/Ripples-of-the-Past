package com.github.standobyte.jojo.capability.entity.hamonutil;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil.HamonAttackProperties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.EggEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

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
            HamonPowerType.emitHamonSparkParticles(projectile.level, null, 
                    projectile.getX(), projectile.getY(0.5), projectile.getZ(), getHamonDamage());
        }
    }
    
    public float getHamonDamage() {
        float damage = hamonBaseDmg;
        if (maxChargeTicks >= 0) {
            damage *= MathHelper.clamp((float) (maxChargeTicks - projectile.tickCount) / (float) maxChargeTicks, 0F, 1F);
        }
        return damage;
    }
    
    public void onTargetHit(RayTraceResult target) {
        World world = projectile.level;
        if (!world.isClientSide()) {
            // adds hamon damage to splash water bottle
            if (projectile instanceof PotionEntity) {
                PotionEntity potionEntity = (PotionEntity) projectile;
                if (MCUtil.isPotionWaterBottle(potionEntity)) {
                    AxisAlignedBB waterSplashArea = projectile.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
                    List<LivingEntity> splashedEntity = world.getEntitiesOfClass(LivingEntity.class, waterSplashArea, 
                            EntityPredicates.LIVING_ENTITY_STILL_ALIVE.and(EntityPredicates.NO_SPECTATORS).and(entity -> !entity.is(potionEntity.getOwner())));
                    if (!splashedEntity.isEmpty()) {
                        for (LivingEntity targetEntity : splashedEntity) {
                            double distSqr = projectile.distanceToSqr(targetEntity);
                            if (distSqr < 16.0) {
                                dealHamonDamageToTarget(targetEntity, hamonBaseDmg * (1 - (float) (distSqr / 16)));
                            }
                        }
                    }
                }
                
                return;
            }
            
            // add hamon damage on direct hit
            if (target.getType() == RayTraceResult.Type.ENTITY) {
                dealHamonDamageToTarget(((EntityRayTraceResult) target).getEntity(), hamonBaseDmg);
            }
            
            // memorize charged egg entity to potentially charge the chicken(s) coming out of it
            if (projectile instanceof EggEntity) {
                world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.addChargedEggEntity((EggEntity) projectile));
            }
        }
    }
    
    private boolean dealHamonDamageToTarget(Entity entity, float damage) {
        if (damage > 0) {
            Entity owner = getProjectileOwner();
            if (multiplyWithUserStrength) {
                if (owner instanceof LivingEntity) {
                    LivingEntity hamonUser = (LivingEntity) owner;
                    return GeneralUtil.orElseFalse(INonStandPower.getNonStandPowerOptional(hamonUser), power -> {
                        return GeneralUtil.orElseFalse(power.getTypeSpecificData(ModPowers.HAMON.get()), hamon -> {
                            if (DamageUtil.dealHamonDamage(entity, getHamonDamage(), projectile, owner)) {
                                hamon.hamonPointsFromAction(HamonStat.STRENGTH, spentEnergy);
                                return true;
                            }
                            return false;
                        });
                    });
                }
            }
            
            return DamageUtil.dealHamonDamage(entity, getHamonDamage(), projectile, owner, 
                    HamonAttackProperties::noSrcEntityHamonMultiplier);
        }
        
        return false;
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
