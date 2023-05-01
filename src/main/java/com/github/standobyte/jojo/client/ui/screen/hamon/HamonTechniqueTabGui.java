package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill.HamonSkillType;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill.Technique;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkillSet;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonTechniqueTabGui extends HamonSkillsTabGui {
    private Technique technique;
    private Map<Technique, HamonCharacterTechniqueBox> availableHamonTechniques = Collections.emptyMap();
    private Technique selectedTechnique = null;
    private final List<IReorderingProcessor> availableTechniqueSkillLines;
    private final List<IReorderingProcessor> tabLockedLines;
    
    HamonTechniqueTabGui(Minecraft minecraft, HamonScreen screen, int index, String title) {
        super(minecraft, screen, index, title, -1, -1);
        if (!isLocked()) {
            fillSkillLines();
        }
        availableTechniqueSkillLines = minecraft.font.split(new TranslationTextComponent("hamon.technique_available"), 100);
        tabLockedLines = minecraft.font.split(new TranslationTextComponent("hamon.techniques_locked", 
                        HamonSkillSet.techniqueLevelReq(0)), 200);
    }
    
    @Override
    protected ITextComponent createTabDescription(String key) {
        return new TranslationTextComponent(key, new TranslationTextComponent("hamon.techniques.tab.desc.only_one"));
    }
    
    private int techniqueYStarting() {
        return 103;
    }
    private static final int TECHNIQUE_Y_GAP = 10;
    
    private void fillSkillLines() {
        skills.clear();
        
        // available techniques
        List<Technique> techniques;
        this.technique = screen.hamon.getTechnique();
        if (technique != null) {
            techniques = Util.make(new ArrayList<>(), list -> list.add(technique));
        }
        else {
            techniques = new ArrayList<>(Arrays.asList(Technique.values()));
            Collections.sort(techniques, TECHNIQUES_ORDER);
        }
        
        List<HamonScreenButton> newButtons = new ArrayList<>();
        
        // technique names, buttons, skill squares and y coordinates
        availableHamonTechniques = new LinkedHashMap<>();
        int techniqueY = techniqueYStarting();
        for (Technique technique : techniques) {
            List<IReorderingProcessor> name = minecraft.font.split(new TranslationTextComponent("hamon.technique." + technique.name().toLowerCase()), 192);
            HamonCharacterTechniqueBox techniqueBox = new HamonCharacterTechniqueBox(technique, techniqueY, name, minecraft.font);
            availableHamonTechniques.put(technique, techniqueBox);
            
            List<HamonSkill> skills = technique.getSkills();
            int j = 0;
            for (HamonSkill skill : skills) {
                int x = HamonScreen.WINDOW_WIDTH - 21 - (skills.size() - j) * 28;
                int y = techniqueY + name.size() * 9 + 4;
                HamonSkillElementLearnable skillSquare = new HamonSkillElementLearnable(skill, 
                        screen.hamon, minecraft.player, screen.teacherSkills, 
                        false, x, y);
                this.skills.put(skill, skillSquare);
                techniqueBox.addSkill(skillSquare);
                j++;
            }
            
            techniqueY += techniqueBox.getHeight() + TECHNIQUE_Y_GAP;
        }
        
        maxY = techniqueY;
        int actualWindowHeight = HamonScreen.WINDOW_HEIGHT - HamonScreen.WINDOW_UPPER_BORDER - HamonScreen.WINDOW_THIN_BORDER;
        if (maxY < actualWindowHeight) {
            maxY = -1;
            scrollY = 0;
        }
        else {
            scrollY = Math.max(scrollY, -maxY + actualWindowHeight);
        }
        
        if (getSelectedSkill() != null) {
            selectSkill(skills.get(getSelectedSkill().getHamonSkill()));
        }
        
        newButtons.forEach(screen::addButton);
    }
    
    @Override
    protected HamonSkillType getSkillsType() {
        return HamonSkillType.TECHNIQUE;
    }
    
    @Override
    void drawTab(MatrixStack matrixStack, int windowX, int windowY, boolean isSelected, boolean red) {
        super.drawTab(matrixStack, windowX, windowY, isSelected, red);
        if (screen.hamon.canLearnNewTechniqueSkill()) {
            minecraft.getTextureManager().bind(HamonScreen.WINDOW);
            blit(matrixStack, windowX - 32 + 7, windowY + getTabY() + 3, 248, 206, 8, 8);
        }
    }

    @Override
    protected void drawDesc(MatrixStack matrixStack) {
        if (getSelectedSkill() != null) {
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
        if (screen.hamon.canLearnNewTechniqueSkill()) {
            return availableTechniqueSkillLines;
        }
        return super.additionalTabNameTooltipInfo();
    }
    
    private boolean isLocked() {
        return screen.hamon.getTechnique() == null && !screen.hamon.hasTechniqueLevel(0);
    }
    
    @Override
    protected void updateButton() {
        if (isLocked()) {
            learnButton.visible = false;
            creativeResetButton.visible = false;
        }
        else {
            super.updateButton();
            this.technique = screen.hamon.getTechnique();
            Technique technique = null;
            if (getSelectedSkill() != null) {
                technique = getSelectedSkill().getHamonSkill().getTechnique();
            }
            reorderTechniqueBoxes(technique);
        }
    }
    
    @Override
    protected void drawText(MatrixStack matrixStack) {
        if (!isLocked()) {
            drawDesc(matrixStack);
            availableHamonTechniques.values().forEach(technique -> technique.drawText(matrixStack, minecraft.font, screen.hamon, intScrollX, intScrollY));
        }
        else {
            for (int i = 0; i < tabLockedLines.size(); i++) {
                ClientUtil.drawCenteredString(matrixStack, minecraft.font, tabLockedLines.get(i), 
                        (float) (scrollX - HamonScreen.WINDOW_THIN_BORDER + HamonScreen.WINDOW_WIDTH / 2), (float) (scrollY + 22 + i * 9), 0xFFFFFF);
            }
        }
    }

    @Override
    protected void drawActualContents(HamonScreen screen, MatrixStack matrixStack, int mouseX, int mouseY) {
        if (!isLocked()) {
            availableHamonTechniques.values().forEach(technique -> 
            technique.render(matrixStack, screen.hamon, intScrollX, intScrollY, mouseX, mouseY, selectedTechnique == technique.technique));
            super.drawActualContents(screen, matrixStack, mouseX, mouseY);
        }
    }
    
    @Override
    boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isLocked()) return false;
        return super.mouseClicked(mouseX, mouseY, mouseButton);
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
            availableHamonTechniques.values().forEach(technique -> technique.drawTooltip(
                    screen, matrixStack, intScrollX, intScrollY, mouseX, mouseY));
        }
    }
    
    @Override
    void updateTab() {
        if (!isLocked()) {
            super.updateTab();
            Technique newTechnique = screen.hamon.getTechnique();
            this.technique = newTechnique;
            fillSkillLines();
            updateButton();
            

//            for (HamonSkillElementLearnable skillElement : skills.values()) {
//                skillElement.updateState(screen.hamon, minecraft.player, screen.teacherSkills);
//            }
        }
    }
    
    private void reorderTechniqueBoxes(@Nullable Technique firstTechnique) {
        selectedTechnique = firstTechnique;
        if (availableHamonTechniques.size() <= 1) {
            return;
        }
        List<Technique> techniques = new ArrayList<>(Arrays.asList(Technique.values()));
        Collections.sort(techniques, TECHNIQUES_ORDER);
        if (firstTechnique != null) {
            Collections.sort(techniques, (t1, t2) -> t1 == firstTechnique ? -1 : t2 == firstTechnique ? 1 : 0);
        }
        
        int y = techniqueYStarting();
        for (Technique technique : techniques) {
            HamonCharacterTechniqueBox characterBox = availableHamonTechniques.get(technique);
            if (characterBox != null) {
                characterBox.setY(y);
                y += characterBox.getHeight() + TECHNIQUE_Y_GAP;
            }
        }
    }
    
    
    private static final Comparator<Technique> TECHNIQUES_ORDER = (technique1, technique2) -> {
        return technique1.ordinal() - technique2.ordinal();
    };
}
