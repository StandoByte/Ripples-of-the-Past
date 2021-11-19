package com.github.standobyte.jojo.client.ui.screen;

import java.util.List;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonSkillType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonGeneralSkillsTabGui extends HamonSkillsTabGui {
    private static final HamonSkill[][] SKILLS_STRENGTH = {
            {HamonSkill.OVERDRIVE, HamonSkill.SENDO_OVERDRIVE, HamonSkill.TURQUOISE_BLUE_OVERDRIVE, HamonSkill.SUNLIGHT_YELLOW_OVERDRIVE},
            {HamonSkill.PLANT_INFUSION, HamonSkill.THROWABLES_INFUSION, HamonSkill.ANIMAL_INFUSION, HamonSkill.ARROW_INFUSION},
            {HamonSkill.ZOOM_PUNCH, HamonSkill.JUMP, HamonSkill.SPEED_BOOST, HamonSkill.AFTERIMAGES}
    };
    private static final ITextComponent[] NAMES_STRENGTH = {
            new TranslationTextComponent("hamon.skills.overdrive"),
            new TranslationTextComponent("hamon.skills.infusion"),
            new TranslationTextComponent("hamon.skills.flexibility")
    };
    private static final HamonSkill[][] SKILLS_CONTROL = {
            {HamonSkill.HEALING, HamonSkill.PLANTS_GROWTH, HamonSkill.EXPEL_VENOM, HamonSkill.HEALING_TOUCH},
            {HamonSkill.WALL_CLIMBING, HamonSkill.DETECTOR, HamonSkill.LIFE_MAGNETISM, HamonSkill.HAMON_SPREAD},
            {HamonSkill.REPELLING_OVERDRIVE, HamonSkill.PROJECTILE_SHIELD, HamonSkill.WATER_WALKING, HamonSkill.LAVA_WALKING}
    };
    private static final ITextComponent[] NAMES_CONTROL = {
            new TranslationTextComponent("hamon.skills.life"),
            new TranslationTextComponent("hamon.skills.attractant"),
            new TranslationTextComponent("hamon.skills.repellent")
    };
    private static final int[] X_OFFSET = {16, 3, 29, 16};
    private static final int[] Y_OFFSET = {10, 40, 40, 72};

    private final HamonStat skillsType;
    private List<IReorderingProcessor> nextPointHintLines;
    private final List<IReorderingProcessor> unspentPointsLines;
    private final List<IReorderingProcessor> unspentPointsNoTeacherLines;
    private ITextComponent[] skillTreeNames = new ITextComponent[3];

    HamonGeneralSkillsTabGui(Minecraft minecraft, HamonScreen screen, int index, String title, HamonStat skillsType) {
        super(minecraft, screen, index, title, -1, -1);
        this.skillsType = skillsType;
        fillSkillLines();
        unspentPointsLines = minecraft.font.split(new TranslationTextComponent("hamon.unspent_points"), 100);
        unspentPointsNoTeacherLines = minecraft.font.split(new TranslationTextComponent("hamon.unspent_points_no_teacher"), 100);
    }
    
    private void fillSkillLines() {
        HamonSkill[][] skillsOnTab = skillsType == HamonStat.STRENGTH ? SKILLS_STRENGTH : SKILLS_CONTROL;
        skillTreeNames = skillsType == HamonStat.STRENGTH ? NAMES_STRENGTH : NAMES_CONTROL;
        skillArrays = new HamonSkillGuiElement[3][4];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                HamonSkill skill = skillsOnTab[i][j];
                int x = 9 + i * 68 + X_OFFSET[j];
                int y = HamonScreen.WINDOW_HEIGHT - 131 + Y_OFFSET[j];
                skillArrays[i][j] = new HamonSkillGuiElement
                        (skill, j == 3, screen.hamon.canLearnSkill(skill, screen.teacherSkills), screen.hamon.isSkillLearned(skill), x, y, minecraft.font);
            }
        }
    }
    
    @Override
    protected HamonSkillType getSkillsType() {
        return skillsType == HamonStat.STRENGTH ? HamonSkillType.STRENGTH : HamonSkillType.CONTROL;
    }

    @Override
    void drawTab(MatrixStack matrixStack, int windowX, int windowY, boolean isSelected) {
        super.drawTab(matrixStack, windowX, windowY, isSelected);
        int points = screen.hamon.getSkillPoints(skillsType);
            if (points > 0) {
                minecraft.getTextureManager().bind(HamonScreen.WINDOW);
                int textureX = screen.isTeacherNearby ? 248 : 239;
                blit(matrixStack, windowX - 32 + 7, windowY + getTabY(tabIndex) + 3, textureX, 156, 8, 8);
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
        for (int i = 0; i < 3; i++) {
            List<IReorderingProcessor> nameLines = minecraft.font.split(skillTreeNames[i], 75);
            for (int line = 0; line < nameLines.size(); line++) {
                ClientUtil.drawCenteredString(matrixStack, minecraft.font, nameLines.get(line), skillArrays[i][0].x + 13 + intScrollX, skillArrays[i][0].y - 18 + line * 9 + intScrollY, 0xFFFFFF);
            }
        }
    }

    @Override
    protected void drawDesc(MatrixStack matrixStack) {
        int points = screen.hamon.getSkillPoints(skillsType);
        if (selectedSkill != null) {
            drawSkillDesc(matrixStack);
        }
        else {
            ITextComponent lvl = skillsType == HamonStat.STRENGTH ? new TranslationTextComponent("hamon.strength_level", screen.hamon.getHamonStrengthLevel(), HamonData.MAX_STAT_LEVEL) : 
                new TranslationTextComponent("hamon.control_level", screen.hamon.getHamonControlLevel(), HamonData.MAX_STAT_LEVEL);
            drawString(matrixStack, minecraft.font, lvl, intScrollX + 6, intScrollY + 5, 0xFFFFFF);
            ClientUtil.drawRightAlignedString(matrixStack, minecraft.font, new TranslationTextComponent("hamon.skill_points", 
                    new StringTextComponent(String.valueOf(points)).withStyle(points > 0 ? TextFormatting.DARK_GREEN : TextFormatting.DARK_RED)),
                    intScrollX + HamonScreen.WINDOW_WIDTH - 15 - HamonScreen.WINDOW_THIN_BORDER, intScrollY + 5, 0xFFFFFF);
            super.drawDesc(matrixStack);
        }
    }

    @Override
    void drawToolTips(MatrixStack matrixStack, int mouseX, int mouseY, int windowPosX, int windowPosY) {
        super.drawToolTips(matrixStack, mouseX, mouseY, windowPosX, windowPosY);
        if (selectedSkill == null) {
            if (mouseX >= 193 && mouseX <= 205 && mouseY >= 4 && mouseY <= 12) {
                screen.renderTooltip(matrixStack, nextPointHintLines, mouseX, mouseY);
            }
        }
    }

    @Override
    void updateTab() {
        super.updateTab();
        int statLvl = skillsType == HamonStat.STRENGTH ? screen.hamon.getHamonStrengthLevel() : screen.hamon.getHamonControlLevel();
        ITextComponent textComponent;
        if (statLvl < HamonData.MAX_SKILL_POINTS_LVL) {
            textComponent = new TranslationTextComponent("hamon.next_point." + (skillsType == HamonStat.STRENGTH ? "strength" : "control"), screen.hamon.nextSkillPointLvl(skillsType));
        }
        else {
            textComponent = new TranslationTextComponent("hamon.max_skill_points");
        }
        nextPointHintLines = minecraft.font.split(textComponent, 100);
    }
}
