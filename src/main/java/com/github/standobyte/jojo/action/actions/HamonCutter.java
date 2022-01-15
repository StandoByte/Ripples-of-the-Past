package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonCutterEntity;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.world.World;

public class HamonCutter extends HamonAction {

    public HamonCutter(HamonAction.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, LivingEntity performer, INonStandPower power, ActionTarget target) {
        if (!(user.getMainHandItem().getItem() instanceof PotionItem || user.getOffhandItem().getItem() instanceof PotionItem)) {
            return conditionMessage("potion");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            ItemStack potionItem = user.getMainHandItem();
            if (!(potionItem.getItem() instanceof PotionItem)) {
                potionItem = user.getOffhandItem();
                if (!(potionItem.getItem() instanceof PotionItem)) {
                    return;
                }
            }

            PlayerEntity player = null;
            if (user instanceof PlayerEntity) {
                player = (PlayerEntity) user;
            }
            if (player == null || !player.abilities.instabuild) {
                potionItem.shrink(1);
                if (player != null) { 
                    player.inventory.add(new ItemStack(Items.GLASS_BOTTLE));
                }
            }

            for (int i = 0; i < 8; i++) {
                HamonCutterEntity hamonCutterEntity = new HamonCutterEntity(user, world, potionItem);
                hamonCutterEntity.shootFromRotation(user, 1.35F + user.getRandom().nextFloat() * 0.3F, 10.0F);
                world.addFreshEntity(hamonCutterEntity);
            }
        }
    }
}

