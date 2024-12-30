package com.github.standobyte.jojo.client.ui.actionshud;

import static com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer.BARS_WIDTH_PX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
import com.github.standobyte.jojo.client.ControllerStand;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.controls.ActionKeybindEntry;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.controls.HudControlSettings;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.client.ui.BlitFloat;
import com.github.standobyte.jojo.client.ui.ShortKeybindTextComponent;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsModeConfig.SelectedTargetIcon;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer.BarType;
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
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.stand.IStandManifestation;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.general.ObjectWrapper;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("deprecation")
public class ActionsOverlayGui extends AbstractGui {
    public static final ResourceLocation OVERLAY_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/overlay.png");
    protected static final ResourceLocation RADIAL_INDICATOR = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/radial_indicator.png");
    
    protected static ActionsOverlayGui instance = null;
    protected final Minecraft mc;
    protected int tickCount;
    
    public final ActionsModeConfig<INonStandPower> nonStandUiMode = new ActionsModeConfig<>(PowerClassification.NON_STAND);
    public final ActionsModeConfig<IStandPower> standUiMode = new ActionsModeConfig<>(PowerClassification.STAND);
    @Nullable
    protected ActionsModeConfig<?> currentMode = null;
    
    protected final ElementTransparency modeSelectorTransparency = new ElementTransparency(40, 10);
    protected final ElementTransparency energyBarTransparency = new ElementTransparency(40, 10);
    protected final ElementTransparency staminaBarTransparency = new ElementTransparency(40, 10);
    protected final ElementTransparency resolveBarTransparency = new ElementTransparency(40, 10);
    protected final ElementTransparency powerNameTransparency = new ElementTransparency(40, 10);
    protected final ElementTransparency crosshairFillTransparency = new ElementTransparency(8, 6) {
        private static final int FADE_IN_TICKS = 2;
        @Override
        float getValue(float partialTick) {
            if (ticksMax - ticks <= FADE_IN_TICKS) {
                return (ticksMax - ticks + partialTick) / FADE_IN_TICKS;
            }
            return super.getValue(partialTick);
        }
    };
    protected Action<?> crosshairFillLastAction;
    protected final Map<InputHandler.ActionKey, ElementTransparency> actionNameTransparency = Arrays.stream(InputHandler.ActionKey.values())
            .collect(Maps.toImmutableEnumMap(hotbar -> hotbar, hotbar -> new ElementTransparency(40, 10)));
    protected final Map<InputHandler.ActionKey, ITextComponent> lastActionName = new EnumMap<>(InputHandler.ActionKey.class);
    protected final Map<ControlScheme.Hotbar, FadeOut> actionHotbarFold = Arrays.stream(ControlScheme.Hotbar.values())
            .collect(Maps.toImmutableEnumMap(hotbar -> hotbar, hotbar -> new FadeOut(40, 10)));
    protected final Map<PowerClassification, ElementTransparency> customKeybindActionTransparency = Arrays.stream(PowerClassification.values())
            .collect(Maps.toImmutableEnumMap(Function.identity(), __ -> new ElementTransparency(40, 10)));
    protected final Map<Exercise, ElementTransparency> exerciseBarsTransparency = Arrays.stream(Exercise.values())
            .collect(Maps.toImmutableEnumMap(Function.identity(), __ -> new ElementTransparency(40, 10)));
    protected final Map<HamonStatIncNotif, ElementTransparency> hamonLvlIncreaseTransparency = Arrays.stream(HamonStatIncNotif.values())
            .collect(Maps.toImmutableEnumMap(Function.identity(), __ -> new ElementTransparency(100, 20)));
    
    public static enum HamonStatIncNotif {
        STRENGTH,
        CONTROL,
        BREATHING
    }
    
    protected final BarsRenderer verticalBars = new VerticalBarsRenderer(this, 
            energyBarTransparency, staminaBarTransparency, resolveBarTransparency);
    protected final BarsRenderer horizontalBars = new HorizontalBarsRenderer(this, 
            energyBarTransparency, staminaBarTransparency, resolveBarTransparency);
    
    protected FadeOut[] tickingFadeOut = Streams.concat(Stream.of(
            modeSelectorTransparency,
            energyBarTransparency,
            staminaBarTransparency,
            resolveBarTransparency,
            powerNameTransparency,
            crosshairFillTransparency), 
            actionNameTransparency.values().stream(),
            actionHotbarFold.values().stream(),
            customKeybindActionTransparency.values().stream(),
            exerciseBarsTransparency.values().stream(),
            hamonLvlIncreaseTransparency.values().stream())
            .toArray(FadeOut[]::new);
    
    protected boolean attackSelection;
    protected boolean abilitySelection;
    protected boolean hotbarsEnabled;
    protected boolean altDeselectsAttack;
    protected boolean altDeselectsAbility;
    
    protected ActionsOverlayGui(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            try {
                instance = new ActionsOverlayGui(mc);
                MinecraftForge.EVENT_BUS.register(instance);
            }
            catch (Throwable e) {
                JojoMod.getLogger().error(e);
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
    
    public void onHamonStatIncreased(HamonStatIncNotif hamonStat) {
        hamonLvlIncreaseTransparency.get(hamonStat).reset();
    }
    
    public boolean isActive() {
        return currentMode != null && currentMode.getControlScheme().hotbarsEnabled;
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
    
    protected ActionsModeConfig<?> getHudMode(PowerClassification power) {
        switch (power) {
        case STAND:
            return standUiMode;
        case NON_STAND:
            return nonStandUiMode;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    

    
    protected ActionTarget _target;
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void render(RenderGameOverlayEvent.Pre event) {
        _target = null;
        if (!hudRenders()) {
            return;
        }
        RenderGameOverlayEvent.ElementType elementTypeRender = event.getType();
        
        MatrixStack matrixStack = event.getMatrixStack();
        float partialTick = event.getPartialTicks();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        PositionConfig hotbarsPosConfig = ClientModSettings.getSettingsReadOnly().hotbarsPosition;
        PositionConfig barsPosConfig = ClientModSettings.getSettingsReadOnly().barsPosition;
        boolean showModeSelector = false;
        
        switch (elementTypeRender) {
        case ALL:
            updateHotkeyUi();
            
            for (int i = 0; i < hotbarIsRendered.length; i++) {
                hotbarIsRendered[i] = false;
            }
            if (hotbarWillRender(currentMode, InputHandler.ActionKey.ATTACK))       hotbarIsRendered[0] = true;
            if (hotbarWillRender(currentMode, InputHandler.ActionKey.ABILITY))      hotbarIsRendered[1] = true;
            if (!hotkeyInHudActions.isEmpty())                                      hotbarIsRendered[2] = true;
            if (hotkeyRenderOffHudAction)                                           hotbarIsRendered[3] = true;
            
            
            updateWarnings(currentMode);
            updateElementPositions(barsPosConfig, hotbarsPosConfig, 
                    hotbarIsRendered[0], hotbarIsRendered[1], hotbarIsRendered[2], hotbarIsRendered[3], 
                    screenWidth, screenHeight);
            
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
            renderPowerIcon(matrixStack, lmbHotbarPosition, modeIcon, standAlpha);
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
                
                if (hotbarIsRendered[0]) {
                    renderActionsHotbar(matrixStack, lmbHotbarPosition, InputHandler.ActionKey.ATTACK,
                            currentMode, getMouseTarget(), partialTick);
                }
                if (hotbarIsRendered[1]) {
                    renderActionsHotbar(matrixStack, rmbHotbarPosition, InputHandler.ActionKey.ABILITY,
                            currentMode, getMouseTarget(), partialTick);
                }
                
                renderLeapIcon(matrixStack, currentMode, screenWidth, screenHeight);
                
                RenderSystem.disableRescaleNormal();
                RenderSystem.disableBlend();
                break;
            case TEXT:
                int color = getPowerUiColor(currentMode.getPower());
                drawPowerName(matrixStack, lmbHotbarPosition, currentMode, color, partialTick);

                if (hotbarIsRendered[0]) drawHotbarText(matrixStack, lmbHotbarPosition, InputHandler.ActionKey.ATTACK, 
                        currentMode, getMouseTarget(), color, partialTick);
                if (hotbarIsRendered[1]) drawHotbarText(matrixStack, rmbHotbarPosition, InputHandler.ActionKey.ABILITY, currentMode, 
                        getMouseTarget(), color, partialTick);
                break;
            default:
                break;
            }
        }

        if (hotbarIsRendered[2]) {
            switch (elementTypeRender) {
            case ALL:
                RenderSystem.enableRescaleNormal();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                
                renderInHudKeybindActionSlots(matrixStack, inHudHotkeysPosition, getMouseTarget(), partialTick);
                
                RenderSystem.disableRescaleNormal();
                RenderSystem.disableBlend();
                break;
            case TEXT:
                renderInHudKeybindActionNames(matrixStack, inHudHotkeysPosition);
                break;
            default:
                break;
            }
        }
        
        if (hotbarIsRendered[3]) {
            Action<?> action = lastHotkeyPressedAction.getAction();
            ActionsModeConfig<?> hudMode = getHudMode(action.getPowerClassification());
            switch (elementTypeRender) {
            case ALL:
                RenderSystem.enableRescaleNormal();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                
                renderOffHudKeybindActionSlot(matrixStack, 
                        offHudHotkeyPosition, action, 
                        getMouseTarget(), partialTick);
                
                RenderSystem.disableRescaleNormal();
                RenderSystem.disableBlend();
                break;
            case TEXT:
                drawCustomKeybindActionText(matrixStack, offHudHotkeyPosition, 
                        action, hudMode, getMouseTarget(), 
                        getPowerUiColor(hudMode.getPower()), partialTick);
                break;
            default:
                break;
            }
        }

        switch (elementTypeRender) {
        case ALL:
            renderWarningIcons(matrixStack, warningsPosition, warningLines);
            break;
        case TEXT:
            drawWarningText(matrixStack, warningsPosition, warningLines);
            if (ControllerStand.getInstance().isControllingStand()) {
                StandEntity stand = ControllerStand.getInstance().getManuallyControlledStand();
                drawStandRemoteRange(matrixStack, stand.distanceTo(mc.player), (float) stand.getRangeEfficiency());
            }
            break;
        default:
            break;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void renderPost(RenderGameOverlayEvent.Post event) {
        if (!hudRenders()) {
            return;
        }
        
        MatrixStack matrixStack = event.getMatrixStack();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        float partialTick = event.getPartialTicks();
        switch (event.getType()) {
        case CROSSHAIRS:
            if (mc.options.getCameraType().isFirstPerson()
                    && !(mc.options.renderDebug && !mc.player.isReducedDebugInfo() && !mc.options.reducedDebugInfo)) {
                RenderSystem.defaultBlendFunc();
                renderCrosshair(matrixStack, screenWidth, screenHeight, partialTick);
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            }
            break;
        default:
            break;
        }
    }
    
    protected boolean hudRenders() {
        return !JojoModUtil.tmpSpectatorCantUsePowers(mc.player) && !mc.options.hideGui && !mc.player.isDeadOrDying();
    }
    
    protected boolean[] hotbarIsRendered = new boolean[4];
    
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
            if (power.hasPower()) {
                return ((INonStandPower) power).getType().getColor().orElse(-1);
            }
            break;
        case STAND:
            return ((IStandPower) power).getStandInstance()
                    .flatMap(StandSkinsManager.getInstance()::getStandSkin)
                    .map(skin -> skin.color)
                    .orElse(-1);
        }
        return -1;
    }

    

    protected ActionKeybindEntry lastHotkeyPressedAction;
    protected boolean hotkeyRenderOffHudAction;
    protected List<HudHotkey> hotkeyInHudActions = new ArrayList<>();
    protected void updateHotkeyUi() {
        hotkeyRenderOffHudAction = lastHotkeyPressedAction != null;
        hotkeyInHudActions.clear();
        
        for (PowerClassification power : PowerClassification.values()) {
            ActionsModeConfig<?> mode = getHudMode(power);
            if (!customKeybindActionTransparency.get(power).shouldRender()) {
                if (lastHotkeyPressedAction == mode.lastHotkeyAction) {
                    lastHotkeyPressedAction = null;
                    hotkeyRenderOffHudAction = false;
                }
                mode.lastHotkeyAction = null;
            }
            
            for (ActionKeybindEntry entry : HudControlSettings.getInstance().getControlScheme(power).getCustomKeybinds()) {
                if (entry.isVisibleInHud() && entry.getHudInteraction().canTrigger(power == getCurrentMode()) && !entry.getKeybind().isUnbound()) {
                    IFormattableTextComponent keyName = 
                            new StringTextComponent("[")
                            .append(new ShortKeybindTextComponent(entry.getKeybind()))
                            .append(new StringTextComponent("]"));
                    hotkeyInHudActions.add(new HudHotkey(entry, keyName));
                    if (entry == mode.lastHotkeyAction) {
                        hotkeyRenderOffHudAction = false;
                    }
                }
            }
        }
    }
    
    List<ActionKeybindEntry> heldThisTick = new ArrayList<>();
    public void resetHeldThisTick() {
        heldThisTick.clear();
    }
    
    public void setHeldThisTick(ActionKeybindEntry action) {
        heldThisTick.add(action);
    }
    
    public void setCustomKeybindAction(PowerClassification power, ActionKeybindEntry action) {
        ActionsModeConfig<?> hudMode = getHudMode(power);
        if (hudMode != null) {
            hudMode.lastHotkeyAction = action;
            customKeybindActionTransparency.get(power).reset();
        }
        lastHotkeyPressedAction = action;
    }
    
    protected static class HudHotkey {
        final ActionKeybindEntry actionEntry;
        final IFormattableTextComponent keyName;
        final int maxWidth;
        
        public HudHotkey(ActionKeybindEntry actionEntry, IFormattableTextComponent keyName) {
            this.actionEntry = actionEntry;
            this.keyName = keyName;
            this.maxWidth = Minecraft.getInstance().font.width(keyName.copy().withStyle(TextFormatting.BOLD));
        }
    }
    
    

    protected static final int HOTBARS_ELEMENT_HEIGHT_PX = 110;
    
    protected final ElementPosition barsPosition = new ElementPosition();
    protected BarsRenderer barsRenderer;
    protected final ElementPosition lmbHotbarPosition = new ElementPosition();
    protected final ElementPosition rmbHotbarPosition = new ElementPosition();
    protected final ElementPosition inHudHotkeysPosition = new ElementPosition();
    protected final ElementPosition offHudHotkeyPosition = new ElementPosition();
    protected final ElementPosition warningsPosition = new ElementPosition();
    protected List<ITextComponent> warningLines = new ArrayList<>();
    protected final ElementPosition standStrengthPosition = new ElementPosition();
    protected final ElementPosition modeSelectorPosition = new ElementPosition();
    protected final ElementPosition hamonExerciseBarsPosition = new ElementPosition();
    
    protected void updateElementPositions(PositionConfig barsConfig, PositionConfig hotbarsConfig, 
            boolean renderLmbHotbar, boolean renderRmbHotbar, boolean renderHudKeybinds, boolean renderOffHudKeybind, 
            int screenWidth, int screenHeight) {
        int halfWidth = screenWidth / 2;
        int halfHeight = screenHeight / 2;
        barsRenderer = barsConfig.barsOrientation == BarsOrientation.HORIZONTAL ? horizontalBars : verticalBars;
        barsPosition.x = barsConfig.getXPos(screenWidth);
        barsPosition.y = barsConfig.getYPos(screenHeight, 
                barsConfig.barsOrientation == BarsOrientation.HORIZONTAL ? BARS_WIDTH_PX : VerticalBarsRenderer.BAR_HEIGHT);
        
        boolean barsBelowHotbars = hotbarsConfig == PositionConfig.TOP_LEFT && barsConfig == PositionConfig.LEFT
                || hotbarsConfig == PositionConfig.TOP_RIGHT && barsConfig == PositionConfig.RIGHT;
        int hotbarsRendered = 0;
        if (renderLmbHotbar)   ++hotbarsRendered;
        if (renderRmbHotbar)   ++hotbarsRendered;
        if (renderHudKeybinds) ++hotbarsRendered;
        int hotbarsElementHeight = 20 + getHotbarsYDiff() * hotbarsRendered;
        
        if (currentMode != null && barsBelowHotbars) {
            int offset = hotbarsElementHeight + INDENT + VerticalBarsRenderer.ICON_HEIGHT;
            barsPosition.y = Math.max(lmbHotbarPosition.y + offset, barsPosition.y);
        }
        barsPosition.alignment = barsConfig.alignment;

        boolean hotbarAboveBarsShift = false;
        lmbHotbarPosition.x = hotbarsConfig.getXPos(screenWidth);
        lmbHotbarPosition.y = hotbarsConfig.getYPos(screenHeight, hotbarsElementHeight);
        if (currentMode != null) {
            if (barsConfig == hotbarsConfig) {
                switch (barsConfig.barsOrientation) {
                case HORIZONTAL:
                    lmbHotbarPosition.y += BARS_WIDTH_PX + INDENT;
                    break;
                case VERTICAL:
                    lmbHotbarPosition.x += hotbarsConfig.alignment == Alignment.RIGHT ? -(BARS_WIDTH_PX + INDENT) : BARS_WIDTH_PX + INDENT;
                    break;
                }
            }
            else if (barsConfig == PositionConfig.TOP_LEFT && hotbarsConfig == PositionConfig.LEFT
                    || barsConfig == PositionConfig.TOP_RIGHT && hotbarsConfig == PositionConfig.RIGHT) {
                lmbHotbarPosition.y = Math.max(barsPosition.y + BARS_WIDTH_PX + INDENT, lmbHotbarPosition.y);
            }
            if (barsConfig.barsOrientation == BarsOrientation.VERTICAL) {
                barsPosition.y = Math.min(barsPosition.y, screenHeight - 116);
            }
        }
        lmbHotbarPosition.alignment = hotbarsConfig.alignment;
        
        rmbHotbarPosition.x = lmbHotbarPosition.x;
        rmbHotbarPosition.y = lmbHotbarPosition.y;
        rmbHotbarPosition.alignment = hotbarsConfig.alignment;
        if (renderLmbHotbar) {
            rmbHotbarPosition.y += getHotbarsYDiff();
        }
        
        inHudHotkeysPosition.x = rmbHotbarPosition.x;
        inHudHotkeysPosition.y = rmbHotbarPosition.y;
        inHudHotkeysPosition.alignment = hotbarsConfig.alignment;
        if (renderRmbHotbar) {
            inHudHotkeysPosition.y += getHotbarsYDiff();
            if (barsBelowHotbars && !hotbarAboveBarsShift && barsPosition.y <= 74 + inHudHotkeysPosition.y) {
                hotbarAboveBarsShift = true;
                inHudHotkeysPosition.x += inHudHotkeysPosition.alignment == Alignment.RIGHT ? -24 : 24;
            }
        }
        
        offHudHotkeyPosition.x = inHudHotkeysPosition.x;
        offHudHotkeyPosition.y = inHudHotkeysPosition.y;
        offHudHotkeyPosition.alignment = hotbarsConfig.alignment;
        if (renderHudKeybinds) {
            offHudHotkeyPosition.y += getHotbarsYDiff();
            if (barsBelowHotbars && !hotbarAboveBarsShift && barsPosition.y <= 74 + offHudHotkeyPosition.y) {
                hotbarAboveBarsShift = true;
                offHudHotkeyPosition.x += offHudHotkeyPosition.alignment == Alignment.RIGHT ? -24 : 24;
            }
        }
        
        warningsPosition.x = offHudHotkeyPosition.x + (warningsPosition.alignment == Alignment.RIGHT ? -16 : 16);
        warningsPosition.y = offHudHotkeyPosition.y + 22;
        warningsPosition.alignment = hotbarsConfig.alignment;
        if (renderOffHudKeybind) {
            warningsPosition.y += getHotbarsYDiff();
            if (barsBelowHotbars && !hotbarAboveBarsShift && barsPosition.y <= 20 + warningsPosition.y) {
                hotbarAboveBarsShift = true;
                warningsPosition.x += warningsPosition.alignment == Alignment.RIGHT ? -24 : 24;
            }
        }
        
        standStrengthPosition.x = warningsPosition.x;
        standStrengthPosition.y = warningsPosition.y + warningLines.size() * 16;
        standStrengthPosition.alignment = warningsPosition.alignment;
        if (!warningLines.isEmpty()) {
            if (barsBelowHotbars && !hotbarAboveBarsShift && barsPosition.y <= 20 + standStrengthPosition.y) {
                hotbarAboveBarsShift = true;
                standStrengthPosition.x += standStrengthPosition.alignment == Alignment.RIGHT ? -24 : 24;
            }
        }
        
        modeSelectorPosition.x = hotbarsConfig.alignment == Alignment.LEFT ? halfWidth + 9 : halfWidth - 29;
        modeSelectorPosition.y = halfHeight - 31;
        modeSelectorPosition.alignment = hotbarsConfig.alignment;

        hamonExerciseBarsPosition.x = 10;
        hamonExerciseBarsPosition.y = screenHeight - 5;
        for (ElementTransparency bar : exerciseBarsTransparency.values()) {
            if (bar.shouldRender()) {
                hamonExerciseBarsPosition.y -= 9;
            }
        }
        for (ElementTransparency hamonStat : hamonLvlIncreaseTransparency.values()) {
            if (hamonStat.shouldRender()) {
                hamonExerciseBarsPosition.y -= 6;
            }
        }
        hamonExerciseBarsPosition.alignment = Alignment.LEFT;
    }
    
    protected void updateWarnings(@Nullable ActionsModeConfig<?> mode) {
        warningLines.clear();
        if (mode != null) {
            appendWarnings(warningLines, mode, ControlScheme.Hotbar.LEFT_CLICK);
            appendWarnings(warningLines, mode, ControlScheme.Hotbar.RIGHT_CLICK);
        }
    }
    
    protected <P extends IPower<P, ?>> void appendWarnings(List<ITextComponent> list, 
            ActionsModeConfig<P> powerMode, ControlScheme.Hotbar actionType) {
        boolean shiftVariation = InputHandler.useShiftActionVariant(mc);
        Action<P> action = powerMode.getSelectedAction(actionType, shiftVariation, getMouseTarget());
        if (action != null) {
            action.appendWarnings(list, powerMode.getPower(), mc.player);
        }
    }
    
    

    protected void renderBars(MatrixStack matrixStack, ElementPosition pos, BarsRenderer renderer, float partialTick) {
        int x = pos.x;
        int y = pos.y;
        if (renderer != null) {
            mc.getTextureManager().bind(OVERLAY_LOCATION);
            renderer.render(matrixStack, x, y, pos.alignment, 
                    currentMode, nonStandUiMode, standUiMode, 
                    tickCount, partialTick, mc);
        }
    }
    
    protected void drawBarsText(MatrixStack matrixStack, BarsRenderer renderer, float partialTick) {
        if (renderer != null) {
            renderer.drawTextAfterRender(matrixStack, getCurrentMode(), nonStandUiMode.getPower(), 
                    standUiMode.getPower(), tickCount, partialTick, mc.font, this);
        }
    }

    
    
    protected <P extends IPower<P, ?>> boolean hotbarWillRender(ActionsModeConfig<P> powerMode, InputHandler.ActionKey hotbar) {
        if (powerMode == null) return false;
        P power = powerMode.getPower();
        if (!power.hasPower()) return false;
        List<Action<?>> actions = getEnabledActions(power, hotbar);
        return !actions.isEmpty();
    }

    protected <P extends IPower<P, ?>> void renderActionsHotbar(MatrixStack matrixStack, 
            ElementPosition position, InputHandler.ActionKey actionKey, ActionsModeConfig<P> mode, ActionTarget target, float partialTick) {
        P power = mode.getPower();
        List<Action<?>> actions = getEnabledActions(power, actionKey);

        int x = position.x;
        int y = position.y + getHotbarsYDiff() - 6;
        int hotbarLength = actions.size() * 20 + 2;
        ControlScheme.Hotbar actionHotbar = actionKey.getHotbar();
        if (position.alignment == Alignment.RIGHT) {
            x -= hotbarLength;
        }
        int selected = actionHotbar != null ? mode.getSelectedSlot(actionHotbar) : 0;
        boolean shift = InputHandler.useShiftActionVariant(mc);
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
        
        boolean foldHotbar = ClientModSettings.getSettingsReadOnly().hudHotbarFold;
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
        int heldSlot = -1;
        if (heldAction != null) {
            heldSlot = actions.indexOf(heldAction.isShiftVariation() ? heldAction.getBaseVariation() : heldAction);
        }
        Action<P> selectedAction;
        if (actionHotbar != null) {
            selectedAction = mode.getSelectedAction(actionHotbar, shift, getMouseTarget());
        }
        else { // quick access slot
            selectedAction = resolveVisibleActionInSlot((Action<P>) actions.get(0), shift, power, target);
        }
        if (selectedAction != null) {
            if (selectedAction == heldAction) {
                if (heldSlot > -1) {
                    renderActionHoldProgress(matrixStack, power, heldAction, power.getHeldActionTicks(), partialTick, x + hotbarFold.getSlotWithIndex(heldSlot).pos, y);
                }
            }
            else {
                if (selected > -1) {
                    renderActionHoldProgress(matrixStack, power, selectedAction, -1, partialTick, x + hotbarFold.getSlotWithIndex(selected).pos, y);
                }
            }
        }
    }

    protected <P extends IPower<P, ?>> void renderInHudKeybindActionSlots(MatrixStack matrixStack, 
            ElementPosition position, ActionTarget target, float partialTick) {
        int x = position.x;
        List<HudHotkey> hotkeysIterate = position.alignment == Alignment.RIGHT ? Lists.reverse(hotkeyInHudActions) : hotkeyInHudActions;
        for (HudHotkey hotkeySlotUi : hotkeysIterate) {
            if (hotkeySlotUi.actionEntry.getAction() == null || hotkeySlotUi.actionEntry.getKeybind().isUnbound()) continue;
            
            int nextOffset = Math.max(hotkeySlotUi.maxWidth + 6, 28);
            int offset = Math.max((nextOffset - 28) / 2, 0);
            if (position.alignment == Alignment.RIGHT) {
                nextOffset = -nextOffset;
                offset = -offset;
            }
            x += offset;
            
            float alpha = !hotbarsEnabled ? 0.25F : 1.0F;
            ActionKeybindEntry entry = hotkeySlotUi.actionEntry;
            Action<P> action = (Action<P>) entry.getAction();
            renderSingleActionInSlot(matrixStack, 
                    x, position.y, position.alignment, action, 
                    target, alpha, partialTick, heldThisTick.contains(entry));
            
            x += nextOffset - offset;
        }
    }

    protected <P extends IPower<P, ?>> void renderInHudKeybindActionNames(MatrixStack matrixStack, ElementPosition position) {
        int x = position.x + 9;
        if (position.alignment == Alignment.RIGHT) {
            x -= 26;
        }
        int y = position.y + 16 + 3;
        List<HudHotkey> hotkeysIterate = position.alignment == Alignment.RIGHT ? Lists.reverse(hotkeyInHudActions) : hotkeyInHudActions;
        for (HudHotkey hotkeySlotUi : hotkeysIterate) {
            if (hotkeySlotUi.actionEntry.getAction() == null || hotkeySlotUi.actionEntry.getKeybind().isUnbound()) continue;
            
            int nextOffset = Math.max(hotkeySlotUi.maxWidth + 6, 28);
            int offset = Math.max((nextOffset - 28) / 2, 0);
            if (position.alignment == Alignment.RIGHT) {
                nextOffset = -nextOffset;
                offset = -offset;
            }
            x += offset;
            IFormattableTextComponent keyName = hotkeySlotUi.keyName;
            if (heldThisTick.contains(hotkeySlotUi.actionEntry)) {
                keyName = keyName.withStyle(TextFormatting.BOLD);
            }

            int width = mc.font.width(keyName);
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            
            int color = getPowerUiColor(getHudMode(hotkeySlotUi.actionEntry.getAction().getPowerClassification()).getPower());
            float alpha = !hotbarsEnabled ? 0.25F : 1.0F;
            
            int textOffset = (28 - width) / 2;
            if (position.alignment == Alignment.RIGHT) {
                textOffset = 8 - textOffset;
            }
            int textX = x + textOffset;
            drawBackdrop(matrixStack, textX, y, width, position.alignment, null, alpha, 0);
            drawString(matrixStack, mc.font, keyName, textX, y, position.alignment, color, alpha);
            
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
            x += nextOffset - offset;
        }
    }

    protected <P extends IPower<P, ?>> void renderOffHudKeybindActionSlot(MatrixStack matrixStack, 
            ElementPosition position, Action<P> action, 
            ActionTarget target, float partialTick) {
        if (action != null) {
            ElementTransparency transparency = customKeybindActionTransparency.get(action.getPowerClassification());
            float alpha = transparency.getAlpha(partialTick);
            renderSingleActionInSlot(matrixStack, 
                    position.x, position.y, position.alignment, action, 
                    target, alpha, partialTick, true);
        }
    }
    
    protected <P extends IPower<P, ?>> void renderSingleActionInSlot(MatrixStack matrixStack, 
            int x, int y, Alignment alignment, Action<P> action, 
            ActionTarget target, float alpha, float partialTick, boolean isSelected) {
        if (action == null) return;
        ActionsModeConfig<P> hudMode = (ActionsModeConfig<P>) getHudMode(action.getPowerClassification());
        P power = hudMode.getPower();
        if (!power.hasPower()) return;
        
        y += getHotbarsYDiff() - 6;
        int hotbarLength = 22;
        if (alignment == Alignment.RIGHT) {
            x -= hotbarLength;
        }
        int selected = isSelected ? 0 : -1;
        
        switch (alignment) {
        case LEFT:
            x += 12;
            break;
        case RIGHT:
            x -= 12;
            break;
        }

        boolean shift = InputHandler.useShiftActionVariant(mc);
        action = resolveVisibleActionInSlot(action, shift, power, getMouseTarget());
        
        // hotbar
        HotbarRenderer.renderFoldingHotbar(matrixStack, mc, x, y, HotbarFold.noFold(1), alpha);

        // action icon
        x += 3;
        y += 3;
        SelectedTargetIcon targetIcon = new SelectedTargetIcon();
        renderActionIcon(matrixStack, targetIcon, hudMode, action, target, x, y, partialTick, isSelected, alpha);
        
        // target type icon
        int[] tex = targetIcon.getIconTex();
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
    
    protected <P extends IPower<P, ?>> List<Action<?>> getEnabledActions(P power, InputHandler.ActionKey actionKey) {
        ControlScheme controlScheme = HudControlSettings.getInstance().getControlScheme(power);
        if (!controlScheme.hotbarsEnabled) {
            return Collections.emptyList();
        }
        return controlScheme.getActionsHotbar(actionKey.getHotbar()).getEnabledActions();
    }
    
    protected void renderMouseIcon(MatrixStack matrixStack, int x, int y, InputHandler.ActionKey actionKey) {
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
    
    protected void renderMouseIcon(MatrixStack matrixStack, int x, int y, InputHandler.MouseButton button) {
        blit(matrixStack, x, y, 216 + button.ordinal() * 10, 128, 9, 16);
    }
    
    protected int getHotbarsYDiff() {
        return 2 * 2 + 22 + mc.font.lineHeight;
    }
    
    protected <P extends IPower<P, ?>> void renderActionIcon(MatrixStack matrixStack, SelectedTargetIcon targetIcon, ActionsModeConfig<P> mode, 
            Action<P> action, ActionTarget target, int x, int y, 
            float partialTick, boolean isSelected, float hotbarAlpha) {
        renderActionIcon(matrixStack, targetIcon, mode, 
                action, target, x, y, 
                partialTick, isSelected, hotbarAlpha,
                0, 16);
    }
    
    protected <P extends IPower<P, ?>> void renderActionIcon(MatrixStack matrixStack, SelectedTargetIcon targetIcon, ActionsModeConfig<P> mode, 
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
        
        ActionConditionResult result = actionAvailability(action, mode, targetIcon, target, isSelected);
        if (!result.isPositive()) {
            float brightness;
            float alpha;
            if (!result.isQueued()) {
                brightness = 0.2f;
                alpha = 0.5f * hotbarAlpha;
            }
            else {
                brightness = 0.75f;
                alpha = 0.75f * hotbarAlpha;
            }
            if (cutWidth > 0) {
                ClientUtil.enableGlScissor(x + leftCut, y, cutWidth, 16);
                
                // action icon
                boolean changeColor = brightness < 1 || alpha < 1;
                if (changeColor) RenderSystem.color4f(brightness, brightness, brightness, alpha);
                action.renderActionIcon(matrixStack, power, x, y);
                if (changeColor) RenderSystem.color4f(1, 1, 1, 1);
                
                // cooldown
                float ratio = power.getCooldownRatio(action, partialTick);
                if (ratio > 0) {
                    ClientUtil.fillSingleRect(x, y + 16.0F * (1.0F - ratio), 16, 16.0F * ratio, 255, 255, 255, 127);
                }
                
                ClientUtil.disableGlScissor();
            }
        } else {
            if (cutWidth > 0) {
                ClientUtil.enableGlScissor(x + leftCut, y, cutWidth, 16);
                
                // action icon
                boolean changeColor = hotbarAlpha < 1;
                if (changeColor) RenderSystem.color4f(1, 1, 1, hotbarAlpha);
                action.renderActionIcon(matrixStack, power, x, y);
                if (changeColor) RenderSystem.color4f(1, 1, 1, 1);
                
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
            ClientUtil.fillRect(Tessellator.getInstance().getBuilder(), barX, barY, learningProgress * 13.0F, 1, 0, 255, 0, 255);
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
    
    protected <P extends IPower<P, ?>> ActionConditionResult actionAvailability(Action<P> action, ActionsModeConfig<P> mode, 
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
    
    protected <P extends IPower<P, ?>> void drawHotbarText(MatrixStack matrixStack, ElementPosition position, 
            InputHandler.ActionKey actionKey, @Nonnull ActionsModeConfig<P> mode, ActionTarget target, 
            int color, float partialTick) {
        P power = mode.getPower();
        int x = position.x;
        int y = position.y + 16 + 3;
        switch (position.alignment) {
        case LEFT:
            x += 12;
            break;
        case RIGHT:
            x -= 12;
            break;
        }
        boolean shift = InputHandler.useShiftActionVariant(mc);
        Action<P> selectedAction = mode.getSelectedAction(actionKey.getHotbar(), shift, getMouseTarget());
        if (selectedAction != null) {
            // action name
            ITextComponent actionName = actionName(selectedAction, power, target);
            ElementTransparency transparency = actionNameTransparency.get(actionKey);

            Action<P> baseAction = selectedAction.getBaseVariation();
            ITextComponent baseActionName = baseAction != null ? actionName(baseAction, power, target) : actionName;
            if (!baseActionName.equals(lastActionName.put(actionKey, baseActionName))) {
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
                if (!hotbarsEnabled) alpha = mulAlpha(alpha, 0.25F);
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
    
    protected static <P extends IPower<P, ?>> ITextComponent actionName(Action<P> action, P power, ActionTarget target) {
        String translationKey = action.getTranslationKey(power, target);
        ITextComponent actionName = action.getTranslatedName(power, translationKey);
        if (action.getHoldDurationMax(power) > 0) {
            actionName = new TranslationTextComponent("jojo.overlay.hold", actionName);
        }
        return actionName;
    }
    
    protected <P extends IPower<P, ?>> void drawCustomKeybindActionText(MatrixStack matrixStack, ElementPosition position, 
            Action<P> action, @Nonnull ActionsModeConfig<?> currentMode, ActionTarget target, 
            int color, float partialTick) {
        ActionsModeConfig<P> mode = (ActionsModeConfig<P>) currentMode;
        P power = mode.getPower();
        int x = position.x;
        int y = position.y + 16 + 3;
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
            float alpha;
            HudTextRender renderMode = ClientModSettings.getSettingsReadOnly().hudTextRender;
            switch (renderMode) {
            case NEVER:
                alpha = 0;
                break;
            default:
                alpha = transparency.getAlpha(partialTick);
                break;
            }
            
            if (alpha > 0) {
                if (!hotbarsEnabled) alpha = mulAlpha(alpha, 0.25F);
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
    
    protected float getNameAlpha(ElementTransparency transparency, float partialTick) {
        HudTextRender renderMode = ClientModSettings.getSettingsReadOnly().hudTextRender;
        switch (renderMode) {
        case NEVER:
            return 0;
        case FADE_OUT:
            return transparency.getAlpha(partialTick);
        default:
            return 1;
        }
    }
    
    protected float mulAlpha(float alpha, float multiplier) {
        return Math.max(alpha * multiplier, 1f / 63f);
    }
    
    
    
//    public boolean isSelectedActionHeld(ActionType actionType) {
//        return getSelectedActionHoldDuration(actionType, currentMode) > 0;
//    }
//    
//    protected <P extends IPower<P, ?>> int getSelectedActionHoldDuration(ActionType actionType, @Nonnull ActionsModeConfig<P> mode) {
//        Action<P> action = mode.getSelectedAction(actionType, InputHandler.useShiftActionVariant(mc));
//        if (action != null) {
//            return action.getHoldDurationMax(mode.getPower());
//        }
//        return 0;
//    }
    
    

    protected void renderPowerIcon(MatrixStack matrixStack, ElementPosition position, @Nullable ActionsModeConfig<?> mode, float alpha) {
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
    
    protected void drawPowerName(MatrixStack matrixStack, ElementPosition position, @Nonnull ActionsModeConfig<?> mode, int color, float partialTick) {
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



    protected void renderWarningIcons(MatrixStack matrixStack, ElementPosition position, List<ITextComponent> warningLines) {
        mc.getTextureManager().bind(OVERLAY_LOCATION);
        int x = position.x;
        int y = position.y - 4;
        for (int line = 0; line < warningLines.size(); line++) {
            blit(matrixStack, x, y, 132, 240, 16, 16);
            y += 16;
        }
    }
    
    protected void drawWarningText(MatrixStack matrixStack, ElementPosition position, List<ITextComponent> warningLines) {
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
    
    protected void drawStandRemoteRange(MatrixStack matrixStack, float distance, float damageFactor) {
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
    
    

    protected <P extends IPower<P, ?>> void renderActionHoldProgress(MatrixStack matrixStack, P power, Action<P> action, 
            int ticks, float partialTick, float x, float y) {
        if (action == null) return;

        int ticksToFire = action.getHoldDurationToFire(power);
        if (ticksToFire > 0) {
            float ratio;
            float alpha = !hotbarsEnabled ? 0.25F : 1.0F;
            if (ticks < 0) {
                ratio = 0;
                alpha = mulAlpha(alpha, 0.75F);
            }
            else {
                ratio = MathHelper.clamp(((float) ticks + partialTick) / (float) ticksToFire, 0, 1);
            }

            if (alpha < 1) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            }
            mc.getTextureManager().bind(RADIAL_INDICATOR);
//            int deg = (int) (ratio * 360F);
//            blitFloat(matrixStack, x, y, deg % 19 * 13, deg / 19 * 13, 13, 13);
            RadialBar.render(matrixStack, x, y, 0, ratio, 0, 0, 234, 234, 13, 13, 256, 256, getBlitOffset());
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    
    
    protected final List<ActionsModeConfig<?>> modes = Collections.unmodifiableList(Arrays.asList(
            null,
            nonStandUiMode,
            standUiMode
            ));
    protected void renderModeSelector(MatrixStack matrixStack, ElementPosition position, float partialTick) {
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
    
    
    
    protected void renderModeSelectorIcons(MatrixStack matrixStack, int x, int y, float partialTick) {
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
    
    protected void drawModeSelectorNames(MatrixStack matrixStack, ElementPosition position, int color, float partialTick) {
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
    protected ITextComponent getModeNameForSelector(ActionsModeConfig<?> mode) {
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

    protected Map<ActionsModeConfig<?>, Supplier<KeyBinding>> modeKeys = ImmutableMap.of(
            nonStandUiMode, () -> InputHandler.getInstance().nonStandMode,
            standUiMode, () -> InputHandler.getInstance().standMode);
    @Nullable
    protected ITextComponent getKeyName(ActionsModeConfig<?> mode) {
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
    
    
    
    protected void renderLeapIcon(MatrixStack matrixStack, @Nonnull ActionsModeConfig<?> mode, int screenWidth, int screenHeight) {
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
    
    protected void renderIconBarAtCrosshair(MatrixStack matrixStack, int screenWidth, int screenHeight, float partialTick) {
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
                        float finisherValue = ((StandEntity) stand).getFinisherMeter(partialTick);
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
    
    protected <P extends IPower<P, ?>> void renderCrosshair(MatrixStack matrixStack, int screenWidth, int screenHeight, float partialTick) {
        mc.textureManager.bind(ActionsOverlayGui.OVERLAY_LOCATION);
        matrixStack.pushPose();
        float x = (screenWidth - 15) / 2;
        float y = (screenHeight - 15) / 2;
        matrixStack.translate(x + 7.5f, y + 7.5f, 0);
        x = -7.5f;
        y = -7.5f;
        
        for (PowerClassification powerClass : PowerClassification.values()) {
            P power = (P) getHudMode(powerClass).getPower();
            if (power != null) {
                Action<P> heldAction = power.getHeldAction();
                if (heldAction != null) {
                    int ticksToFire = heldAction.getHoldDurationToFire(power);
                    if (ticksToFire > 0) {
                        float heldActionRatio = MathHelper.clamp(((float) power.getHeldActionTicks() + partialTick) / (float) ticksToFire, 0, 1);
                        if (heldActionRatio > 0) {
                            if (heldActionRatio < 1) {
                                crosshairFillLastAction = null;
                            }
                            else if (crosshairFillLastAction != heldAction) {
                                crosshairFillLastAction = heldAction;
                                crosshairFillTransparency.reset();
                            }
                            
                            float height = heldActionRatio == 1 ? 15 : 3 + 9 * heldActionRatio;
                            BlitFloat.blitFloat(matrixStack, x, y + 15 - height, 88, 30 - height, 15, height, 256, 256);
                            
                            break;
                        }
                    }
                }
            }
        }
        
        if (crosshairFillTransparency.shouldRender()) {
            float alpha = crosshairFillTransparency.getAlpha(partialTick);
            if (alpha > 0) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.color4f(1, 1, 1, alpha);
                float scale = 1 + alpha * 0.25f;
                matrixStack.scale(scale, scale, 1);
                BlitFloat.blitFloat(matrixStack, x, y, 88, 0, 15, 15, 256, 256);
                RenderSystem.color4f(1, 1, 1, 1);
            }
        }
        
        matrixStack.popPose();
    }
    
    protected boolean renderBowChargeIcon(MatrixStack matrixStack, BowChargeEffectInstance<?, ?> bowCharge, float partialTick, int x, int y) {
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

    protected void renderFilledIcon(MatrixStack matrixStack, int x, int y, boolean translucent, float fill, 
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

    protected void renderHamonExerciseBars(MatrixStack matrixStack, ElementPosition position, HamonData hamon, float partialTick) {
        int x = position.x;
        int y = position.y;
        
        for (HamonStatIncNotif hamonStat : HamonStatIncNotif.values()) {
            ElementTransparency transparency = hamonLvlIncreaseTransparency.get(hamonStat);
            if (transparency.shouldRender()) {
                IFormattableTextComponent statName = new TranslationTextComponent("hamon.stat_lvl_increase." + hamonStat.name().toLowerCase());
                int value = 0;
                switch (hamonStat) {
                case STRENGTH:
                    statName.withStyle(Style.EMPTY.withColor(Color.fromRgb(0xE21100)));
                    value = hamon.getStatLevel(HamonStat.STRENGTH);
                    break;
                case CONTROL:
                    statName.withStyle(Style.EMPTY.withColor(Color.fromRgb(0x15AF00)));
                    value = hamon.getStatLevel(HamonStat.CONTROL);
                    break;
                case BREATHING:
                    statName.withStyle(Style.EMPTY.withColor(Color.fromRgb(0x0070D8)));
                    value = (int) hamon.getBreathingLevel();
                    break;
                }
                ITextComponent increaseMsg = new TranslationTextComponent("hamon.stat_lvl_increase", statName, value);
                matrixStack.pushPose();
                matrixStack.scale(0.5f, 0.5f, 1);
                drawBackdrop(matrixStack, x * 2, y * 2, mc.font.width(increaseMsg), Alignment.LEFT, transparency, 0, partialTick);
                mc.font.drawShadow(matrixStack, increaseMsg, x * 2, y * 2, transparency.makeTextColorTranclucent(0xFFFFFF, partialTick));
                matrixStack.popPose();
                y += 6;
            }
        }
        
        mc.getTextureManager().bind(HamonScreen.WINDOW);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
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
    

    
    public void switchMode(PowerClassification power) {
        Objects.requireNonNull(power);
        if (getCurrentMode() != power) {
            setMode(power);
        }
        else {
            setMode(null);
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
    
    protected final PowerClassification[] modesOrder = {
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
    
    protected boolean setPowerMode(@Nullable ActionsModeConfig<?> mode) {
        if (mode == null) {
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
        else if (currentMode != mode && mode.getPower().hasPower()) {
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

    protected static final IntBinaryOperator INC = (i, n) -> (i + 2) % (n + 1) - 1;
    protected static final IntBinaryOperator DEC = (i, n) -> (i + n + 1) % (n + 1) - 1;
    protected <P extends IPower<P, ?>> void scrollAction(ActionsModeConfig<P> mode, ControlScheme.Hotbar hotbar, boolean backwards) {
        P power = mode.getPower();
        ControlScheme controlScheme = HudControlSettings.getInstance().getControlScheme(power);
        List<Action<?>> actions = controlScheme.getActionsHotbar(hotbar).getEnabledActions();
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
        ControlScheme controlScheme = HudControlSettings.getInstance().getControlScheme(getCurrentMode());
        Action<P> action = (Action<P>) controlScheme.getActionsHotbar(hotbar).getBaseActionInSlot(index);
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

    protected final PacketBuffer extraInputBuf = new PacketBuffer(Unpooled.buffer());
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
            action.clWriteExtraData(extraInputBuf);
            boolean actionWentOff = power.clickAction(action, sneak, mouseTarget, extraInputBuf);
            extraInputBuf.clear();
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
    
    
    
    public void updatePowersCache() {
        setMode(null);
        standUiMode.setPower(IStandPower.getPlayerStandPower(mc.player));
        standUiMode.autoOpened = false;
        nonStandUiMode.setPower(INonStandPower.getPlayerNonStandPower(mc.player));
        nonStandUiMode.autoOpened = false;
    }
    
    protected void blitFloat(MatrixStack pMatrixStack, float pX, float pY, 
            float pUOffset, float pVOffset, float pUWidth, float pVHeight) {
        BlitFloat.blitFloat(pMatrixStack, 
                pX, pY, this.getBlitOffset(), 
                pUOffset, pVOffset, pUWidth, pVHeight, 256, 256);
    }
    
    
    
    protected boolean outOfBreath = false;
    protected boolean outOfBreathMaskSprite = false;
    protected int outOfBreathSpriteTicks = 0;
    protected float prevAir;
    protected float vignetteBeforeFadeAway = -1;
    public void setOutOfBreath(boolean mask) {
        outOfBreath = true;
        outOfBreathMaskSprite = mask;
        outOfBreathSpriteTicks = 15;
        vignetteBeforeFadeAway = -1;
        prevAir = 0;
        BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).resetRedHighlight();
        mc.gui.setOverlayMessage(new TranslationTextComponent("hamon.out_of_breath"), false);
    }
    
    public boolean isPlayerOutOfBreath() {
        return outOfBreath;
    }
    
    protected void tickOutOfBreathEffect() {
        if (outOfBreath && outOfBreathSpriteTicks == 0) {
            prevAir = mc.player.getAirSupply();
            if (prevAir >= mc.player.getMaxAirSupply()) {
                outOfBreath = false;
            }
        }
        if (outOfBreathSpriteTicks > 0) outOfBreathSpriteTicks--;
    }
    
    protected void renderOutOfBreathSprite(MatrixStack matrixStack, float partialTick, int windowWidth, int windowHeight) {
        if (outOfBreathSpriteTicks > 0) {
            boolean bubblePopped = outOfBreathSpriteTicks < 11;
            mc.getTextureManager().bind(ClientUtil.ADDITIONAL_UI);
            blit(matrixStack, windowWidth / 2 - 16, windowHeight / 2 - 16, bubblePopped ? 160 : 128, outOfBreathMaskSprite ? 32 : 0, 32, 32);
        }
    }
    
    protected void renderOutOfBreathVignette(MatrixStack matrixStack, float partialTick) {
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

    protected static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/vignette.png");
    protected void renderVignette(MatrixStack matrixStack, float r, float g, float b) {
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
    
    
    
    protected class ElementPosition {
        protected int x;
        protected int y;
        protected Alignment alignment;
    }

    protected static final int INDENT = 4;
    public enum PositionConfig {
        TOP_LEFT(Alignment.LEFT, BarsOrientation.HORIZONTAL, 
                screenWidth -> INDENT, (screenHeight, elementHeight) -> INDENT),
        TOP_RIGHT(Alignment.RIGHT, BarsOrientation.HORIZONTAL, 
                screenWidth -> screenWidth - INDENT, (screenHeight, elementHeight) -> INDENT),
        LEFT(Alignment.LEFT, BarsOrientation.VERTICAL, 
                screenWidth -> INDENT, (screenHeight, elementHeight) -> (screenHeight - elementHeight) / 2),
        RIGHT(Alignment.RIGHT, BarsOrientation.VERTICAL, 
                screenWidth -> screenWidth - INDENT, (screenHeight, elementHeight) -> (screenHeight - elementHeight) / 2);
        
        final Alignment alignment;
        final BarsOrientation barsOrientation;
        protected final IntUnaryOperator xPos;
        protected final IntBinaryOperator yPos;
        
        private PositionConfig(Alignment alignment, BarsOrientation barsOrientation, 
                IntUnaryOperator xPos, IntBinaryOperator yPos) {
            this.alignment = alignment;
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
    
    public enum HudTextRender {
        ALWAYS,
        FADE_OUT,
        NEVER
    }
}
