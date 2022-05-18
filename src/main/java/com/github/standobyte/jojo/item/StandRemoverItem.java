package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class StandRemoverItem extends Item {

    public StandRemoverItem(Properties properties) {
        super(properties);

        DispenserBlock.registerBehavior(this, new DefaultDispenseItemBehavior() {
            protected ItemStack execute(IBlockSource blockSource, ItemStack stack) {
                if (JojoModUtil.dispenseOnNearbyEntity(blockSource, stack, entity -> {
                    return IStandPower.getStandPowerOptional(entity).map(power -> {
                        return power.hasPower() && power.clear();
                    }).orElse(false);
                }, false)) {
                    return stack;
                }
                return super.execute(blockSource, stack);
            }
        });
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        IStandPower power = IStandPower.getPlayerStandPower(player);
        if (!world.isClientSide()) {
            if (power.hasPower() && power.clear()) {
                return ActionResult.success(stack);
            }
            return ActionResult.fail(stack);
        }
        else if (power.hasPower()) {
            return ActionResult.success(stack);
        }
        return ActionResult.fail(stack);
    }
}
