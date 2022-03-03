package com.github.standobyte.jojo.action.actions;

import java.util.UUID;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.world.World;

public class StandEntityBlock extends StandEntityAction {
    private static final AttributeModifier BLOCK_KNOCKBACK_RESISTANCE = new AttributeModifier(
            UUID.fromString("32853956-c7b6-4e32-9edf-fecd6ed95e7c"), "Knockback resistance while blocking", 0.8, AttributeModifier.Operation.ADDITION);
    
    public StandEntityBlock() {
        this(new StandEntityAction.Builder());
    }

    protected StandEntityBlock(StandEntityAction.Builder builder) {
        super(builder.standAutoSummonMode(AutoSummonMode.ARMS).holdType().standPose(StandPose.BLOCK)
                .defaultStandOffsetFromUser().standUserSlowDownFactor(0.3F).standOffsetFromUser(0, 0.3));
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return stand.canStartBlocking() || stand.isStandBlocking() ? ActionConditionResult.POSITIVE : ActionConditionResult.NEGATIVE;
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase) {
        if (!world.isClientSide()) {
            ModifiableAttributeInstance knockbackRes = standEntity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (knockbackRes != null) {
                knockbackRes.addTransientModifier(BLOCK_KNOCKBACK_RESISTANCE);
            }
        }
    }

    @Override
    public void onClear(IStandPower standPower, StandEntity standEntity) {
        ModifiableAttributeInstance knockbackRes = standEntity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackRes != null) {
            knockbackRes.removeModifier(BLOCK_KNOCKBACK_RESISTANCE);
        }
    }
}
