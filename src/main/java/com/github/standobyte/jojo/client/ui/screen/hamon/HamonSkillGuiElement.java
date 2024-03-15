package com.github.standobyte.jojo.client.ui.screen.hamon;

import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.text.IFormattableTextComponent;

public class HamonSkillGuiElement {
    protected final AbstractHamonSkill skill;
    protected final IFormattableTextComponent name;
    protected int x;
    protected int y;
    protected final int width;
    protected final int height;
    
    public HamonSkillGuiElement(AbstractHamonSkill skill, 
            int x, int y, int width, int height) {
        this(skill, skill.getNameTranslated(), x, y, width, height);
    }
    
    public HamonSkillGuiElement(AbstractHamonSkill skill, IFormattableTextComponent name, 
            int x, int y, int width, int height) {
        this.skill = skill;
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public boolean isMouseOver(int scrollX, int scrollY, int mouseX, int mouseY) {
        double realX = scrollX + this.x;
        double realY = scrollY + this.y;
        return mouseX >= realX && mouseX < realX + width && mouseY >= realY && mouseY < realY + height;
    }
    
    public void renderSkillIcon(MatrixStack matrixStack, int x, int y) {
        HamonSkillsTabGui.renderHamonSkillIcon(matrixStack, skill, this.x + x, this.y + y);
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    void drawTooltip(HamonScreen hamonScreen, MatrixStack matrixStack, int mouseX, int mouseY) {
        hamonScreen.renderTooltip(matrixStack, name, mouseX, mouseY);
    }
    
    public AbstractHamonSkill getHamonSkill() {
        return skill;
    }
}
