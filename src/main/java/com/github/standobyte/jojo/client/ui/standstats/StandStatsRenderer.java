package com.github.standobyte.jojo.client.ui.standstats;

import java.util.Arrays;
import java.util.List;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class StandStatsRenderer {
    private static final int HEXAGON_EXPAND_TICKS = 20;
    public static final ResourceLocation STAND_STATS_UI = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/stand_stats.png");
    private static final ResourceLocation STAND_STATS_BG = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/stand_stats_bg.png");
    
    @SuppressWarnings("deprecation")
    public static void renderStandStats(MatrixStack matrixStack, Minecraft mc, 
            int x, int y, int screenWidth, int screenHeight, 
            int tick, float partialTick, 
            float alpha, 
            int mouseX, int mouseY, int maxTextWidth) {
        IStandPower.getStandPowerOptional(mc.player).ifPresent(power -> {
            if (power.hasPower()) {

                StandStats stats = power.getType().getStats();
                float statLeveling = power.getStatsDevelopment();
                
                int color = StandSkinsManager.getUiColor(power);
                int[] rgb = ClientUtil.rgbInt(color);

                double[] statVal = new double[6];
                statVal[0] = stats.getBasePower() + stats.getDevPower(statLeveling);
                if (statVal[0] > 0) statVal[0] = (statVal[0] + 1) / 3;

                statVal[1] = stats.getBaseAttackSpeed() + stats.getDevAttackSpeed(statLeveling);
                if (statVal[1] > 0) statVal[1] = (statVal[1] + 1) / 3;

                statVal[2] = stats.getEffectiveRange() + (stats.getMaxRange() - stats.getEffectiveRange()) * 0.5;
                if (statVal[2] > 0) statVal[2] = Math.log(statVal[2] / 1.5) / Math.log(2) + 1; // log2(val / 1.5)

                statVal[3] = stats.getBaseDurability() + stats.getDevDurability(statLeveling);
                if (statVal[3] > 0) statVal[3] = (statVal[3] + 1) / 3;

                statVal[4] = stats.getBasePrecision() + stats.getDevPrecision(statLeveling);
                if (statVal[4] > 0) statVal[4] = (statVal[4] + 1) / 3;

                if (statLeveling < 1) {
                    statVal[5] = 1 + (1 - statLeveling) * power.getMaxResolveLevel();
                }
                else if (power.hasUnlockedMatching(action -> action.isTrained() && power.getLearningProgressRatio(action) < 1)) {
                    statVal[5] = 1;
                }

                String[] statRank = new String[6];

                for (int i = 0; i < statVal.length; i++) {
                    String rank;
                         if (statVal[i] >= 5) rank = "A"; // 14+
                    else if (statVal[i] >= 4) rank = "B"; // 11-14
                    else if (statVal[i] >= 3) rank = "C"; // 8-11
                    else if (statVal[i] >= 2) rank = "D"; // 5-8
                    else if (statVal[i] > 0)  rank = "E"; // 0-5
                    else                      rank = "∅"; // 0
                    statRank[i] = rank;

                    statVal[i] = statVal[i] <= 1 ? statVal[i] * 4 : 4 + (statVal[i] - 1) * 3;

                    if (statVal[i] > 0) {
                        statVal[i] = Math.min(statVal[i] + 1, 20);
                    }

                    if (tick < HEXAGON_EXPAND_TICKS) {
                        statVal[i] *= ((double) tick + (double) partialTick) / (double) HEXAGON_EXPAND_TICKS;
                    }
                    statVal[i] *= 3;
                }

                float xCenter = x + 76.5F;
                float yCenter = y + 76.5F;

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                mc.textureManager.bind(STAND_STATS_BG);
                AbstractGui.blit(matrixStack, x, y, 0, 0, 0, 153, 153, 256, 256);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                mc.textureManager.bind(STAND_STATS_UI);
                AbstractGui.blit(matrixStack, x, y, 0, 0, 0, 153, 153, 256, 256);

                fillHexagon(xCenter, yCenter, 
                        statVal[0], statVal[1], statVal[2], 
                        statVal[3], statVal[4], statVal[5], 
                        rgb[0], rgb[1], rgb[2], 192);
                RenderSystem.disableBlend();
                

                List<IReorderingProcessor> standName = mc.font.split(new TranslationTextComponent("jojo.stand_stat.stand_name", power.getName()), maxTextWidth);
                List<IReorderingProcessor> standUser = mc.font.split(new TranslationTextComponent("jojo.stand_stat.stand_user", mc.player.getDisplayName()), maxTextWidth);
                int width = 0;
                if (standName.size() > 1 || standUser.size() > 1) {
                    width = maxTextWidth;
                }
                else {
                    if (!standName.isEmpty()) {
                        width = mc.font.width(standName.get(0));
                    }
                    if (!standUser.isEmpty()) {
                        width = Math.max(width, mc.font.width(standUser.get(0)));
                    }
                }

                int standUserY = y - 5 - Math.max(standUser.size(), 1) * 9;
                int userIconY = standUserY;
                if (standUser.size() <= 1) {
                    userIconY -= 5;
                }

                int standNameY = standUserY - 9 - Math.max(standName.size(), 1) * 9;
                int standIconY = standNameY;
                if (standName.size() <= 1) {
                    standIconY -= 5;
                }

                mc.getTextureManager().bind(power.getType().getIconTexture(power));
                AbstractGui.blit(matrixStack, x + 135 - width, standIconY, 0, 0, 16, 16, 16, 16);
                ClientUtil.drawLines(matrixStack, mc.font, standName, 
                        x + 153 - width, standNameY, 0, color, true, true);

                ResourceLocation playerFace = mc.player.getSkinTextureLocation();
                mc.getTextureManager().bind(playerFace);

                AbstractGui.blit(matrixStack, x + 135 - width, userIconY, 16, 16, 16, 16, 128, 128);
                if (mc.options.getModelParts().contains(PlayerModelPart.HAT)) {
                    matrixStack.pushPose();
                    matrixStack.translate(x + 135 - width, userIconY, 0);
                    matrixStack.scale(9F/8F, 9F/8F, 0);
                    matrixStack.translate(-1, -1, 0);
                    AbstractGui.blit(matrixStack, 0, 0, 80, 16, 16, 16, 128, 128);
                    matrixStack.popPose();
                }
                ClientUtil.drawLines(matrixStack, mc.font, standUser, 
                        x + 153 - width, standUserY, 0, color, true, true);

                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);


                for (int i = 0; i < statRank.length; i++) {
                    float statX = xCenter + STAT_RANK_NAME_OFFSETS[i][0];
                    float statY = yCenter + STAT_RANK_NAME_OFFSETS[i][1];
                    ITextComponent rank = new StringTextComponent(statRank[i]).withStyle(TextFormatting.BOLD);
                    ClientUtil.drawCenteredStringNoShadow(matrixStack, mc.font, rank, statX, statY, 0x000000);
                    int letterWidth = mc.font.width(rank);
                    if (mouseX >= statX - letterWidth / 2 && mouseX <= statX + letterWidth / 2 && mouseY >= statY && mouseY <= statY + mc.font.lineHeight) {
                        GuiUtils.drawHoveringText(matrixStack, Arrays.asList(new TranslationTextComponent(STAT_NAME_KEYS[i])), 
                                mouseX, mouseY, screenWidth, screenHeight, -1, mc.font);
                    }
                }
            }
        });
    }
    private static final String[] STAT_NAME_KEYS = {
            "jojo.stand_stat.strength",
            "jojo.stand_stat.speed",
            "jojo.stand_stat.range",
            "jojo.stand_stat.durability",
            "jojo.stand_stat.precision",
            "jojo.stand_stat.dev_potential",
    };
    private static final float[][] STAT_RANK_NAME_OFFSETS = {
            {0, -72}, 
            {58, -39}, 
            {58, 32}, 
            {0, 65}, 
            {-58, 32}, 
            {-58, -39}
    };
    

    /*
     *             r1
     *             |
     *    r6 \     |      / r2   
     *         \   |    /
     *           center
     *         /   |    \
     *    r5 /     |      \ r3   
     *             |
     *             r4
     */
    private static final double COS_PI_BY_6 = Math.sqrt(3.0) / 2.0;
    private static final double SIN_PI_BY_6 = 0.5;
    private static void fillHexagon(double xCenter, double yCenter, 
            double r1, double r2, double r3, double r4, double r5, double r6, 
            int red, int green, int blue, int alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuilder();

        /*
         *          {x1, y1}
         * 
         * {x6, y6}          {x2, y2}
         * 
         *          {x0, y0}
         * 
         * {x5, y5}          {x3, y3}
         * 
         *          {x4, y4}
         */
        double x1 = xCenter;                        double y1 = yCenter - r1;
        double x2 = xCenter + r2 * COS_PI_BY_6;     double y2 = yCenter - r2 * SIN_PI_BY_6;
        double x3 = xCenter + r3 * COS_PI_BY_6;     double y3 = yCenter + r3 * SIN_PI_BY_6;
        double x4 = xCenter;                        double y4 = yCenter + r4;
        double x5 = xCenter - r5 * COS_PI_BY_6;     double y5 = yCenter + r5 * SIN_PI_BY_6;
        double x6 = xCenter - r6 * COS_PI_BY_6;     double y6 = yCenter - r6 * SIN_PI_BY_6;
        bufferBuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(xCenter, yCenter, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x1, y1, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x6, y6, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x5, y5, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x4, y4, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x3, y3, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x2, y2, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x1, y1, 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().end();

        if (r1 > 0 && r6 <= 0 && r2 <= 0) {
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(x1 + 2,                    yCenter + 2,                0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x1 + 2,                    y1,                         0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter - 2,               y1,                         0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter - 2,               yCenter + 2,                0.0D).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().end();
        }
        if (r2 > 0 && r1 <= 0 && r3 <= 0) {
            bufferBuilder.begin(9, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(xCenter + 2 * SIN_PI_BY_6, yCenter + 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x2 + 2 * SIN_PI_BY_6,      y2 + 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x2 - 2 * SIN_PI_BY_6,      y2 - 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter - 2 * SIN_PI_BY_6, yCenter - 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().end();
        }
        if (r3 > 0 && r2 <= 0 && r4 <= 0) {
            bufferBuilder.begin(9, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(xCenter + 2 * SIN_PI_BY_6, yCenter - 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter - 2 * SIN_PI_BY_6, yCenter + 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x3 - 2 * SIN_PI_BY_6,      y3 + 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x3 + 2 * SIN_PI_BY_6,      y3 - 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().end();
        }
        if (r4 > 0 && r3 <= 0 && r5 <= 0) {
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(xCenter - 2,               yCenter - 2,                0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter - 2,               y4,                         0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x4 + 2,                    y4,                         0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x4 + 2,                    yCenter - 2,                0.0D).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().end();
        }
        if (r5 > 0 && r4 <= 0 && r6 <= 0) {
            bufferBuilder.begin(9, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(xCenter - 2 * SIN_PI_BY_6, yCenter - 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x5 - 2 * SIN_PI_BY_6,      y5 - 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x5 + 2 * SIN_PI_BY_6,      y5 + 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter + 2 * SIN_PI_BY_6, yCenter + 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().end();
        }
        if (r6 > 0 && r5 <= 0 && r1 <= 0) {
            bufferBuilder.begin(9, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(xCenter + 2 * SIN_PI_BY_6, yCenter - 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x6 + 2 * SIN_PI_BY_6,      y6 - 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x6 - 2 * SIN_PI_BY_6,      y6 + 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter - 2 * SIN_PI_BY_6, yCenter + 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().end();
        }

        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }
    
    
    
    
}
