package com.github.standobyte.jojo.client.ui.standstats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.client.ui.BlitFloat;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class StandStatsRenderer {
    public static final ResourceLocation STAND_STATS_UI = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/stand_stats.png");
    

    /*
     * "A" - 14+
     * "B" - 11-14
     * "C" - 8-11
     * "D" - 5-8
     * "E" - 0-5
     * "∅" - 0
     */
    public static List<String> STAT_LETTERS = Util.make(new ArrayList<>(), list -> {
        Collections.addAll(list, "∅", "E", "D", "C", "B", "A", "A", "S");
    });
    public static final String REFERENCE_MARK = "\u203B";
    
    public static String getRankFromConvertedValue(double value) {
        int rankIndex;
             if (value >= 2) rankIndex = MathHelper.floor(value);
        else if (value > 0)  rankIndex = 1;
        else                      rankIndex = 0;
        return STAT_LETTERS.get(Math.min(rankIndex, STAT_LETTERS.size() - 1));
    }
    
    private static final double LN_2 = Math.log(2);
    public static enum StandStat {
        STRENGTH        (new TranslationTextComponent("jojo.stand_stat.strength"),       0,  -72) {
            @Override
            double getValueConverted(IStandPower standData, StandStats stats, float levelRatio) {
                double value = stats.getBasePower() + stats.getDevPower(levelRatio);
                if (value > 0) value = (value + 1) / 3;
                return value;
            }
        },
        SPEED           (new TranslationTextComponent("jojo.stand_stat.speed"),          58, -39) {
            @Override
            double getValueConverted(IStandPower standData, StandStats stats, float levelRatio) {
                double value = stats.getBaseAttackSpeed() + stats.getDevAttackSpeed(levelRatio);
                if (value > 0) value = (value + 1) / 3;
                return value;
            }
        },
        RANGE           (new TranslationTextComponent("jojo.stand_stat.range"),          58,  32) {
            @Override
            double getValueConverted(IStandPower standData, StandStats stats, float levelRatio) {
                double value = stats.getEffectiveRange() + (stats.getMaxRange() - stats.getEffectiveRange()) * 0.5;
                if (value > 0) value = Math.log(value / 1.5) / LN_2 /* or log2(val / 1.5) */ + 1; 
                return value;
            }
        },
        DURABILITY      (new TranslationTextComponent("jojo.stand_stat.durability"),     0,   65) {
            @Override
            double getValueConverted(IStandPower standData, StandStats stats, float levelRatio) {
                double value = stats.getBaseDurability() + stats.getDevDurability(levelRatio);
                if (value > 0) value = (value + 1) / 3;
                return value;
            }
        },
        PRECISION       (new TranslationTextComponent("jojo.stand_stat.precision"),     -58,  32) {
            @Override
            double getValueConverted(IStandPower standData, StandStats stats, float levelRatio) {
                double value = stats.getBasePrecision() + stats.getDevPrecision(levelRatio);
                if (value > 0) value = (value + 1) / 3;
                return value;
            }
        },
        DEV_POTENTIAL   (new TranslationTextComponent("jojo.stand_stat.dev_potential"), -58, -39) {
            @Override
            double getValueConverted(IStandPower standData, StandStats stats, float levelRatio) {
                double value = 0;
                if (levelRatio < 1) {
                    value = 1 + (1 - levelRatio) * standData.getMaxResolveLevel();
                }
                else if (standData.hasUnlockedMatching(action -> action.isTrained() && standData.getLearningProgressRatio(action) < 1)) {
                    value = 1;
                }
                return value;
            }
        };
        
        private final ITextComponent name;
        private final int x;
        private final int y;
        
        private StandStat(ITextComponent name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }
        
        abstract double getValueConverted(IStandPower standData, StandStats stats, float levelRatio);
    }
    
    
    private static final Map<ResourceLocation, ICosmeticStandStats> OVERRIDE_STAT = new HashMap<>();
    public static void overrideCosmeticStats(ResourceLocation standId, ICosmeticStandStats override) {
        OVERRIDE_STAT.put(standId, override);
    }
    
    public static interface ICosmeticStandStats {
        /*
         *  These methods can be used in conjunction to render different stats for something like Sub-Stands or Stand Acts:
         *  Create a few more instances of ICosmeticStandStats for each Sub-Stands/Act/etc. and keep them as final fields in the main instance.
         *  Each time the pause screen is opened, choose one of them randomly (or on specific conditions, 
         *  like having SHA summoned to render its stats) and set it to another (non-final) field.
         *  Then return that randomly chosen instance in getSubStandStats().
         */
        default void onPauseScreenOpened() {}
        
        @Nonnull
        default ICosmeticStandStats getSubStandStats() {
            return this;
        }
        
        
        default void preStatsRenderFrame(IStandPower standData, float partialTick) {}
        
        default double statConvertedValue(StandStat stat, IStandPower standData, StandStats stats, float statLeveling) {
            return stat.getValueConverted(standData, stats, statLeveling);
        }
        
        default String statRankLetter(StandStat stat, IStandPower standData, double statConvertedValue) {
            return getRankFromConvertedValue(statConvertedValue);
        }
        
        default ITextComponent standName(IStandPower standData) {
            return standData.getName();
        }
        
        default ResourceLocation standIcon(IStandPower standData) {
            return standData.getType().getIconTexture(standData);
        }
        
        default List<ITextComponent> statTooltip(StandStat stat, IStandPower standData) {
            List<ITextComponent> tooltip = new ArrayList<>();
            tooltip.add(stat.name);
            return tooltip;
        }
        
        
        public static final ICosmeticStandStats DEFAULT = new ICosmeticStandStats() {};
        public static ICosmeticStandStats getHandler(IStandPower standData) {
            ICosmeticStandStats handler = standData.hasPower() ? OVERRIDE_STAT.getOrDefault(standData.getType().getRegistryName(), DEFAULT) : DEFAULT;
            return handler;
        }
    }
    
    

    public static final int statsWidth = 163;
    public static final int statsHeight = 163;
    
    private static final int BORDERS_FADE_IN_END = 20;
    private static final int HEXAGON_TICK_START = 20;
    private static final int HEXAGON_EXPAND_TICKS = 30;
    private static final int LETTER_TICK_START = 50;
    private static final float LETTER_FADE_IN = 1.5f;
    
    @SuppressWarnings("deprecation")
    public static void renderStandStats(MatrixStack matrixStack, Minecraft mc, 
            int x, int y, int screenWidth, int screenHeight, 
            int tick, float partialTick, 
            float bgAlpha, boolean invertBnW, 
            int mouseX, int mouseY, int maxTextWidth) {
        IStandPower.getStandPowerOptional(mc.player).ifPresent(power -> {
            if (!power.hasPower()) return;

            matrixStack.pushPose();
            matrixStack.translate(0, 0, 1);
            
            StandStats stats = power.getType().getStats();
            float statLeveling = power.getStatsDevelopment();
            ICosmeticStandStats override = ICosmeticStandStats.getHandler(power).getSubStandStats();
            override.preStatsRenderFrame(power, partialTick);
            
            int color = StandSkinsManager.getUiColor(power);
            int[] rgb = ClientUtil.rgbInt(color);

            double[] statVal = new double[6];
            String[] statRank = new String[6];
            
            for (int i = 0; i < statVal.length; i++) {
                StandStat stat = StandStat.values()[i];
                statVal[i] = override.statConvertedValue(stat, power, stats, statLeveling);
                statRank[i] = override.statRankLetter(stat, power, statVal[i]);
            }

            float xCenter = x + statsWidth / 2f;
            float yCenter = y + statsHeight / 2f;
            
            float scale = tick < HEXAGON_TICK_START ? 0.75f + 0.25f * ((tick + partialTick) / HEXAGON_TICK_START) : 1;
            float bordersAlpha = tick < BORDERS_FADE_IN_END ? (tick + partialTick) / BORDERS_FADE_IN_END : 1;
            if (scale < 1) {
                matrixStack.translate(xCenter * (1 - scale), yCenter * (1 - scale), 0);
                matrixStack.scale(scale, scale, 1);
            }

            mc.textureManager.bind(STAND_STATS_UI);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // background
            if (invertBnW) RenderSystem.color4f(0, 0, 0, bgAlpha * bordersAlpha);
            else           RenderSystem.color4f(1, 1, 1, bgAlpha * bordersAlpha);
            AbstractGui.blit(matrixStack, x, y, 0, 256, 0, statsWidth, statsHeight, 512, 512);
            
            // circles
            if (invertBnW) RenderSystem.color4f(1, 1, 1, bordersAlpha);
            else           RenderSystem.color4f(0, 0, 0, bordersAlpha);
            AbstractGui.blit(matrixStack, x, y, 0, 0, 0, statsWidth, statsHeight, 512, 512);
            AbstractGui.blit(matrixStack, x, y, 0, 0, 164, statsWidth, statsHeight, 512, 512);
            AbstractGui.blit(matrixStack, x, y, 0, 256, 164, statsWidth, statsHeight, 512, 512);
            
            // rotating outer ring effect
            float outerRingRot = 0;
            float innerRingRot = 0;
            if (tick < LETTER_TICK_START) {
                outerRingRot = (tick + partialTick) * -6f;
                innerRingRot = (tick + partialTick) * 9f;
            }
            matrixStack.pushPose();
            matrixStack.translate(xCenter, yCenter, 0);
            
            matrixStack.pushPose();
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(outerRingRot));
            BlitFloat.blitFloat(matrixStack, -statsWidth / 2f, -statsHeight / 2f, 0, 0, 328, statsWidth, statsHeight, 512, 512);
            matrixStack.popPose();

            matrixStack.pushPose();
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(innerRingRot));
            BlitFloat.blitFloat(matrixStack, -statsWidth / 2f, -statsHeight / 2f, 0, 256, 328, statsWidth, statsHeight, 512, 512);
            matrixStack.popPose();
            
            // scale letters
            for (int i = 1; i < 6; i++) {
                if (STAT_LETTERS.size() <= i) break;
                String letter = STAT_LETTERS.get(i);
                mc.font.draw(matrixStack, new StringTextComponent(letter), 
                        3.5f, -18.5f - (i - 1) * 9f, 
                        ClientUtil.addAlpha(invertBnW ? 0xFFFFFF : 0x000000, bordersAlpha));
            }
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            
            RenderSystem.color4f(1, 1, 1, 1);
            
            matrixStack.popPose();
            
            // stats hexagon
            if (tick >= HEXAGON_TICK_START) {
                int tick_ = tick - HEXAGON_TICK_START;
                for (int i = 0; i < statVal.length; i++) {
                    statVal[i] = statVal[i] <= 1 ? statVal[i] * 4 : 4 + (statVal[i] - 1) * 3;

                    if (statVal[i] > 0) {
                        statVal[i] = Math.min(statVal[i] + 1, 20);
                    }
                    
                    if (tick_ < HEXAGON_EXPAND_TICKS) {
                        statVal[i] *= ((double) tick_ + (double) partialTick) / (double) HEXAGON_EXPAND_TICKS;
                    }
                    statVal[i] *= 3;
                }
                
                fillHexagon(xCenter, yCenter, 
                        statVal[0], statVal[1], statVal[2], 
                        statVal[3], statVal[4], statVal[5], 
                        rgb[0], rgb[1], rgb[2], 192);
                RenderSystem.disableBlend();
            }
            
            // stand name and user
            if (tick >= HEXAGON_TICK_START) {
                List<IReorderingProcessor> standName = mc.font.split(new TranslationTextComponent("jojo.stand_stat.stand_name", override.standName(power)), maxTextWidth);
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
    
                mc.getTextureManager().bind(override.standIcon(power));
                AbstractGui.blit(matrixStack, x + statsWidth - 18 - width, standIconY, 0, 0, 16, 16, 16, 16);
                ClientUtil.drawLines(matrixStack, mc.font, standName, 
                        x + statsWidth - width, standNameY, 0, color, true, true);
    
                ClientUtil.renderPlayerFace(matrixStack, x + statsWidth - 18 - width, userIconY, mc.player);
                
                ClientUtil.drawLines(matrixStack, mc.font, standUser, 
                        x + statsWidth - width, standUserY, 0, color, true, true);
    
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            }

            // rank letters on the outer ring
            float tick_ = (tick + partialTick)/ LETTER_FADE_IN;
            for (int i = 0; i < statRank.length; i++) {
                int letterStartTick = (int) (LETTER_TICK_START / LETTER_FADE_IN) + i;
                int letterTicks = STAT_LETTERS.size() - 2;
                int letterFullTick = letterStartTick + letterTicks;
                if (tick_ < letterStartTick) continue;
                
                StandStat stat = StandStat.values()[i];
                float statX = xCenter + stat.x;
                float statY = yCenter + stat.y;
                String statRankLetter = statRank[i];
                
                if (tick_ < letterFullTick) {
                    int letterIndex = "∞".equals(statRankLetter) ? STAT_LETTERS.size() : STAT_LETTERS.indexOf(statRankLetter);
                    if (letterIndex > 1) {
                        int index = letterIndex - (int) ((letterFullTick - tick_) * letterIndex / letterTicks);
                        if (index < STAT_LETTERS.size()) {
                            statRankLetter = STAT_LETTERS.get(Math.max(1, index));
                        }
                    }
                }
                float letterAlpha = tick_ >= letterFullTick ? 1 : 0.25f + 0.75f * 
                        (float) (tick_ - letterStartTick) / (float) (letterFullTick - letterStartTick);
                
                
                ITextComponent rank = new StringTextComponent(statRankLetter).withStyle(TextFormatting.BOLD);
                int letterWidth = mc.font.width(rank);
                
                switch (statRankLetter) {
                case "∅":
                    letterWidth = 9;
                    renderLetterFromTex(matrixStack, letterAlpha, invertBnW, statX, statY, letterWidth, 0, 504);
                    break;
                case "∞":
                    letterWidth = 9;
                    renderLetterFromTex(matrixStack, letterAlpha, invertBnW, statX, statY, letterWidth, 12, 504);
                    break;
                case REFERENCE_MARK:
                    letterWidth = 9;
                    renderLetterFromTex(matrixStack, letterAlpha, invertBnW, statX + 1f, statY, letterWidth, 24, 504);
                    break;
                default:
                    ClientUtil.drawCenteredStringNoShadow(matrixStack, mc.font, rank, 
                            statX, statY, 
                            ClientUtil.addAlpha(invertBnW ? 0xFFFFFF : 0x000000, letterAlpha));
                }
                
                // stat name tooltip
                if (mouseX >= statX - letterWidth / 2 && mouseX <= statX + letterWidth / 2 && mouseY >= statY && mouseY <= statY + mc.font.lineHeight) {
                    GuiUtils.drawHoveringText(matrixStack, override.statTooltip(stat, power), 
                            mouseX, mouseY, screenWidth, screenHeight, -1, mc.font);
                }
            }
            
            matrixStack.popPose();
        });
    }
    
    @SuppressWarnings("deprecation")
    private static void renderLetterFromTex(MatrixStack matrixStack, float letterAlpha, boolean invertBnW, 
            float statX, float statY, float letterWidth, int texX, int texY) {
        Minecraft.getInstance().textureManager.bind(STAND_STATS_UI);
        if (invertBnW) RenderSystem.color4f(1, 1, 1, letterAlpha);
        else           RenderSystem.color4f(0, 0, 0, letterAlpha);
        RenderSystem.enableBlend();
        BlitFloat.blitFloat(matrixStack, statX - letterWidth / 2, statY, texX, texY, 8, 7, 512, 512);
        RenderSystem.color4f(1, 1, 1, 1);
    }
    

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
    // FIXME hexagon rendering above the screen's tooltips
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
