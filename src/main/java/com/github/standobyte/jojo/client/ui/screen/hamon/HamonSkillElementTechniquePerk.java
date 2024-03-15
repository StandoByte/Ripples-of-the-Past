package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonSkillElementTechniquePerk extends HamonSkillGuiElement {
    private final List<IReorderingProcessor> perkDesc;
    @Nullable private ItemStack itemIcon;

    public HamonSkillElementTechniquePerk(AbstractHamonSkill skill, int x, int y, FontRenderer font) {
        super(skill, x, y, 16, 16);
        this.perkDesc = Stream.concat(
                font.split(new TranslationTextComponent("hamon.technique_perk", skill.getNameTranslated()), 200).stream(), 
                font.split(skill.getDescTranslated().withStyle(TextFormatting.ITALIC), 200).stream())
                .collect(Collectors.toList());
    }

    public HamonSkillElementTechniquePerk(int x, int y, FontRenderer font, ITextProperties... desc) {
        super(null, new StringTextComponent(""), x, y, 16, 16);
        this.perkDesc = Arrays.stream(desc)
                .flatMap(line -> font.split(line, 200).stream())
                .collect(Collectors.toList());
    }

    @Override
    void drawTooltip(HamonScreen hamonScreen, MatrixStack matrixStack, int mouseX, int mouseY) {
        hamonScreen.renderTooltip(matrixStack, perkDesc, mouseX, mouseY);
    }
    
    
    
    public HamonSkillElementTechniquePerk withItemIcon(ItemStack item) {
        this.itemIcon = item;
        return this;
    }
    
    @Override
    public void renderSkillIcon(MatrixStack matrixStack, int x, int y) {
        if (skill != null) {
            super.renderSkillIcon(matrixStack, x, y);
        }
        if (itemIcon != null) {
            Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(itemIcon, this.x + x, this.y + y);
        }
    }
    
    boolean isVisible() {
        if (skill == ModHamonSkills.DEEP_PASS.get() || skill == ModHamonSkills.CRIMSON_BUBBLE.get()) {
            return !JojoModConfig.getCommonConfigInstance(true).keepHamonOnDeath.get();
        }
        return true;
    }
}
