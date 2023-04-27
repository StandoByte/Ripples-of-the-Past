package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class SCFlameSwingEntity extends MRFlameEntity {
    
    public SCFlameSwingEntity(LivingEntity shooter, World world) {
        super(ModEntityTypes.SC_FLAME.get(), shooter, world);
    }

    public SCFlameSwingEntity(EntityType<? extends SCFlameSwingEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }
    
    @Override
    public int ticksLifespan() {
        return 20;
    }

    private static final Vector3d OFFSET = new Vector3d(0.0, -0.3, 0.75);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET;
    }
    
    @Override
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean blockDestroyed) {
        if (!level.isClientSide) {
            if (ForgeEventFactory.getMobGriefingEvent(level, getEntity())) {
                super.afterBlockHit(blockRayTraceResult, blockDestroyed);
            }
        }
    }
}
