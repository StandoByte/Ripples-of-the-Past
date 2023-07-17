package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class BreathControlMaskItem extends CustomModelArmorItem {

    public BreathControlMaskItem(Properties builder) {
        super(ModArmorMaterials.BREATH_CONTROL_MASK, EquipmentSlotType.HEAD, builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("item.jojo.breath_control_mask.hint").withStyle(TextFormatting.GRAY));
        tooltip.add(new TranslationTextComponent("item.jojo.breath_control_mask.hint2").withStyle(TextFormatting.GRAY));
        tooltip.add(new StringTextComponent(" "));
    }
}
