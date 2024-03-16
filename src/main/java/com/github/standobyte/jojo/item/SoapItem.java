package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class SoapItem extends Item {

    public SoapItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public void inventoryTick(ItemStack pStack, World pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected) {
        if (pEntity instanceof PlayerEntity) {
            PlayerInventory inventory = ((PlayerEntity) pEntity).inventory;
            ItemStack emptyGloves = MCUtil.findInInventory(inventory, 
                    item -> !item.isEmpty() 
                    && item.getItem() == ModItems.BUBBLE_GLOVES.get()
                    && TommyGunItem.getAmmo(item) <= 0);
            if (!emptyGloves.isEmpty()) {
                BubbleGlovesItem.reload(emptyGloves, pEntity, pLevel, pStack);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, World pLevel, LivingEntity pEntityLiving) {
        super.finishUsingItem(pStack, pLevel, pEntityLiving);
        PlayerEntity playerentity = pEntityLiving instanceof PlayerEntity ? (PlayerEntity)pEntityLiving : null;
        
        if (!pLevel.isClientSide) {
            pEntityLiving.addEffect(new EffectInstance(
                     Effects.POISON, 100, 0, false, true, true));
            pEntityLiving.addEffect(new EffectInstance(
                    Effects.CONFUSION, 300, 1, false, true, true));
         }
        
        if (playerentity == null || !playerentity.abilities.instabuild) {
            if (pStack.isEmpty()) {
               return new ItemStack(Items.GLASS_BOTTLE);
            }
            if (playerentity != null) {
               playerentity.inventory.add(new ItemStack(Items.GLASS_BOTTLE));
            }
            pStack.shrink(1);
         }
           return pStack;
     }
    
    @Override
    public int getUseDuration(ItemStack pStack) {
        return 32;
     }

    @Override
     public UseAction getUseAnimation(ItemStack pStack) {
        return UseAction.DRINK;
     }
     
     @Override
     public SoundEvent getDrinkingSound() {
        return SoundEvents.HONEY_DRINK;
     }

     @Override
     public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
        return DrinkHelper.useDrink(pLevel, pPlayer, pHand);
     }
}
