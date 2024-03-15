package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.util.mc.MCUtil;

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
    
    private static final int MAX_AMMO = 500;
    
    private boolean reload(ItemStack stack, LivingEntity entity, World world) {
        int ammoToLoad = MAX_AMMO - TommyGunItem.getAmmo(stack);
        if (ammoToLoad > 0) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                ammoToLoad = 500;
                IInventory inventory = player.inventory;
                ItemStack soapItem = null;
                soapItem = MCUtil.findInInventory(inventory, item -> useSoap(item));
                    if (!player.abilities.instabuild) {
                        if (!soapItem.isEmpty()) {
                            soapItem.shrink(1);
                            MCUtil.giveItemTo(player, new ItemStack(Items.GLASS_BOTTLE), true);
                        } else {
                            return false;
                        }
                    }
                if (!world.isClientSide()) {
                  player.getCooldowns().addCooldown(this, 200);
                    stack.getTag().putInt("Ammo", ammoToLoad);
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
            return reload(stack, player, world) ? ActionResult.consume(stack) : ActionResult.fail(stack);
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
