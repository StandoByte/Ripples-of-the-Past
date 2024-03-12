package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleBarrierEntity;
import com.github.standobyte.jojo.item.SoapItem;
import com.github.standobyte.jojo.item.TommyGunItem;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class HamonBubbleBarrier extends HamonAction {

    public HamonBubbleBarrier(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (getBubbleItem(user).isEmpty() || TommyGunItem.getAmmo(getBubbleItem(user)) <= 0 
        		&& getBubbleItem(user).getItem() instanceof SoapItem != true) {
            return conditionMessage("soap");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            world.addFreshEntity(new HamonBubbleBarrierEntity(world, user, power));
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
    	if (!world.isClientSide()) {
    	PlayerEntity player = null;
		player = (PlayerEntity) user;
    		if((getBubbleItem(user).getItem() instanceof SoapItem) == true) {
    			if (!player.abilities.instabuild) {
    				getBubbleItem(user).shrink(1);
                	MCUtil.giveItemTo(user, new ItemStack(Items.GLASS_BOTTLE), true);
    			}
            } else {
            	TommyGunItem.consumeAmmo(getBubbleItem(user), 50);
            }
    	}
    }
}
