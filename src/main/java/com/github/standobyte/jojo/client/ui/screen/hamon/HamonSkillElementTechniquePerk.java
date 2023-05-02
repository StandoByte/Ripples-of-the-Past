package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonSkillElementTechniquePerk extends HamonSkillGuiElement {
    private final List<IReorderingProcessor> perkDesc;

    public HamonSkillElementTechniquePerk(AbstractHamonSkill skill, int x, int y, FontRenderer font) {
        super(skill, x, y, 16, 16);
        this.perkDesc = Stream.concat(
                font.split(new TranslationTextComponent("hamonSkill." + skill.getName() + ".name"), 200).stream(), 
                font.split(new TranslationTextComponent("hamonSkill." + skill.getName() + ".desc").withStyle(TextFormatting.ITALIC), 200).stream()).collect(Collectors.toList());
    }

    @Override
    void drawTooltip(HamonScreen hamonScreen, MatrixStack matrixStack, int mouseX, int mouseY) {
        hamonScreen.renderTooltip(matrixStack, perkDesc, mouseX, mouseY);
    }
    
    boolean isVisible() {
        if (skill == ModHamonSkills.DEEP_PASS.get() || skill == ModHamonSkills.CRIMSON_BUBBLE.get()) {
            return !JojoModConfig.getCommonConfigInstance(true).keepHamonOnDeath.get();
        }
        return true;
    }
}
