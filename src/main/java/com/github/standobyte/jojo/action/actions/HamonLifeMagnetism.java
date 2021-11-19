package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;

import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class HamonLifeMagnetism extends HamonAction {

    public HamonLifeMagnetism(Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IPower<?> power, ActionTarget target) {
        ItemStack heldItemStack = performer.getMainHandItem();
        if (!heldItemStack.isEmpty()) {
            return conditionMessage("hand");
        }
        if (target.getType() != TargetType.BLOCK
                || !(user.level.getBlockState(target.getBlockPos()).getBlock() instanceof LeavesBlock)) {
            return conditionMessage("leaves");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void perform(World world, LivingEntity user, IPower<?> power, ActionTarget target) {
        if (!world.isClientSide()) {
            world.destroyBlock(target.getBlockPos(), false);
            LeavesGliderEntity glider = new LeavesGliderEntity(world);
            glider.moveTo(target.getBlockPos(), user.xRot, user.yRot);
            world.addFreshEntity(glider);
            HamonData hamon = ((INonStandPower) power).getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
            hamon.hamonPointsFromAction(HamonStat.CONTROL, getManaCost());
        }
    }

}
