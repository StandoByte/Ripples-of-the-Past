package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.item.GEBodyTissueItem;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
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
        if (!GoldExperienceCreateLifeform.canGiveLifeTo(offHandItem)) {
            return conditionMessage("ge_lifeform_material_item");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            
            ItemStack offHandItem = user.getOffhandItem();
            if (offHandItem.getItem() instanceof BucketItem) {
                BucketItem bucketType = (BucketItem) offHandItem.getItem();
                bucketType.checkExtraContent(world, offHandItem, getControlledEntity(user, userPower).blockPosition());
            }
            if (!(user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild)) {
                offHandItem.shrink(1);
            }
            
            ItemStack tissueItem = new ItemStack(ModItems.GOLD_EXPERIENCE_BODY_TISSUE.get());
            GEBodyTissueItem.onCreated(userPower, tissueItem);
            MCUtil.giveItemTo(user, tissueItem, false);
            
            MCUtil.playSound(user.level, null, user, ModSounds.GOLD_EXPERIENCE_LIFE_START.get(), 
                    SoundCategory.AMBIENT, 1.0F, 0.95F + user.getRandom().nextFloat() * 0.1F, StandUtil::playerCanHearStands);
        }
    }
    
}
