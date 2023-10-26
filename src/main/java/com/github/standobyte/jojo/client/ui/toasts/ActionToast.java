package com.github.standobyte.jojo.client.ui.toasts;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ActionToast implements IToast {
    protected static final ITextComponent NAME = new TranslationTextComponent("jojo.action.toast.title");
    protected static final int TIME_MS = 5000;
    protected final ITextComponent description;
    protected final IActionToastType type;
    protected final List<ResourceLocation> actionIcons = Lists.newArrayList();
    private ResourceLocation powerTypeIcon;
    private int lastChanged;
    private boolean changed;
    
    protected ActionToast(IActionToastType type, ResourceLocation actionIcon, ResourceLocation powerTypeIcon) {
        this.type = type;
        this.description = new TranslationTextComponent("jojo.action.toast." + type.getName() + ".description");
        this.powerTypeIcon = powerTypeIcon;
        this.actionIcons.add(actionIcon);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public IToast.Visibility render(MatrixStack matrixStack, ToastGui toastGui, long timeMs) {
        if (changed) {
            lastChanged = (int) timeMs;
            changed = false;
        }
        
        if (actionIcons.isEmpty()) {
            return IToast.Visibility.HIDE;
        } else {
            Minecraft mc = toastGui.getMinecraft();
            mc.getTextureManager().bind(TEXTURE);
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            toastGui.blit(matrixStack, 0, 0, 0, 32, 160, 32);
            mc.font.draw(matrixStack, NAME, 30.0F, 7.0F, -11534256);
            mc.font.draw(matrixStack, description, 30.0F, 18.0F, -16777216);
            matrixStack.pushPose();
            matrixStack.scale(0.5F, 0.5F, 1.0F);
            
            mc.getTextureManager().bind(powerTypeIcon);
            ToastGui.blit(matrixStack, 3, 3, 0, 0, 16, 16, 16, 16);
            
            matrixStack.popPose();
            renderIcon(matrixStack, toastGui, (int) timeMs);
            return timeMs - lastChanged >= TIME_MS ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
        }
    }
    
    protected void renderIcon(MatrixStack matrixStack, ToastGui toastGui, int timeMs) {
        int actionsCount = actionIcons.size();
        int actionShowUpTime = Math.max(1, TIME_MS / actionsCount);
        int actionIndex = timeMs / actionShowUpTime % actionsCount;
        
        ResourceLocation actionIcon = actionIcons.get(actionIndex);
        toastGui.getMinecraft().getTextureManager().bind(actionIcon);
        ToastGui.blit(matrixStack, 8, 8, 0, 0, 16, 16, 16, 16);
    }
    
    protected void addAction(ResourceLocation actionIcon, ResourceLocation powerTypeIcon) {
        if (actionIcons.add(actionIcon)) {
            this.powerTypeIcon = powerTypeIcon;
            changed = true;
        }
    }
    
    public static void addOrUpdate(ToastGui toastGui, IActionToastType type, Action<?> action, IPower<?, ?> power) {
        ActionToast toast = toastGui.getToast(ActionToast.class, type);
        ResourceLocation actionIcon = ClientUtil.getActionIcon(action, power);
        ResourceLocation powerTypeIcon = ClientUtil.getIconPowerType(power);
        if (toast == null) {
            toastGui.addToast(type.createToast(actionIcon, powerTypeIcon));
        } else {
            toast.addAction(actionIcon, powerTypeIcon);
        }
    }
    
    @Override
    public IActionToastType getToken() {
        return type;
    }
    
    public static enum Type implements IActionToastType {
//        NON_STAND_ATTACK("non_stand.attack", PowerClassification.NON_STAND, ActionType.ATTACK, false),
//        NON_STAND_ABILITY("non_stand.ability", PowerClassification.NON_STAND, ActionType.ABILITY, false),
        STAND_ATTACK("stand.attack", PowerClassification.STAND, ActionType.ATTACK, false),
        STAND_ABILITY("stand.ability", PowerClassification.STAND, ActionType.ABILITY, false),
        STAND_ATTACK_VARIATION("stand.attack.shift", PowerClassification.STAND, ActionType.ATTACK, true),
        STAND_ABILITY_VARIATION("stand.ability.shift", PowerClassification.STAND, ActionType.ABILITY, true);
        
        private final String name;
        private final PowerClassification classification;
        private final ActionType actionType;
        private final boolean shiftVariation;
        
        private Type(String name, PowerClassification classification, ActionType actionType, boolean shiftVariation) {
            this.name = name;
            this.classification = classification;
            this.actionType = actionType;
            this.shiftVariation = shiftVariation;
        }
        
        @Nullable
        public static Type getToastType(PowerClassification classification, ActionType actionType, boolean shiftVariation) {
            for (Type toastType : Type.values()) {
                if (toastType.classification == classification
                        && toastType.actionType == actionType
                        && toastType.shiftVariation == shiftVariation) {
                    return toastType;
                }
            }
            return null;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public IToast createToast(ResourceLocation actionIcon, ResourceLocation powerTypeIcon) {
            return new ActionToast(this, actionIcon, powerTypeIcon);
        }
    }
    
    protected static interface IActionToastType {
        String getName();
        IToast createToast(ResourceLocation actionIcon, ResourceLocation powerTypeIcon);
    }
}
