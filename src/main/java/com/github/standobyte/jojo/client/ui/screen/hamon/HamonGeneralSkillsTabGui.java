package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.List;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonResetSkillsButtonPacket.HamonSkillsTab;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonGeneralSkillsTabGui extends HamonSkillsTabGui {
    private static final BaseHamonSkill[][] SKILLS_STRENGTH;
    private static final ITextComponent[] NAMES_STRENGTH = {
            new TranslationTextComponent("hamon.skills.overdrive"),
            new TranslationTextComponent("hamon.skills.infusion"),
            new TranslationTextComponent("hamon.skills.flexibility")
    };
    private static final BaseHamonSkill[][] SKILLS_CONTROL;
    private static final ITextComponent[] NAMES_CONTROL = {
            new TranslationTextComponent("hamon.skills.life"),
            new TranslationTextComponent("hamon.skills.attractant"),
            new TranslationTextComponent("hamon.skills.repellent")
    };
    static {
        SKILLS_STRENGTH = new BaseHamonSkill[][] {
            {
                ModHamonSkills.OVERDRIVE.get(), 
                ModHamonSkills.SENDO_OVERDRIVE.get(), 
                ModHamonSkills.TURQUOISE_BLUE_OVERDRIVE.get(), 
                ModHamonSkills.SUNLIGHT_YELLOW_OVERDRIVE.get()},
            {
                ModHamonSkills.THROWABLES_INFUSION.get(), 
                ModHamonSkills.PLANT_INFUSION.get(), 
                ModHamonSkills.ARROW_INFUSION.get(), 
                ModHamonSkills.ANIMAL_INFUSION.get()},
            {
                ModHamonSkills.ZOOM_PUNCH.get(), 
                ModHamonSkills.JUMP.get(), 
                ModHamonSkills.SPEED_BOOST.get(), 
                ModHamonSkills.AFTERIMAGES.get()}
        };
        SKILLS_CONTROL = new BaseHamonSkill[][] {
            {
                ModHamonSkills.HEALING.get(), 
                ModHamonSkills.PLANTS_GROWTH.get(), 
                ModHamonSkills.EXPEL_VENOM.get(), 
                ModHamonSkills.HEALING_TOUCH.get()},
            {
                ModHamonSkills.WALL_CLIMBING.get(), 
                ModHamonSkills.DETECTOR.get(), 
                ModHamonSkills.LIFE_MAGNETISM.get(), 
                ModHamonSkills.HAMON_SPREAD.get()},
            {
                ModHamonSkills.WATER_WALKING.get(), 
                ModHamonSkills.PROJECTILE_SHIELD.get(), 
                ModHamonSkills.LAVA_WALKING.get(), 
                ModHamonSkills.REPELLING_OVERDRIVE.get()}
        };
    }
    
    private static final int[] X_OFFSET = {16, 3, 29, 16};
    private static final int[] Y_OFFSET = {18, 44, 44, 72};
    private int[][] skillTreeNamePos;

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
        skills.clear();
        BaseHamonSkill[][] skillsOnTab = skillsType == HamonStat.STRENGTH ? SKILLS_STRENGTH : SKILLS_CONTROL;
        skillTreeNames = skillsType == HamonStat.STRENGTH ? NAMES_STRENGTH : NAMES_CONTROL;
        skillTreeNamePos = new int[3][2];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                BaseHamonSkill skill = skillsOnTab[i][j];
                int x = 9 + i * 68 + X_OFFSET[j];
                int y = HamonScreen.WINDOW_HEIGHT - 131 + Y_OFFSET[j];
                skills.put(skill, new HamonSkillElementLearnable(skill, 
                        screen.hamon, minecraft.player, screen.teacherSkills, j == 3, x, y));
            }
            skillTreeNamePos[i][0] = 9 + i * 68 + X_OFFSET[0] + 13;
            skillTreeNamePos[i][1] = HamonScreen.WINDOW_HEIGHT - 131 + Y_OFFSET[0] - 18;
        }
    }
    
    @Override
    protected HamonSkillsTab getSkillsType() {
        return skillsType == HamonStat.STRENGTH ? HamonSkillsTab.STRENGTH : HamonSkillsTab.CONTROL;
    }

    @Override
    void drawTab(MatrixStack matrixStack, int windowX, int windowY, boolean isSelected, boolean red) {
        super.drawTab(matrixStack, windowX, windowY, isSelected, red);
        
        minecraft.getTextureManager().bind(HamonSkillsTabGui.HAMON_SKILLS);
        int texY = skillsType == HamonStat.STRENGTH ? 0 : 64;
        blit(matrixStack, windowX - 32 + 13, windowY + getTabY() + 6, 16, 16, 128, texY, 64, 64, 256, 256);
        
        int points = screen.hamon.getSkillPoints(skillsType);
        if (points > 0) {
            minecraft.getTextureManager().bind(HamonScreen.WINDOW);
            int textureX = screen.isTeacherNearby ? 248 : 239;
            blit(matrixStack, windowX - 32 + 7, windowY + getTabY() + 3, textureX, 206, 8, 8);
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
                ClientUtil.drawCenteredString(matrixStack, minecraft.font, nameLines.get(line), 
                        skillTreeNamePos[i][0] + intScrollX, 
                        skillTreeNamePos[i][1] + line * 9 + intScrollY, 0xFFFFFF);
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
                    intScrollX + HamonScreen.WINDOW_WIDTH - 15 - HamonScreen.WINDOW_THIN_BORDER, intScrollY + 5, 0xFFFFFF);
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
