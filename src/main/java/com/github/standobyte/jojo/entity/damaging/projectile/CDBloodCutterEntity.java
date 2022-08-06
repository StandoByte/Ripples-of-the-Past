package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;

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
        return 5.0F;
    }
    
    protected void breakProjectile(TargetType targetType, RayTraceResult hitTarget) {
        if (targetType != TargetType.ENTITY || ((EntityRayTraceResult) hitTarget).getEntity() instanceof LivingEntity) {
            super.breakProjectile(targetType, hitTarget);
            splashBlood();
        }
    }
    
    private void splashBlood() {
        if (!level.isClientSide()) {
            // FIXME !! (blood cutter) blood tag
        }
        else {
            level.playLocalSound(getX(), getY(), getZ(), ModSounds.WATER_SPLASH.get(), getSoundSource(), 1.0F, 1.0F, false);
            // FIXME !! (blood cutter) blood particles
        }
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
