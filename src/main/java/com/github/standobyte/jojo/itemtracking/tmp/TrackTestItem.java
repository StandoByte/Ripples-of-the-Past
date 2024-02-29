package com.github.standobyte.jojo.itemtracking.tmp;

import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TrackTestItem extends Item {

    public TrackTestItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            if (player.isShiftKeyDown()) {
                TrackerItemStack.setTracked(item, (ServerPlayerEntity) player);
            }
            else {
                SaveFileUtilCapProvider.getSaveFileCap(((ServerWorld) world).getServer()).getItemsTracker().test(world, (ServerPlayerEntity) player);
            }
        }
        return ActionResult.pass(item);
    }
}
