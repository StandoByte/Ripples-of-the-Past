package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.GEBodyTissueItem;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class GoldExperienceHealingItem extends StandEntityAction {

    public GoldExperienceHealingItem(Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ItemStack offHandItem = user.getOffhandItem();
        if (offHandItem.isEmpty()) {
            return conditionMessage("ge_lifeform_material_only_item");
        }
        if (HamonUtil.isItemLivingMatter(offHandItem)) {
            return conditionMessage("ge_lifeform_material_item");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            ItemStack offHandItem = user.getOffhandItem();
            offHandItem.shrink(1);
            ItemStack tissueItem = new ItemStack(ModItems.GOLD_EXPERIENCE_BODY_TISSUE.get());
            GEBodyTissueItem.onCreated(userPower, tissueItem);
            MCUtil.giveItemTo(user, tissueItem, false);
        }
    }
    
}
