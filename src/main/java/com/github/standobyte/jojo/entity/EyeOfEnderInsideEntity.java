package com.github.standobyte.jojo.entity;

import java.util.UUID;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.EyeOfEnderEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

public class EyeOfEnderInsideEntity extends EyeOfEnderEntity {
    private Entity entityInsideOf;
    private UUID entityInsideOfUUID;

    public EyeOfEnderInsideEntity(EntityType<? extends EyeOfEnderEntity> type, World world) {
        super(type, world);
    }

    public EyeOfEnderInsideEntity(World world, LivingEntity entity) {
        this(ModEntityTypes.EYE_OF_ENDER_INSIDE.get(), world);
        this.setPos(entity.getX(), entity.getY(0.5), entity.getZ());
        entity.startRiding(this, true);
        this.entityInsideOf = entity;
        this.entityInsideOfUUID = entity.getUUID();
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
        if (!level.isClientSide) {
            if (tickCount < 75) {
                if (entityInsideOf == null) {
                    entityInsideOf = ((ServerWorld) level).getEntity(entityInsideOfUUID);
                }
                // FIXME dismount prevention
                if (entityInsideOf != null && !this.is(entityInsideOf.getVehicle())) {
                    entityInsideOf.startRiding(this, true);
                }
            }
            else {
                if (isVehicle()) {
                    getPassengers().forEach(entity -> DamageUtil.hurtThroughInvulTicks(entity, DamageUtil.EYE_OF_ENDER_SHARDS, 2));
                }
                playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
                level.levelEvent(2003, blockPosition(), 0);
                remove();
                return;
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
        return EntityType.EYE_OF_ENDER.getDescription();
    }
    
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
