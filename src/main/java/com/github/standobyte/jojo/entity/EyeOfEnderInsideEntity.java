package com.github.standobyte.jojo.entity;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.EyeOfEnderEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EyeOfEnderInsideEntity extends EyeOfEnderEntity {

    public EyeOfEnderInsideEntity(EntityType<? extends EyeOfEnderEntity> type, World world) {
        super(type, world);
    }

    public EyeOfEnderInsideEntity(World world, double x, double y, double z) {
        this(ModEntityTypes.EYE_OF_ENDER_INSIDE.get(), world);
        this.setPos(x, y, z);
    }

    @Override
    public void positionRider(Entity rider) {
        if (this.hasPassenger(rider)) {
            rider.setPos(getX(), getY() + (getBbHeight() - rider.getBbHeight()) / 2, getZ());
        }
    }
    
    @Override
    public double getPassengersRidingOffset() {
        return getBbHeight() / 2;
    }

    // FIXME !!!!!!!!!!!! disable dismounting
    @Override
    public void tick() {
        if (!level.isClientSide && tickCount >= 75) {
            if (isVehicle()) {
                getPassengers().forEach(entity -> DamageUtil.hurtThroughInvulTicks(entity, DamageUtil.EYE_OF_ENDER_SHARDS, 2));
            }
            playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
            level.levelEvent(2003, blockPosition(), 0);
            remove();
        }
        else {
            super.tick();
        }
    }
    
    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return !isVehicle() && super.shouldRender(cameraX, cameraY, cameraZ);
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
