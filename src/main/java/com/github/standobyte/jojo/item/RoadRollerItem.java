package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.RoadRollerEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
                    .map(stand -> stand.getType() == ModStandTypes.THE_WORLD.get())
                    .orElse(false)) {
                JojoModUtil.sayVoiceLine(player, ModSounds.DIO_ROAD_ROLLER.get());
            }
            if (!player.abilities.instabuild) {
                handStack.shrink(1);
            }
        }
        return ActionResult.consume(handStack);
    }
    
    private static final ITextComponent DESC = new TranslationTextComponent("item.jojo.road_roller_2");
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(DESC);
    }

}
