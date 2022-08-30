package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

// FIXME !!!!! onInput
public abstract class StandEntityActionModifier extends StandAction implements IStandPhasedAction {

    public StandEntityActionModifier(AbstractBuilder<?> builder) {
        super(builder);
    }
    
    @Override
    protected final void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (power.isActive()) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            stand.getCurrentTask().ifPresent(task -> task.addModifierAction(this, stand));
        }
    }   
    
    @Override
    public boolean validateInput() {
        return true;
    }
}
