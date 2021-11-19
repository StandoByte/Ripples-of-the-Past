package com.github.standobyte.jojo.client.ui;

import java.util.List;
import java.util.function.IntBinaryOperator;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.Action.TargetRequirement;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.ui.sprites.SpriteUploaders;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
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
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ActionsOverlayGui extends AbstractGui { // TODO config to move it to another corner
    private static ActionsOverlayGui instance = null;
    
    private static final StringTextComponent EMPTY_STRING_COMPONENT = new StringTextComponent("");
    private static final ResourceLocation OVERLAY_TEX_PATH = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/overlay.png");
    private static final ResourceLocation VANILLA_WIDGETS_TEX_PATH = new ResourceLocation("textures/gui/widgets.png");
    private static final int Y_OFFSET = 3;
    private static final int BAR_HEIGHT = 8;
    private static final int BAR_LENGTH = 202;
    private static final int ICON_HEIGHT = 16;
    private static final int ICON_LENGTH = 16;
    private static final int HOTBAR_SQUARE_HEIGHT = 24;
    private final int BARS_Y_GAP;
    private static final ITextComponent SHIFT_KEY = new KeybindTextComponent("key.sneak");
    
    private Minecraft mc;
    private ActionTarget mouseTarget;
    private int screenWidth;
    private int screenHeight;
    
    private UiMode mode = UiMode.NONE;
    private ITextComponent powerName;
    private ResourceLocation powerIconPath;
    private IStandPower standPower;
    private INonStandPower nonStandPower;
    @Nullable
    private IPower<?> currentPower;
    
    private ActionsHotbarData attackData = new ActionsHotbarData();
    private ActionsHotbarData abilityData = new ActionsHotbarData();
    
    private static final ImmutableMap<ResourceLocation, BarTexture> NON_STAND_BAR_TEX = ImmutableMap.<ResourceLocation, BarTexture>builder()
            .put(new ResourceLocation(JojoMod.MOD_ID, "hamon"), BarTexture.HAMON)
            .put(new ResourceLocation(JojoMod.MOD_ID, "vampirism"), BarTexture.BLOOD)
        .build();
    
    public ActionsOverlayGui(Minecraft mc) {
        this.mc = mc;
        BARS_Y_GAP = HOTBAR_SQUARE_HEIGHT + mc.font.lineHeight + 2;
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
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void render(RenderGameOverlayEvent.Pre event) {
        MatrixStack matrixStack = event.getMatrixStack();
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR || mc.options.hideGui || mode == UiMode.NONE) {
            return;
        }
        
        screenWidth = mc.getWindow().getGuiScaledWidth();
        screenHeight = mc.getWindow().getGuiScaledHeight();
        mouseTarget = ActionTarget.fromRayTraceResult(mc.hitResult);
        
        int x = 2;
        
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            RenderSystem.enableRescaleNormal();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int y = Y_OFFSET;

            mc.getTextureManager().bind(OVERLAY_TEX_PATH);
         // leap icon
            if (currentPower.isLeapUnlocked()) {
                float iconFill = currentPower.getLeapCooldownPeriod() != 0 ? 
                        1F - (float) currentPower.getLeapCooldown() / (float) currentPower.getLeapCooldownPeriod() : 1;
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
                boolean translucent = !currentPower.canLeap();
                blit(matrixStack, iconX, iconY, 96, 238, 18, 18);
                if (translucent) {
                    RenderSystem.color4f(0.5F, 0.5F, 0.5F, 0.75F);
                }
                int px = (int) (19F * iconFill);
                blit(matrixStack, iconX, iconY + 18 - px, 96 + 18, 238 + 18 - px, 18, px);
                if (translucent) {
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
         // mana bar
            BarTexture barTexture;
            if (mode == UiMode.STAND) {
                barTexture = BarTexture.STAMINA;
            }
            else {
                barTexture = NON_STAND_BAR_TEX.get(nonStandPower.getType().getRegistryName());
            }
            renderBar(matrixStack, x + 3, y, barTexture, (int) ((BAR_LENGTH - 2) * currentPower.getMana() / currentPower.getMaxMana()));
         // exp bar
            if (mode == UiMode.STAND) {
                renderBar(matrixStack, screenWidth - BAR_LENGTH - 1, y, BarTexture.EXP, (BAR_LENGTH - 2) * standPower.getExp() / IStandPower.MAX_EXP);
            }
         // power icon
            if (currentPower.isActive()) {
                mc.getTextureManager().bind(powerIconPath);
                blit(matrixStack, x, BAR_HEIGHT + Y_OFFSET + 3, 0, 0, 16, 16, 16, 16);
            }
         // attacks hotbar
            float attacksHotbarAlpha = hotbarAlpha(ActionType.ATTACK);
            float abilitiesHotbarAlpha = hotbarAlpha(ActionType.ABILITY);
            mc.getTextureManager().bind(VANILLA_WIDGETS_TEX_PATH);
            y += BAR_HEIGHT + ICON_HEIGHT + mc.font.lineHeight + 7;
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, attacksHotbarAlpha);
            renderHotbar(matrixStack, x, y, ActionType.ATTACK);
         // abilities hotbar
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, abilitiesHotbarAlpha);
            renderHotbar(matrixStack, x, y + BARS_Y_GAP, ActionType.ABILITY);
         // attack icons
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, attacksHotbarAlpha);
            renderIcons(matrixStack, x + 3, y + 3, ActionType.ATTACK, attacksHotbarAlpha, event.getPartialTicks());
         // ability icons
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, abilitiesHotbarAlpha);
            renderIcons(matrixStack, x + 3, y + 3 + BARS_Y_GAP, ActionType.ABILITY, abilitiesHotbarAlpha, event.getPartialTicks());
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bind(OVERLAY_TEX_PATH);
            renderTargetIcon(matrixStack, ActionType.ATTACK, x, y);
            renderTargetIcon(matrixStack, ActionType.ABILITY, x, y + BARS_Y_GAP);
            
            RenderSystem.disableRescaleNormal();
            RenderSystem.disableBlend();
        }
        
        else if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            int y = Y_OFFSET + BAR_HEIGHT + 1;
         // power name
            drawString(matrixStack, mc.font, powerName, 
                    4 + ICON_LENGTH, y + (ICON_HEIGHT + 2 - mc.font.lineHeight) / 2, currentPower.getType().getColor());
            
            y += ICON_HEIGHT + 5;
            renderHotbarText(matrixStack, x, y, currentPower, ActionType.ATTACK);

            y += mc.font.lineHeight + 1 + HOTBAR_SQUARE_HEIGHT + 1;
            renderHotbarText(matrixStack, x, y, currentPower, ActionType.ABILITY);
        }
    }

    private void renderBar(MatrixStack matrixStack, int x, int y, BarTexture barTexture, int value) {
        blit(matrixStack, x + 1, y, 1, BAR_HEIGHT * barTexture.ordinal(), value, BAR_HEIGHT);
        blit(matrixStack, x, y, 0, 0, BAR_LENGTH, BAR_HEIGHT);
        blit(matrixStack, x - 4, y - 3, BAR_LENGTH, 15 * barTexture.ordinal(), 10, 14);
    }
    
    public boolean noActionSelected(ActionType type) {
        if (currentPower == null) {
            return true;
        }
        return getHotbarData(type).noActionSelected();
    }
    
    private float hotbarAlpha(ActionType type) {
        return getHotbarData(type).noActionSelected() ? 0.5F : 1.0F;
    }
    
    private void renderHotbar(MatrixStack matrixStack, int x, int y, ActionType type) {
        List<Action> actions = currentPower.getActions(type);
        if (actions.size() == 0) {
            return;
        }
        ActionsHotbarData hotbarData = getHotbarData(type);
        int hotbarLength = 1 + 20 * actions.size();
        blit(matrixStack, x, y, 0, 0, hotbarLength, 22);
        blit(matrixStack, x + hotbarLength, y, 181, 0, 1, 22);
        if (!hotbarData.noActionSelected()) {
            blit(matrixStack, x - 1 + hotbarData.getSelectedIndex() * 20, y - 1, 0, 22, 24, 24);
        }
    }
    
    @SuppressWarnings("deprecation")
    private void renderIcons(MatrixStack matrixStack, int x, int y, ActionType type, float hotbarAlpha, float partialTick) {
        List<Action> actions = currentPower.getActions(type);
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            if (currentPower.getHeldAction() != null) {
                if (currentPower.getHeldAction() == action.getShiftVariationIfPresent()) {
                    action = action.getShiftVariationIfPresent();
                }
            }
            if (shiftVarSelected(action, mc.player.isShiftKeyDown())) {
                action = action.getShiftVariationIfPresent();
            }
            ActionsHotbarData hotbarData = getHotbarData(type);
            
            if (currentPower.isActionUnlocked(action)) {
                TextureAtlasSprite textureAtlasSprite = SpriteUploaders.getActionSprites().getSprite(action);
                mc.getTextureManager().bind(textureAtlasSprite.atlas().location());
                boolean isSelected = i == hotbarData.getSelectedIndex();
                boolean rightTarget = false;
                if (isSelected) {
                    rightTarget = currentPower.checkTargetType(action, action.getPerformer(mc.player, currentPower), mouseTarget).isPositive();
                    hotbarData.isRightTarget = rightTarget;
                    hotbarData.isAvaliable = rightTarget && currentPower.checkRequirements(action, action.getPerformer(mc.player, currentPower), mouseTarget, false).isPositive();
                }
                if (isSelected && !hotbarData.isAvaliable || !currentPower.checkRequirements(action, action.getPerformer(mc.player, currentPower), mouseTarget, true).isPositive()) {
                    RenderSystem.color4f(0.2F, 0.2F, 0.2F, 0.5F * hotbarAlpha);
                    blit(matrixStack, x + 20 * i, y, 0, 16, 16, textureAtlasSprite);
                    float ratio = currentPower.getCooldownRatio(action, partialTick);
                    if (ratio > 0) {
                        RenderSystem.disableDepthTest();
                        RenderSystem.disableTexture();
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder bufferBuilder = tessellator.getBuilder();
                        this.fillRect(bufferBuilder, x + 20 * i, y + 16.0F * (1.0F - ratio), 16, 16.0F * ratio, 255, 255, 255, (int) (127F * hotbarAlpha));
                        RenderSystem.enableTexture();
                        RenderSystem.enableDepthTest();
                    }
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, hotbarAlpha);
                } else {
                    blit(matrixStack, x + 20 * i, y, 0, 16, 16, textureAtlasSprite);
                }
            }
        }
    }
    
    public boolean shiftVarSelected(Action action, boolean shift) {
        return currentPower.getHeldAction() != action && shift && currentPower.isActionUnlocked(action.getShiftVariationIfPresent()) 
                || currentPower.getHeldAction() == action.getShiftVariationIfPresent();
    }

    private void fillRect(BufferBuilder bufferBuilder, int x, double y, int width, double height, int red, int green, int blue, int alpha) {
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(x + 0 , y + 0, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x + 0 , y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x + width , y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x + width , y + 0, 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().end();
    }
    
    private void renderTargetIcon(MatrixStack matrixStack, ActionType type, int x, int y) {
        Action action = getSelectedAction(type);
        if (action != null) {
            TargetRequirement iconType = action.getTargetRequirement();
            if (iconType != TargetRequirement.NONE) {
                ActionsHotbarData hotbarData = getHotbarData(type);
                x += 13 + hotbarData.getSelectedIndex() * 20;
                y -= 1;
                int iconX = iconType == TargetRequirement.BLOCK ? 0 : iconType == TargetRequirement.ENTITY ? 32 : 64;
                int iconY = hotbarData.isRightTarget ? 192 : 224;
                matrixStack.pushPose();
                matrixStack.scale(0.5F, 0.5F, 0F);
                matrixStack.translate(x, y, 0);
                blit(matrixStack, x, y, iconX, iconY, 32, 32);
                matrixStack.popPose();
            }
        }
    }
    
    private void renderHotbarText(MatrixStack matrixStack, int x, int y, IPower<?> power, ActionType type) {
        ActionsHotbarData hotbarData = getHotbarData(type);
        if (!hotbarData.noActionSelected()) {
            ITextComponent curAttackName = 
                    !shiftVarSelected(getSelectedAction(type, false), mc.player.isShiftKeyDown()) ? 
                    hotbarData.name
                    : hotbarData.nameShift;
            drawString(matrixStack, mc.font, curAttackName, x, y, currentPower.getType().getColor());
        }
        y += mc.font.lineHeight + 1;
        if (!hotbarData.noActionSelected()) {
            drawHoldDuration(matrixStack, x, y, type);
        }
    }
    
    private void drawHoldDuration(MatrixStack matrixStack, int hotbarX, int hotbarY, ActionType type) {
        Action action = getSelectedAction(type);
        if (action != null) {
            int ticksHeld = 0;
            if (InputHandler.getInstance().getHeldActionType() == type && currentPower.getHeldAction() != null) {
                action = currentPower.getHeldAction();
                ticksHeld = currentPower.getHeldActionTicks();
            }
            int ticksToFire = action.getHoldDurationToFire(currentPower);
            if (ticksToFire > 0) {
                ActionsHotbarData hotbarData = getHotbarData(type);
                int index = hotbarData.getSelectedIndex();
                ticksToFire = Math.max(ticksToFire - ticksHeld, 0);
                int seconds = ticksToFire == 0 ? 0 : (ticksToFire - 1) / 20 + 1;
                int color = ticksToFire == 0 ? 0x00FF00 : ticksHeld == 0 ? 0x808080 : 0xFFFFFF;
                ClientUtil.drawRightAlignedString(matrixStack, mc.font, String.valueOf(seconds), hotbarX + 20 * index + 20, hotbarY + 13, color);
            }
        }
    }

    public void scrollMode() {
        switch(mode) {
        case NONE:
            if (nonStandPower.hasPower()) {
                setMode(UiMode.NON_STAND);
            }
            else if (standPower.hasPower()) {
                setMode(UiMode.STAND);
            }
            break;
        case NON_STAND:
            if (standPower.hasPower()) {
                setMode(UiMode.STAND);
            }
            else {
                setMode(UiMode.NONE);
            }
            break;
        case STAND:
            setMode(UiMode.NONE);
            break;
        }
    }
    
    public void scrollActiveAttack(boolean backwards) {
        scrollHotbar(ActionType.ATTACK, backwards);
    }
    
    public void scrollActiveAbility(boolean backwards) {
        scrollHotbar(ActionType.ABILITY, backwards);
    }

    private static final IntBinaryOperator INC = (i, n) -> (i + 2) % (n + 1) - 1;
    private static final IntBinaryOperator DEC = (i, n) -> (i + n + 1) % (n + 1) - 1;
    private void scrollHotbar(ActionType type, boolean backwards) {
        if (mode == UiMode.NONE) {
            return;
        }
        List<Action> actions = currentPower.getActions(type);
        if (actions.size() == 0) {
            return;
        }
        ActionsHotbarData hotbarData = getHotbarData(type);
        int startingIndex = hotbarData.getSelectedIndex();
        IntBinaryOperator operator = backwards ? DEC : INC;
        int i;
        for (i = operator.applyAsInt(startingIndex, actions.size()); 
             i > -1 && i % actions.size() != startingIndex && !currentPower.isActionUnlocked(actions.get(i));
             i = operator.applyAsInt(i, actions.size())) {
        }
        hotbarData.setSelectedIndex(getMode(), i);
        updateActionName(type);
    }
    
    public UiMode getMode() {
        return mode;
    }
    
    public IPower<?> getCurrentPower() {
        return currentPower;
    }
    
    public int getIndex(ActionType type) {
        return getHotbarData(type).getSelectedIndex();
    }

    @Nullable
    public Action getSelectedAction(ActionType type) {
        return getSelectedAction(type, true);
    }

    @Nullable
    public Action getSelectedAction(ActionType type, boolean checkShift) {
        if (currentPower == null) {
            return null;
        }
        List<Action> actions = currentPower.getActions(type);
        int index = getIndex(type);
        if (index > -1) {
            if (index >= actions.size()) {
                selectAction(type, -1);
                return null;
            }
            Action action = actions.get(getIndex(type));
            if (checkShift && shiftVarSelected(action, mc.player.isShiftKeyDown())) {
                action = action.getShiftVariationIfPresent();
            }
            return action;
        }
        return null;
    }
    
    private ActionsHotbarData getHotbarData(ActionType type) {
        switch (type) {
        case ATTACK:
            return attackData;
        case ABILITY:
            return abilityData;
        }
        return null;
    }

    public void updatePowersCache() {
        setMode(UiMode.NONE);
        standPower = IStandPower.getPlayerStandPower(mc.player);
        nonStandPower = INonStandPower.getPlayerNonStandPower(mc.player);
    }
    
    public void setMode(UiMode mode) {
        if (mode == UiMode.STAND && !standPower.hasPower() || mode == UiMode.NON_STAND && !nonStandPower.hasPower() || this.mode == mode) {
            return;
        }
        this.mode = mode;
        switch(mode) {
        case NONE:
            setCurrentPower(null);
            break;
        case NON_STAND:
            setCurrentPower(nonStandPower);
            break;
        case STAND:
            setCurrentPower(standPower);
            break;
        }
    }
    
    private void setCurrentPower(@Nullable IPower<?> power) {
        currentPower = power;
        if (power == null) {
            return;
        }
        powerName = new TranslationTextComponent(currentPower.getType().getTranslationKey());
        powerIconPath = currentPower.getType().getIconTexture();
        selectAction(ActionType.ATTACK, attackData.getSavedSelectedIndex(getMode()));
        selectAction(ActionType.ABILITY, abilityData.getSavedSelectedIndex(getMode()));
    }
    
    public void selectAction(ActionType type, int index) {
        if (mode == UiMode.NONE) {
            return;
        }
        List<Action> actions = currentPower.getActions(type);
        ActionsHotbarData hotbarData = getHotbarData(type);
        if (index < 0 || actions.size() < index + 1 || !currentPower.isActionUnlocked(actions.get(index))) {
            hotbarData.setSelectedIndex(getMode(), -1);
        }
        else {
            hotbarData.setSelectedIndex(getMode(), index);
        }
        updateActionName(type);
    }
    
    public void updateActionName(Action action) {
        if (!updateActionName(action, ActionType.ATTACK)) {
            updateActionName(action, ActionType.ABILITY);
        }
    }
    
    public boolean updateActionName(Action action, ActionType type) {
        if (action != null && getSelectedAction(type) == action) {
            updateActionName(type);
            return true;
        }
        return false;
    }
    
    public void updateActionName(ActionType type) {
        ITextComponent actionName;
        ITextComponent actionNameShift;
        
        List<Action> actions = currentPower.getActions(type);
        ActionsHotbarData hotbarData = getHotbarData(type);
        int selectedAction = hotbarData.getSelectedIndex();
        if (actions.size() > 0 && selectedAction > -1) {
            Action action = actions.get(selectedAction);
            String key = type.toString().toLowerCase();
            if (action.hasShiftVariation() && currentPower.isActionUnlocked(action.getShiftVariationIfPresent())) {
                Action shiftAction = action.getShiftVariationIfPresent();
                actionName = actionNameLine(action, shiftAction, key);
                actionNameShift = actionNameLine(shiftAction, null, key);
            }
            else {
                actionName = actionNameLine(action, null, key);
                actionNameShift = actionName;
            }
        }
        else {
            actionName = EMPTY_STRING_COMPONENT;
            actionNameShift = EMPTY_STRING_COMPONENT;
        }
        
        hotbarData.name = actionName;
        hotbarData.nameShift = actionNameShift;
    }
    
    private ITextComponent actionNameLine(Action action, @Nullable Action shiftVariation, String actionTypeKey) {
        String tlKey = "overlay.actions." + actionTypeKey;
        if (action.getHoldDurationMax() > 0) {
            tlKey = tlKey + ".hold";
        }
        if (shiftVariation != null) {
            return new TranslationTextComponent(tlKey + ".shift", action.getName(currentPower), 
                    SHIFT_KEY, shiftVariation.getNameShortened(currentPower));
        }
        return new TranslationTextComponent(tlKey, action.getName(currentPower));
    }
    
    public enum UiMode {
        NONE,
        NON_STAND,
        STAND
    };
    
    private enum BarTexture {
        NONE,
        STAMINA,
        EXP,
        BLOOD,
        HAMON,
        ENERGY
    }
}
