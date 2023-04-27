package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class SatiporojaScarfBindingEntity extends OwnerBoundProjectileEntity {
    
    public SatiporojaScarfBindingEntity(World world, LivingEntity entity) {
        super(ModEntityTypes.SATIPOROJA_SCARF_BINDING.get(), entity, world);
    }

    public SatiporojaScarfBindingEntity(EntityType<? extends SatiporojaScarfBindingEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) {
            Entity ensnaredEntity = getEntityAttachedTo();
            if (ensnaredEntity == null || !ensnaredEntity.isAlive()) {
                Entity owner = getOwner();
                if (owner instanceof PlayerEntity) {
                    ((PlayerEntity) owner).getCooldowns().addCooldown(ModItems.SATIPOROJA_SCARF.get(), 0);
                }
                remove();
            }
        }
    }

    @Override
    protected Vector3d getNextOriginOffset() {
        return getOriginPoint();
    }

    @Override
    public int ticksLifespan() {
        return 100;
    }

    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return DamageUtil.dealHamonDamage(target, 0.003F, this, owner);
    }

    @Override
    public float getBaseDamage() {
        return 0;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }
    
    @Override
    protected float movementSpeed() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return false;
    }
}
