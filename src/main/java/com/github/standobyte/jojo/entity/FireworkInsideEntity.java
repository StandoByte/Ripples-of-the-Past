package com.github.standobyte.jojo.entity;

import java.util.UUID;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

public class FireworkInsideEntity extends FireworkRocketEntity {
    private Entity entityInsideOf;
    private UUID entityInsideOfUUID;

    public FireworkInsideEntity(EntityType<? extends FireworkRocketEntity> type, World world) {
        super(type, world);
    }

    public FireworkInsideEntity(World world, ItemStack item, LivingEntity entity) {
        super(ModEntityTypes.FIREWORK_INSIDE.get(), world);
        this.setPos(entity.getX(), entity.getY(0.5), entity.getZ());
        entity.startRiding(this, true);
        this.entityInsideOf = entity;
        this.entityInsideOfUUID = entity.getUUID();
        
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

    @Override
    public void tick() {
        if (!level.isClientSide()) {
            if (entityInsideOf == null) {
                entityInsideOf = ((ServerWorld) level).getEntity(entityInsideOfUUID);
            }
            // FIXME dismount prevention
            if (entityInsideOf != null && !this.is(entityInsideOf.getVehicle())) {
                entityInsideOf.startRiding(this, true);
            }
        }
        super.tick();
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
    protected ITextComponent getTypeName() {
        return EntityType.FIREWORK_ROCKET.getDescription();
    }
    
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
