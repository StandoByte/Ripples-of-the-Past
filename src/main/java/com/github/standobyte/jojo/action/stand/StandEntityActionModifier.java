package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

public abstract class StandEntityActionModifier extends StandAction implements IStandPhasedAction {

    public StandEntityActionModifier(AbstractBuilder<?> builder) {
        super(builder);
    }
    
    @Override
    public void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (!world.isClientSide() && power.isActive()) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            stand.getCurrentTask().ifPresent(task -> task.addModifierAction(this, stand));
        }
    }
    
    @Override
    public boolean greenSelection(IStandPower power, ActionConditionResult conditionCheck) {
        return conditionCheck.isPositive();
    }
    
    @Override
    public boolean isLegalInHud(IStandPower power) {
        return false;
    }
    
    
    
    protected class TriggeredFlag {}
}
