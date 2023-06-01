package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class GlovesItem extends Item {

    public GlovesItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (openFingers()) {
            tooltip.add(new TranslationTextComponent("item.jojo.gloves.hint").withStyle(TextFormatting.GRAY));
        }
    }
    
    public boolean openFingers() {
        return true;
    }

}
