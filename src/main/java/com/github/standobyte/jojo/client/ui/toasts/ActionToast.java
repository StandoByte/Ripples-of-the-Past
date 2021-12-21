package com.github.standobyte.jojo.client.ui.toasts;

import java.util.List;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ui.sprites.SpriteUploaders;
import com.github.standobyte.jojo.power.IPowerType;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ActionToast implements IToast {
    private static final ITextComponent NAME = new TranslationTextComponent("jojo.action.toast.title");
    private final ITextComponent description;
    private final Type type;
    private final List<Action<?>> actions = Lists.newArrayList();
    private IPowerType<?, ?> powerType;
    private long lastChanged;
    private boolean changed;

    private ActionToast(Type type, Action<?> action, IPowerType<?, ?> powerType) {
        this.type = type;
        this.description = new TranslationTextComponent("jojo.action.toast." + type.powerType + type.actionType + "description");
        this.powerType = powerType;
        this.actions.add(action);
    }

    @Override
    public IToast.Visibility render(MatrixStack matrixStack, ToastGui toastGui, long delta) {
        if (changed) {
            lastChanged = delta;
            changed = false;
        }

        if (actions.isEmpty()) {
            return IToast.Visibility.HIDE;
        } else {
            Minecraft mc = toastGui.getMinecraft();
            mc.getTextureManager().bind(TEXTURE);
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            toastGui.blit(matrixStack, 0, 0, 0, 32, 160, 32);
            mc.font.draw(matrixStack, NAME, 30.0F, 7.0F, -11534256);
            mc.font.draw(matrixStack, description, 30.0F, 18.0F, -16777216);
            Action<?> action = actions.get((int)(delta / Math.max(1L, 5000L / (long)actions.size()) % (long)actions.size()));
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.6F, 0.6F, 1.0F);
            matrixStack.pushPose();
            matrixStack.scale(0.6F, 0.6F, 1.0F);
            mc.getTextureManager().bind(powerType.getIconTexture());
            ToastGui.blit(matrixStack, 3, 3, 0, 0, 16, 16, 16, 16);
            RenderSystem.popMatrix();
            matrixStack.popPose();
            TextureAtlasSprite textureAtlasSprite = SpriteUploaders.getActionSprites().getSprite(action);
            mc.getTextureManager().bind(textureAtlasSprite.atlas().location());
            ToastGui.blit(matrixStack, 8, 8, 0, 16, 16, textureAtlasSprite);
            return delta - this.lastChanged >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
        }
    }

    protected void addAction(Action<?> action, IPowerType<?, ?> powerType) {
        if (actions.add(action)) {
            this.powerType = powerType;
            changed = true;
        }
    }

    public static void addOrUpdate(ToastGui toastGui, Type type, Action<?> action, IPowerType<?, ?> powerType) {
        ActionToast toast = toastGui.getToast(ActionToast.class, type);
        if (toast == null) {
            toastGui.addToast(new ActionToast(type, action, powerType));
        } else {
            toast.addAction(action, powerType);
        }

    }

    @Override
    public Type getToken() {
        return type;
    }

    public static enum Type {
        NON_STAND_ATTACK("non_stand.", "attack."),
        NON_STAND_ABILITY("non_stand.", "ability."),
        STAND_ATTACK("stand.", "attack."),
        STAND_ABILITY("stand.", "ability."),
        STAND_ATTACK_VARIATION("stand.", "attack.shift."),
        STAND_ABILITY_VARIATION("stand.", "ability.shift.");
        
        private final String powerType;
        private final String actionType;
        
        private Type(String powerType, String actionType) {
            this.powerType = powerType;
            this.actionType = actionType;
        }
    }
}
