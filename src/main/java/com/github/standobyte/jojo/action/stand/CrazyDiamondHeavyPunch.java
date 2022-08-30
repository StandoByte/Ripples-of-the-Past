package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

//FIXME !!!!! (normal heavy) TW-like armor piercing
public class CrazyDiamondHeavyPunch extends StandEntityHeavyAttack {

    public CrazyDiamondHeavyPunch(Builder builder) {
        super(builder);
    }
    
//    @Override
//    protected StandEntityActionModifier getRecoveryFollowup(IStandPower standPower, StandEntity standEntity) {
//        return ModActions.CRAZY_DIAMOND_LEAVE_OBJECT.get();
//    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {
        super.onTaskSet(world, standEntity, standPower, phase, task, ticks);
        if (!world.isClientSide()) {
            LivingEntity user = standPower.getUser();
            ItemStack item = user.getOffhandItem();
            if (user != null && !item.isEmpty() && CrazyDiamondLeaveObject.canUseItem(item)) {
                ItemStack itemForStand = item.copy();
                itemForStand.setCount(1);
                // FIXME !!!!! (normal heavy) swap in case it already holds an item
                standEntity.setItemInHand(Hand.MAIN_HAND, itemForStand);
                item.shrink(1);
            }
        }
    }

}
