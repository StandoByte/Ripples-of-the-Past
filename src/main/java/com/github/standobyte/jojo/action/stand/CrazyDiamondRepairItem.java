package com.github.standobyte.jojo.action.stand;

import java.util.List;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CrazyDiamondRepairItem extends StandEntityAction {

    public CrazyDiamondRepairItem(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ItemStack itemToRepair = itemToRepair(user);
        if (itemToRepair == null || itemToRepair.isEmpty()) {
            return conditionMessage("item_offhand");
        }
        if (!canBeRepaired(itemToRepair)) {
            return conditionMessage("no_repair");
        }
        return super.checkSpecificConditions(user, power, target);
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        LivingEntity user = userPower.getUser();
        if (user != null) {
            if (!world.isClientSide()) {
                ItemStack itemToRepair = itemToRepair(user);
                if (!itemToRepair.isEmpty()) {
                    repair(user, itemToRepair, task.getTick());
                }
            }
            else {
                CustomParticlesHelper.createCDRestorationParticle(user, Hand.OFF_HAND);
            }
        }
    }

    @Override
    public void appendWarnings(List<ITextComponent> list, IStandPower power, PlayerEntity clientPlayerUser) {
        ItemStack itemToRepair = itemToRepair(clientPlayerUser);
        if (!itemToRepair.isEmpty() && itemToRepair.isEnchanted()) {
            list.add(new TranslationTextComponent("jojo.crazy_diamond_fix.warning", itemToRepair.getDisplayName()));
        }
    }
    
    private ItemStack itemToRepair(LivingEntity entity) {
        return entity.getOffhandItem();
    }
    
    private boolean canBeRepaired(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && (itemStack.isDamaged() || itemStack.isEnchanted()
                || itemStack.getItem() == Items.CHIPPED_ANVIL || itemStack.getItem() == Items.DAMAGED_ANVIL);
    }
    
    public int repair(LivingEntity user, ItemStack itemStack, int ticks) {
        itemStack.removeTagKey("Enchantments");
        itemStack.removeTagKey("StoredEnchantments");
        int damage = Math.min(itemStack.getDamageValue(), 50);
        itemStack.setDamageValue(itemStack.getDamageValue() - 50);
        itemStack.setRepairCost(0);
        if (ticks % 40 == 39) {
            ItemStack newStack = null;
            if (itemStack.getItem() == Items.CHIPPED_ANVIL) {
                newStack = new ItemStack(Items.ANVIL);
            }
            else if (itemStack.getItem() == Items.DAMAGED_ANVIL) {
                newStack = new ItemStack(Items.CHIPPED_ANVIL);
            }
            if (newStack != null && user instanceof PlayerEntity) {
                user.setItemInHand(Hand.OFF_HAND, DrinkHelper.createFilledResult(itemStack, (PlayerEntity) user, newStack, false));
                damage += 2000;
            }
        }
        return damage;
    }
}
