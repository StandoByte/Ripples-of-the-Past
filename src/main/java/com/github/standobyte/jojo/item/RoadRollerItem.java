package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.entity.RoadRollerEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class RoadRollerItem extends Item {

    public RoadRollerItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack handStack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            RoadRollerEntity roadRoller = new RoadRollerEntity(world);
            roadRoller.copyPosition(player);
            world.addFreshEntity(roadRoller);
            player.startRiding(roadRoller);
            roadRoller.setOwner(player);
            if (IStandPower.getStandPowerOptional(player)
                    .map(stand -> stand.getType() == ModStands.THE_WORLD.getStandType())
                    .orElse(false)) {
                JojoModUtil.sayVoiceLine(player, ModSounds.DIO_ROAD_ROLLER.get());
            }
            if (!player.abilities.instabuild) {
                handStack.shrink(1);
            }
        }
        return ActionResult.consume(handStack);
    }

}
