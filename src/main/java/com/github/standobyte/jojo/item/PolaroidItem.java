package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.client.polaroid.PolaroidHelper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class PolaroidItem extends Item {

    public PolaroidItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (world.isClientSide()) {
            PolaroidHelper.takePicture(null, null, false);
        }
        
        return ActionResult.consume(stack);
    }
}
