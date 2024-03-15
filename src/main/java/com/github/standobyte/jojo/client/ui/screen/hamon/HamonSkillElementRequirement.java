package com.github.standobyte.jojo.client.ui.screen.hamon;

import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.text.TextFormatting;

public class HamonSkillElementRequirement extends HamonSkillGuiElement {

    public HamonSkillElementRequirement(AbstractHamonSkill skill, int x, int y) {
        super(skill, x, y, 16, 16);
    }

    @Override
    void drawTooltip(HamonScreen hamonScreen, MatrixStack matrixStack, int mouseX, int mouseY) {
        hamonScreen.renderTooltip(matrixStack, 
                name.withStyle(hamonScreen.hamon.isSkillLearned(getHamonSkill()) ? TextFormatting.GREEN : TextFormatting.RED), 
                mouseX, mouseY);
    }
}
