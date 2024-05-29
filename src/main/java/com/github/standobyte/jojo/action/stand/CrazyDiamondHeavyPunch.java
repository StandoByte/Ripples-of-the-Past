package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class CrazyDiamondHeavyPunch extends StandEntityHeavyAttack {

    public CrazyDiamondHeavyPunch(Builder builder) {
        super(builder);
    }
    
    @Override
    protected StandEntityActionModifier getRecoveryFollowup(IStandPower standPower, StandEntity standEntity) {
        return ModStandsInit.CRAZY_DIAMOND_LEAVE_OBJECT.get();
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {
        super.onTaskSet(world, standEntity, standPower, phase, task, ticks);
        if (!world.isClientSide()) {
            LivingEntity user = standPower.getUser();
            ItemStack item = user.getOffhandItem();
            if (user != null && !item.isEmpty() && CrazyDiamondLeaveObject.canUseItem(item)) {
                ItemStack itemForStand = item.split(1);
                standEntity.takeItem(standEntity.handItemSlot(Hand.MAIN_HAND), itemForStand, true, user);
            }
        }
    }

    @Override
    protected void onTaskStopped(World world, StandEntity standEntity, IStandPower standPower, StandEntityTask task, @Nullable StandEntityAction newAction) {
        if (!world.isClientSide()) {
            standEntity.dropItemTo(standEntity.handItemSlot(Hand.MAIN_HAND), standPower.getUser());
        }
    }

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return super.punchEntity(stand, target, dmgSource)
                .armorPiercing((float) stand.getAttackDamage() * 0.01F);
    }
}
