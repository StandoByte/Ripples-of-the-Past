package com.github.standobyte.jojo.entity.damaging.projectile;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HGEmeraldEntity extends ModdedProjectileEntity {
    @Nullable
    private IStandPower userStandPower;

    public HGEmeraldEntity(LivingEntity shooter, World world, @Nullable IStandPower standPower) {
        super(ModEntityTypes.HG_EMERALD.get(), shooter, world);
        userStandPower = standPower;
    }

    public HGEmeraldEntity(EntityType<? extends HGEmeraldEntity> type, World world) {
        super(type, world);
    }

    @Override
    public boolean standDamage() {
        return true;
    }

    @Override
    public float getBaseDamage() {
        return 1.5F;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 1.0F;
    }

    @Override
    protected int ticksLifespan() {
        return 100;
    }

    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (!level.isClientSide() && entityHurt && userStandPower != null) {
            Entity target = entityRayTraceResult.getEntity();
            if (target.getClassification(false) == EntityClassification.MONSTER || target.getType() == EntityType.PLAYER) {
                userStandPower.addLearningProgressPoints(ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH.get(), 0.004F);
            }
        }
    }

    private static final Vector3d OFFSET = new Vector3d(0.0, -0.3, 0.75);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET;
    }
}
