package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleEntity;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.BubbleGlovesItem;
import com.github.standobyte.jojo.item.SoapItem;
import com.github.standobyte.jojo.item.TommyGunItem;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class HamonBubbleLauncher extends HamonAction {

    public HamonBubbleLauncher(HamonAction.Builder builder) {
        super(builder.holdType());
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
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
    	int x = 4;
    	PlayerEntity player = null;
		player = (PlayerEntity) user;
    	if (requirementsFulfilled && !world.isClientSide()) {
    		if((getBubbleItem(user).getItem() instanceof SoapItem) == true) {
    			x = 36;
    			if (!player.abilities.instabuild) {
    				getBubbleItem(user).shrink(1);
                	MCUtil.giveItemTo(user, new ItemStack(Items.GLASS_BOTTLE), true);
    			}
            } else {
            	TommyGunItem.consumeAmmo(getBubbleItem(user), 1);
            }
            for (int i = 0; i < x; i++) {
                HamonBubbleEntity bubbleEntity = new HamonBubbleEntity(user, world);
                float velocity = 0.1F + user.getRandom().nextFloat() * 0.5F;
                bubbleEntity.shootFromRotation(user, velocity, 16.0F);
                world.addFreshEntity(bubbleEntity);
            	}
    	}		
    }
            
}
