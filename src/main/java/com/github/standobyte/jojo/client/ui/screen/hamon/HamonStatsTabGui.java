package com.github.standobyte.jojo.client.ui.screen.hamon;

import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_HEIGHT;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_THIN_BORDER;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_UPPER_BORDER;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_WIDTH;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonMeditationPacket;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData.Exercise;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;

@SuppressWarnings("deprecation")
public class HamonStatsTabGui extends HamonTabGui {
    private final List<IReorderingProcessor> strengthDescLines;
    private final List<IReorderingProcessor> controlDescLines;
    private final List<IReorderingProcessor> breathingDescLines;
    private final List<IReorderingProcessor> exercisesDescLines;
    private final IFormattableTextComponent breathMaskHoverable;
    private final List<IReorderingProcessor> breathingDeteriorationLines;
    private final List<IReorderingProcessor> breathingStatGapLines;
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

    HamonStatsTabGui(Minecraft minecraft, HamonScreen screen, String title) {
        super(minecraft, screen, title, -1, 1);
        int textWidth = WINDOW_WIDTH - 30;
        strengthDescLines = minecraft.font.split(new TranslationTextComponent("hamon.strength_stat.desc"), textWidth);
        controlDescLines = minecraft.font.split(new TranslationTextComponent("hamon.control_stat.desc"), textWidth);
        breathingDescLines = minecraft.font.split(new TranslationTextComponent("hamon.breathing_stat.desc"), textWidth);
        breathMaskHoverable = new TranslationTextComponent("hamon.breathing_stat.desc2.mask")
                .withStyle(TextFormatting.UNDERLINE)
                .withStyle(style -> {
                    ItemStack item = new ItemStack(ModItems.BREATH_CONTROL_MASK.get());
                    item.enchant(Enchantments.BINDING_CURSE, 1);
                    return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemHover(item)));
                });
        exercisesDescLines = minecraft.font.split(new TranslationTextComponent("hamon.breathing_stat.desc2", breathMaskHoverable), textWidth);
        breathingDeteriorationLines = minecraft.font.split(new TranslationTextComponent("hamon.breathing_stat.desc3"), textWidth);
        breathingStatGapLines = minecraft.font.split(new TranslationTextComponent("hamon.breathing_stat.desc4", JojoModConfig.getCommonConfigInstance(true).breathingStatGap.get()), textWidth);
        statLimitTooltip = minecraft.font.split(new TranslationTextComponent("hamon.stat_limited"), 150);
        meditationTooltip = Streams.concat(
                minecraft.font.split(new TranslationTextComponent("hamon.meditation_button", new KeybindTextComponent("key.sneak"), new KeybindTextComponent("jojo.key.hamon_skills_window")),
                        150).stream(),
                minecraft.font.split(new TranslationTextComponent("hamon.meditation_button.stability_hint").withStyle(TextFormatting.GRAY, TextFormatting.ITALIC),
                        150).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void addButtons() {
        addButton(abandonTrainingButton = new HamonScreenButton(screen.windowPosX() + 13, screen.windowPosY() + 999, 204, 20, 
                new TranslationTextComponent("hamon.abandon.tab"), button -> {
                    screen.abandonTrainingTab.setPrevTab(this);
                    screen.selectTab(screen.abandonTrainingTab);
                }));
    }

    @Override
    protected void drawActualContents(HamonScreen screen, MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
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
        blit(matrixStack, intScrollX + 154, strengthStatY + 1, 203, 234, (int) (50 * pts), 5);
        blit(matrixStack, intScrollX + 153, strengthStatY, 202, 227, 52, 7);
        if (hamonStrengthLimited = level < HamonData.MAX_STAT_LEVEL
                && level >= (int) breathingTraining + JojoModConfig.getCommonConfigInstance(true).breathingStatGap.get()) {
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
        blit(matrixStack, intScrollX + 154, controlStatY + 1, 203, 239, (int) (50 * pts), 5);
        blit(matrixStack, intScrollX + 153, controlStatY, 202, 227, 52, 7);
        if (hamonControlLimited = level < HamonData.MAX_STAT_LEVEL
                && level >= (int) breathingTraining + JojoModConfig.getCommonConfigInstance(true).breathingStatGap.get()) {
            blit(matrixStack, intScrollX + 142, controlStatY, 230, 206, 8, 8);
        }

        // breathing training stat bar
        pts = breathingTraining == HamonData.MAX_BREATHING_LEVEL ? 1.0F : breathingTraining - (int)breathingTraining;
        blit(matrixStack, intScrollX + 154, breathingStatY + 1, 203, 244, (int) (50 * pts), 5);
        blit(matrixStack, intScrollX + 153, breathingStatY, 202, 227, 52, 7);

        // exercise bars
        drawExerciseBar(this, matrixStack, intScrollX + 15, exercises1Y, screen.hamon, Exercise.MINING, 1.0F, true);
        drawExerciseBar(this, matrixStack, intScrollX + 111, exercises1Y, screen.hamon, Exercise.RUNNING, 1.0F, true);
        drawExerciseBar(this, matrixStack, intScrollX + 15, exercises2Y, screen.hamon, Exercise.SWIMMING, 1.0F, true);
        drawExerciseBar(this, matrixStack, intScrollX + 111, exercises2Y, screen.hamon, Exercise.MEDITATION, 1.0F, true);
        if (screen.mouseInsideWindow(
                mouseX + screen.windowPosX() + WINDOW_THIN_BORDER, 
                mouseY + screen.windowPosY() + WINDOW_UPPER_BORDER)
                && mouseAtMeditationBar(mouseX, mouseY)) {
            ClientUtil.fillSingleRect(intScrollX + 112, exercises2Y + 1, 90, 5, 255, 255, 255, 79);
        }

        // total exercises bar
        int exercisedCompleted = screen.hamon.getCompleteExercisesCount();
        float maxIncompleteExercise = screen.hamon.getMaxIncompleteExercise();
        int length = 48 * exercisedCompleted;
        blit(matrixStack, intScrollX + 13,          exercisesAvgY + 1,  1,          234,  length,       5);
        int length2 = (int) (48 * maxIncompleteExercise);
        blit(matrixStack, intScrollX + 13 + length, exercisesAvgY + 1,  1 + length, 239,  length2,      5);
        length += length2;
        blit(matrixStack, intScrollX + 13 + length, exercisesAvgY + 1,  1 + length, 244,  192 - length, 5);
        blit(matrixStack, intScrollX + 12,          exercisesAvgY,      0,          227,  194,          7);
        // bonus icon
        if (screen.hamon.getTrainingBonus(false) > 0) {
            boolean bonusWillAddUp = screen.hamon.getBreathingIncrease(minecraft.player, false) > 0;
            blit(matrixStack, intScrollX + 3, exercisesAvgY - 1, bonusWillAddUp ? 230 : 239, 216, 8, 8);
        }
        
        // all exercises checkmark
        matrixStack.pushPose();
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        if (!screen.hamon.allExercisesCompleted()) {
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
        
        textY += 4;
        if (JojoModConfig.getCommonConfigInstance(true).breathingTrainingDeterioration.get()) {
            for (int i = 0; i < this.breathingDeteriorationLines.size(); i++) {
                textY += minecraft.font.lineHeight;
                minecraft.font.draw(matrixStack, breathingDeteriorationLines.get(i), (float) textX, (float) textY, 0xFFFFFF);
            }
        }
        for (int i = 0; i < this.breathingStatGapLines.size(); i++) {
            textY += minecraft.font.lineHeight;
            minecraft.font.draw(matrixStack, breathingStatGapLines.get(i), (float) textX, (float) textY, 0xFFFFFF);
        }
        
        setMaxY(textY + 39 - intScrollY);
        abandonTrainingButton.getWidgetExtension().setY(screen.windowPosY() + textY + 30 - intScrollY);
    }

    @Override
    void drawIcon(MatrixStack matrixStack, int windowX, int windowY, ItemRenderer itemRenderer) {
//        int iconX = windowX - 32 + 12;
//        int iconY = windowY + getTabY() + 6;
        int iconX = tabPositioning.getIconX(windowX, index, WINDOW_WIDTH);
        int iconY = tabPositioning.getIconY(windowY, index, WINDOW_HEIGHT);
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
    boolean mouseClicked(double mouseX, double mouseY, int mouseButton, boolean mouseInsideWindow) {
        if (mouseInsideWindow && mouseAtMeditationBar((int) mouseX, (int) mouseY)) {
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

        float breathingIncrease = screen.hamon.getBreathingIncrease(minecraft.player, false);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        float breathingBonus = screen.hamon.getTrainingBonus(true);
        IFormattableTextComponent bonusTooltip = new TranslationTextComponent(
                "hamon.training_bonus", decimalFormat.format(breathingBonus));
        boolean bonusWillAddUp = breathingIncrease > 0;
        if (!bonusWillAddUp) {
            bonusTooltip.withStyle(TextFormatting.DARK_GRAY, TextFormatting.ITALIC);
        }
        if (breathingBonus > 0 && 
                mouseX >= 3 && mouseX <= 11 && 
                mouseY >= exercisesAvgY && mouseY <= exercisesAvgY + 7) {
            screen.renderTooltip(matrixStack, minecraft.font.split(bonusTooltip, 150), mouseX, mouseY);
        }
        
        else if (mouseX >= 199 && mouseX < 207 && mouseY > exercisesAvgY && mouseY < exercisesAvgY + 8) {
            screen.renderTooltip(matrixStack, completedExerciseTooltip(null), mouseX, mouseY);
        }
        
        else if (mouseX >= 12 && mouseX < 207 && mouseY > exercisesAvgY && mouseY < exercisesAvgY + 8) {
            ITextComponent totalExercises1 = new TranslationTextComponent("hamon.exercise.all.count", 
                    screen.hamon.getCompleteExercisesCount(), Exercise.values().length);
            
            ITextComponent totalExercises2 = null;
            if (breathingIncrease > 0) {
                ITextComponent incWord = new TranslationTextComponent("hamon.exercise.all.tooltip_green").withStyle(TextFormatting.GREEN);
                if (breathingBonus > 0) {
                    totalExercises2 = new TranslationTextComponent("hamon.exercise.all.day_end_increase.bonus", 
                            incWord, decimalFormat.format(breathingIncrease - breathingBonus), decimalFormat.format(breathingBonus));
                }
                else {
                    totalExercises2 = new TranslationTextComponent("hamon.exercise.all.day_end_increase", 
                            incWord, decimalFormat.format(breathingIncrease));
                }
            }
            else if (breathingIncrease < 0) {
                totalExercises2 = new TranslationTextComponent("hamon.exercise.all.day_end_decrease", 
                        new TranslationTextComponent("hamon.exercise.all.tooltip_red").withStyle(TextFormatting.RED), decimalFormat.format(-breathingIncrease));
            }
            
            List<IReorderingProcessor> totalExercisesTooltip = new ArrayList<>(minecraft.font.split(totalExercises1, 120));
            if (totalExercises2 != null) {
                totalExercisesTooltip.add(StringTextComponent.EMPTY.getVisualOrderText());
                totalExercisesTooltip.addAll(minecraft.font.split(totalExercises2, 120));
            }
            screen.renderTooltip(matrixStack, totalExercisesTooltip, mouseX, mouseY);
        }
        
        else {
            for (HamonData.Exercise exercise : HamonData.Exercise.values()) {
                int x = intScrollX + 100 + exercise.ordinal() % 2 * 96;
                int y = (exercise.ordinal() < 2 ? exercises1Y : exercises2Y) - 1;
                if (mouseX >= x && mouseX < x + 8 && mouseY >= y && mouseY < y + 8) {
                    screen.renderTooltip(matrixStack, completedExerciseTooltip(exercise), mouseX, mouseY);
                    return;
                }
            }
            
            if (mouseAtMeditationBar(mouseX, mouseY)) {
                screen.renderTooltip(matrixStack, meditationTooltip, mouseX, mouseY);
            }
        }
        
        int exercisesDescLine = (mouseY - exercisesAvgY) / minecraft.font.lineHeight - 1;
        boolean maskNameTooltip = false;
        if (exercisesDescLine >= 0 && exercisesDescLine < exercisesDescLines.size()) {
            Style style = minecraft.font.getSplitter().componentStyleAtWidth(exercisesDescLines.get(exercisesDescLine), mouseX - WINDOW_THIN_BORDER);
            if (style != null && style.getHoverEvent() != null) {
                screen.renderComponentHoverEffect(matrixStack, style, mouseX, mouseY);
                maskNameTooltip = true;
            }
        }
        breathMaskHoverable.getStyle().setUnderlined(!maskNameTooltip);
    }
    
    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#.#");
    
    private List<IReorderingProcessor> completedExerciseTooltip(
            @Nullable Exercise exercise /*null signifies the tooltip about the bonus for completing all 4 exercises */) {
        String tooltip1Key;
        boolean hasBuff;
        List<IFormattableTextComponent> tooltip2 = new ArrayList<>();
        
        if (exercise != null) {
            tooltip1Key = "hamon.exercise.completion_buff_hint";
            hasBuff = screen.hamon.isExerciseComplete(exercise);
            tooltip2.add(new TranslationTextComponent(String.format("hamon.exercise.%s.completion_buff", exercise.name().toLowerCase()), 
                    PERCENTAGE_FORMAT.format(exercise.getBuffPercentage()), 
                    new TranslationTextComponent("hamon.exercise.completion_buff_hint2")));
        }
        
        else {
            tooltip1Key = "hamon.exercise.full_completion_hint";
            hasBuff = screen.hamon.allExercisesCompleted();
            tooltip2.add(new TranslationTextComponent("hamon.exercise.full_completion_buff", 
                    PERCENTAGE_FORMAT.format((HamonData.ALL_EXERCISES_EFFICIENCY_MULTIPLIER - 1F) * 100F), 
                    new TranslationTextComponent("hamon.exercise.completion_buff_hint2")));
            Collections.addAll(tooltip2,
                    new StringTextComponent(" "),
                    new TranslationTextComponent("hamon.exercise.full_completion_hint3"));
        }

        List<IReorderingProcessor> tooltip = new ArrayList<>();
        if (hasBuff) {
            tooltip2.forEach(text -> {
                tooltip.addAll(minecraft.font.split(text, 150));
            });
        }
        else {
            tooltip.addAll(minecraft.font.split(new TranslationTextComponent(tooltip1Key), 150));
            tooltip2.forEach(text -> {
                text.withStyle(TextFormatting.GRAY, TextFormatting.ITALIC);
                tooltip.addAll(minecraft.font.split(text, 150));
            });
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
