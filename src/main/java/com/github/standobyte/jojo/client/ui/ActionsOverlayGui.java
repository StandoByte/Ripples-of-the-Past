package com.github.standobyte.jojo.client.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.ui.sprites.SpriteUploaders;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ActionsOverlayGui extends AbstractGui {
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation OVERLAY_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/overlay.png");
    
    private static ActionsOverlayGui instance = null;
    private final Minecraft mc;
    
    private final ActionsHotbarConfig<INonStandPower> nonStandUiConfig = new ActionsHotbarConfig<>(PowerClassification.NON_STAND);
    private final ActionsHotbarConfig<IStandPower> standUiConfig = new ActionsHotbarConfig<>(PowerClassification.STAND);
    @Nullable
    private ActionsHotbarConfig<?> currentMode = null;
    
    private Action.TargetRequirement selectedTargetType;
    private boolean selectedRightTarget;
    private Action.TargetRequirement rmbTargetType;
    private boolean rmbRightTarget;
    
    private final ElementTransparency actionNameTransparency = new ElementTransparency(40, 10);
    private final ElementTransparency rmbActionNameTransparency = new ElementTransparency(40, 10);
    private final ElementTransparency modeSelectorTransparency = new ElementTransparency(40, 10);
    private final ElementTransparency energyBarTransparency = new ElementTransparency(40, 10);
    private final ElementTransparency staminaBarTransparency = new ElementTransparency(40, 10);
    private final ElementTransparency resolveBarTransparency = new ElementTransparency(40, 10);
    private ElementTransparency[] tickingTransparencies = new ElementTransparency[] {
            actionNameTransparency,
            rmbActionNameTransparency,
            modeSelectorTransparency,
            energyBarTransparency,
            staminaBarTransparency,
            resolveBarTransparency
    };
    
    private ActionsOverlayGui(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new ActionsOverlayGui(mc);
            MinecraftForge.EVENT_BUS.register(instance);
        }
    }
    
    public static ActionsOverlayGui getInstance() {
        return instance;
    }
    
    public void tick() {
        for (ElementTransparency element : tickingTransparencies) {
            element.tick();
        }
        INonStandPower power = nonStandUiConfig.getPower();
        if (power != null) {
            if (power.getEnergy() < power.getMaxEnergy()) {
                energyBarTransparency.reset();
            }
        }
        IStandPower standPower = standUiConfig.getPower();
        if (standPower != null) {
            if (standPower.getStamina() < standPower.getMaxStamina()) {
                staminaBarTransparency.reset();
            }
            if (standPower.getResolve() > 0) {
                resolveBarTransparency.reset();
            }
        }
    }
    
    public boolean isActive() {
        return currentMode != null;
    }
    
    @Nullable
    public PowerClassification getCurrentPower() {
        if (currentMode == null) {
            return null;
        }
        return currentMode.powerClassification;
    }
    
    /*
     * input:
     * FIXME hold LAlt(?) to move the selected action around on hotbar
     * FIXME navigating between pages
     * 
     * FIXME sync to the server to save the configs between sessions
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void render(RenderGameOverlayEvent.Pre event) {
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR || mc.options.hideGui) {
            return;
        }

        
        MatrixStack matrixStack = event.getMatrixStack();
        float partialTick = event.getPartialTicks();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int barsX = screenWidth - 36;
        int barsY = (screenHeight- BAR_HEIGHT) / 2;
        int modeHotbarX = screenWidth - 62;
        int modeHotbarY = screenHeight / 2 - (modes.size() * 10);
        int powerIconX = 3; // FIXME power name & icon position (preferrably somewhere near the hotbar)
        int powerIconY = 3;

        if (currentMode != null) {
            if (currentMode.getPower() == null || currentMode.getUnlockedActions() == null) {
                JojoMod.LOGGER.debug("Failed rendering {} hotbar", currentMode.powerClassification);
                currentMode = null;
                return;
            }
            int hotbarX = screenWidth / 2 - 91;
            int hotbarY = screenHeight - 22;
            HandSide offHandSide = mc.player.getMainArm().getOpposite();
            if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
                event.setCanceled(true);
                RenderSystem.enableRescaleNormal();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                
                renderActionsHotbar(matrixStack, hotbarX, hotbarY, offHandSide);
                
                renderActionIcons(matrixStack, hotbarX + 3, hotbarY + 3, partialTick, offHandSide);
                
                renderHotbarIcons(matrixStack, hotbarX, hotbarY, offHandSide);
                
                RenderSystem.disableRescaleNormal();
                RenderSystem.disableBlend();
                // FIXME pages for when there are > 9 actions (hamon)
            }
            else if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                RenderSystem.enableRescaleNormal();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                renderPowerIcon(matrixStack, powerIconX, powerIconY);
                RenderSystem.disableRescaleNormal();
                RenderSystem.disableBlend();
            }
            else if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
                drawActionName(matrixStack, screenWidth / 2F, screenHeight - 59, partialTick, 
                        currentMode.getSelectedAction(), actionNameTransparency);
                drawActionName(matrixStack, hotbarX + getRmbSlotXOffset(offHandSide), screenHeight - 59, partialTick, 
                        currentMode.getRmbAction(), rmbActionNameTransparency); // FIXME rmb action name (should i even care about it?)
                drawPowerName(matrixStack, powerIconX, powerIconY);
                drawModeSelectorNames(matrixStack, modeHotbarX - 5, modeHotbarY, partialTick);
                drawHoldDuration(matrixStack, hotbarX, hotbarY);
                // FIXME remote stand distance & strength
            }
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            RenderSystem.enableRescaleNormal();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            
            renderModeSelector(matrixStack, modeHotbarX, modeHotbarY, partialTick);
            renderBars(matrixStack, barsX, barsY, partialTick);
            
            RenderSystem.disableRescaleNormal();
            RenderSystem.disableBlend();
        }
    }
    
    
    
    private void renderActionsHotbar(MatrixStack matrixStack, int x, int y, HandSide rmbSlotSide) {
        mc.getTextureManager().bind(WIDGETS_LOCATION);
        int blitOffset = getBlitOffset();
        setBlitOffset(-90);
        int slots = currentMode.getAllActionsCount();
        // hotbar
        renderHotbar(matrixStack, x, y, slots);
        // selection
        if (currentMode.getSelectedSlot() >= 0) {
           blit(matrixStack, x - 1 + currentMode.getSelectedSlot() * 20, y - 1, 0, 22, 24, 22);
        }
        // rmb slot
        if (currentMode.getRmbAction() != null) {
            int slotTex = rmbSlotSide == HandSide.LEFT ? 24 : 53;
            int xOffset = getRmbSlotXOffset(rmbSlotSide);
            if (rmbSlotSide == HandSide.RIGHT) {
                xOffset -= 7;
            }
            blit(matrixStack, x + xOffset, y - 1, slotTex, 22, 29, 24);
        }
        setBlitOffset(blitOffset);
    }
    
    private int getRmbSlotXOffset(HandSide side) {
        return side == HandSide.LEFT ? -29 : 20 * currentMode.getAllActionsCount() + 9;
    }
    
    private void renderHotbar(MatrixStack matrixStack, int x, int y, int slots) {
        int hotbarLength = 20 * slots + 1;
        blit(matrixStack, x, y, 0, 0, hotbarLength, 22);
        blit(matrixStack, x + hotbarLength, y, 181, 0, 1, 22);
    }
    
    
    
    private void renderActionIcons(MatrixStack matrixStack, int x, int y, float partialTick, HandSide rmbSlotSide) {
        List<Action> actions = currentMode.getUnlockedActions();
        IPower<?> power = currentMode.getPower();
        for (int i = 0; i < actions.size(); i++) {
            renderActionIcon(matrixStack, actions.get(i), power, x + 20 * i, y, partialTick, i == currentMode.getSelectedSlot(), false);
        }
        if (currentMode.getRmbAction() != null) {
            renderActionIcon(matrixStack, currentMode.getRmbAction(), power, x + getRmbSlotXOffset(rmbSlotSide), y, partialTick, false, true);
        }
    }
    
    private void renderActionIcon(MatrixStack matrixStack, Action action, IPower<?> power, int x, int y, float partialTick, boolean isSelected, boolean isRmbSlot) {
        if (power.getHeldAction() != null) {
            if (power.getHeldAction() == action.getShiftVariationIfPresent()) {
                action = action.getShiftVariationIfPresent();
            }
        }
        if (shiftVarSelected(power, action, mc.player.isShiftKeyDown())) {
            action = action.getShiftVariationIfPresent();
        }
        
        TextureAtlasSprite textureAtlasSprite = SpriteUploaders.getActionSprites().getSprite(action);
        mc.getTextureManager().bind(textureAtlasSprite.atlas().location());
        
        if (!isActionAvaliable(power, action, ActionTarget.fromRayTraceResult(mc.hitResult), isSelected, isRmbSlot)) {
            RenderSystem.color4f(0.2F, 0.2F, 0.2F, 0.5F);
            blit(matrixStack, x, y, 0, 16, 16, textureAtlasSprite);
            
            float ratio = power.getCooldownRatio(action, partialTick);
            if (ratio > 0) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuilder();
                fillRect(bufferBuilder, x, y + 16.0F * (1.0F - ratio), 16, 16.0F * ratio, 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            blit(matrixStack, x, y, 0, 16, 16, textureAtlasSprite);
        }
    }
    
    private boolean isActionAvaliable(IPower<?> power, Action action, ActionTarget mouseTarget, boolean isSelected, boolean isRmbSlot) {
        if (isSelected || isRmbSlot) {
            boolean rightTarget = power.checkTargetType(action, action.getPerformer(mc.player, power), mouseTarget).isPositive();
            Action.TargetRequirement targetType = action.getTargetRequirement();
            if (isSelected) {
                selectedTargetType = targetType;
                selectedRightTarget = rightTarget;
            }
            else {
                rmbTargetType = targetType;
                rmbRightTarget = rightTarget;
            }
            return rightTarget && power.checkRequirements(action, action.getPerformer(mc.player, power), mouseTarget, false).isPositive();
        }
        else {
            return power.checkRequirements(action, action.getPerformer(mc.player, power), mouseTarget, true).isPositive();
        }
    }
    
    private void fillRect(BufferBuilder bufferBuilder, int x, double y, int width, double height, int red, int green, int blue, int alpha) {
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(x + 0 , y + 0, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x + 0 , y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x + width , y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x + width , y + 0, 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().end();
    }
    
    public static boolean shiftVarSelected(IPower<?> power, Action action, boolean shift) {
        return power.getHeldAction() != action && shift && power.isActionUnlocked(action.getShiftVariationIfPresent()) 
                || power.getHeldAction() == action.getShiftVariationIfPresent();
    }
    
    

    private void renderHotbarIcons(MatrixStack matrixStack, int hotbarX, int hotbarY, HandSide offHandSide) {
        // vanilla attack strength scale (since the hotbar event was cancelled)
        int leapIconX = offHandSide == HandSide.LEFT ? hotbarX + 182 + 6 : hotbarX - 22;
        int iconsY = hotbarY + 2;
        if (mc.options.attackIndicator == AttackIndicatorStatus.HOTBAR) {
            float iconFill = mc.player.getAttackStrengthScale(0.0F);
            if (iconFill < 1.0F) {
                mc.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
                int attackIndicatorX = offHandSide == HandSide.LEFT ? hotbarX + 182 + 6 : hotbarX - 22;
                int px = (int) (iconFill * 19.0F);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                blit(matrixStack, attackIndicatorX, iconsY, 0, 94, 18, 18);
                blit(matrixStack, attackIndicatorX, iconsY + 18 - px, 18, 112 - px, 18, px);
                leapIconX += offHandSide == HandSide.LEFT ? 20 : -20;
            }
        }
        mc.getTextureManager().bind(OVERLAY_LOCATION);
        
        // leap icon
        IPower<?> power = currentMode.getPower();
        if (power.isLeapUnlocked()) {
            float iconFill = power.getLeapCooldownPeriod() != 0 ? 
                    1F - (float) power.getLeapCooldown() / (float) power.getLeapCooldownPeriod() : 1;
            boolean translucent = !power.canLeap();
            blit(matrixStack, leapIconX, iconsY, 96, 238, 18, 18);
            if (translucent) {
                RenderSystem.color4f(0.5F, 0.5F, 0.5F, 0.75F);
            }
            int px = (int) (19F * iconFill);
            blit(matrixStack, leapIconX, iconsY + 18 - px, 96 + 18, 238 + 18 - px, 18, px);
            if (translucent) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        
        // target type icons
        renderTargetIcon(matrixStack, hotbarX + currentMode.getSelectedSlot() * 20 + 13, hotbarY - 1, selectedTargetType, selectedRightTarget);
        renderTargetIcon(matrixStack, hotbarX + getRmbSlotXOffset(offHandSide) + 13, hotbarY - 1, rmbTargetType, rmbRightTarget);
    }
    
    private void renderTargetIcon(MatrixStack matrixStack, int x, int y, 
            Action.TargetRequirement iconType, boolean rightTarget) {
        if (iconType != null) {
            int iconTexX;
            switch (iconType) {
            case BLOCK:
                iconTexX = 0;
                break;
            case ENTITY:
                iconTexX = 32;
                break;
            case ANY:
                iconTexX = 64;
                break;
            default:
                return;
            }
            int iconTexY = rightTarget ? 192 : 224;
            matrixStack.pushPose();
            matrixStack.scale(0.5F, 0.5F, 0F);
            matrixStack.translate(x, y, 0);
            blit(matrixStack, x, y, iconTexX, iconTexY, 32, 32);
            matrixStack.popPose();
        }
    }
    
    

    private void renderBars(MatrixStack matrixStack, int barsX, int barsY, float partialTick) {
        mc.getTextureManager().bind(OVERLAY_LOCATION);
        INonStandPower power = nonStandUiConfig.getPower();
        boolean prevBarThin = false;
        if (power.hasPower()) {
            renderBar(matrixStack, barsX, barsY, 
                    32, 0, 240, getBarIconTexY(power.getType()), 
                    // FIXME get cost value
                    power.getEnergy(), power.getMaxEnergy(), 500f, 
                    currentMode == nonStandUiConfig, energyBarTransparency, partialTick, power.getType().getColor()); // FIXME vampirism blood levels
            prevBarThin = currentMode != nonStandUiConfig;
        }
        IStandPower standPower = standUiConfig.getPower();
        if (standPower.hasPower()) {
            if (standPower.usesStamina()) {
                barsX += prevBarThin ? 8 : 13;
                renderBar(matrixStack, barsX, barsY, 
                        48, 0, 240, 0, 
                        // FIXME get cost value
                        standPower.getStamina(), standPower.getMaxStamina(), 300f, 
                        currentMode == standUiConfig, staminaBarTransparency, partialTick, 0xFFFFFF);
                prevBarThin = currentMode != standUiConfig;
            }
            if (standPower.usesResolve()) {
                barsX += prevBarThin ? 8 : 13;
                renderBar(matrixStack, barsX, barsY, 
                        32, 0, 240, 15, 
                        standPower.getResolve(), IStandPower.MAX_EXP, 0, 
                        currentMode == standUiConfig, resolveBarTransparency, partialTick, standPower.getType().getColor());
            }
        }
    }

    private long counter = 0;
    private static final int BAR_HEIGHT = 100;
    private static final int BAR_HEIGHT_SHORTENED = 75;
    private void renderBar(MatrixStack matrixStack, int barX, int barY, 
            int texX, int texY, int iconTexX, int iconTexY, 
            float amount, float max, float cost, 
            boolean highlight, ElementTransparency transparency, float partialTick, int color) {
        int value = (int) ((float) BAR_HEIGHT * (amount / max));
        int costValue = (int) ((float) BAR_HEIGHT * (cost / max));
        float[] rgb = ClientUtil.rgb(color);
        if (highlight) {
            RenderSystem.color4f(rgb[0], rgb[1], rgb[2], 1.0F);
            // bar fill
            blit(matrixStack, barX + 1, barY + BAR_HEIGHT - value + 1, 
                    texX + 1, texY + BAR_HEIGHT - value + 1, 6, value);
            // border
            blit(matrixStack, barX, barY, 16, 0, 8, BAR_HEIGHT + 2);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            // cost
            if (costValue > 0) {
                counter += Util.getMillis();
                float alpha = Math.max(Math.abs(MathHelper.sin(counter / 10000000F)), 0.25F) + 0.25F;
                boolean notEnough = cost > amount;
                fill(matrixStack, 
                        barX + 1, notEnough ? barY + BAR_HEIGHT - costValue + 1 : barY + 1, 
                        barX + 7, notEnough ? barY + BAR_HEIGHT + 1 : barY + costValue + 1, 
                        ElementTransparency.addAlpha(0xFFFFFF, alpha * 0.5F));
                RenderSystem.enableBlend();
            }
            // scale
            blit(matrixStack, barX + 1, barY + 1, 1, 1, 6, BAR_HEIGHT);
            // icon
            blit(matrixStack, barX - 1, barY - 12, iconTexX, iconTexY, 10, 14);
        }
        else if (transparency.shouldRender()) {
            barY += (BAR_HEIGHT - BAR_HEIGHT_SHORTENED);
            texX += 8;
            RenderSystem.color4f(rgb[0], rgb[1], rgb[2], transparency.getAlpha(partialTick));
            // bar fill
            blit(matrixStack, barX + 1, barY + BAR_HEIGHT - value + 1, 
                    texX + 1, texY + BAR_HEIGHT - value + 1, 6, value);
            // border
            blit(matrixStack, barX, barY, 24, 0, 8, BAR_HEIGHT + 2);
            // scale
            blit(matrixStack, barX + 1, barY + 1, 9, 1, 6, BAR_HEIGHT);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    private int getBarIconTexY(NonStandPowerType<?> type) {
        if (type == ModNonStandPowers.VAMPIRISM.get()) {
            return 30;
        }
        if (type == ModNonStandPowers.HAMON.get()) {
            return 45;
        }
        return 165;
    }
    
    
    
    private final List<ActionsHotbarConfig<?>> modes = Collections.unmodifiableList(Arrays.asList(
            null,
            nonStandUiConfig,
            standUiConfig
            ));
    private void renderModeSelector(MatrixStack matrixStack, int x, int y, float partialTick) {
        if (modeSelectorTransparency.shouldRender()) {
            mc.getTextureManager().bind(WIDGETS_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, modeSelectorTransparency.getAlpha(partialTick));
            matrixStack.pushPose();
            matrixStack.translate(x, y, 0);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90));
            matrixStack.translate(-x, -y - 22, 0);
            renderHotbar(matrixStack, x, y, modes.size());
            int selectedMode = modes.indexOf(currentMode);
            if (selectedMode > -1) {
                blit(matrixStack, x + selectedMode * 20 - 1, y - 1, 0, 22, 24, 24);
            }
            matrixStack.popPose();
            renderModeSelectorIcons(matrixStack, x + 3, y + 3, partialTick);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    
    
    private void renderModeSelectorIcons(MatrixStack matrixStack, int x, int y, float partialTick) {
        for (ActionsHotbarConfig<?> mode : modes) {
            if (mode != null) {
                IPower<?> power = mode.getPower();
                if (power.hasPower()) {
                    mc.getTextureManager().bind(power.getType().getIconTexture());
                    blit(matrixStack, x, y, 0, 0, 16, 16, 16, 16);
                }
            }
            y += 20;
        }
    }
    
    

    private void renderPowerIcon(MatrixStack matrixStack, int iconX, int iconY) {
        IPower<?> power = currentMode.getPower();
        if (power.isActive()) {
            mc.getTextureManager().bind(power.getType().getIconTexture());
            blit(matrixStack, iconX, iconY, 0, 0, 16, 16, 16, 16);
        }
    }
    
    
    
    private void drawActionName(MatrixStack matrixStack, float x, int y, float partialTick, 
            Action action, ElementTransparency transparency) {
        if (action != null && transparency.shouldRender()) {
            ITextComponent actionName = action.getName(currentMode.getPower());
            if (action.getHoldDurationMax() > 0) {
                actionName = new TranslationTextComponent("jojo.overlayv2.hold", actionName);
            }
            if (action.hasShiftVariation()) {
                actionName = new TranslationTextComponent("jojo.overlayv2.shift", actionName, 
                        new KeybindTextComponent(mc.options.keyShift.getName()), action.getShiftVariationIfPresent().getName(currentMode.getPower()));
            }
            int width = mc.font.width(actionName);
            x -= width / 2F;
            if (!mc.gameMode.canHurtPlayer()) {
                y += 14;
            }
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            drawBackdrop(matrixStack, (int) x, y, width, transparency, partialTick);
            mc.font.drawShadow(matrixStack, actionName, x, (float) y, 
                    transparency.makeTextColorTranclucent(currentMode.getPower().getType().getColor(), partialTick));
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }
    }
    
    private void drawBackdrop(MatrixStack matrixStack, int x, int y, int width, 
            @Nullable ElementTransparency transparency, float partialTick) {
        int backdropColor = mc.options.getBackgroundColor(0.0F);
        if (backdropColor != 0) {
            fill(matrixStack, x - 2, y - 2, x + width + 2, y + mc.font.lineHeight + 2, ColorHelper.PackedColor.multiply(backdropColor, 
                            transparency != null ? transparency.makeTextColorTranclucent(0xFFFFFF, partialTick) : 0xFFFFFFFF));
        }
    }
    
    
    
    private void drawPowerName(MatrixStack matrixStack, int x, int y) {
        x += 19;
        IPower<?> power = currentMode.getPower();
        ITextComponent name = new TranslationTextComponent(power.getType().getTranslationKey());
        drawBackdrop(matrixStack, x, y, mc.font.width(name), null, 1.0F);
        drawString(matrixStack, mc.font, name, x, y, power.getType().getColor());
    }
    
    
    
    private void drawModeSelectorNames(MatrixStack matrixStack, int x, int y, float partialTick) {
        if (modeSelectorTransparency.shouldRender()) {
            y += (22 - mc.font.lineHeight) / 2;
            for (ActionsHotbarConfig<?> mode : modes) {
                ITextComponent name = getModeNameForSelector(mode);
                if (name != null) {
                    int color = getModeColor(mode);
                    int width = mc.font.width(name);
                    drawBackdrop(matrixStack, x - width, y, width, modeSelectorTransparency, partialTick);
                    mc.font.drawShadow(matrixStack, name, x - width, y, 
                            modeSelectorTransparency.makeTextColorTranclucent(color, partialTick));
                }
                y += 22;
            }
        }
    }

    @Nullable
    private ITextComponent getModeNameForSelector(ActionsHotbarConfig<?> mode) {
        ITextComponent name;
        if (mode == null) {
            if (currentMode == null) {
                return null;
            }
            name = new TranslationTextComponent("jojo.overlayv2.mode_deselect");
        }
        else {
            IPower<?> power = mode.getPower();
            if (!power.hasPower()) {
                return null;
            }
            name = new TranslationTextComponent(power.getType().getTranslationKey());
        }
        ITextComponent keyName = getKeyName(mode);
        if (keyName != null) {
            name = new TranslationTextComponent("jojo.overlayv2.mode_key", keyName, name);
        }
        return name;
    }

    private Map<ActionsHotbarConfig<?>, Supplier<KeyBinding>> modeKeys = ImmutableMap.of(
            nonStandUiConfig, () -> InputHandler.getInstance().nonStandHotbar,
            standUiConfig, () -> InputHandler.getInstance().standHotbar);
    @Nullable
    private ITextComponent getKeyName(ActionsHotbarConfig<?> mode) {
        if (mode == currentMode) {
            return null;
        }
        if (mode == null) {
            mode = currentMode;
        }
        Supplier<KeyBinding> keySupplier = modeKeys.get(mode);
        if (keySupplier == null || keySupplier.get() == null) {
            return null;
        }
        return new KeybindTextComponent(keySupplier.get().getName());
    }
    
    private int getModeColor(ActionsHotbarConfig<?> mode) {
        if (mode == null) {
            return 0xFFFFFF;
        }
        return mode.getPower().getType().getColor();
    }
    
    
    
    // FIXME hold duration
    private void drawHoldDuration(MatrixStack matrixStack, int hotbarX, int hotbarY) {
        
    }
    
    
    
    public void setMode(@Nullable PowerClassification power) {
        if (power == null) {
            setMode(null, true);
        }
        else {
            ActionsHotbarConfig<?> chosenMode = null;
            switch (power) {
            case NON_STAND:
                chosenMode = nonStandUiConfig;
                break;
            case STAND:
                chosenMode = standUiConfig;
                break;
            }
            setMode(chosenMode, true);
        }
    }
    
    private void setMode(@Nullable ActionsHotbarConfig<?> mode, boolean chosenManually) {
        if (mode != null && currentMode != mode) {
            if (mode.getPower().hasPower()) {
                mode.chosenManually = chosenManually;
                modeSelectorTransparency.reset();
                if (currentMode != null) {
                    if (mode != nonStandUiConfig) {
                        energyBarTransparency.reset();
                    }
                    else if (mode != standUiConfig) {
                        staminaBarTransparency.reset();
                        resolveBarTransparency.reset();
                    }
                }
                currentMode = mode;
                actionNameTransparency.reset();
                rmbActionNameTransparency.reset();
            }
        }
        else {
            modeSelectorTransparency.reset();
            if (currentMode == nonStandUiConfig) {
                energyBarTransparency.reset();
            }
            else if (currentMode == standUiConfig) {
                staminaBarTransparency.reset();
                resolveBarTransparency.reset();
            }
            currentMode = null;
        }
    }

    public void onStandSummon() {
        if (currentMode != standUiConfig) {
            setMode(standUiConfig, false);
        }
    }

    public void onStandUnsummon() {
        if (currentMode == standUiConfig && !standUiConfig.chosenManually) {
            setMode(null, false);
        }
    }
    
    public boolean scrollAction(boolean backwards) {
        if (currentMode != null) {
            currentMode.scrollSelectedSlot(backwards);
            actionNameTransparency.reset();
            return true;
        }
        return false;
    }

    public boolean selectAction(int slot) {
        if (currentMode != null) {
            currentMode.selectSlot(slot);
            actionNameTransparency.reset();
            return true;
        }
        return false;
    }
    
    public void swapRmbAction(boolean shift) {
        if (shift) {
            if (currentMode.moveRmbActionToList()) {
                actionNameTransparency.reset();
            }
        }
        else {
            currentMode.swapRmbAndCurrentActions();
            actionNameTransparency.reset();
            rmbActionNameTransparency.reset();
        }
    }

    
    
    public void updatePowersCache() { // FIXME actions aren't updated in-game
        setMode(null, true);
        standUiConfig.setPower(IStandPower.getPlayerStandPower(mc.player));
        nonStandUiConfig.setPower(INonStandPower.getPlayerNonStandPower(mc.player));
    }
    
    
    
    private static class ElementTransparency {
        private final int ticksMax;
        private final int ticksStartFadeAway;
        private int ticks;
        
        private ElementTransparency(int ticksMax, int ticksStartFadeAway) {
            this.ticksMax = ticksMax;
            this.ticksStartFadeAway = ticksStartFadeAway;
        }
        
        private void reset() {
            ticks = ticksMax;
        }
        
        private boolean shouldRender() {
            return ticks > 0;
        }
        
        private int makeTextColorTranclucent(int color, float partialTick) {
            return addAlpha(color, getAlpha(partialTick));
        }
        
        private static final float MIN_ALPHA = 1F / 63F;
        private float getAlpha(float partialTick) {
            if (ticks >= ticksStartFadeAway) {
                return 1F;
            }
            return Math.max((ticks - partialTick) / (float) ticksStartFadeAway, MIN_ALPHA);
        }
        
        private void tick() {
            if (ticks > 0) {
                ticks--;
            }
        }
        
        private static int addAlpha(int color, float alpha) {
            return color | ((int) (255F * alpha)) << 24 & -0x1000000;
        }
    }
}
