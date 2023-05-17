package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.World;

public class SilverChariotTakeOffArmor extends StandEntityAction {

    public SilverChariotTakeOffArmor(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        if (stand instanceof SilverChariotEntity) {
            SilverChariotEntity chariot = (SilverChariotEntity) stand;
            if (chariot.isArmsOnlyMode()) {
                return ActionConditionResult.NEGATIVE;
            }
            if (!chariot.hasArmor()) {
                return conditionMessage("chariot_armor");
            }
            return ActionConditionResult.POSITIVE;
        }
        return ActionConditionResult.NEGATIVE;
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            if (standEntity instanceof SilverChariotEntity) {
                SilverChariotEntity chariot = (SilverChariotEntity) standEntity;
                chariot.setArmor(!chariot.hasArmor());
                chariot.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.addAfterimages(10, -1);
                });
                chariot.playSound(ModSounds.SILVER_CHARIOT_ARMOR_OFF.get(), 1.0F, 1.0F);
            }
        }
    }
}
