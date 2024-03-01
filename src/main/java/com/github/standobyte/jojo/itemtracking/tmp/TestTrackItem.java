package com.github.standobyte.jojo.itemtracking.tmp;

import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TestTrackItem extends Item {

    public TestTrackItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            SaveFileUtilCapProvider.getSaveFileCap(((ServerWorld) world).getServer()).getItemsTracker().tmpTest(world, (ServerPlayerEntity) player);
        }
        return ActionResult.pass(item);
    }
}
