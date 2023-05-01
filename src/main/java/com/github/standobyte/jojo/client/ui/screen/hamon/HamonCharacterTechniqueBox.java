package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill.Technique;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.IReorderingProcessor;

public class HamonCharacterTechniqueBox {
    final Technique technique;
    private final List<IReorderingProcessor> name;
    private final List<HamonSkillElementLearnable> skills = new ArrayList<>();
    private int x;
    private int y;
    private final int width;
    private final int height;
    
    public HamonCharacterTechniqueBox(Technique technique, int y, List<IReorderingProcessor> name, FontRenderer font) {
        this.technique = technique;
        this.x = 8;
        this.y = y;
        this.name = name;
        this.width = HamonScreen.WINDOW_WIDTH - HamonScreen.WINDOW_THIN_BORDER * 2 - 16;
        this.height = Math.max(name.size() * 9 + 26, 36);
    }
    
    public void render(MatrixStack matrixStack, HamonData hamon, int x, int y, int mouseX, int mouseY, boolean selected) {
        int col1 = selected ? 0x80101000 : 0x80100010;
        int col2 = selected ? 0x5050FF00 : 0x505000FF;
        int col3 = selected ? 0x50287F00 : 0x5028007F;
        ClientUtil.drawTooltipRectangle(matrixStack, 
                this.x + x, this.y + y, width, height, 
                col1, col2, col3, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
    
    public void drawText(MatrixStack matrixStack, FontRenderer font, HamonData hamon, int x, int y) {
        for (int line = 0; line < name.size(); line++) {
            font.drawShadow(matrixStack, name.get(line), 
                    this.x + x * 18 + 3, this.y + y + 1 + line * 9, 0xFFFFFF);
        }
    }
    
    public void drawTooltip(HamonScreen hamonScreen, MatrixStack matrixStack, int x, int y, int mouseX, int mouseY) {
        x += this.x;
        y += this.y;
    }
    
    int getHeight() {
        return height;
    }
    
    public void addSkill(HamonSkillElementLearnable skill) {
        skills.add(skill);
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        int yDiff = y - this.y;
        for (HamonSkillGuiElement skill : skills) {
            skill.setY(skill.getY() + yDiff);
        }
        this.y = y;
    }
}
