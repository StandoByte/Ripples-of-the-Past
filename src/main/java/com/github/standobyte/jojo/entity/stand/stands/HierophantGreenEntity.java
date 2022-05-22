package com.github.standobyte.jojo.entity.stand.stands;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.github.standobyte.jojo.action.actions.HierophantGreenBarrier;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGBarrierEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HierophantGreenEntity extends StandEntity {
    private static final UUID SPEED_MODIFIER_RETRACTION_UUID = UUID.fromString("a421b1ab-85a8-4164-a9ba-dbda0bc560ce");
    private static final AttributeModifier SPEED_MODIFIER_RETRACTION = new AttributeModifier(SPEED_MODIFIER_RETRACTION_UUID, "Retraction speed boost", 2.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final DataParameter<Integer> PLACED_BARRIERS = EntityDataManager.defineId(HierophantGreenEntity.class, DataSerializers.INT);
    
    private HGBarrierEntity stringToUser;
    private HGBarrierEntity stringFromStand;
    private List<HGBarrierEntity> placedBarriers = new LinkedList<HGBarrierEntity>();
    private boolean canBarriersShoot;
    
    public HierophantGreenEntity(StandEntityType<HierophantGreenEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) {
            Iterator<HGBarrierEntity> iter = placedBarriers.iterator();
            while (iter.hasNext()) {
                HGBarrierEntity barrier = iter.next();
                if (!barrier.isAlive() || barrier.wasRipped()) {
                    iter.remove();
                }
            }
            setPlacedBarriersCount(placedBarriers.size());
            canBarriersShoot = true;
        }
    }
    
    public void shootEmeraldsFromBarriers(Vector3d pos, int multiplier) {
        if (canBarriersShoot && getUserPower() != null) {
            // FIXME (!) emeralds from barriers change
            float staminaCost = (ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED.get()).getStaminaCostTicking(getUserPower());
            int barrierEmeralds = Math.max(getPlacedBarriersCount() * multiplier / 10, 1);
            for (int i = 0; i < barrierEmeralds && getUserPower().consumeStamina(staminaCost); i++) {
                placedBarriers.get(random.nextInt(placedBarriers.size())).shootEmeralds(pos, 1);
            }
            canBarriersShoot = false;
        }
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
                setPlacedBarriersCount(placedBarriers.size());
            }
            stringFromStand = new HGBarrierEntity(level, this);
            stringFromStand.setOriginBlockPos(blockPos);
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
        if (flag == StandFlag.BEING_RETRACTED && !value && isCloseToEntity(getUser())) {
            if (stringToUser != null && stringToUser.isAlive() && stringToUser.is(stringFromStand)) {
                ModifiableAttributeInstance speedAttributeInstance = getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAttributeInstance.getModifier(SPEED_MODIFIER_RETRACTION_UUID) != null) {
                    speedAttributeInstance.removeModifier(SPEED_MODIFIER_RETRACTION);
                }
                stringToUser.remove();
            }
        }
    }
}
