package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonSkillElementLearnable extends HamonSkillGuiElement {
    private final boolean isFinal;
    private State state;
    
    public HamonSkillElementLearnable(AbstractHamonSkill skill, 
            HamonData hamon, LivingEntity user, Collection<? extends AbstractHamonSkill> teacherSkills, 
            boolean isFinal, int x, int y) {
        super(skill, x, y, 26, 26);
        this.isFinal = isFinal;
        updateState(hamon, user, teacherSkills);
    }
    
    void updateState(HamonData hamon, LivingEntity user, Collection<? extends AbstractHamonSkill> teacherSkills) {
        boolean canBeLearned = hamon.canLearnSkill(user, skill, teacherSkills).isPositive();
        boolean isLearned = hamon.isSkillLearned(skill);
        updateState(canBeLearned, isLearned);
    }
    
    State getState() {
        return state;
    }
    
    private void updateState(boolean canBeLearned, boolean isLearned) {
        this.state = State.getState(isFinal, canBeLearned, isLearned);
    }
    
    void blitBgSquare(MatrixStack matrixStack, int x, int y) {
        AbstractGui.blit(matrixStack, getX() + x, getY() + y, 
                state.textureX, state.textureY, 26, 26, 256, 256);
    }
    
    void blitBgSquareSelection(MatrixStack matrixStack, int x, int y) {
        int texY = state.isFinal ? 78 : 0;
        AbstractGui.blit(matrixStack, getX() + x, getY() + y, 
                26, texY, 26, 26, 256, 256);
    }
    
    void blitBgSquareRequirement(MatrixStack matrixStack, int x, int y) {
        int texY = state.isFinal ? 104 : 26;
        AbstractGui.blit(matrixStack, getX() + x, getY() + y, 
                26, texY, 26, 26, 256, 256);
    }
    
    @Override
    void drawTooltip(HamonScreen hamonScreen, MatrixStack matrixStack, int mouseX, int mouseY) {
        List<ITextComponent> tooltip = new ArrayList<>();
        tooltip.add(name);
        
        List<AbstractHamonSkill> missingSkills = skill.getRequiredSkills().filter(skill -> 
        !hamonScreen.hamon.isSkillLearned(skill)).collect(Collectors.toList());
        if (!missingSkills.isEmpty()) {
            tooltip.add(new TranslationTextComponent("hamon.skill.required_skills_list").withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
            for (AbstractHamonSkill skill : missingSkills) {
                tooltip.add(skill.getNameTranslated().withStyle(TextFormatting.RED));
            }
        }
        
        hamonScreen.renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY);
    }
    
    enum State {
        CLOSED_NORMAL(0, 0,         false,  false,  false),
        CLOSED_FINAL(0, 78,         true,   false,  false),
        OPENED_NORMAL(0, 26,        false,  true,   false),
        OPENED_FINAL(0, 104,        true,   true,   false),
        LEARNED_NORMAL(0, 52,       false,  false,  true),
        LEARNED_FINAL(0, 130,       true,   false,  true);
        
        public final boolean isFinal;
        public final boolean canBeLearned;
        public final boolean isLearned;
        private final int textureX;
        private final int textureY;
        
        private State(int textureX, int textureY, 
                boolean isFinal, boolean canBeLearned, boolean isLearned) {
            this.textureX = textureX;
            this.textureY = textureY;
            this.isFinal = isFinal;
            this.canBeLearned = canBeLearned;
            this.isLearned = isLearned;
        }
        
        private static State getState(boolean isFinal, boolean canBeLearned, boolean isLearned) {
            int stateIndex = isFinal ? 1 : 0;
            stateIndex += isLearned ? 4 : canBeLearned ? 2 : 0;
            return State.values()[stateIndex];
        }
    }
}
