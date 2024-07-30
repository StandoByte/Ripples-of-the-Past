package com.github.standobyte.jojo.action.non_stand;

import java.util.Optional;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.GameplayEventHandler;
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

public class ZombieClawLacerate extends ZombieAction {

    public ZombieClawLacerate(ZombieAction.Builder builder) {
        super(builder.doNotCancelClick());
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if(power.getTypeSpecificData(ModPowers.ZOMBIE.get()).get().isDisguiseEnabled()) {
            switch (target.getType()) {
            case BLOCK:
                return ActionConditionResult.POSITIVE;
            case ENTITY:
                return ActionConditionResult.POSITIVE;
            default:
                return ActionConditionResult.NEGATIVE;
            }
        }
        return conditionMessage("disguise");
    }
 
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
    	VampirismClawLacerate.punchPerform(world, user, power, target, ModSounds.THE_WORLD_PUNCH_HEAVY_ENTITY.get(), 1.0F, 1.0F);
    }
    
}
