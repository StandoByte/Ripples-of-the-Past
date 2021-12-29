package com.github.standobyte.jojo.entity.stand.stands;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.github.standobyte.jojo.client.ui.ActionsOverlayGui;
import com.github.standobyte.jojo.entity.damaging.projectile.HGEmeraldEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGBarrierEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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
    public void punch(boolean singlePunch) {}
    
    @Override
    public int rangedAttackDuration(boolean shift) {
        return (shift ? ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED : ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH).get().getCooldownValue();
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
    
    @Override
    public void rangedAttackTick(int ticks, boolean shift) {
        if (!level.isClientSide()) {
            float damageReduction = rangeEfficiencyFactor();
            int emeralds = shift ? 2 : 1;
            for (int i = 0; i < emeralds; i++) {
                HGEmeraldEntity emeraldEntity = new HGEmeraldEntity(this, level);
                emeraldEntity.setDamageFactor(damageReduction);
                emeraldEntity.shootFromRotation(this, shift ? 1.25F : 0.75F, shift ? 2.0F : 8.0F);
                level.addFreshEntity(emeraldEntity);
            }
            int barriers = getPlacedBarriersCount();
            if (barriers > 0) {
                RayTraceResult rayTrace = JojoModUtil.rayTrace(isManuallyControlled() ? this : getUser(), 
                        getMaxRange(), entity -> entity instanceof LivingEntity && canAttack((LivingEntity) entity));
                if (rayTrace.getType() != RayTraceResult.Type.MISS) {
                    shootEmeraldsFromBarriers(rayTrace.getLocation(), shift, 1);
                }
            }
        }
    }
    
    public void shootEmeraldsFromBarriers(Vector3d pos, boolean shift, int multiplier) {
        if (canBarriersShoot && getUserPower() != null) {
            float manaCost = (shift ? ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH.get() : ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED.get())
                    .getManaCost() / rangedAttackDuration(shift) * 0.5F;
            int barrierEmeralds = Math.max(getPlacedBarriersCount() * multiplier / 10, 1);
            float damageReduction = rangeEfficiencyFactor();
            for (int i = 0; i < barrierEmeralds && getUserPower().consumeMana(manaCost); i++) {
                placedBarriers.get(random.nextInt(placedBarriers.size())).shootEmeralds(pos, 1, shift, damageReduction);
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
    
    public void attachBarrier(BlockPos blockPos) {
        if (!level.isClientSide()) {
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
        }
    }
    
    public int getPlacedBarriersCount() {
        return entityData.get(PLACED_BARRIERS);
    }
    
    private void setPlacedBarriersCount(int value) {
        entityData.set(PLACED_BARRIERS, value);
    }
    
    @Override
    public void onSyncedDataUpdated(DataParameter<?> parameter) {
        super.onSyncedDataUpdated(parameter);
        if (level.isClientSide() && PLACED_BARRIERS.equals(parameter)) {
            // FIXME action learning progress
//            ActionsOverlayGui.getInstance().updateActionName(ModActions.HIEROPHANT_GREEN_BARRIER.get(), ActionType.ABILITY);
        }
    }
    
    @Override
    public void remove() {
        super.remove();
        if (level.isClientSide()) {
            // FIXME action learning progress
//            ActionsOverlayGui.getInstance().updateActionName(ModActions.HIEROPHANT_GREEN_BARRIER.get(), ActionType.ABILITY);
        }
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
