package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.text.DecimalFormat;
import java.util.List;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonMeditationPacket;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonData.Exercise;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

@SuppressWarnings("deprecation")
public class HamonStatsTabGui extends HamonTabGui {
    private final List<IReorderingProcessor> strengthDescLines;
    private final List<IReorderingProcessor> controlDescLines;
    private final List<IReorderingProcessor> breathingDescLines;
    private final List<IReorderingProcessor> exercisesDescLines;
    private final List<IReorderingProcessor> statLimitTooltip;
    private final List<IReorderingProcessor> meditationTooltip;

    private HamonScreenButton abandonTrainingButton;
    private boolean hamonStrengthLimited;
    private boolean hamonControlLimited;
    private int strengthStatY;
    private int controlStatY;
    private int breathingStatY;
    private int exercises1Y;
    private int exercises2Y;
    private int exercisesAvgY;

    HamonStatsTabGui(Minecraft minecraft, HamonScreen screen, int index, String title) {
        super(minecraft, screen, index, title, -1, 1);
        int textWidth = HamonScreen.WINDOW_WIDTH - 30;
        strengthDescLines = minecraft.font.split(new TranslationTextComponent("hamon.strength_stat.desc"), textWidth);
        controlDescLines = minecraft.font.split(new TranslationTextComponent("hamon.control_stat.desc"), textWidth);
        ITextComponent desc = JojoModConfig.getCommonConfigInstance(true).breathingTrainingDeterioration.get() ? 
                new TranslationTextComponent("hamon.breathing_stat.desc", new TranslationTextComponent("hamon.breathing_stat.notice"))
                : new TranslationTextComponent("hamon.breathing_stat.desc");
        breathingDescLines = minecraft.font.split(desc, textWidth);
        exercisesDescLines = minecraft.font.split(new TranslationTextComponent("hamon.exercises_average"), textWidth);
        statLimitTooltip = minecraft.font.split(new TranslationTextComponent("hamon.stat_limited"), 150);
        meditationTooltip = minecraft.font.split(new TranslationTextComponent("hamon.meditation_button", 
                new KeybindTextComponent("key.sneak"), new KeybindTextComponent("jojo.key.hamon_skills_window")), 100);
    }

    @Override
    void addButtons() {
        screen.addButton(abandonTrainingButton = new HamonScreenButton(screen.windowPosX() + 13, screen.windowPosY() + 999, 204, 20, 
                new TranslationTextComponent("hamon.abandon.tab"), button -> {
                    screen.abandonTrainingTab.setPrevTab(this);
                    screen.selectTab(screen.abandonTrainingTab);
                }));
    }
    
    @Override
    List<HamonScreenButton> getButtons() {
        return ImmutableList.of(abandonTrainingButton);
    }

    @Override
    protected void drawActualContents(HamonScreen screen, MatrixStack matrixStack, int mouseX, int mouseY) {
        minecraft.getTextureManager().bind(HamonScreen.WINDOW);
        float breathingTraining = screen.hamon.getBreathingLevel();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        // hamon strength bar
        float pts;
        int level = screen.hamon.getHamonStrengthLevel();
        if (level == HamonData.MAX_STAT_LEVEL) {
            pts = 1.0F;
        }
        else {
            int ptsAtLvl = HamonData.pointsAtLevel(level);
            pts = ((float) (screen.hamon.getHamonStrengthPoints() - ptsAtLvl)) / (HamonData.pointsAtLevel(level + 1) - ptsAtLvl);
        }
        blit(matrixStack, intScrollX + 154, strengthStatY + 1, 203, 235, (int) (50 * pts), 5);
        blit(matrixStack, intScrollX + 153, strengthStatY, 202, 228 , 52, 7);
        if (hamonStrengthLimited = level < HamonData.MAX_STAT_LEVEL && level >= (int) breathingTraining + HamonData.MIN_BREATHING_EXCEED) {
            blit(matrixStack, intScrollX + 142, strengthStatY, 230, 206, 8, 8);
        }

        // hamon control bar
        level = screen.hamon.getHamonControlLevel();
        if (level == HamonData.MAX_STAT_LEVEL) {
            pts = 1.0F;
        }
        else {
            int ptsAtLevel = HamonData.pointsAtLevel(level);
            pts = ((float) (screen.hamon.getHamonControlPoints() - ptsAtLevel)) / (HamonData.pointsAtLevel(level + 1) - ptsAtLevel);
        }
        blit(matrixStack, intScrollX + 154, controlStatY + 1, 203, 240, (int) (50 * pts), 5);
        blit(matrixStack, intScrollX + 153, controlStatY, 202, 228, 52, 7);
        if (hamonControlLimited = level < HamonData.MAX_STAT_LEVEL && level >= (int) breathingTraining + HamonData.MIN_BREATHING_EXCEED) {
            blit(matrixStack, intScrollX + 142, controlStatY, 230, 206, 8, 8);
        }

        // breathing training bar
        pts = breathingTraining == HamonData.MAX_BREATHING_LEVEL ? 1.0F : breathingTraining - (int)breathingTraining;
        blit(matrixStack, intScrollX + 154, breathingStatY + 1, 203, 245, (int) (50 * pts), 5);
        blit(matrixStack, intScrollX + 153, breathingStatY, 202, 228, 52, 7);
        // bonus icon
        if (screen.hamon.getTrainingBonus() > 0) {
            blit(matrixStack, intScrollX + 200, breathingStatY - 9, 230, 216, 8, 8);
        }

        // exercise bars
        drawExerciseBar(this, matrixStack, intScrollX + 15, exercises1Y, screen.hamon, Exercise.MINING, 1.0F, true);
        drawExerciseBar(this, matrixStack, intScrollX + 111, exercises1Y, screen.hamon, Exercise.RUNNING, 1.0F, true);
        drawExerciseBar(this, matrixStack, intScrollX + 15, exercises2Y, screen.hamon, Exercise.SWIMMING, 1.0F, true);
        drawExerciseBar(this, matrixStack, intScrollX + 111, exercises2Y, screen.hamon, Exercise.MEDITATION, 1.0F, true);
        if (screen.mouseInsideWindow(
                mouseX + screen.windowPosX() + HamonScreen.WINDOW_THIN_BORDER, 
                mouseY + screen.windowPosY() + HamonScreen.WINDOW_UPPER_BORDER)
                && mouseAtMeditationBar(mouseX, mouseY)) {
            ClientUtil.fillSingleRect(intScrollX + 112, exercises2Y + 1, 90, 5, 255, 255, 255, 79);
        }

        // total exercises bar
        int length = (int) (200 * screen.hamon.getAverageExercisePoints());
        blit(matrixStack, intScrollX + 5, exercisesAvgY + 1, 1, 235, length, 5);
        blit(matrixStack, intScrollX + 5 + length, exercisesAvgY + 1, 1 + length, 240, 200 - length, 5);
        blit(matrixStack, intScrollX + 4, exercisesAvgY, 0, 228, 202, 7);
        
        matrixStack.pushPose();
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        if (screen.hamon.getAverageExercisePoints() < 1.0F) {
            RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        }
        blit(matrixStack, (intScrollX + 198) * 2, (exercisesAvgY - 1) * 2, 230, 188, 16, 16);
        matrixStack.popPose();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.disableBlend();
    }
    
    public static void drawExerciseBar(AbstractGui gui, MatrixStack matrixStack, int x, int y, HamonData hamon, 
            Exercise exercise, float alpha, boolean renderShadowCheckmark) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        int ticks = hamon.getExerciseTicks(exercise);
        int ticksMax = exercise.getMaxTicks(hamon);
        gui.blit(matrixStack, x + 1, y + 1, 93, 250, 90 * ticks / ticksMax, 5);
        gui.blit(matrixStack, x, y, 0, 249, 92, 7);
        
        matrixStack.pushPose();
        matrixStack.scale(0.5F, 0.5F, 0.5F);

        gui.blit(matrixStack, (x - 3) * 2, (y - 1) * 2, 230, 124 + exercise.ordinal() * 16, 16, 16);
        
        if (renderShadowCheckmark || ticks >= ticksMax) {
            if (ticks < ticksMax) {
                RenderSystem.color4f(0.0F, 0.0F, 0.0F, alpha);
            }
            gui.blit(matrixStack, (x + 85) * 2, (y - 1) * 2, 230, 188, 16, 16);
        }
        
        matrixStack.popPose();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void drawText(MatrixStack matrixStack) {
        int textX = intScrollX + 5;
        int textY = intScrollY + 6;
        strengthStatY = textY;
        drawString(matrixStack, minecraft.font, new TranslationTextComponent("hamon.strength_level", screen.hamon.getHamonStrengthLevel(), HamonData.MAX_STAT_LEVEL), textX - 3, textY, 0xFFFFFF);
        textY += 2;
        for (int i = 0; i < strengthDescLines.size(); i++) {
            textY += minecraft.font.lineHeight;
            minecraft.font.draw(matrixStack, strengthDescLines.get(i), (float) textX, (float) textY, 0xFFFFFF);
        }
        textY += 15;
        controlStatY = textY;
        drawString(matrixStack, minecraft.font, new TranslationTextComponent("hamon.control_level", screen.hamon.getHamonControlLevel(), HamonData.MAX_STAT_LEVEL), textX - 3, textY, 0xFFFFFF);
        textY += 2;
        for (int i = 0; i < controlDescLines.size(); i++) {
            textY += minecraft.font.lineHeight;
            minecraft.font.draw(matrixStack, controlDescLines.get(i), (float) textX, (float) textY, 0xFFFFFF);
        }
        textY += 15;
        breathingStatY = textY;
        drawString(matrixStack, minecraft.font, new TranslationTextComponent("hamon.breathing_level", (int) screen.hamon.getBreathingLevel(), (int) HamonData.MAX_BREATHING_LEVEL), textX - 3, textY, 0xFFFFFF);
        textY += 2;
        for (int i = 0; i < breathingDescLines.size(); i++) {
            textY += minecraft.font.lineHeight;
            minecraft.font.draw(matrixStack, breathingDescLines.get(i), (float) textX, (float) textY, 0xFFFFFF);
        }
        textY += 11;
        exercises1Y = textY;
        AbstractGui.drawCenteredString(matrixStack, minecraft.font, new TranslationTextComponent("hamon.mining_exercise"), intScrollX + 60, textY, 0xFFFFFF);
        AbstractGui.drawCenteredString(matrixStack, minecraft.font, new TranslationTextComponent("hamon.running_exercise"), intScrollX + 156, textY, 0xFFFFFF);
        textY += 9;
        exercises2Y = textY;
        AbstractGui.drawCenteredString(matrixStack, minecraft.font, new TranslationTextComponent("hamon.swimming_exercise"), intScrollX + 60, textY, 0xFFFFFF);
        AbstractGui.drawCenteredString(matrixStack, minecraft.font, new TranslationTextComponent("hamon.meditation"), intScrollX + 156, textY, 0xFFFFFF);
        textY += 11;
        exercisesAvgY = textY;
        for (int i = 0; i < exercisesDescLines.size(); i++) {
            textY += minecraft.font.lineHeight;
            minecraft.font.draw(matrixStack, exercisesDescLines.get(i), (float) textX, (float) textY, 0xFFFFFF);
        }
        maxY = textY + 39 - intScrollY;
        abandonTrainingButton.setY(screen.windowPosY() + textY + 30 - intScrollY);
    }

    @Override
    void drawTab(MatrixStack matrixStack, int windowX, int windowY, boolean isSelected, boolean red) {
        super.drawTab(matrixStack, windowX, windowY, isSelected, red);
        
        int iconX = windowX - 32 + 12;
        int iconY = windowY + getTabY() + 6;
        minecraft.getTextureManager().bind(HamonSkillsTabGui.HAMON_SKILLS);
        float barRatio = (float) screen.hamon.getHamonStrengthLevel() / (float) HamonData.MAX_STAT_LEVEL;
        blit(matrixStack, iconX + 3, iconY, MathHelper.floor(barRatio * 11F), 16, 229, 0, 22, 32, 256, 256);
        barRatio = (float) screen.hamon.getHamonControlLevel() / (float) HamonData.MAX_STAT_LEVEL;
        blit(matrixStack, iconX + 3, iconY, MathHelper.floor(barRatio * 11F), 16, 229, 32, 22, 32, 256, 256);
        barRatio = screen.hamon.getBreathingLevel() / HamonData.MAX_BREATHING_LEVEL;
        blit(matrixStack, iconX + 3, iconY, MathHelper.floor(barRatio * 11F), 16, 229, 64, 22, 32, 256, 256);
        blit(matrixStack, iconX, iconY, 16, 16, 192, 0, 32, 32, 256, 256);
    }

    @Override
    boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseAtMeditationBar((int) mouseX, (int) mouseY)) {
            PacketManager.sendToServer(new ClHamonMeditationPacket(!screen.hamon.isMeditating()));
            screen.onClose();
        }
        return false;
    }

    @Override
    boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    @Override
    void drawToolTips(MatrixStack matrixStack, int mouseX, int mouseY, int windowPosX, int windowPosY) {
        if (mouseX >= 142 && mouseX <= 149) {
            int y = strengthStatY;
            if (hamonStrengthLimited && mouseY >= y && mouseY <= y + 7) {
                screen.renderTooltip(matrixStack, statLimitTooltip, mouseX, mouseY);
            }
            else {
                y = controlStatY;
                if (hamonControlLimited && mouseY >= y && mouseY <= y + 7) {
                    screen.renderTooltip(matrixStack, statLimitTooltip, mouseX, mouseY);
                }
            }
        }
        else if (mouseX >= 153 && mouseX <= 203) {
            int y = strengthStatY;
            if (mouseY >= y && mouseY <= y + 6) {
                int level = screen.hamon.getHamonStrengthLevel();
                if (level == HamonData.MAX_STAT_LEVEL) {
                    screen.renderTooltip(matrixStack, new TranslationTextComponent("hamon.max_level"), mouseX, mouseY);
                }
                else {
                    int ptsAtLvl = HamonData.pointsAtLevel(level);
                    int pts = screen.hamon.getHamonStrengthPoints() - ptsAtLvl;
                    int ptsTotal = HamonData.pointsAtLevel(level + 1) - ptsAtLvl;
                    screen.renderTooltip(matrixStack, new StringTextComponent(pts + "/" + ptsTotal), mouseX, mouseY);
                }
            }
            else {
                y = controlStatY;
                if (mouseY >= y && mouseY <= y + 6) {
                    int level = screen.hamon.getHamonControlLevel();
                    if (level == HamonData.MAX_STAT_LEVEL) {
                        screen.renderTooltip(matrixStack, new TranslationTextComponent("hamon.max_level"), mouseX, mouseY);
                    }
                    else {
                        int ptsAtLvl = HamonData.pointsAtLevel(level);
                        int pts = screen.hamon.getHamonControlPoints() - ptsAtLvl;
                        int ptsTotal = HamonData.pointsAtLevel(level + 1) - ptsAtLvl;
                        screen.renderTooltip(matrixStack, new StringTextComponent(pts + "/" + ptsTotal), mouseX, mouseY);
                    }
                }
            }
        }

        DecimalFormat format = new DecimalFormat("#.##");
        float bonus = screen.hamon.multiplyPositiveBreathingTraining(screen.hamon.getTrainingBonus());
        if (bonus > 0 && 
                mouseX >= 200 && mouseX <= 207 && 
                mouseY >= breathingStatY - 9 && mouseY <= breathingStatY - 2) {
            screen.renderTooltip(matrixStack, minecraft.font.split(new TranslationTextComponent(
                    "hamon.training_bonus", format.format(bonus)), 100), mouseX, mouseY);
        }
        
        else if (mouseX >= 199 && mouseX < 207 && mouseY >= exercisesAvgY && mouseY < exercisesAvgY + 8) {
            IFormattableTextComponent tooltip = new TranslationTextComponent(
                    "hamon.exercise.full_completion_buff", PERCENTAGE_FORMAT.format((HamonData.ALL_EXERCISES_EFFICIENCY_MULTIPLIER - 1F) * 100F));
            if (!screen.hamon.allExercisesCompleted()) {
                tooltip = new TranslationTextComponent("hamon.exercise.completion_buff_hint", tooltip).withStyle(TextFormatting.ITALIC);
            }
            screen.renderTooltip(matrixStack, minecraft.font.split(tooltip, 100), mouseX, mouseY);
        }
        
        else {
            for (HamonData.Exercise exercise : HamonData.Exercise.values()) {
                int x = intScrollX + 100 + exercise.ordinal() % 2 * 96;
                int y = (exercise.ordinal() < 2 ? exercises1Y : exercises2Y) - 1;
                if (mouseX >= x && mouseX < x + 8 && mouseY >= y && mouseY < y + 8) {
                    screen.renderTooltip(matrixStack, minecraft.font.split(completedExerciseTooltip(exercise), 100), mouseX, mouseY);
                    return;
                }
            }
            
            if (mouseAtMeditationBar(mouseX, mouseY)) {
                screen.renderTooltip(matrixStack, meditationTooltip, mouseX, mouseY);
            }
        }
    }
    
    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#.#");
    
    private ITextComponent completedExerciseTooltip(Exercise exercise) {
        String key = "hamon.exercise." + exercise.name().toLowerCase() + ".completion_buff";
        Object[] args = {};
        switch (exercise) {
        case MINING:
            args = new Object[] { PERCENTAGE_FORMAT.format(HamonData.MINING_COMPLETED.getAmount() * 100) };
            break;
        case RUNNING:
            args = new Object[] { PERCENTAGE_FORMAT.format(HamonData.RUNNING_COMPLETED.getAmount() * 100) };
            break;
        case SWIMMING:
            args = new Object[] { PERCENTAGE_FORMAT.format((1 / HamonData.SWIMMING_COMPLETED_BREATH_STABILITY_TIME_MULTIPLIER - 1) * 100) }; 
            break;
        case MEDITATION:
            args = new Object[] { PERCENTAGE_FORMAT.format(HamonData.MEDITATION_COMPLETED_ENERGY_REGEN_TIME_REDUCTION / 20) }; 
            break;
        }
        
        IFormattableTextComponent tooltip = new TranslationTextComponent(key, args);
        if (!screen.hamon.isExerciseComplete(exercise)) {
            tooltip = new TranslationTextComponent("hamon.exercise.completion_buff_hint", tooltip).withStyle(TextFormatting.ITALIC);
        }
        
        return tooltip;
    }
    
    private boolean mouseAtMeditationBar(int mouseX, int mouseY) {
        return mouseX > 108 && mouseX <= 203 && 
                mouseY > exercises2Y && mouseY <= exercises2Y + 7;
    }

    @Override
    void updateTab() {}
}
