package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleEntity;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.BubbleGlovesItem;
import com.github.standobyte.jojo.item.TommyGunItem;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.HandSide;
import net.minecraft.world.World;

public class HamonBubbleLauncher extends HamonAction {

    public HamonBubbleLauncher(HamonAction.Builder builder) {
        super(builder.holdType());
    }
    
    
    public static ActionConditionResult checkSoap(LivingEntity entity) {
        ItemStack item = getSoapItem(entity);
        if (item.isEmpty()) {
            return conditionMessage("soap");
        }
        if (item.getItem() == ModItems.BUBBLE_GLOVES.get() && TommyGunItem.getAmmo(item) <= 0) {
            return conditionMessage("gloves_no_soap");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    public static TookSoapFrom consumeSoap(LivingEntity entity, int glovesAmmo) {
        ItemStack soapItem = getSoapItem(entity);
        if (soapItem.isEmpty()) return TookSoapFrom.NONE;
        
        if (soapItem.getItem() == ModItems.SOAP.get()) {
            // also consuming it in creative because 36 bubbles per tick lag the game
            soapItem.shrink(1);
            MCUtil.giveItemTo(entity, new ItemStack(Items.GLASS_BOTTLE), true);
            return TookSoapFrom.BOTTLE;
        } else {
            BubbleGlovesItem.consumeAmmo(soapItem, glovesAmmo, entity);
            return TookSoapFrom.GLOVES;
        }
    }
    
    public static enum TookSoapFrom {
        BOTTLE,
        GLOVES,
        NONE
    }
    
    public static ItemStack getSoapItem(LivingEntity entity) {
        ItemStack soapItem = entity.getMainHandItem();
        if (!soapItem.isEmpty() && 
                (soapItem.getItem() == ModItems.BUBBLE_GLOVES.get() || soapItem.getItem() == ModItems.SOAP.get())) {
            return soapItem;
        }
        
        soapItem = entity.getOffhandItem();
        if (!soapItem.isEmpty() && 
                (soapItem.getItem() == ModItems.BUBBLE_GLOVES.get() || soapItem.getItem() == ModItems.SOAP.get())) {
            return soapItem;
        }
        
        return ItemStack.EMPTY;
    }
    
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        return checkSoap(user);
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled && !world.isClientSide()) {
            int bubblesCount = 4;
            TookSoapFrom soapSource = consumeSoap(user, 1);
            if (soapSource == TookSoapFrom.BOTTLE) {
                bubblesCount = 36;
            }
            for (int i = 0; i < bubblesCount; i++) {
                HamonBubbleEntity bubbleEntity = new HamonBubbleEntity(user, world);
                float velocity = 0.1F + user.getRandom().nextFloat() * 0.5F;
                bubbleEntity.shootFromRotation(user, velocity, 16.0F);
                world.addFreshEntity(bubbleEntity);
            }
        }        
    }
    
    @Override
    public boolean renderHamonAuraOnItem(ItemStack item, HandSide handSide) {
        return item.getItem() == ModItems.SOAP.get();
    }
            
}
