package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleCutterEntity;
import com.github.standobyte.jojo.item.SoapItem;
import com.github.standobyte.jojo.item.TommyGunItem;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonBubbleCutter extends HamonAction {

    public HamonBubbleCutter(HamonAction.Builder builder) {
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
                	TommyGunItem.consumeAmmo(getBubbleItem(user), 20);
                }
            boolean shift = isShiftVariation();
            int bubbles = shift ? 4 : 8;
            Vector3d shootingPos = null;
            for (int i = 0; i < bubbles; i++) {
                HamonBubbleCutterEntity bubbleCutterEntity = new HamonBubbleCutterEntity(user, world);
                float velocity = 1.35F + user.getRandom().nextFloat() * 0.3F;
                bubbleCutterEntity.setGliding(shift);
                bubbleCutterEntity.setHamonStatPoints(getEnergyCost(power, target) / 10F);
                bubbleCutterEntity.shootFromRotation(user, velocity, shift ? 2.0F : 8.0F);
                if (i == 0) shootingPos = bubbleCutterEntity.position();
                world.addFreshEntity(bubbleCutterEntity);
            }
            HamonUtil.emitHamonSparkParticles(world, null, shootingPos.x, shootingPos.y, shootingPos.z, 0.75F);
        }
    }
}
