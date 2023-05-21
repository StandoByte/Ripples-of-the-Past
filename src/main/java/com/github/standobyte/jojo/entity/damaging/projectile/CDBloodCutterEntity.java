package com.github.standobyte.jojo.entity.damaging.projectile;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.GameplayEventHandler;
import com.github.standobyte.jojo.util.general.GeneralUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class CDBloodCutterEntity extends ModdedProjectileEntity {
    
    public CDBloodCutterEntity(LivingEntity shooter, World world) {
        super(ModEntityTypes.CD_BLOOD_CUTTER.get(), shooter, world);
    }

    public CDBloodCutterEntity(EntityType<? extends CDBloodCutterEntity> type, World world) {
        super(type, world);
    }

    @Override
    public int ticksLifespan() {
        return 100;
    }

    @Override
    protected float getBaseDamage() {
        return 4.0F;
    }
    
    protected void breakProjectile(TargetType targetType, RayTraceResult hitTarget) {
        if (targetType != TargetType.ENTITY || ((EntityRayTraceResult) hitTarget).getEntity() instanceof LivingEntity) {
            super.breakProjectile(targetType, hitTarget);
            splashBlood();
        }
    }
    
    private void splashBlood() {
        if (isInWaterOrBubble()) return;
        if (!level.isClientSide()) {
            GameplayEventHandler.splashBlood(level, getBoundingBox().getCenter(), 4, 6.4F, Optional.ofNullable(getOwner()));
            level.playSound(null, getX(), getY(), getZ(), ModSounds.WATER_SPLASH.get(), getSoundSource(), 1.0F, 1.0F);
        }
    }
    
    @Override
    protected boolean hurtTarget(Entity target, @Nullable LivingEntity owner) {
        if (target instanceof LivingEntity){
            LivingEntity targetLiving = (LivingEntity) target;
            
            if (GeneralUtil.orElseFalse(INonStandPower.getNonStandPowerOptional(targetLiving), power -> {
                if (power.getType() == ModPowers.VAMPIRISM.get()) {
                    target.playSound(ModSounds.VAMPIRE_BLOOD_DRAIN.get(), 1.0F, 1.0F);
                    power.addEnergy(5F);
                    return true;
                }
                return false;
            })) {
                remove();
                return false;
            }
        }
        return super.hurtTarget(target, owner);
    }
    
    public static boolean canHaveBloodDropsOn(Entity target, IStandPower bleedingEntityStand) {
        return !target.is(bleedingEntityStand.getUser()) && target != bleedingEntityStand.getStandManifestation()
                && !(target instanceof StandEntity && !((StandEntity) target).isVisibleForAll())
                && !target.isInWaterOrBubble();
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return false;
    }

}
