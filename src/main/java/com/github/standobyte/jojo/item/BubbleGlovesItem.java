package com.github.standobyte.jojo.item;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class BubbleGlovesItem extends GlovesItem {

    public BubbleGlovesItem(Properties properties) {
        super(properties);
    }
    
    public static final int MAX_AMMO = 500;
    
    public static boolean consumeAmmo(ItemStack gloves, int amount, LivingEntity user) {
        boolean consumed = TommyGunItem.consumeAmmo(gloves, amount);
        
        if (TommyGunItem.getAmmo(gloves) <= 0) {
            reload(gloves, user, user.level, null);
        }
        
        return consumed;
    }
    
    public static boolean reload(ItemStack glovesItem, Entity entity, World world, @Nullable ItemStack soapBottleItem) {
        int ammoToLoad = MAX_AMMO - TommyGunItem.getAmmo(glovesItem);
        if (ammoToLoad > 0) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                ammoToLoad = 500;
                IInventory inventory = player.inventory;
                ItemStack soapItem = null;
                soapItem = soapBottleItem != null ? soapBottleItem : MCUtil.findInInventory(inventory, item -> useSoap(item));
                if (!player.abilities.instabuild) {
                    if (!soapItem.isEmpty()) {
                        soapItem.shrink(1);
                        MCUtil.giveItemTo(player, new ItemStack(Items.GLASS_BOTTLE), true);
                    } else {
                        return false;
                    }
                }
                if (!world.isClientSide()) {
                    glovesItem.getTag().putInt("Ammo", ammoToLoad);
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return reload(stack, player, world, null) ? ActionResult.consume(stack) : ActionResult.fail(stack);
        } else {
            return ActionResult.fail(stack);
        }
    }
    
    public static boolean useSoap(ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof SoapItem;
    }
    
    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return TommyGunItem.getAmmo(stack) < MAX_AMMO;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1 - ((double) TommyGunItem.getAmmo(stack) / (double) MAX_AMMO);
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().putInt("Ammo", MAX_AMMO);
            items.add(stack);
        }
    }
    
}
