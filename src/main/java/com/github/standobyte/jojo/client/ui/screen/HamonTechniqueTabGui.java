package com.github.standobyte.jojo.client.ui.screen;

import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkillSet;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonSkillType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.Technique;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonTechniqueTabGui extends HamonSkillsTabGui {
    HamonSkill.Technique technique;
    private static final Technique[] TECHNIQUE_ORDER = {Technique.JONATHAN, Technique.ZEPPELI, Technique.JOSEPH, Technique.CAESAR, Technique.LISA_LISA};
    private static final Map<Technique, HamonSkill[]> SKILLS_BY_TECHNIQUE = ImmutableMap.<Technique, HamonSkill[]>builder()
            .put(Technique.JONATHAN, new HamonSkill[] {HamonSkill.NATURAL_TALENT, HamonSkill.SCARLET_OVERDRIVE, HamonSkill.METAL_SILVER_OVERDRIVE})
            .put(Technique.ZEPPELI, new HamonSkill[] {HamonSkill.HAMON_CUTTER, HamonSkill.TORNADO_OVERDRIVE, HamonSkill.DEEP_PASS})
            .put(Technique.JOSEPH, new HamonSkill[] {HamonSkill.CLACKER_VOLLEY, HamonSkill.ROPE_TRAP, HamonSkill.CHEAT_DEATH})
            .put(Technique.CAESAR, new HamonSkill[] {HamonSkill.BUBBLE_LAUNCHER, HamonSkill.BUBBLE_CUTTER, HamonSkill.CRIMSON_BUBBLE})
            .put(Technique.LISA_LISA, new HamonSkill[] {HamonSkill.AJA_STONE_KEEPER, HamonSkill.SATIPOROJA_SCARF, HamonSkill.SNAKE_MUFFLER})
        .build();
    private ITextComponent[] skillLineNames;
    private final List<IReorderingProcessor> avaliableTechniqueSkillLines;
    private final List<IReorderingProcessor> tabLockedLines;
    
    HamonTechniqueTabGui(Minecraft minecraft, HamonScreen screen, int index, String title) {
        super(minecraft, screen, index, title, -1, screen.hamon.getTechnique() == null && screen.hamon.haveTechniqueLevel() ? 247 : -1);
        this.technique = screen.hamon.getTechnique();
        fillSkillLines();
        avaliableTechniqueSkillLines = minecraft.font.split(new TranslationTextComponent("hamon.technique_avaliable"), 100);
        tabLockedLines = minecraft.font.split(new TranslationTextComponent("hamon.techniques_locked", HamonSkillSet.TECHNIQUE_MINIMAL_STAT_LVL), 200);
    }
    
    @Override
    protected ITextComponent createTabDescription(String key) {
        return new TranslationTextComponent(key, HamonSkillSet.techniqueLevelReq(0), HamonSkillSet.techniqueLevelReq(1), HamonSkillSet.techniqueLevelReq(2));
    }
    
    private void fillSkillLines() {
        if (technique == null) {
            skillArrays = new HamonSkillGuiElement[TECHNIQUE_ORDER.length][3];
            skillLineNames = new TranslationTextComponent[TECHNIQUE_ORDER.length];
            for (int i = 0; i < TECHNIQUE_ORDER.length; i++) {
                for (int j = 0; j < 3; j++) {
                    HamonSkill skill = SKILLS_BY_TECHNIQUE.get(TECHNIQUE_ORDER[i])[j];
                    int x = HamonScreen.WINDOW_WIDTH - 21 - (3 - j) * 30;
                    int y = HamonScreen.WINDOW_HEIGHT - 135 + i * 30;
                    skillArrays[i][j] = new HamonSkillGuiElement
                            (skill, false, screen.hamon.canLearnSkill(skill, screen.teacherSkills), screen.hamon.isSkillLearned(skill), x, y, minecraft.font);
                }
                skillLineNames[i] = new TranslationTextComponent("hamon.technique." + TECHNIQUE_ORDER[i].name().toLowerCase());
            }
        }
        else {
            techniqueChosen();
        }
    }
    
    @Override
    protected HamonSkillType getSkillsType() {
        return HamonSkillType.TECHNIQUE;
    }
    
    private void techniqueChosen() {
        skillArrays = new HamonSkillGuiElement[1][3];
        int y = HamonScreen.WINDOW_HEIGHT - 135;
        for (int j = 0; j < 3; j++) {
            HamonSkill skill = SKILLS_BY_TECHNIQUE.get(technique)[j];
            int x = HamonScreen.WINDOW_WIDTH - 21 - (3 - j) * 30;
            skillArrays[0][j] = new HamonSkillGuiElement
                    (skill, false, screen.hamon.canLearnSkill(skill, screen.teacherSkills), screen.hamon.isSkillLearned(skill), x, y, minecraft.font);
        }
        skillLineNames = new TranslationTextComponent[] { new TranslationTextComponent("hamon.technique." + technique.name().toLowerCase()) };
    }
    
    @Override
    void drawTab(MatrixStack matrixStack, int windowX, int windowY, boolean isSelected) {
        super.drawTab(matrixStack, windowX, windowY, isSelected);
        if (screen.hamon.haveTechniqueLevel()) {
            minecraft.getTextureManager().bind(HamonScreen.WINDOW);
            blit(matrixStack, windowX - 32 + 7, windowY + getTabY(tabIndex) + 3, 248, 156, 8, 8);
        }
    }

    @Override
    protected void drawDesc(MatrixStack matrixStack) {
        if (selectedSkill != null) {
            drawSkillDesc(matrixStack);
        }
        else {
            for (int i = 0; i < descLines.size(); i++) {
                minecraft.font.draw(matrixStack, descLines.get(i), (float) scrollX + 6, (float) scrollY + 5 + i * 9, 0xFFFFFF);
            }
        }
    }
    
    @Override
    List<IReorderingProcessor> additionalTabNameTooltipInfo() {
        return screen.hamon.haveTechniqueLevel() ? avaliableTechniqueSkillLines : super.additionalTabNameTooltipInfo();
    }
    
    @Override
    protected boolean isLocked() {
        return screen.hamon.getTechnique() == null && !screen.hamon.techniquesUnlocked();
    }
    
    @Override
    protected void updateButton() {
        if (isLocked()) {
            learnButton.visible = false;
            creativeResetButton.visible = false;
        }
        else {
            super.updateButton();
        }
    }
    
    @Override
    protected void drawText(MatrixStack matrixStack) {
        if (!isLocked()) {
            drawDesc(matrixStack);
            for (int i = 0; i < skillLineNames.length; i++) {
                List<IReorderingProcessor> nameLines = minecraft.font.split(skillLineNames[i], 100);
                for (int line = 0; line < nameLines.size(); line++) {
                    minecraft.font.drawShadow(matrixStack, nameLines.get(line), intScrollX + 10, 
                            intScrollY + HamonScreen.WINDOW_HEIGHT - 128 + i * 30 + line * 9, 0xFFFFFF);
                }
            }
        }
        else {
            for (int i = 0; i < tabLockedLines.size(); i++) {
                ClientUtil.drawCenteredString(matrixStack, minecraft.font, tabLockedLines.get(i), 
                        (float) (scrollX - HamonScreen.WINDOW_THIN_BORDER + HamonScreen.WINDOW_WIDTH / 2), (float) (scrollY + 22 + i * 9), 0xFFFFFF);
            }
        }
    }

    @Override
    boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return isLocked() ? false : super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return isLocked() ? false : super.mouseReleased(mouseX, mouseY, mouseButton);
    }
    
    @Override
    void scroll(double xMovement, double yMovement) {
        if (!isLocked()) {
            super.scroll(xMovement, yMovement);
        }
    }
    
    @Override
    void drawToolTips(MatrixStack matrixStack, int mouseX, int mouseY, int windowPosX, int windowPosY) {
        if (!isLocked()) {
            super.drawToolTips(matrixStack, mouseX, mouseY, windowPosX, windowPosY);
        }
    }

    @Override
    void updateTab() {
        if (!isLocked()) {
            super.updateTab();
            Technique newTechnique = screen.hamon.getTechnique();
            if (this.technique == null && newTechnique != null) {
                this.technique = newTechnique;
                this.scrollY = 0;
                this.maxY = -1;
                fillSkillLines();
                selectedSkill = skillArrays[0][0];
            }
            if (this.technique != null && newTechnique == null) {
                this.technique = newTechnique;
                this.scrollY = 0;
                this.maxY = 247;
                fillSkillLines();
            }
        }
    }
}
