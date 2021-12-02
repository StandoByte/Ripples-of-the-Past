package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HGEmeraldEntity extends ModdedProjectileEntity {
    
    public HGEmeraldEntity(LivingEntity shooter, World world) {
        super(ModEntityTypes.HG_EMERALD.get(), shooter, world);
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

    private static final Vector3d OFFSET = new Vector3d(0.0, -0.3, 0.75);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET;
    }
}
