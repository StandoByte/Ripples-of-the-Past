package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.CDItemProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CrazyDiamondItemProjectile extends StandEntityAction {

    public CrazyDiamondItemProjectile(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ItemStack itemToShoot = user.getOffhandItem();
        if (itemToShoot == null || itemToShoot.isEmpty() || !(itemToShoot.getItem() instanceof BlockItem)) {
            return conditionMessage("offhand_block");
        }
        return super.checkSpecificConditions(user, power, target);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            if (user != null) {
                CDItemProjectileEntity bullet = new CDItemProjectileEntity(standEntity, world);
                standEntity.shootProjectile(bullet, 3.0F, 0.25F);
            }
        }
    }

}
