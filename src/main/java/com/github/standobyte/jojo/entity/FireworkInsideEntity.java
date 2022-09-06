package com.github.standobyte.jojo.entity;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.reflection.CommonReflection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class FireworkInsideEntity extends FireworkRocketEntity {

    public FireworkInsideEntity(EntityType<? extends FireworkRocketEntity> type, World world) {
        super(type, world);
    }

    public FireworkInsideEntity(World world, double x, double y, double z, ItemStack item) {
        super(ModEntityTypes.FIREWORK_INSIDE.get(), world);
        this.setPos(x, y, z);
        
        int flight = 1;
        if (!item.isEmpty() && item.hasTag()) {
           entityData.set(CommonReflection.getFireworkItemParameter(), item.copy());
           flight += item.getOrCreateTagElement("Fireworks").getByte("Flight");
        }

        setDeltaMovement(random.nextGaussian() * 0.001D, 0.05D, random.nextGaussian() * 0.001D);
        int lifetime = flight * 10;
        CommonReflection.setLifetime(this, lifetime);
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
