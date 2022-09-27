package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.List;

import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonSkillGuiElement {
    final HamonSkill skill;
    final ITextComponent name;
    final List<IReorderingProcessor> description;
    private int stateIndex;
    private State state;
    final int x;
    final int y;
    
    public HamonSkillGuiElement(HamonSkill skill, boolean isFinal, boolean canBeLearned, boolean isLearned, int x, int y, FontRenderer font) {
        this.skill = skill;
        name = new TranslationTextComponent("hamonSkill." + skill.getName() + ".name");
        description = font.split(new TranslationTextComponent("hamonSkill." + skill.getName() + ".desc"), 200);
        stateIndex = isFinal ? 1 : 0;
        this.x = x;
        this.y = y;
        updateState(canBeLearned, isLearned);
    }
    
    void updateState(boolean canBeLearned, boolean isLearned) {
        stateIndex %= 2;
        stateIndex += isLearned ? 4 : canBeLearned ? 2 : 0;
        state = State.values()[stateIndex];
    }
    
    State getState() {
        return state;
    }
    
    public boolean isMouseOver(int scrollX, int scrollY, double mouseX, double mouseY) {
        double realX = scrollX + this.x;
        double realY = scrollY + this.y;
        return mouseX >= realX && mouseX < realX + 26 && mouseY >= realY && mouseY < realY + 26;
    }
    
    public boolean isFinal() {
        return stateIndex % 2 == 1;
    }

    public enum State {
        CLOSED_NORMAL(HamonScreen.WINDOW_WIDTH, 0),
        CLOSED_FINAL(HamonScreen.WINDOW_WIDTH, 78),
        OPENED_NORMAL(HamonScreen.WINDOW_WIDTH, 26),
        OPENED_FINAL(HamonScreen.WINDOW_WIDTH, 104),
        LEARNED_NORMAL(HamonScreen.WINDOW_WIDTH, 52),
        LEARNED_FINAL(HamonScreen.WINDOW_WIDTH, 130);
        
        private int textureX;
        private int textureY;
        
        private State(int textureX, int textureY) {
            this.textureX = textureX;
            this.textureY = textureY;
        }
        
        public int getTextureX() {
            return textureX;
        }
        
        public int getTextureY() {
            return textureY;
        }
    }
}
