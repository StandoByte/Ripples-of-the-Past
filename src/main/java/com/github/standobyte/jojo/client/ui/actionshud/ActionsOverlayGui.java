package com.github.standobyte.jojo.client.ui.actionshud;

import static com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer.BARS_WIDTH_PX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.controls.HudControlSettings;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.client.ui.BlitFloat;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsModeConfig.SelectedTargetIcon;
import com.github.standobyte.jojo.client.ui.actionshud.hotbar.HotbarFold;
import com.github.standobyte.jojo.client.ui.actionshud.hotbar.HotbarRenderer;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonStatsTabGui;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClClickActionPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.bowcharge.BowChargeEffectInstance;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData.Exercise;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.vampirism.VampirismPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.zombie.ZombiePowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandManifestation;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.general.ObjectWrapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("deprecation")
public class ActionsOverlayGui extends AbstractGui {
    public static final ResourceLocation OVERLAY_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/overlay.png");
    private static final ResourceLocation RADIAL_INDICATOR = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/radial_indicator.png");
    
    private static ActionsOverlayGui instance = null;
    private final Minecraft mc;
    private int tickCount;
    
    public final ActionsModeConfig<INonStandPower> nonStandUiMode = new ActionsModeConfig<>(PowerClassification.NON_STAND);
    public final ActionsModeConfig<IStandPower> standUiMode = new ActionsModeConfig<>(PowerClassification.STAND);
    @Nullable
    private ActionsModeConfig<?> currentMode = null;
    
    private final ElementTransparency modeSelectorTransparency = new ElementTransparency(40, 10);
    private final ElementTransparency energyBarTransparency = new ElementTransparency(40, 10);
    private final ElementTransparency staminaBarTransparency = new ElementTransparency(40, 10);
    private final ElementTransparency resolveBarTransparency = new ElementTransparency(40, 10);
    private final ElementTransparency powerNameTransparency = new ElementTransparency(40, 10);
    private final ImmutableMap<InputHandler.ActionKey, ElementTransparency> actionNameTransparency = Arrays.stream(InputHandler.ActionKey.values())
            .collect(Maps.toImmutableEnumMap(hotbar -> hotbar, hotbar -> new ElementTransparency(40, 10)));
    private final Map<InputHandler.ActionKey, ITextComponent> lastActionName = new EnumMap<>(InputHandler.ActionKey.class);
    private final ImmutableMap<ControlScheme.Hotbar, FadeOut> actionHotbarFold = Arrays.stream(ControlScheme.Hotbar.values())
            .collect(Maps.toImmutableEnumMap(hotbar -> hotbar, hotbar -> new FadeOut(40, 10)));
    private final ImmutableMap<PowerClassification, ElementTransparency> customKeybindActionTransparency = Arrays.stream(PowerClassification.values())
            .collect(Maps.toImmutableEnumMap(ex -> ex, ex -> new ElementTransparency(40, 10)));
    private final ImmutableMap<Exercise, ElementTransparency> exerciseBarsTransparency = Arrays.stream(Exercise.values())
            .collect(Maps.toImmutableEnumMap(ex -> ex, ex -> new ElementTransparency(40, 10)));
    
    private final BarsRenderer verticalBars = new VerticalBarsRenderer(this, 
            energyBarTransparency, staminaBarTransparency, resolveBarTransparency);
    private final BarsRenderer horizontalBars = new HorizontalBarsRenderer(this, 
            energyBarTransparency, staminaBarTransparency, resolveBarTransparency);
    
    private FadeOut[] tickingFadeOut = Streams.concat(Stream.of(
            modeSelectorTransparency,
            energyBarTransparency,
            staminaBarTransparency,
            resolveBarTransparency,
            powerNameTransparency), 
            actionNameTransparency.values().stream(),
            actionHotbarFold.values().stream(),
            customKeybindActionTransparency.values().stream(),
            exerciseBarsTransparency.values().stream())
            .toArray(FadeOut[]::new);
    
    private boolean attackSelection;
    private boolean abilitySelection;
    private boolean hotbarsEnabled;
    private boolean altDeselectsAttack;
    private boolean altDeselectsAbility;
    
    private ActionsOverlayGui(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            try {
                
            instance = new ActionsOverlayGui(mc);
            MinecraftForge.EVENT_BUS.register(instance);
            
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
    
    public static ActionsOverlayGui getInstance() {
        return instance;
    }
    
    public void tick() {
        if (!mc.isPaused()) {
            for (FadeOut element : tickingFadeOut) {
                element.tick();
            }
        }
        
        if (attackSelection) actionHotbarFold.get(ControlScheme.Hotbar.LEFT_CLICK).reset();
        if (abilitySelection) actionHotbarFold.get(ControlScheme.Hotbar.RIGHT_CLICK).reset();
        
        INonStandPower power = nonStandUiMode.getPower();
        if (power != null) {
            boolean showEnergyBar;
            if (power.getType() == ModPowers.HAMON.get()) {
                showEnergyBar = power.getEnergy() > 0 || GeneralUtil.orElseFalse(power.getTypeSpecificData(ModPowers.HAMON.get()), hamon -> {
                    return hamon.getBreathStability() < hamon.getMaxBreathStability();
                });
            }
            else {
                showEnergyBar = power.getEnergy() < power.getMaxEnergy();
            }
            if (showEnergyBar) {
                energyBarTransparency.reset();
            }
        }
        IStandPower standPower = standUiMode.getPower();
        if (standPower != null) {
            if (standPower.getStamina() < standPower.getMaxStamina()) {
                staminaBarTransparency.reset();
            }
            if (standPower.getResolve() > 0) {
                resolveBarTransparency.reset();
            }
        }
        
        tickOutOfBreathEffect();
        
        tickCount++;
        if (currentMode != null) {
            currentMode.tick();
        }
    }
    
    public void onHamonExerciseValueChanged(Exercise exercise) {
        exerciseBarsTransparency.get(exercise).reset();
    }
    
    public boolean isActive() {
        return currentMode != null;
    }
    
    public boolean noActionSelected(ControlScheme.Hotbar actionType) {
        return !isActive() || currentMode.getSelectedSlot(actionType) < 0;
    }
    
    @Nullable
    public IPower<?, ?> getCurrentPower() {
        if (currentMode == null) {
            return null;
        }
        return currentMode.getPower();
    }
    
    @Nullable
    public PowerClassification getCurrentMode() {
        if (currentMode == null) {
            return null;
        }
        return currentMode.powerClassification;
    }
    
    private ActionsModeConfig<?> getHudMode(PowerClassification power) {
        switch (power) {
        case STAND:
            return standUiMode;
        case NON_STAND:
            return nonStandUiMode;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    

    
    private ActionTarget _target;
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void render(RenderGameOverlayEvent.Pre event) {
        _target = null;
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR || mc.options.hideGui
                || mc.player.isDeadOrDying()) {
            return;
        }
        
        MatrixStack matrixStack = event.getMatrixStack();
        float partialTick = event.getPartialTicks();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        PositionConfig hotbarsPosConfig = ClientModSettings.getSettingsReadOnly().hotbarsPosition;
        PositionConfig barsPosConfig = ClientModSettings.getSettingsReadOnly().barsPosition;
        boolean showModeSelector = false;
        
        Action<?> lastCustomKeybindAction = null;
        if (currentMode != null) {
            if (customKeybindActionTransparency.get(currentMode.powerClassification).shouldRender()) {
                lastCustomKeybindAction = currentMode.lastCustomKeybindAction;
            }
            else {
                currentMode.lastCustomKeybindAction = null;
            }
        }
        boolean renderCustomKeybindAction = lastCustomKeybindAction != null;
            
        updateWarnings(currentMode);
        updateElementPositions(barsPosConfig, hotbarsPosConfig, renderCustomKeybindAction, screenWidth, screenHeight);
        
        RenderGameOverlayEvent.ElementType elementTypeRender = event.getType();
        switch (elementTypeRender) {
        case ALL:
            RenderSystem.enableRescaleNormal();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            
            renderBars(matrixStack, barsPosition, barsRenderer, partialTick);
            
            if (showModeSelector) {
                renderModeSelector(matrixStack, modeSelectorPosition, partialTick);
            }
            
            if (nonStandUiMode != null && nonStandUiMode.getPower() != null && nonStandUiMode.getPower().hasPower()) {
                nonStandUiMode.getPower().getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    renderHamonExerciseBars(matrixStack, hamonExerciseBarsPosition, hamon, partialTick);
                });
            }
            
            ActionsModeConfig<?> modeIcon = currentMode != null ? currentMode : standUiMode;
            float standAlpha = 1.0F;
            if (modeIcon == standUiMode && modeIcon != null) {
                IStandPower stand = standUiMode.getPower();
                if (stand != null && stand.isActive() && stand.getStandManifestation() instanceof StandEntity) {
                    standAlpha = ((StandEntity) stand.getStandManifestation()).getAlpha(partialTick);
                }
            }
            renderPowerIcon(matrixStack, hotbarsPosition, modeIcon, standAlpha);
            renderIconBarAtCrosshair(matrixStack, screenWidth, screenHeight, partialTick);
            
            renderOutOfBreathSprite(matrixStack, partialTick, screenWidth, screenHeight);
            
            RenderSystem.disableRescaleNormal();
            RenderSystem.disableBlend();
            break;
        case TEXT:
            RenderSystem.enableRescaleNormal();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

//            if (showModeSelector) {
//                drawModeSelectorNames(matrixStack, modeSelectorPosition, partialTick);
//            }
            
            drawBarsText(matrixStack, barsRenderer, partialTick);
            
            RenderSystem.disableRescaleNormal();
            RenderSystem.disableBlend();
            break;
        case HELMET: // VIGNETTE only gets called when the graphics settings are on Fancy, and this overlay is pretty important
            renderOutOfBreathVignette(matrixStack, partialTick);
            break;
        default:
            break;
        }
        
        if (currentMode != null) {
            if (currentMode.getPower() == null || !currentMode.getPower().hasPower()) {
                JojoMod.getLogger().warn("Failed rendering {} HUD", currentMode.powerClassification);
                currentMode = null;
                return;
            }

            switch (elementTypeRender) {
            case ALL:
                RenderSystem.enableRescaleNormal();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                
                for (int i = 0; i < hotbarsRendered.length; i++) {
                    hotbarsRendered[i] = false;
                }
                int hotbarsRenderedCount = 0;
                if (renderActionsHotbar(matrixStack, hotbarsPosition, InputHandler.ActionKey.ATTACK,
                        currentMode, getMouseTarget(), hotbarsRenderedCount, partialTick)) { 
                    hotbarsRendered[0] = true; 
                    hotbarsRenderedCount++; 
                }
                if (renderActionsHotbar(matrixStack, hotbarsPosition, InputHandler.ActionKey.ABILITY, 
                        currentMode, getMouseTarget(), hotbarsRenderedCount, partialTick)) { 
                    hotbarsRendered[1] = true; 
                    hotbarsRenderedCount++; 
                }
                if (renderCustomKeybindAction) {
                    renderCustomKeybindActionSlot(matrixStack, hotbarsPosition, lastCustomKeybindAction, currentMode, getMouseTarget(), 
                            hotbarsRenderedCount, partialTick);
                }
                
                renderWarningIcons(matrixStack, warningsPosition, warningLines);
                
                renderLeapIcon(matrixStack, currentMode, screenWidth, screenHeight);
                
                RenderSystem.disableRescaleNormal();
                RenderSystem.disableBlend();
                break;
            case TEXT:
                int color = getPowerUiColor(currentMode.getPower());
                drawPowerName(matrixStack, hotbarsPosition, currentMode, color, partialTick);

                int hotbarI = 0;
                if (hotbarsRendered[0]) drawHotbarText(matrixStack, hotbarsPosition, InputHandler.ActionKey.ATTACK, 
                        currentMode, getMouseTarget(), color, hotbarI++, partialTick);
                if (hotbarsRendered[1]) drawHotbarText(matrixStack, hotbarsPosition, InputHandler.ActionKey.ABILITY, currentMode, 
                        getMouseTarget(), color, hotbarI++, partialTick);
                if (renderCustomKeybindAction) {
                    drawCustomKeybindActionText(matrixStack, hotbarsPosition, lastCustomKeybindAction, currentMode, getMouseTarget(), color, hotbarI, partialTick);
                }
                
                drawWarningText(matrixStack, warningsPosition, warningLines);
                break;
            default:
                break;
            }
        }
    }
    private boolean[] hotbarsRendered = new boolean[3];
    
    public ActionTarget getMouseTarget() {
        if (_target == null) {
            if (InputHandler.getInstance().mouseTarget != null) {
                _target = ActionTarget.fromRayTraceResult(InputHandler.getInstance().mouseTarget);
            }
            else {
                _target = ActionTarget.EMPTY;
            }
        }
        return _target;
    }
    
    public static int getPowerUiColor(IPower<?, ?> power) {
        switch (power.getPowerClassification()) {
        case NON_STAND:
            if (power.getType() == ModPowers.HAMON.get()) {
                return HamonPowerType.COLOR;
            }
            else if (power.getType() == ModPowers.VAMPIRISM.get()) {
                return VampirismPowerType.COLOR;
            }
            else if (power.getType() == ModPowers.ZOMBIE.get()) {
                return ZombiePowerType.COLOR;
            }
            else {
                return -1;
            }
        case STAND:
            return ((IStandPower) power).getStandInstance()
                    .flatMap(StandSkinsManager.getInstance()::getStandSkin)
                    .map(skin -> skin.color)
                    .orElse(-1);
        }
        return -1;
    }



    private static final int HOTBARS_ELEMENT_HEIGHT_PX = 110;
    
    private final ElementPosition barsPosition = new ElementPosition();
    private BarsRenderer barsRenderer;
    private final ElementPosition hotbarsPosition = new ElementPosition();
    private final ElementPosition warningsPosition = new ElementPosition();
    private List<ITextComponent> warningLines = new ArrayList<>();
    private final ElementPosition standStrengthPosition = new ElementPosition();
    private final ElementPosition modeSelectorPosition = new ElementPosition();
    private final ElementPosition hamonExerciseBarsPosition = new ElementPosition();
    
    private void updateElementPositions(PositionConfig barsConfig, PositionConfig hotbarsConfig, 
            boolean renderCustomKeybindAction, int screenWidth, int screenHeight) {
        int halfWidth = screenWidth / 2;
        int halfHeight = screenHeight / 2;
        barsRenderer = barsConfig.barsOrientation == BarsOrientation.HORIZONTAL ? horizontalBars : verticalBars;
        barsPosition.x = barsConfig.getXPos(screenWidth);
        barsPosition.y = barsConfig.getYPos(screenHeight, 
                barsConfig.barsOrientation == BarsOrientation.HORIZONTAL ? BARS_WIDTH_PX : VerticalBarsRenderer.BAR_HEIGHT);
        if (isActive() && (hotbarsConfig == PositionConfig.TOP_LEFT && barsConfig == PositionConfig.LEFT
                || hotbarsConfig == PositionConfig.TOP_RIGHT && barsConfig == PositionConfig.RIGHT)) {
            int offset = HOTBARS_ELEMENT_HEIGHT_PX + INDENT + VerticalBarsRenderer.ICON_HEIGHT;
            if (renderCustomKeybindAction) {
                offset += 24;
            }
            barsPosition.y = Math.max(hotbarsPosition.y + offset, barsPosition.y);
        }
        barsPosition.alignment = barsConfig.aligment;
        
        hotbarsPosition.x = hotbarsConfig.getXPos(screenWidth);
        hotbarsPosition.y = hotbarsConfig.getYPos(screenHeight, 
                HOTBARS_ELEMENT_HEIGHT_PX);
        if (isActive()) {
            if (barsConfig == hotbarsConfig) {
                switch (barsConfig.barsOrientation) {
                case HORIZONTAL:
                    hotbarsPosition.y += BARS_WIDTH_PX + INDENT;
                    break;
                case VERTICAL:
                    hotbarsPosition.x += hotbarsConfig.aligment == Alignment.RIGHT ? -(BARS_WIDTH_PX + INDENT) : BARS_WIDTH_PX + INDENT;
                    break;
                }
            }
            else if (barsConfig == PositionConfig.TOP_LEFT && hotbarsConfig == PositionConfig.LEFT
                    || barsConfig == PositionConfig.TOP_RIGHT && hotbarsConfig == PositionConfig.RIGHT) {
                hotbarsPosition.y = Math.max(barsPosition.y + BARS_WIDTH_PX + INDENT, hotbarsPosition.y);
            }
        }
        hotbarsPosition.alignment = hotbarsConfig.aligment;
        
        warningsPosition.x = hotbarsPosition.x;
        warningsPosition.y = hotbarsPosition.y + 92;
        if (renderCustomKeybindAction) {
            warningsPosition.y += 34;
        }
        warningsPosition.alignment = hotbarsPosition.alignment;
        if (hotbarsConfig == PositionConfig.TOP_LEFT && barsConfig == PositionConfig.LEFT) {
            warningsPosition.x += 32;
        }
        else if (hotbarsConfig == PositionConfig.TOP_RIGHT && barsConfig == PositionConfig.RIGHT) {
            warningsPosition.x -= 22;
        }
        if (warningsPosition.alignment == Alignment.RIGHT) {
            warningsPosition.x -= 16;
        }
        
        standStrengthPosition.x = hotbarsPosition.x;
        standStrengthPosition.y = hotbarsPosition.y + 92 + warningLines.size() * 16;
        if (renderCustomKeybindAction) {
            standStrengthPosition.y += 34;
        }
        standStrengthPosition.alignment = hotbarsPosition.alignment;
        if (hotbarsConfig == PositionConfig.TOP_LEFT && barsConfig == PositionConfig.LEFT) {
            standStrengthPosition.x += 32;
        }
        else if (hotbarsConfig == PositionConfig.TOP_RIGHT && barsConfig == PositionConfig.RIGHT) {
            standStrengthPosition.x -= 22;
        }
        
        modeSelectorPosition.x = hotbarsConfig.aligment == Alignment.LEFT ? halfWidth + 9 : halfWidth - 29;
        modeSelectorPosition.y = halfHeight - 31;
        modeSelectorPosition.alignment = hotbarsConfig.aligment;

        hamonExerciseBarsPosition.x = 10;
        hamonExerciseBarsPosition.y = screenHeight - 5;
        for (ElementTransparency bar : exerciseBarsTransparency.values()) {
            if (bar.shouldRender()) {
                hamonExerciseBarsPosition.y -= 9;
            }
        }
        hamonExerciseBarsPosition.alignment = Alignment.LEFT;
    }
    
    private void updateWarnings(@Nullable ActionsModeConfig<?> mode) {
        warningLines.clear();
        if (mode != null) {
            appendWarnings(warningLines, mode, ControlScheme.Hotbar.LEFT_CLICK);
            appendWarnings(warningLines, mode, ControlScheme.Hotbar.RIGHT_CLICK);
        }
    }
    
    private <P extends IPower<P, ?>> void appendWarnings(List<ITextComponent> list, 
            ActionsModeConfig<P> powerMode, ControlScheme.Hotbar actionType) {
        boolean shiftVariation = InputHandler.useShiftActionVariant(mc);
        Action<P> action = powerMode.getSelectedAction(actionType, shiftVariation, getMouseTarget());
        if (action != null) {
            action.appendWarnings(list, powerMode.getPower(), mc.player);
        }
    }
    
    

    private void renderBars(MatrixStack matrixStack, ElementPosition pos, BarsRenderer renderer, float partialTick) {
        int x = pos.x;
        int y = pos.y;
        if (renderer != null) {
            mc.getTextureManager().bind(OVERLAY_LOCATION);
            renderer.render(matrixStack, x, y, pos.alignment, 
                    currentMode, nonStandUiMode, standUiMode, 
                    tickCount, partialTick, mc);
        }
    }
    
    private void drawBarsText(MatrixStack matrixStack, BarsRenderer renderer, float partialTick) {
        if (renderer != null) {
            renderer.drawTextAfterRender(matrixStack, getCurrentMode(), nonStandUiMode.getPower(), 
                    standUiMode.getPower(), tickCount, partialTick, mc.font, this);
        }
    }

    

    private <P extends IPower<P, ?>> boolean renderActionsHotbar(MatrixStack matrixStack, 
            ElementPosition position, InputHandler.ActionKey actionKey, ActionsModeConfig<P> mode, ActionTarget target, 
            int ordinal, float partialTick) {
        P power = mode.getPower();
        if (!power.hasPower()) return false;
        List<Action<?>> actions = getEnabledActions(power, actionKey);
        if (actions.isEmpty()) return false;

        int x = position.x;
        int y = position.y + getHotbarsYDiff() - 6 + getHotbarsYDiff() * ordinal;
        int hotbarLength = actions.size() * 20 + 2;
        ControlScheme.Hotbar actionHotbar = actionKey.getHotbar();
        if (position.alignment == Alignment.RIGHT) {
            x -= hotbarLength;
        }
        int selected = actionHotbar != null ? mode.getSelectedSlot(actionHotbar) : 0;
        boolean shift = mc.player.isShiftKeyDown();
        float alpha = selected < 0 || !hotbarsEnabled ? 0.25F : 1.0F;
        // mouse button icon
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        mc.getTextureManager().bind(OVERLAY_LOCATION);
        switch (position.alignment) {
        case LEFT:
            renderMouseIcon(matrixStack, x, y + 2, actionKey);
            x += 12;
            break;
        case RIGHT:
            renderMouseIcon(matrixStack, x + hotbarLength - 9, y + 2, actionKey);
            x -= 12;
            break;
        }
        
        boolean foldHotbar = ClientModSettings.getSettingsReadOnly().hudHotbarsFold;
        float foldProgress = foldHotbar && actionHotbar != null ? (1 - actionHotbarFold.get(actionHotbar).getValue(partialTick)) : 0;
        HotbarFold hotbarFold = HotbarFold.makeHotbarFold(actions.size(), selected, foldProgress, position.alignment);
        
        HotbarRenderer.renderFoldingHotbar(matrixStack, mc, x, y, hotbarFold, alpha);
        SelectedTargetIcon selectedActionTargetIcon = mode.getTargetIcon(actionKey);
        // action icons
        x += 3;
        y += 3;
        final int posX = x;
        final int posY = y;
        hotbarFold.renderSlots(slot -> {
            if (slot.getFrameRenderedWidth() > 0) {
                int i = slot.slotIndex;
                Action<P> action = resolveVisibleActionInSlot((Action<P>) actions.get(i), shift, power, getMouseTarget());
                
                float leftIconEdge = slot.pos + 2 + HotbarRenderer.EDGE_EXTRA_WIDTH;
                float rightIconEdge = leftIconEdge + 16;
                float leftRenderBorder = slot.getFrameRenderedLeftEdge();
                float rightRenderBorder = leftRenderBorder + slot.getFrameRenderedWidth();
                
                leftRenderBorder = Math.max(leftRenderBorder, leftIconEdge);
                rightRenderBorder = Math.min(rightRenderBorder, rightIconEdge);
                float iconLeftCut = leftRenderBorder - leftIconEdge;
                float iconCutWidth = rightRenderBorder - leftRenderBorder;
                
                renderActionIcon(matrixStack, selectedActionTargetIcon, mode, 
                        action, target, posX + slot.pos, posY, 
                        partialTick, i == selected, alpha, 
                        iconLeftCut, iconCutWidth);
            }
        });
        
        // target type icon
        if (selected >= 0 && selected < actions.size()) {
            if (selectedActionTargetIcon != null) {
                int[] tex = selectedActionTargetIcon.getIconTex();
                if (tex != null) {
                    mc.getTextureManager().bind(OVERLAY_LOCATION);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                    int texX = tex[0];
                    int texY = tex[1];
                    float iconX = x + hotbarFold.getSlotWithIndex(selected).pos + 10;
                    float iconY = y - 4;
                    matrixStack.pushPose();
                    matrixStack.scale(0.5F, 0.5F, 1F);
                    matrixStack.translate(iconX, iconY, 0);
                    blitFloat(matrixStack, iconX, iconY, texX, texY, 32, 32);
                    matrixStack.popPose();
                }
            }
        }
        
        // hotbar controls lock icon
        if (actionHotbar != null) {
            if (InputHandler.getInstance().areControlsLockedForHotbar(actionHotbar)) {
                mc.getTextureManager().bind(OVERLAY_LOCATION);
                if (position.alignment == Alignment.LEFT) {
                    blit(matrixStack, x + hotbarLength - 2, y, 240, 240, 16, 16);
                }
                else {
                    blit(matrixStack, x - 20, y, 240, 240, 16, 16);
                }
            }
        }
        
        // highlight when hotbar key is pressed
        boolean highlightSelection = false;
        switch (actionKey) {
        case ATTACK:
            highlightSelection = attackSelection;
            break;
        case ABILITY:
            highlightSelection = abilitySelection;
            break;
        default:
            break;
        }
        if (highlightSelection) {
            int highlightAlpha = (int) (ClientUtil.getHighlightAlpha(tickCount + partialTick, 40F, 40F, 0.25F, 0.5F) * 255F);
            if (selected >= 0) {
                ClientUtil.fillSingleRect(x + hotbarFold.getSlotWithIndex(selected).pos - 4, y - 4, 24, 23, 255, 255, 255, highlightAlpha);
            }
            else {
                ClientUtil.fillSingleRect(x - 3, y - 3, hotbarLength, 22, 255, 255, 255, highlightAlpha);
            }
        }
        
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        // hotbar controls lock icon
        if (actionHotbar != null) {
            if (InputHandler.getInstance().areControlsLockedForHotbar(actionHotbar)) {
                mc.getTextureManager().bind(OVERLAY_LOCATION);
                if (position.alignment == Alignment.LEFT) {
                    blit(matrixStack, x + hotbarLength - 2, y, 240, 240, 16, 16);
                }
                else {
                    blit(matrixStack, x - 20, y, 240, 240, 16, 16);
                }
            }
        }
        
        // hold progress indicator
        x += 11;
        y += 10;
//        if (position.alignment == Alignment.RIGHT) {
//            x -= 20 * power.getActions(actionType).getEnabled().size();
//        }
        
        Action<P> heldAction = power.getHeldAction();
        int slot = -1;
        if (heldAction != null) {
            slot = actions.indexOf(
                    heldAction.isShiftVariation() ? heldAction.getBaseVariation() : heldAction);
            if (slot > -1) {
                renderActionHoldProgress(matrixStack, power, heldAction, power.getHeldActionTicks(), partialTick, x + hotbarFold.getSlotWithIndex(slot).pos, y);
            }
        }
        
        
        Action<P> selectedAction;
        if (actionHotbar != null) {
            selectedAction = mode.getSelectedAction(actionHotbar, shift, getMouseTarget());
        }
        else { // quick access slot
            selectedAction = resolveVisibleActionInSlot((Action<P>) actions.get(0), shift, power, target);
        }
        if (selectedAction != null && selectedAction != heldAction) {
            slot = selected;
            if (slot > -1) {
                renderActionHoldProgress(matrixStack, power, selectedAction, -1, partialTick, x + hotbarFold.getSlotWithIndex(slot).pos, y);
            }
        }
        
        return true;
    }

    private final SelectedTargetIcon skbaTargetIcon /*short for "selected keybind action"*/ = new SelectedTargetIcon();
    private <P extends IPower<P, ?>> void renderCustomKeybindActionSlot(MatrixStack matrixStack, 
            ElementPosition position, Action<P> action, ActionsModeConfig<?> currentMode, ActionTarget target, 
            int ordinal, float partialTick) {
        if (action == null) return;
        ActionsModeConfig<P> mode = (ActionsModeConfig<P>) currentMode;
        P power = mode.getPower();
        if (!power.hasPower()) return;
        
        int x = position.x;
        int y = position.y + getHotbarsYDiff() - 6 + getHotbarsYDiff() * ordinal;
        int hotbarLength = 22;
        if (position.alignment == Alignment.RIGHT) {
            x -= hotbarLength;
        }
        int selected = 0;
        ElementTransparency transparency = customKeybindActionTransparency.get(mode.powerClassification);
        float alpha = transparency.getAlpha(partialTick);

        switch (position.alignment) {
        case LEFT:
            x += 12;
            break;
        case RIGHT:
            x -= 12;
            break;
        }
        
        // hotbar
        HotbarRenderer.renderFoldingHotbar(matrixStack, mc, x, y, HotbarFold.makeHotbarFold(1, 0, 0, position.alignment), alpha);

        // action icon
        x += 3;
        y += 3;
        renderActionIcon(matrixStack, skbaTargetIcon, mode, action, target, x, y, partialTick, true, alpha);
        
        // target type icon
        int[] tex = skbaTargetIcon.getIconTex();
        if (tex != null) {
            mc.getTextureManager().bind(OVERLAY_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            int texX = tex[0];
            int texY = tex[1];
            int iconX = x + 20 * selected + 10;
            int iconY = y - 4;
            matrixStack.pushPose();
            matrixStack.scale(0.5F, 0.5F, 1F);
            matrixStack.translate(iconX, iconY, 0);
            blit(matrixStack, iconX, iconY, texX, texY, 32, 32);
            matrixStack.popPose();
        }
        
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        // hold progress indicator
        x += 11;
        y += 10;
//        if (position.alignment == Alignment.RIGHT) {
//            x -= 20;
//        }
        
        Action<P> heldAction = power.getHeldAction();
        if (heldAction == action) {
            renderActionHoldProgress(matrixStack, power, heldAction, power.getHeldActionTicks(), partialTick, x, y);
        }
    }
    
    private <P extends IPower<P, ?>> List<Action<?>> getEnabledActions(P power, InputHandler.ActionKey actionKey) {
        switch (actionKey) {
        case ATTACK:
        case ABILITY:
            return HudControlSettings.getInstance()
                    .getControlScheme(power)
                    .getActionsHotbar(actionKey.getHotbar())
                    .getEnabledActions();
        default:
            return Collections.emptyList();
        }
    }
    
    private void renderMouseIcon(MatrixStack matrixStack, int x, int y, InputHandler.ActionKey actionKey) {
        InputHandler.MouseButton button = null;
        switch (actionKey) {
        case ATTACK:
            button = InputHandler.MouseButton.LEFT;
            break;
        case ABILITY:
            button = InputHandler.MouseButton.RIGHT;
            break;
        }
        if (button != null) {
            renderMouseIcon(matrixStack, x, y, button);
        }
    }
    
    private void renderMouseIcon(MatrixStack matrixStack, int x, int y, InputHandler.MouseButton button) {
        blit(matrixStack, x, y, 216 + button.ordinal() * 10, 128, 9, 16);
    }
    
    private int getHotbarsYDiff() {
        return 2 * 2 + 22 + mc.font.lineHeight;
    }
    
    private <P extends IPower<P, ?>> void renderActionIcon(MatrixStack matrixStack, SelectedTargetIcon targetIcon, ActionsModeConfig<P> mode, 
            Action<P> action, ActionTarget target, int x, int y, 
            float partialTick, boolean isSelected, float hotbarAlpha) {
        renderActionIcon(matrixStack, targetIcon, mode, 
                action, target, x, y, 
                partialTick, isSelected, hotbarAlpha,
                0, 16);
    }
    
    private <P extends IPower<P, ?>> void renderActionIcon(MatrixStack matrixStack, SelectedTargetIcon targetIcon, ActionsModeConfig<P> mode, 
            Action<P> action, ActionTarget target, float x, float y, 
            float partialTick, boolean isSelected, float hotbarAlpha, 
            float leftCut, float cutWidth) {
        if (action == null) return;
        P power = mode.getPower();
        
        // green highlight
        boolean heldReadyToFire = power.getHeldAction() == action
                && action.getHoldDurationToFire(power) > 0
                && power.getHeldActionTicks() >= action.getHoldDurationToFire(power);
        if (heldReadyToFire) {
            ClientUtil.fillSingleRect(x - 2, y - 2, 20, 20, 0, 255, 0, 127);
        }
        
        ResourceLocation icon = action.getIconTexture(power);
        mc.getTextureManager().bind(icon);
        
        ActionConditionResult result = actionAvailability(action, mode, targetIcon, target, isSelected);
        if (!result.isPositive()) {
            if (!result.isQueued()) {
                RenderSystem.color4f(0.2F, 0.2F, 0.2F, 0.5F * hotbarAlpha);
            }
            else {
                RenderSystem.color4f(0.75F, 0.75F, 0.75F, 0.75F * hotbarAlpha);
            }
            if (cutWidth > 0) {
                ClientUtil.enableGlScissor(x + leftCut, y, cutWidth, 16);
                // action icon
                BlitFloat.blitFloat(matrixStack, 
                        x, y, 
                        0, 0, 
                        16, 16, 
                        16, 16);
                // cooldown
                float ratio = power.getCooldownRatio(action, partialTick);
                if (ratio > 0) {
                    ClientUtil.fillSingleRect(x, y + 16.0F * (1.0F - ratio), 16, 16.0F * ratio, 255, 255, 255, 127);
                }
                ClientUtil.disableGlScissor();
            }
        } else {
            RenderSystem.color4f(1, 1, 1, hotbarAlpha);
            if (cutWidth > 0) {
                ClientUtil.enableGlScissor(x + leftCut, y, cutWidth, 16);
                // action icon
                BlitFloat.blitFloat(matrixStack, 
                        x, y, 
                        0, 0, 
                        16, 16, 
                        16, 16);
                ClientUtil.disableGlScissor();
            }
        }
        // learning bar
        float learningProgress = power.getLearningProgressRatio(action);
        if (learningProgress >= 0 && learningProgress < 1) {
            RenderSystem.disableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();
            float barX = x + 2;
            float barY = y + 13;
            ClientUtil.fillRect(Tessellator.getInstance().getBuilder(), barX, barY, 13, 2, 0, 0, 0, 255);
            ClientUtil.fillRect(Tessellator.getInstance().getBuilder(), barX, barY, Math.round(learningProgress * 13.0F), 1, 0, 255, 0, 255);
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }
        // slot selection
        if (isSelected) {
            boolean greenSelection = heldReadyToFire || action.greenSelection(power, result);
            HotbarRenderer.renderSlotSelection(matrixStack, mc, x, y, hotbarAlpha, greenSelection);
        }
    }
    
    private <P extends IPower<P, ?>> ActionConditionResult actionAvailability(Action<P> action, ActionsModeConfig<P> mode, 
            SelectedTargetIcon targetIcon, ActionTarget mouseTarget, boolean isSelected) {
        P power = mode.getPower();
        ObjectWrapper<ActionTarget> targetContainer = new ObjectWrapper<>(mouseTarget);
        if (isSelected) {
            ActionConditionResult targetCheck = power.checkTarget(action, targetContainer);
            if (targetIcon != null) {
                targetIcon.update(action.getTargetRequirement(), targetCheck.isPositive());
            }
            if (!targetCheck.isPositive()) {
                return targetCheck;
            }
        }
        return power.checkRequirements(action, targetContainer, !isSelected);
    }
    
    public void setHotbarButtonsDows(boolean attack, boolean ability) {
        if (hotbarsEnabled) {
            altDeselectsAttack = attack;
            altDeselectsAbility = ability;
        }
        else {
            altDeselectsAttack = false;
            altDeselectsAbility = false;
        }
        
        this.attackSelection = attack;
        this.abilitySelection = ability;
    }
    
    public void setHotbarsEnabled(boolean enabled) {
        if (this.hotbarsEnabled && !enabled) {
            specialKeyDeselect();
        }
        this.hotbarsEnabled = enabled;
    }
    
    public void specialKeyDeselect() {
        if (altDeselectsAttack) {
            selectAction(ControlScheme.Hotbar.LEFT_CLICK, -1);
        }
        if (altDeselectsAbility) {
            selectAction(ControlScheme.Hotbar.RIGHT_CLICK, -1);
        }
    }
    
    public boolean areHotbarsEnabled() {
        return hotbarsEnabled;
    }
    
    private <P extends IPower<P, ?>> void drawHotbarText(MatrixStack matrixStack, ElementPosition position, 
            InputHandler.ActionKey actionKey, @Nonnull ActionsModeConfig<P> mode, ActionTarget target, 
            int color, int ordinal, float partialTick) {
        P power = mode.getPower();
        int x = position.x;
        int y = position.y + 16 + 3 + getHotbarsYDiff() * ordinal;
        switch (position.alignment) {
        case LEFT:
            x += 12;
            break;
        case RIGHT:
            x -= 12;
            break;
        }
        boolean shift = mc.player.isShiftKeyDown();
        Action<P> selectedAction = mode.getSelectedAction(actionKey.getHotbar(), shift, getMouseTarget());
        if (selectedAction != null) {
            // action name
            String translationKey = selectedAction.getTranslationKey(power, target);
            ITextComponent actionName = selectedAction.getTranslatedName(power, translationKey);
            if (selectedAction.getHoldDurationMax(power) > 0) {
                actionName = new TranslationTextComponent("jojo.overlay.hold", actionName);
            }
            ElementTransparency transparency = actionNameTransparency.get(actionKey);
            if (!actionName.equals(lastActionName.put(actionKey, actionName))) {
                transparency.reset();
            }
            if (selectedAction.hasShiftVariation()) {
                Action<P> shiftVar = selectedAction.getShiftVariationIfPresent().getVisibleAction(power, getMouseTarget());
                if (shiftVar != null) {
                    actionName = new TranslationTextComponent("jojo.overlay.shift", actionName, 
                            new KeybindTextComponent(mc.options.keyShift.getName()), 
                            shiftVar.getNameShortened(power, shiftVar.getTranslationKey(power, target)));
                }
            }
            
            float alpha = getNameAlpha(transparency, partialTick);
            
            if (alpha > 0) {
                if (!hotbarsEnabled) alpha *= 0.25F;
                int width = mc.font.width(actionName);
                RenderSystem.pushMatrix();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                
                drawBackdrop(matrixStack, x, y, width, position.alignment, null, alpha, 0);
                drawString(matrixStack, mc.font, actionName, x, y, position.alignment, color, alpha);
                
                RenderSystem.disableBlend();
                RenderSystem.popMatrix();
            }
        }
        else {
            lastActionName.remove(actionKey);
        }
    }
    
    private <P extends IPower<P, ?>> void drawCustomKeybindActionText(MatrixStack matrixStack, ElementPosition position, 
            Action<P> action, @Nonnull ActionsModeConfig<?> currentMode, ActionTarget target, 
            int color, int ordinal, float partialTick) {
        ActionsModeConfig<P> mode = (ActionsModeConfig<P>) currentMode;
        P power = mode.getPower();
        int x = position.x;
        int y = position.y + 16 + 3 + getHotbarsYDiff() * ordinal;
        switch (position.alignment) {
        case LEFT:
            x += 12;
            break;
        case RIGHT:
            x -= 12;
            break;
        }
        if (action != null) {
            // action name
            String translationKey = action.getTranslationKey(power, target);
            ITextComponent actionName = action.getTranslatedName(power, translationKey);
            if (action.getHoldDurationMax(power) > 0) {
                actionName = new TranslationTextComponent("jojo.overlay.hold", actionName);
            }
            if (action.hasShiftVariation()) {
                Action<P> shiftVar = action.getShiftVariationIfPresent().getVisibleAction(power, getMouseTarget());
                if (shiftVar != null) {
                    actionName = new TranslationTextComponent("jojo.overlay.shift", actionName, 
                            new KeybindTextComponent(mc.options.keyShift.getName()), 
                            shiftVar.getNameShortened(power, shiftVar.getTranslationKey(power, target)));
                }
            }
            
            ElementTransparency transparency = customKeybindActionTransparency.get(mode.powerClassification);
            float alpha = transparency.getAlpha(partialTick);
            
            if (alpha > 0) {
                if (!hotbarsEnabled) alpha *= 0.25F;
                int width = mc.font.width(actionName);
                RenderSystem.pushMatrix();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                
                drawBackdrop(matrixStack, x, y, width, position.alignment, null, alpha, 0);
                drawString(matrixStack, mc.font, actionName, x, y, position.alignment, color, alpha);
                
                RenderSystem.disableBlend();
                RenderSystem.popMatrix();
            }
        }
    }
    
    private float getNameAlpha(ElementTransparency transparency, float partialTick) {
        HudNamesRender renderMode = ClientModSettings.getSettingsReadOnly().hudNamesRender;
        switch (renderMode) {
        case NEVER:
            return 0;
        case FADE_AWAY:
            float alpha = transparency.getAlpha(partialTick);
            return alpha;
        default:
            return 1;
        }
    }
    
    
    
//    public boolean isSelectedActionHeld(ActionType actionType) {
//        return getSelectedActionHoldDuration(actionType, currentMode) > 0;
//    }
//    
//    private <P extends IPower<P, ?>> int getSelectedActionHoldDuration(ActionType actionType, @Nonnull ActionsModeConfig<P> mode) {
//        Action<P> action = mode.getSelectedAction(actionType, mc.player.isShiftKeyDown());
//        if (action != null) {
//            return action.getHoldDurationMax(mode.getPower());
//        }
//        return 0;
//    }
    
    

    private void renderPowerIcon(MatrixStack matrixStack, ElementPosition position, @Nullable ActionsModeConfig<?> mode, float alpha) {
        int x = position.x;
        if (position.alignment == Alignment.RIGHT) {
            x -= 16;
        }
        int y = position.y;
        if (mode != null && mode.getPower() != null && mode.getPower().isActive()) {
            if (alpha < 1.0F) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            }
            
            mc.getTextureManager().bind(mode.getPower().clGetPowerTypeIcon());
            blit(matrixStack, x, y, 0, 0, 16, 16, 16, 16);
            
            if (alpha < 1.0F) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }
    
    private void drawPowerName(MatrixStack matrixStack, ElementPosition position, @Nonnull ActionsModeConfig<?> mode, int color, float partialTick) {
        float alpha = getNameAlpha(powerNameTransparency, partialTick);
        if (alpha > 0) {
            int x = position.x + (position.alignment == Alignment.RIGHT ? -19 : 19);
            int y = position.y + (16 - mc.font.lineHeight) / 2;
            IPower<?, ?> power = mode.getPower();
            ITextComponent name = power.getName();
            drawBackdrop(matrixStack, x, y, mc.font.width(name), position.alignment, null, alpha, 0);
            drawString(matrixStack, mc.font, name, x, y, position.alignment, color, alpha);
        }
    }



    private void renderWarningIcons(MatrixStack matrixStack, ElementPosition position, List<ITextComponent> warningLines) {
        mc.getTextureManager().bind(OVERLAY_LOCATION);
        int x = position.x;
        int y = position.y - 4;
        for (int line = 0; line < warningLines.size(); line++) {
            blit(matrixStack, x, y, 132, 240, 16, 16);
            y += 16;
        }
    }
    
    private void drawWarningText(MatrixStack matrixStack, ElementPosition position, List<ITextComponent> warningLines) {
        int x = position.x;
        int y = position.y;
        switch (position.alignment) {
        case LEFT:
            x += 18;
            break;
        case RIGHT:
            x -= 2;
            break;
        }
        for (ITextComponent line : warningLines) {
            drawBackdrop(matrixStack, x, y, mc.font.width(line), position.alignment, null, 1.0F, 0);
            drawString(matrixStack, mc.font, line, x, y, position.alignment, 0xFFFFFF);
            y += 16;
        }
    }
    
    public void drawStandRemoteRange(MatrixStack matrixStack, float distance, float damageFactor) {
        int x = standStrengthPosition.x;
        int y = standStrengthPosition.y;
        Alignment alignment = standStrengthPosition.alignment;
        ITextComponent distanceString = new StringTextComponent(String.format("%.2f m", distance));
        drawBackdrop(matrixStack, x, y, mc.font.width(distanceString), alignment, null, 1.0F, 0);
        drawString(matrixStack, mc.font, distanceString, x, y, alignment, 0xFFFFFF);
        if (damageFactor < 1) {
            y += 12;
            ITextComponent strength = new TranslationTextComponent("jojo.overlay.stand_strength", String.format("%.2f%%", damageFactor * 100F));
            drawBackdrop(matrixStack, x, y, mc.font.width(strength), alignment, null, 1.0F, 0);
            drawString(matrixStack, mc.font, strength, x, y, alignment, 0xFF4040);
        }
    }
    
    

    private <P extends IPower<P, ?>> void renderActionHoldProgress(MatrixStack matrixStack, P power, Action<P> action, 
            int ticks, float partialTick, float x, float y) {
        if (action == null) return;

        int ticksToFire = action.getHoldDurationToFire(power);
        if (ticksToFire > 0) {
            float ratio;
            float alpha = !hotbarsEnabled ? 0.25F : 1.0F;
            if (ticks < 0) {
                ratio = 0;
                alpha *= 0.75F;
            }
            else {
                ratio = MathHelper.clamp(((float) ticks + partialTick) / (float) ticksToFire, 0, 1);
            }

            if (alpha < 1) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            }
            mc.getTextureManager().bind(RADIAL_INDICATOR);
            int deg = (int) (ratio * 360F);
            blitFloat(matrixStack, x, y, deg % 19 * 13, deg / 19 * 13, 13, 13);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    
    
    private final List<ActionsModeConfig<?>> modes = Collections.unmodifiableList(Arrays.asList(
            null,
            nonStandUiMode,
            standUiMode
            ));
    private void renderModeSelector(MatrixStack matrixStack, ElementPosition position, float partialTick) {
        if (modeSelectorTransparency.shouldRender()) {
            int x = position.x;
            int y = position.y;
            matrixStack.pushPose();
            matrixStack.translate(x, y, 0);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90));
            matrixStack.translate(-x, -y - 22, 0);
            HotbarRenderer.renderHotbar(matrixStack, mc, x, y, modes.size(), modeSelectorTransparency.getAlpha(partialTick));
            int selectedMode = modes.indexOf(currentMode);
            if (selectedMode > -1) {
                blit(matrixStack, x + selectedMode * 20 - 1, y - 1, 0, 22, 24, 24, 512, 512);
            }
            matrixStack.popPose();
            renderModeSelectorIcons(matrixStack, x + 3, y + 3, partialTick);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    
    
    private void renderModeSelectorIcons(MatrixStack matrixStack, int x, int y, float partialTick) {
        for (ActionsModeConfig<?> mode : modes) {
            if (mode != null) {
                IPower<?, ?> power = mode.getPower();
                if (power.hasPower()) {
                    mc.getTextureManager().bind(power.clGetPowerTypeIcon());
                    blit(matrixStack, x, y, 0, 0, 16, 16, 16, 16);
                }
            }
            y += 20;
        }
    }
    
    private void drawModeSelectorNames(MatrixStack matrixStack, ElementPosition position, int color, float partialTick) {
        if (modeSelectorTransparency.shouldRender()) {
            int x = position.x + (position.alignment == Alignment.LEFT ? 26 : -4);
            int y = position.y + (22 - mc.font.lineHeight) / 2;
            for (ActionsModeConfig<?> mode : modes) {
                ITextComponent name = getModeNameForSelector(mode);
                if (name != null) {
                    drawBackdrop(matrixStack, x, y, mc.font.width(name), position.alignment, modeSelectorTransparency, 0, partialTick);
                    drawString(matrixStack, mc.font, name, x, y, position.alignment, modeSelectorTransparency.makeTextColorTranclucent(color, partialTick));
                }
                y += 22;
            }
        }
    }
    
    @Nullable
    private ITextComponent getModeNameForSelector(ActionsModeConfig<?> mode) {
        ITextComponent name;
        if (mode == null) {
            if (currentMode == null) {
                return null;
            }
            name = new TranslationTextComponent("jojo.overlay.mode_deselect");
        }
        else {
            IPower<?, ?> power = mode.getPower();
            if (!power.hasPower()) {
                return null;
            }
            name = power.getName();
        }
        ITextComponent keyName = getKeyName(mode);
        if (keyName != null) {
            name = new TranslationTextComponent("jojo.overlay.mode_key", keyName, name);
        }
        return name;
    }

    private Map<ActionsModeConfig<?>, Supplier<KeyBinding>> modeKeys = ImmutableMap.of(
            nonStandUiMode, () -> InputHandler.getInstance().nonStandMode,
            standUiMode, () -> InputHandler.getInstance().standMode);
    @Nullable
    private ITextComponent getKeyName(ActionsModeConfig<?> mode) {
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
    
    
    
    private void renderLeapIcon(MatrixStack matrixStack, @Nonnull ActionsModeConfig<?> mode, int screenWidth, int screenHeight) {
        IPower<?, ?> power = mode.getPower();
        if (power.isLeapUnlocked()) {
            mc.getTextureManager().bind(OVERLAY_LOCATION);
            boolean rightSide = mc.player.getMainArm() == HandSide.RIGHT;
            int iconX = rightSide ? screenWidth / 2 + 91 + 6 : screenWidth / 2 - 91 - 22;
            if (mc.options.attackIndicator == AttackIndicatorStatus.HOTBAR) {
                if (rightSide) {
                    iconX += 20;
                }
                else {
                    iconX -= 20;
                }
            }
            int iconY = screenHeight - 20;
            
            float iconFill = power.getLeapCooldownPeriod() != 0 ? 
                    1F - (float) power.getLeapCooldown() / (float) power.getLeapCooldownPeriod() : 1;
            boolean translucent = !InputHandler.getInstance().canPlayerLeap();
            
            renderFilledIcon(matrixStack, iconX, iconY, translucent, iconFill, 
                    96, 238, 114, 238, 18, 18, 0xFFFFFF);
        }
    }
    
    private void renderIconBarAtCrosshair(MatrixStack matrixStack, int screenWidth, int screenHeight, float partialTick) {
        int xLeft = screenWidth / 2 - 24;
        int xRight = screenWidth / 2 + 8;
        int y = screenHeight / 2 - 8;
        
        boolean renderedBowCharge = false;
        if (nonStandUiMode != null && nonStandUiMode.getPower() != null) {
            renderedBowCharge |= renderBowChargeIcon(matrixStack, nonStandUiMode.getPower().getBowChargeEffect(), 
                    partialTick, xLeft, y);
        }
        if (standUiMode != null && standUiMode.getPower() != null) {
            renderedBowCharge |= renderBowChargeIcon(matrixStack, standUiMode.getPower().getBowChargeEffect(), 
                    partialTick, renderedBowCharge ? xRight : xLeft, y);
        }
        if (renderedBowCharge) return;
        
        if (currentMode == standUiMode) {
            IStandPower standPower = standUiMode.getPower();
            if (standPower.isActive()) {
                boolean isFinisherVariationUnlocked = standPower.getType()
                        .getStandFinisherPunch().map(action -> action.isUnlocked(standPower)).orElse(false);
                boolean isFinisherMechanicUnlocked = isFinisherVariationUnlocked || 
                        StandUtil.isFinisherMechanicUnlocked(standPower);
                if (isFinisherMechanicUnlocked) {
                    mc.getTextureManager().bind(OVERLAY_LOCATION);
                    IStandManifestation stand = standPower.getStandManifestation();
                    if (stand instanceof StandEntity) {
                        float finisherValue = ((StandEntity) stand).getFinisherMeter();
                        if (finisherValue > 0) {
                            int x = modeSelectorPosition.alignment == Alignment.LEFT ? xLeft - 1 : xRight + 1;
                            boolean heavyVariation = isFinisherVariationUnlocked && ((StandEntity) stand).willHeavyPunchBeFinisher();
                            renderFilledIcon(matrixStack, x, y - 1, false, finisherValue, 
                                    96, 216, heavyVariation ? 132 : 114, 216, 18, 18, 0xFFFFFF);
                        }
                    }
                }
            }
        }
    }
    
    private boolean renderBowChargeIcon(MatrixStack matrixStack, BowChargeEffectInstance<?, ?> bowCharge, float partialTick, int x, int y) {
        if (bowCharge != null && bowCharge.isBeingCharged()) {
            mc.getTextureManager().bind(bowCharge.getPower().clGetPowerTypeIcon());
            float fill = bowCharge.getProgress(partialTick);
            if (fill < 1) {
                RenderSystem.color4f(0, 0, 0, 1);
                BlitFloat.blitFloat(matrixStack, x, y, 0, 0, 16, 16 * (1 - fill), 16, 16);
                RenderSystem.color4f(1, 1, 1, 1);
                float px = 16F * fill;
                BlitFloat.blitFloat(matrixStack, x, y + 16 - px, 0, 16 - px, 16, px, 16, 16);
            }
            else {
                blit(matrixStack, x, y, 0, 0, 16, 16, 16, 16);
                float chargeCompletionTicks = bowCharge.getTicksAfterFullCharge() + partialTick;
                if (chargeCompletionTicks < 10) {
                    float whiteHighlight;
                    if (chargeCompletionTicks < 2F) {
                        whiteHighlight = chargeCompletionTicks * 0.5F;
                    }
                    else {
                        whiteHighlight = MathUtil.fadeOut(chargeCompletionTicks, 10, 0.3F);
                    }
                    // no idea how that works in the slightest
                    // wanted to make the icon fully white
                    // instead it just gets a bit brighter
                    // it is what it is
                    RenderSystem.blendFuncSeparate(
                            GlStateManager.SourceFactor.SRC_ALPHA, 
                            GlStateManager.DestFactor.CONSTANT_ALPHA, 
                            GlStateManager.SourceFactor.ONE, 
                            GlStateManager.DestFactor.ZERO);
                    RenderSystem.color4f(whiteHighlight, whiteHighlight, whiteHighlight, 1.0F);
                    
                    blit(matrixStack, x, y, 0, 0, 16, 16, 16, 16);
                    
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.defaultBlendFunc();
                }
            }
            return true;
        }
        return false;
    }

    private void renderFilledIcon(MatrixStack matrixStack, int x, int y, boolean translucent, float fill, 
            int texX, int texY, int fillTexX, int fillTexY, int texWidth, int texHeight, int color) {
        blit(matrixStack, x, y, texX, texY, texWidth, texHeight);
        float[] rgb = ClientUtil.rgb(color);
        float alpha = 1.0F;
        if (translucent) {
            for (int i = 0; i < 3; i++) {
                rgb[i] *= 0.5F;
            }
            alpha = 0.75F;
        }
        RenderSystem.color4f(rgb[0], rgb[1], rgb[2], alpha);
        float px = 18F * fill;
        blitFloat(matrixStack, x, y + texHeight - px, fillTexX, fillTexY + texHeight - px, texWidth, px);
        if (translucent) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void renderHamonExerciseBars(MatrixStack matrixStack, ElementPosition position, HamonData hamon, float partialTick) {
        mc.getTextureManager().bind(HamonScreen.WINDOW);
        int x = position.x;
        int y = position.y;
        for (Exercise exercise : Exercise.values()) {
            ElementTransparency transparency = exerciseBarsTransparency.get(exercise);
            if (transparency.shouldRender()) {
                HamonStatsTabGui.drawExerciseBar(this, matrixStack, x, y, hamon, exercise, transparency.getAlpha(partialTick), false);
                y += 9;
            }
        }
    }
    
    
    
    void drawString(MatrixStack matrixStack, FontRenderer font, ITextComponent text, int x, int y, Alignment alignment, int color, float alpha) {
        if (alpha > 0) {
            drawString(matrixStack, font, text, x, y, alignment, color + ((int) (Math.min(alpha, 1F) * 256F) << 24));
        }
    }
    
    void drawString(MatrixStack matrixStack, FontRenderer font, ITextComponent text, int x, int y, Alignment alignment, int color) {
        if (alignment == Alignment.RIGHT) {
            x -= font.width(text);
        }
        drawString(matrixStack, font, text, x, y, color);
    }
    
    void drawBackdrop(MatrixStack matrixStack, int x, int y, int width, Alignment alignment, 
            @Nullable ElementTransparency transparency, float alpha, float partialTick) {
        int backdropColor = mc.options.getBackgroundColor(0.0F);
        if (backdropColor != 0) {
            if (alignment == Alignment.RIGHT) {
                x -= width;
            }
            ClientUtil.drawBackdrop(matrixStack, x, y, width, 
                    transparency != null ? transparency.getAlpha(partialTick) : alpha);
        }
    }
    
    
    
    public boolean setMode(@Nullable PowerClassification power) {
        if (power == null) {
            return setPowerMode(null);
        }
        else {
            ActionsModeConfig<?> chosenMode = null;
            switch (power) {
            case NON_STAND:
                chosenMode = nonStandUiMode;
                break;
            case STAND:
                chosenMode = standUiMode;
                break;
            }
            return setPowerMode(chosenMode);
        }
    }
    
    private final PowerClassification[] modesOrder = {
            null,
            PowerClassification.NON_STAND,
            PowerClassification.STAND
    };
    public void scrollMode() {
        int iCurrent = 0;
        int modes = modesOrder.length;
        for (; iCurrent < modes && getCurrentMode() != modesOrder[iCurrent]; iCurrent++);
        for (int i = 1; i <= modes || !setMode(modesOrder[(iCurrent + i) % modes]); i++);
    }
    
    private boolean setPowerMode(@Nullable ActionsModeConfig<?> mode) {
        if (mode != null && currentMode != mode) {
            if (mode.getPower().hasPower()) {
                modeSelectorTransparency.reset();
                powerNameTransparency.reset();
                actionNameTransparency.values().forEach(ElementTransparency::reset);
                actionHotbarFold.values().forEach(FadeOut::reset);
                if (currentMode != null) {
                    if (mode != nonStandUiMode) {
                        energyBarTransparency.reset();
                    }
                    else if (mode != standUiMode) {
                        staminaBarTransparency.reset();
                        resolveBarTransparency.reset();
                    }
                }
                currentMode = mode;
                currentMode.resetSelectedTick();
                return true;
            }
            return false;
        }
        else {
            modeSelectorTransparency.reset();
            if (currentMode == nonStandUiMode) {
                energyBarTransparency.reset();
            }
            else if (currentMode == standUiMode) {
                staminaBarTransparency.reset();
                resolveBarTransparency.reset();
            }
            currentMode = null;
            return true;
        }
    }
    
    public void revealActionNames() {
        powerNameTransparency.reset();
        actionNameTransparency.values().forEach(ElementTransparency::reset);
    }
    
    public void onStandSummon() {
        if (currentMode != standUiMode) {
            setPowerMode(standUiMode);
            standUiMode.autoOpened = true;
        }
    }
    
    public void onStandUnsummon() {
        if (currentMode == standUiMode && standUiMode.autoOpened) {
            setMode(null);
            standUiMode.autoOpened = false;
        }
    }
    
    
    
    public void selectAction(ControlScheme.Hotbar hotbar, int slot) {
        if (currentMode != null) {
            currentMode.setSelectedSlot(hotbar, slot, getMouseTarget());
            actionHotbarFold.get(hotbar).reset();
        }
    }


    public void scrollAction(ControlScheme.Hotbar hotbar, boolean backwards) {
        if (currentMode != null) {
            scrollAction(currentMode, hotbar, backwards);
        }
    }

    private static final IntBinaryOperator INC = (i, n) -> (i + 2) % (n + 1) - 1;
    private static final IntBinaryOperator DEC = (i, n) -> (i + n + 1) % (n + 1) - 1;
    private <P extends IPower<P, ?>> void scrollAction(ActionsModeConfig<P> mode, ControlScheme.Hotbar hotbar, boolean backwards) {
        P power = mode.getPower();
        List<Action<?>> actions = HudControlSettings.getInstance()
                .getControlScheme(power)
                .getActionsHotbar(hotbar).getEnabledActions();
        if (actions.size() == 0) {
            return;
        }
        int startingIndex = mode.getSelectedSlot(hotbar);
        IntBinaryOperator operator = backwards ? DEC : INC;
        int i;
        ActionTarget target = getMouseTarget();
        for (i = operator.applyAsInt(startingIndex, actions.size()); 
             i > -1 && i % actions.size() != startingIndex && ((Action<P>) actions.get(i)).getVisibleAction(power, target) == null;
             i = operator.applyAsInt(i, actions.size())) {
        }
        mode.setSelectedSlot(hotbar, i, target);
        actionHotbarFold.get(hotbar).reset();
    }
    


    @Nullable
    public <P extends IPower<P, ?>> Pair<Action<P>, Boolean> onClick(P power, ControlScheme.Hotbar mouseButton, boolean shiftVariant, boolean sneak) {
        if (currentMode != null) {
            int selectedIndex = currentMode.getSelectedSlot(mouseButton);
            if (selectedIndex >= 0) {
                return onClick(power, mouseButton, shiftVariant, sneak, selectedIndex);
            }
        }

        return Pair.of(null, false);
    }

    @Nullable
    public <P extends IPower<P, ?>> Pair<Action<P>, Boolean> onClick(
            P power, ControlScheme.Hotbar hotbar, boolean shiftVariant, boolean sneak, int index) {
        Action<P> action = (Action<P>) HudControlSettings.getInstance()
                .getControlScheme(getCurrentMode())
                .getActionsHotbar(hotbar)
                .getBaseActionInSlot(index);
        action = resolveVisibleActionInSlot(action, shiftVariant, power, getMouseTarget());
        return onActionClick(power, action, sneak);
    }
    
    @Nullable
    public static <P extends IPower<P, ?>> Action<P> resolveVisibleActionInSlot(
            Action<P> baseAction, boolean shiftVariant, P power, ActionTarget target) {
        if (baseAction == null) return null;
        
        baseAction = baseAction.getVisibleAction(power, target);
        Action<P> held = power.getHeldAction();
        if (baseAction == held) {
            return baseAction;
        }
        if (baseAction != null && baseAction.hasShiftVariation()) {
            Action<P> shiftVar = baseAction.getShiftVariationIfPresent().getVisibleAction(power, target);
            if (shiftVar != null && (shiftVariant || shiftVar == held)) {
                baseAction = shiftVar;
            }
        }
        return baseAction;
    }
    
    // sends the packet which fires the action to the server
    @Nullable
    public <P extends IPower<P, ?>> Pair<Action<P>, Boolean> onActionClick(P power, Action<P> action, boolean sneak) {
        if (power != null && action != null) {
            if (power.getHeldAction() != null && action.getHoldDurationMax(power) > 0) {
                return Pair.of(action, true);
            }
            ActionTarget mouseTarget = getMouseTarget();
            ClClickActionPacket packet = new ClClickActionPacket(
                    power.getPowerClassification(), action, mouseTarget, sneak);
            PacketManager.sendToServer(packet);
            boolean actionWentOff = power.clickAction(action, sneak, mouseTarget);
            return Pair.of(action, actionWentOff);
        }
        return null;
    }
    
    
    
    // determines if the action is selected in a hotbar or is available in one mouse click/key press in any other way (quick access)
    // used for stuff like Crazy Diamond's terrain restoration (to show previously broken blocks), markers for homing bullets and anchor blocks, etc.
    public boolean showExtraActionHud(Action<?> action) {
        if (!areHotbarsEnabled() || currentMode == null) {
            return false;
        }
        boolean shift = InputHandler.useShiftActionVariant(mc);
        
        for (ControlScheme.Hotbar hotbar : ControlScheme.Hotbar.values()) {
            if (currentMode.getSelectedAction(hotbar, shift, getMouseTarget()) == action) {
                return true;
            }
        }
        
        /* Eclipse doesn't show me this compilation error
             method ... cannot be applied to given types;
             required: P,boolean
             found: capture#1 of ?,boolean
             reason: inference variable P has incompatible bounds
                 equality constraints: capture#1 of ?
                 lower bounds: com.github.standobyte.jojo.power.IPower<capture#1 of ?,capture#2 of ?>
        */
//        if (getQuickAccessAction(currentMode.getPower(), shift) == action) {
//            return true;
//        }
        
        return false;
    }
    
    public boolean isActionSelectedAndEnabled(Action<?> action) {
        return isActionSelectedAndEnabled(a -> a == action);
    }
    
    public boolean isActionSelectedAndEnabled(Action<?>... anyOf) {
        return isActionSelectedAndEnabled(a -> Stream.of(anyOf).anyMatch(action -> a == action));
    }
    
    public boolean isActionSelectedAndEnabled(Predicate<Action<?>> action) {
        return getSelectedEnabledActions().anyMatch(action);
        // FIXME (hud) generics moment
//                || action.test(getQuickAccessAction(currentMode.getPower(), shift));
    }
    
    public Stream<Action<?>> getSelectedEnabledActions() {
        if (!areHotbarsEnabled() || currentMode == null) {
            return Stream.empty();
        }
        boolean shift = mc.player.isShiftKeyDown();
        return Stream.of(ControlScheme.Hotbar.values()).map(hotbar -> currentMode.getSelectedAction(hotbar, shift, getMouseTarget()));
    }
    
    public void setCustomKeybindAction(PowerClassification power, Action<?> action) {
        ActionsModeConfig<?> hudMode = getHudMode(power);
        if (hudMode != null) {
            hudMode.lastCustomKeybindAction = action;
            customKeybindActionTransparency.get(power).reset();
        }
    }
    
    
    
    public void updatePowersCache() {
        setMode(null);
        standUiMode.setPower(IStandPower.getPlayerStandPower(mc.player));
        standUiMode.autoOpened = false;
        nonStandUiMode.setPower(INonStandPower.getPlayerNonStandPower(mc.player));
        nonStandUiMode.autoOpened = false;
    }
    
    private void blitFloat(MatrixStack pMatrixStack, float pX, float pY, 
            float pUOffset, float pVOffset, float pUWidth, float pVHeight) {
        BlitFloat.blitFloat(pMatrixStack, 
                pX, pY, this.getBlitOffset(), 
                pUOffset, pVOffset, pUWidth, pVHeight, 256, 256);
    }
    
    
    
    private boolean outOfBreath = false;
    private boolean outOfBreathMaskSprite = false;
    private int outOfBreathSpriteTicks = 0;
    private float prevAir;
    private float vignetteBeforeFadeAway = -1;
    public void setOutOfBreath(boolean mask) {
        outOfBreath = true;
        outOfBreathMaskSprite = mask;
        outOfBreathSpriteTicks = 15;
        vignetteBeforeFadeAway = -1;
        prevAir = 0;
    }
    
    public boolean isPlayerOutOfBreath() {
        return outOfBreath;
    }
    
    private void tickOutOfBreathEffect() {
        if (outOfBreath) {
            prevAir = mc.player.getAirSupply();
            if (prevAir >= mc.player.getMaxAirSupply()) {
                outOfBreath = false;
            }
        }
        if (outOfBreathSpriteTicks > 0) outOfBreathSpriteTicks--;
    }
    
    private void renderOutOfBreathSprite(MatrixStack matrixStack, float partialTick, int windowWidth, int windowHeight) {
        if (outOfBreathSpriteTicks > 0) {
            boolean bubblePopped = outOfBreathSpriteTicks < 11;
            mc.getTextureManager().bind(ClientUtil.ADDITIONAL_UI);
            blit(matrixStack, windowWidth / 2 - 16, windowHeight / 2 - 16, bubblePopped ? 160 : 128, outOfBreathMaskSprite ? 32 : 0, 32, 32);
        }
    }
    
    private void renderOutOfBreathVignette(MatrixStack matrixStack, float partialTick) {
        if (outOfBreath) {
            float air = MathHelper.lerp(partialTick, prevAir, (float) mc.player.getAirSupply()) / (float) mc.player.getMaxAirSupply();
            float vignette;
            if (air < 0.75F) {
                vignette = 0.8F + (MathHelper.sin((tickCount + partialTick) * 0.2F) + 1) * 0.1F;
            }
            else {
                if (vignetteBeforeFadeAway < 0) {
                    vignetteBeforeFadeAway = 0.8F + (MathHelper.sin((tickCount + partialTick) * 0.2F) + 1) * 0.1F;
                }
                vignette = 4 * (-air + 1) * vignetteBeforeFadeAway;
            }
            renderVignette(matrixStack, vignette, vignette, vignette);
        }
    }

    private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/vignette.png");
    private void renderVignette(MatrixStack matrixStack, float r, float g, float b) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.color4f(r, g, b, 1.0F);

        double screenWidth = mc.getWindow().getGuiScaledWidth();
        double screenHeight = mc.getWindow().getGuiScaledHeight();
        mc.getTextureManager().bind(VIGNETTE_LOCATION);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.vertex(0.0D, screenHeight, -90.0D).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(screenWidth, screenHeight, -90.0D).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(screenWidth, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
        tessellator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }
    
    
    
    private class ElementPosition {
        private int x;
        private int y;
        private Alignment alignment;
    }

    private static final int INDENT = 4;
    public enum PositionConfig {
        TOP_LEFT(Alignment.LEFT, BarsOrientation.HORIZONTAL, 
                screenWidth -> INDENT, (screenHeight, elementHeight) -> INDENT),
        TOP_RIGHT(Alignment.RIGHT, BarsOrientation.HORIZONTAL, 
                screenWidth -> screenWidth - INDENT, (screenHeight, elementHeight) -> INDENT),
        LEFT(Alignment.LEFT, BarsOrientation.VERTICAL, 
                screenWidth -> INDENT, (screenHeight, elementHeight) -> (screenHeight - elementHeight) / 2),
        RIGHT(Alignment.RIGHT, BarsOrientation.VERTICAL, 
                screenWidth -> screenWidth - INDENT, (screenHeight, elementHeight) -> (screenHeight - elementHeight) / 2);
        
        final Alignment aligment;
        final BarsOrientation barsOrientation;
        private final IntUnaryOperator xPos;
        private final IntBinaryOperator yPos;
        
        private PositionConfig(Alignment alignment, BarsOrientation barsOrientation, 
                IntUnaryOperator xPos, IntBinaryOperator yPos) {
            this.aligment = alignment;
            this.barsOrientation = barsOrientation;
            this.xPos = xPos;
            this.yPos = yPos;
        }
        
        int getXPos(int screenWidth) {
            return xPos.applyAsInt(screenWidth);
        }
        
        int getYPos(int screenHeight, int elementHeight) {
            return yPos.applyAsInt(screenHeight, elementHeight);
        }
    }
    
    public enum Alignment {
        LEFT,
        RIGHT
    }
    
    enum BarsOrientation {
        VERTICAL,
        HORIZONTAL
    }
    
    public enum HudNamesRender {
        ALWAYS,
        FADE_AWAY,
        NEVER
    }
}
