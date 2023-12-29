package com.github.standobyte.jojo.entity.stand.stands;

import java.util.Optional;
import java.util.UUID;

import com.github.standobyte.jojo.action.stand.HierophantGreenBarrier;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGBarrierEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.mod.HGBarriersNet;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HierophantGreenEntity extends StandEntity {
    private static final UUID SPEED_MODIFIER_RETRACTION_UUID = UUID.fromString("a421b1ab-85a8-4164-a9ba-dbda0bc560ce");
    private static final AttributeModifier SPEED_MODIFIER_RETRACTION = new AttributeModifier(SPEED_MODIFIER_RETRACTION_UUID, "Retraction speed boost", 2.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final DataParameter<Integer> PLACED_BARRIERS = EntityDataManager.defineId(HierophantGreenEntity.class, DataSerializers.INT);
    
    private HGBarrierEntity stringToUser;
    private HGBarrierEntity stringFromStand;
    private HGBarriersNet placedBarriers = new HGBarriersNet();
    
    public HierophantGreenEntity(StandEntityType<HierophantGreenEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void tick() {
        if (!level.isClientSide()) {
            placedBarriers.tick();
            setPlacedBarriersCount(placedBarriers.getSize());
        }
        super.tick();
    }
    
    public HGBarriersNet getBarriersNet() {
        return placedBarriers;
    }
    
    @Override
    public void setManualControl(boolean manualControl, boolean fixRemotePosition) {
        if (!level.isClientSide()) {
            ModifiableAttributeInstance speedAttributeInstance = getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttributeInstance.getModifier(SPEED_MODIFIER_RETRACTION_UUID) != null) {
                speedAttributeInstance.removeModifier(SPEED_MODIFIER_RETRACTION);
            }
            boolean summonBarrier = !isManuallyControlled() && manualControl;
            boolean removeBarrier = isManuallyControlled() && !manualControl && !fixRemotePosition;
            super.setManualControl(manualControl, fixRemotePosition);
            if (summonBarrier) {
                createUserString();
            }
            else if (removeBarrier) {
                if (stringToUser != null && stringToUser.isAlive() && stringToUser.is(stringFromStand)) {
                    speedAttributeInstance.addTransientModifier(SPEED_MODIFIER_RETRACTION);
                }
            }
        }
    }
    
    public void createUserString() {
        if (!level.isClientSide()) {
            if (stringToUser == null || !stringToUser.isAlive()) {
                stringToUser = new HGBarrierEntity(level, this);
                if (stringFromStand != null && stringFromStand.isAlive()) {
                    stringFromStand.remove();
                }
                stringFromStand = stringToUser;
                stringFromStand.withStandSkin(getStandSkin());
                level.addFreshEntity(stringFromStand);
            }
        }
    }
    
    public boolean canPlaceBarrier() {
        return getPlacedBarriersCount() < HierophantGreenBarrier.getMaxBarriersPlaceable(getUserPower());
    }
    
    public boolean hasBarrierAttached() {
        return getPlacedBarriersCount() > 0 || 
                stringFromStand != null && stringFromStand.isAlive() && stringFromStand != stringToUser;
    }
    
    public void attachBarrier(BlockPos blockPos) {
        if (!level.isClientSide()) {
            if (!canPlaceBarrier()) {
                return;
            }
            if (stringFromStand != null && stringFromStand.isAlive()) {
                if (blockPos.equals(stringFromStand.getOriginBlockPos())) {
                    return;
                }
                stringFromStand.attachToBlockPos(blockPos);
                placedBarriers.add(stringFromStand);
                setPlacedBarriersCount(placedBarriers.getSize());
            }
            stringFromStand = new HGBarrierEntity(level, this);
            stringFromStand.setOriginBlockPos(blockPos);
            stringFromStand.withStandSkin(getStandSkin());
            level.addFreshEntity(stringFromStand);
            playSound(ModSounds.HIEROPHANT_GREEN_BARRIER_PLACED.get(), 1.0F, 1.0F);
        }
    }
    
    public int getPlacedBarriersCount() {
        return entityData.get(PLACED_BARRIERS);
    }
    
    private void setPlacedBarriersCount(int value) {
        entityData.set(PLACED_BARRIERS, value);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PLACED_BARRIERS, 0);
    }
    
    @Override
    protected void setStandFlag(StandFlag flag, boolean value) {
        super.setStandFlag(flag, value);
        if (flag == StandFlag.BEING_RETRACTED && !value && isCloseToUser()) {
            if (stringToUser != null && stringToUser.isAlive() && stringToUser.is(stringFromStand)) {
                ModifiableAttributeInstance speedAttributeInstance = getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAttributeInstance.getModifier(SPEED_MODIFIER_RETRACTION_UUID) != null) {
                    speedAttributeInstance.removeModifier(SPEED_MODIFIER_RETRACTION);
                }
                stringToUser.remove();
            }
        }
    }
    
    @Override
    public void setStandSkin(Optional<ResourceLocation> skinLocation) {
        super.setStandSkin(skinLocation);
        getBarriersNet().setStandSkin(skinLocation);
        if (stringToUser != null) stringToUser.withStandSkin(skinLocation);
        if (stringFromStand != null) stringFromStand.withStandSkin(skinLocation);
    }
}
