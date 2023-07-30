package com.github.standobyte.jojo.client.ui.screen.hamon;

import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_HEIGHT;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_THIN_BORDER;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_WIDTH;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonResetSkillsButtonPacket.HamonSkillsTab;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkillTree;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonGeneralSkillsTabGui extends HamonSkillsTabGui {
    private static final Map<HamonStat, BaseHamonSkillTree[]> SKILL_TREES = Util.make(new EnumMap<>(HamonStat.class), map -> {
        map.put(HamonStat.STRENGTH, new BaseHamonSkillTree[] {
                BaseHamonSkillTree.OVERDRIVE,
                BaseHamonSkillTree.INFUSION,
                BaseHamonSkillTree.FLEXIBILITY
        });
        map.put(HamonStat.CONTROL, new BaseHamonSkillTree[] {
                BaseHamonSkillTree.LIFE_ENERGY,
                BaseHamonSkillTree.ATTRACTANT_REPELLENT,
                BaseHamonSkillTree.BODY_MANIPULATION
        });
    });
    
    private final HamonStat skillsType;
    private List<IReorderingProcessor> nextPointHintLines;
    private final List<IReorderingProcessor> unspentPointsLines;
    private final List<IReorderingProcessor> unspentPointsNoTeacherLines;

    HamonGeneralSkillsTabGui(Minecraft minecraft, HamonScreen screen, String title, HamonStat skillsType) {
        super(minecraft, screen, title, -1, -1);
        this.skillsType = skillsType;
        unspentPointsLines = minecraft.font.split(new TranslationTextComponent("hamon.unspent_points")
                .withStyle(TextFormatting.ITALIC, TextFormatting.GRAY), 100);
        unspentPointsNoTeacherLines = minecraft.font.split(new TranslationTextComponent("hamon.unspent_points_no_teacher")
                .withStyle(TextFormatting.ITALIC, TextFormatting.GRAY), 100);
        
        fillSkillLines();
    }
    
    private int xOffset(int gridX) { return 3 + gridX * 13; }
    private int yOffset(int gridY) { return WINDOW_HEIGHT - 113 + gridY * (gridY < 2 ? 26 : 27); }
    private void fillSkillLines() {
        skills.clear();
        BaseHamonSkillTree[] skillTrees = SKILL_TREES.get(skillsType);
        for (int treeIndex = 0; treeIndex < skillTrees.length; treeIndex++) {
            BaseHamonSkillTree skillTree = skillTrees[treeIndex];
            int tierCount = 0;
            for (List<? extends AbstractHamonSkill> tier : skillTree.getAllTiers()) {
                int tierSize = tier.size();
                for (int tierI = 0; tierI < tierSize; tierI++) {
                    int gridX = tierSize == 1 ? 1 : tierI * MathHelper.ceil(3f / tierSize);
                    int gridY = tierCount;
                    
                    AbstractHamonSkill skill = tier.get(tierI);
                    
                    int x = 9 + treeIndex * 68 + xOffset(gridX);
                    int y = yOffset(gridY);
                    skills.put(skill, new HamonSkillElementLearnable(skill, 
                            screen.hamon, minecraft.player, screen.teacherSkills, gridY == 2, x, y));
                }
                
                tierCount++;
            }
        }
    }
    
    @Override
    protected HamonSkillsTab getSkillsType() {
        return skillsType == HamonStat.STRENGTH ? HamonSkillsTab.STRENGTH : HamonSkillsTab.CONTROL;
    }

    @Override
    void drawIcon(MatrixStack matrixStack, int windowX, int windowY, ItemRenderer itemRenderer) {
        minecraft.getTextureManager().bind(HamonSkillsTabGui.HAMON_SKILLS);
        int x = tabPositioning.getIconX(windowX, index, WINDOW_WIDTH);
        int y = tabPositioning.getIconY(windowY, index, WINDOW_HEIGHT);
        
        int texY = skillsType == HamonStat.STRENGTH ? 0 : 64;
        blit(matrixStack, x, y, 16, 16, 128, texY, 64, 64, 256, 256);
        
        int points = screen.hamon.getSkillPoints(skillsType);
        if (points > 0) {
            minecraft.getTextureManager().bind(HamonScreen.WINDOW);
            int textureX = screen.isTeacherNearby ? 248 : 239;
            blit(matrixStack, x - 6, y - 3, textureX, 206, 8, 8);
        }
    }

    @Override
    List<IReorderingProcessor> additionalTabNameTooltipInfo() {
        int points = screen.hamon.getSkillPoints(skillsType);
            return points > 0 ? screen.isTeacherNearby ? unspentPointsLines : unspentPointsNoTeacherLines : super.additionalTabNameTooltipInfo();
    }

    @Override
    protected void drawText(MatrixStack matrixStack) {
        drawDesc(matrixStack);
        BaseHamonSkillTree[] trees = SKILL_TREES.get(skillsType);
        for (int i = 0; i < 3; i++) {
            List<IReorderingProcessor> nameLines = minecraft.font.split(
                    new TranslationTextComponent(String.format("hamon.skills.%s", trees[i].getName())), 75);
            for (int line = 0; line < nameLines.size(); line++) {
                ClientUtil.drawCenteredString(matrixStack, minecraft.font, nameLines.get(line), 
                        9 + i * 68 + xOffset(1) + 13 + intScrollX, 
                        yOffset(0) - 18 + line * minecraft.font.lineHeight + intScrollY, 
                        0xFFFFFF);
            }
        }
    }

    @Override
    protected void drawDesc(MatrixStack matrixStack) {
        int points = screen.hamon.getSkillPoints(skillsType);
        if (getSelectedSkill() != null) {
            drawSkillDesc(matrixStack);
        }
        else {
            ITextComponent lvl = skillsType == HamonStat.STRENGTH ? new TranslationTextComponent("hamon.strength_level", screen.hamon.getHamonStrengthLevel(), HamonData.MAX_STAT_LEVEL) : 
                new TranslationTextComponent("hamon.control_level", screen.hamon.getHamonControlLevel(), HamonData.MAX_STAT_LEVEL);
            drawString(matrixStack, minecraft.font, lvl, intScrollX + 6, intScrollY + 5, 0xFFFFFF);
            ClientUtil.drawRightAlignedString(matrixStack, minecraft.font, new TranslationTextComponent("hamon.skill_points", 
                    new StringTextComponent(String.valueOf(points)).withStyle(points > 0 ? TextFormatting.DARK_GREEN : TextFormatting.DARK_RED)),
                    intScrollX + WINDOW_WIDTH - 15 - WINDOW_THIN_BORDER, intScrollY + 5, 0xFFFFFF);
            super.drawDesc(matrixStack);
        }
    }

    @Override
    void drawToolTips(MatrixStack matrixStack, int mouseX, int mouseY, int windowPosX, int windowPosY) {
        super.drawToolTips(matrixStack, mouseX, mouseY, windowPosX, windowPosY);
        if (getSelectedSkill() == null) {
            if (mouseX >= 193 && mouseX <= 205 && mouseY >= 4 && mouseY <= 12) {
                screen.renderTooltip(matrixStack, nextPointHintLines, mouseX, mouseY);
            }
        }
        
        int yMax = yOffset(0) + intScrollY;
        if (mouseY >= yMax - minecraft.font.lineHeight * 2 && mouseY < yMax) {
            int x0 = 6 + intScrollX;
            int xWidth = 68;
            int i = (mouseX - x0) / xWidth;
            if (i >= 0 && i < 3) {
                BaseHamonSkillTree skillTree = SKILL_TREES.get(skillsType)[i];
                ITextComponent toooltip = new TranslationTextComponent(String.format("hamon.skills.%s.desc", skillTree.getName()))
                        .withStyle(TextFormatting.ITALIC);
                screen.renderTooltip(matrixStack, minecraft.font.split(toooltip, 200), mouseX, mouseY);
            }
        }
    }

    @Override
    void updateTab() {
        super.updateTab();
        int statLvl = skillsType == HamonStat.STRENGTH ? screen.hamon.getHamonStrengthLevel() : screen.hamon.getHamonControlLevel();
        ITextComponent textComponent;
        if (statLvl < HamonData.MAX_STAT_LEVEL) {
            textComponent = new TranslationTextComponent("hamon.next_point." + (skillsType == HamonStat.STRENGTH ? "strength" : "control"), screen.hamon.nextSkillPointLvl(skillsType));
        }
        else {
            textComponent = new TranslationTextComponent("hamon.max_skill_points");
        }
        nextPointHintLines = minecraft.font.split(textComponent, 100);
    }
}
