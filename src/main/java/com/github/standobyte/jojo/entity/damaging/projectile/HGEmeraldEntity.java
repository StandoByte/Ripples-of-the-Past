package com.github.standobyte.jojo.entity.damaging.projectile;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HGEmeraldEntity extends ModdedProjectileEntity {
    @Nullable
    private IStandPower userStandPower;
    private boolean lowerKnockback;
    private boolean breakBlocks;

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
        return 1F;
    }
    
    @Override
    protected float knockbackMultiplier() {
        return lowerKnockback ? 0.5F : super.knockbackMultiplier();
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return breakBlocks ? 1.5F : 0.0F;
    }

    @Override
    public int ticksLifespan() {
        return 100;
    }
    
    public void setLowerKnockback(boolean lowerKnockback) {
        this.lowerKnockback = lowerKnockback;
    }
    
    public void setBreakBlocks(boolean breakBlocks) {
        this.breakBlocks = breakBlocks;
    }

    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (!level.isClientSide() && entityHurt && userStandPower != null) {
            Entity target = entityRayTraceResult.getEntity();
            if (StandUtil.attackingTargetGivesResolve(target)) {
                userStandPower.addLearningProgressPoints(ModStandsInit.HIEROPHANT_GREEN_EMERALD_SPLASH.get(), 0.002F);
            }
        }
    }

    private static final Vector3d OFFSET = new Vector3d(0.0, -0.3, 0.75);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET;
    }
}
