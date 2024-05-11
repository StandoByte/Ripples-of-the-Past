package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class LadybugBroochItem extends Item {
    private final DyeColor dye;
    
    public LadybugBroochItem(Properties pProperties, DyeColor dye) {
        super(pProperties);
        this.dye = dye;
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        boolean success = player.getCapability(LivingUtilCapProvider.CAPABILITY).map(entity -> {
            return entity.addLadybugBrooch(dye);
        }).orElse(false);
        if (success && !world.isClientSide() && !player.abilities.instabuild) {
            itemStack.shrink(1);
        }
        return success ? ActionResult.consume(itemStack) : ActionResult.fail(itemStack);
    }
    
    
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        ClientUtil.addItemReferenceQuote(tooltip, this, JojoMod.MOD_ID + ".ladybug_brooch");
        tooltip.add(ClientUtil.donoItemTooltip("Abreolitus"));
    }
}
