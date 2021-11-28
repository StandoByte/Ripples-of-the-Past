package com.github.standobyte.jojo.client.ui.screen;

import java.util.List;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonStartMeditationPacket;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonData.Exercise;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonStatsTabGui extends HamonTabGui {
    private final List<IReorderingProcessor> strengthDescLines;
    private final List<IReorderingProcessor> controlDescLines;
    private final List<IReorderingProcessor> breathingDescLines;
    private final List<IReorderingProcessor> exercisesDescLines;
    private final List<IReorderingProcessor> statLimitTooltip;
    private final List<IReorderingProcessor> meditationButtonTooltip;

    private Button meditationButton;
    private boolean buttonYRecalculated = false;
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
        ITextComponent desc = JojoModConfig.COMMON.breathingTechniqueDeterioration.get() ? 
                new TranslationTextComponent("hamon.breathing_stat.desc", new TranslationTextComponent("hamon.breathing_stat.notice"))
                : new TranslationTextComponent("hamon.breathing_stat.desc");
        breathingDescLines = minecraft.font.split(desc, textWidth);
        exercisesDescLines = minecraft.font.split(new TranslationTextComponent("hamon.exercises_average"), textWidth);
        statLimitTooltip = minecraft.font.split(new TranslationTextComponent("hamon.stat_limited"), 150);
        meditationButtonTooltip = minecraft.font.split(new TranslationTextComponent("hamon.meditation_button", 
                new KeybindTextComponent("key.sneak"), new KeybindTextComponent("jojo.key.hamon_skills_window")), 100);
    }

    @Override
    void addButtons() {
        meditationButton = new Button(screen.windowPosX() + 213, screen.windowPosY(), 7, 7, new StringTextComponent(""), button -> {
            PacketManager.sendToServer(new ClHamonStartMeditationPacket());
            screen.onClose();
        });
        buttonY = meditationButton.y;
        screen.addButton(meditationButton);
    }
    
    @Override
    List<Widget> getButtons() {
        return ImmutableList.of(meditationButton);
    }

    @Override
    protected void drawActualContents(MatrixStack matrixStack) {
        minecraft.getTextureManager().bind(HamonScreen.WINDOW);
        float breathingTechnique = screen.hamon.getBreathingLevel();
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
        blit(matrixStack, intScrollX + 154, strengthStatY + 1, 200, 230, (int) (50 * pts), 5);
        blit(matrixStack, intScrollX + 153, strengthStatY, 0, 242, 52, 7);
        if (hamonStrengthLimited = level >= (int) breathingTechnique + HamonData.MIN_BREATHING_EXCEED) {
            blit(matrixStack, intScrollX + 142, strengthStatY, 230, 156, 8, 8);
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
        blit(matrixStack, intScrollX + 154, controlStatY + 1, 200, 235, (int) (50 * pts), 5);
        blit(matrixStack, intScrollX + 153, controlStatY, 0, 242, 52, 7);
        if (hamonControlLimited = level >= (int) breathingTechnique + HamonData.MIN_BREATHING_EXCEED) {
            blit(matrixStack, intScrollX + 142, controlStatY, 230, 156, 8, 8);
        }

        // breathing technique bar
        pts = breathingTechnique == HamonData.MAX_BREATHING_LEVEL ? 1.0F : breathingTechnique - (int)breathingTechnique;
        blit(matrixStack, intScrollX + 154, breathingStatY + 1, 200, 240, (int) (50 * pts), 5);
        blit(matrixStack, intScrollX + 153, breathingStatY, 0, 242, 52, 7);

        // exercise bars
        Exercise exercise = Exercise.MINING;
        blit(matrixStack, intScrollX + 16, exercises1Y + 1, 54, 249, 90 * screen.hamon.getExerciseTicks(exercise) / exercise.maxTicks, 5);
        blit(matrixStack, intScrollX + 15, exercises1Y, 53, 242, 92, 7);
        exercise = Exercise.RUNNING;
        blit(matrixStack, intScrollX + 112, exercises1Y + 1, 54, 249, 90 * screen.hamon.getExerciseTicks(exercise) / exercise.maxTicks, 5);
        blit(matrixStack, intScrollX + 111, exercises1Y, 53, 242, 92, 7);
        exercise = Exercise.SWIMMING;
        blit(matrixStack, intScrollX + 16, exercises2Y + 1, 54, 249, 90 * screen.hamon.getExerciseTicks(exercise) / exercise.maxTicks, 5);
        blit(matrixStack, intScrollX + 15, exercises2Y, 53, 242, 92, 7);
        exercise = Exercise.MEDITATION;
        blit(matrixStack, intScrollX + 112, exercises2Y + 1, 54, 249, 90 * screen.hamon.getExerciseTicks(exercise) / exercise.maxTicks, 5);
        blit(matrixStack, intScrollX + 111, exercises2Y, 53, 242, 92, 7);

        blit(matrixStack, intScrollX + 6, exercisesAvgY + 1, 1, 237, (int) (198 * screen.hamon.getAverageExercisePoints()), 5);
        blit(matrixStack, intScrollX + 5, exercisesAvgY, 0, 230, 199, 7);

        RenderSystem.disableBlend();
    }

    @Override
    protected void drawText(MatrixStack matrixStack) {
        int textX = intScrollX + 5;
        int textY = intScrollY + 6;
        strengthStatY = textY;
        drawString(matrixStack, minecraft.font, new TranslationTextComponent("hamon.strength_level", screen.hamon.getHamonStrengthLevel(), HamonData.MAX_STAT_LEVEL), textX - 3, textY, 0xFFFFFF);
        textY += 2;
        for (int i = 0; i < strengthDescLines.size(); i++) {
            textY += 9;
            minecraft.font.draw(matrixStack, strengthDescLines.get(i), (float) textX, (float) textY, 0xFFFFFF);
        }
        textY += 15;
        controlStatY = textY;
        drawString(matrixStack, minecraft.font, new TranslationTextComponent("hamon.control_level", screen.hamon.getHamonControlLevel(), HamonData.MAX_STAT_LEVEL), textX - 3, textY, 0xFFFFFF);
        textY += 2;
        for (int i = 0; i < controlDescLines.size(); i++) {
            textY += 9;
            minecraft.font.draw(matrixStack, controlDescLines.get(i), (float) textX, (float) textY, 0xFFFFFF);
        }
        textY += 15;
        breathingStatY = textY;
        drawString(matrixStack, minecraft.font, new TranslationTextComponent("hamon.breathing_level", (int) screen.hamon.getBreathingLevel(), (int) HamonData.MAX_BREATHING_LEVEL), textX - 3, textY, 0xFFFFFF);
        textY += 2;
        for (int i = 0; i < breathingDescLines.size(); i++) {
            textY += 9;
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
        if (!buttonYRecalculated) {
            buttonY += textY + HamonScreen.WINDOW_UPPER_BORDER - intScrollY;
            meditationButton.y = buttonY;
            buttonYRecalculated = true;
        }
        textY += 11;
        exercisesAvgY = textY;
        for (int i = 0; i < exercisesDescLines.size(); i++) {
            textY += 9;
            minecraft.font.draw(matrixStack, exercisesDescLines.get(i), (float) textX, (float) textY, 0xFFFFFF);
        }
        maxY = textY + 15 - intScrollY;
    }

    @Override
    void renderButtons(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        meditationButton.render(matrixStack, mouseX, mouseY, partialTick);
    }

    @Override
    boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    @Override
    boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    @Override
    void scroll(double xMovement, double yMovement) {
        super.scroll(xMovement, yMovement);
        meditationButton.y = buttonY + (int) scrollY;
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
        if (meditationButton.isMouseOver(mouseX + screen.windowPosX() + HamonScreen.WINDOW_THIN_BORDER, mouseY + screen.windowPosY() + HamonScreen.WINDOW_UPPER_BORDER)) {
            screen.renderTooltip(matrixStack, meditationButtonTooltip, mouseX, mouseY);
        }
    }

    @Override
    void updateTab() {}
}
