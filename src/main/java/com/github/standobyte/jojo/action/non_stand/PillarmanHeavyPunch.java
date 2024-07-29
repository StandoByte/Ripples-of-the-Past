package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;

public class PillarmanHeavyPunch extends PillarmanAction {

    public PillarmanHeavyPunch(PillarmanAction.Builder builder) {
        super(builder.doNotCancelClick());
        stage = 1;
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        switch (target.getType()) {
        case BLOCK:
            return ActionConditionResult.POSITIVE;
        case ENTITY:
            return ActionConditionResult.POSITIVE;
        default:
            return ActionConditionResult.NEGATIVE;
            }

    }

 
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
    	VampirismClawLacerate.punchPerform(world, user, power, target, ModSounds.HAMON_SYO_PUNCH.get(), 1.5F, 0.8F);
    }

}
