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

public class MrPresidentKeyItem extends Item {
    public final boolean masterKey;

    public MrPresidentKeyItem(Properties pProperties, boolean masterKey) {
        super(pProperties);
        this.masterKey = masterKey;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (masterKey) {
            tooltip.add(new TranslationTextComponent(getOrCreateDescriptionId() + ".hint").withStyle(TextFormatting.GRAY));
            tooltip.add(new TranslationTextComponent("item.jojo.creative_only_tooltip").withStyle(TextFormatting.DARK_GRAY));
        }
    }

}
