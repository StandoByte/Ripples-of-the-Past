package com.github.standobyte.jojo.client.ui.toasts;

import java.util.List;

import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

@SuppressWarnings("deprecation")
public class HamonSkillToast implements IToast {
    private static final ITextComponent NAME = new TranslationTextComponent("hamon_skill.toast.title");
    private final Type type;
    private final ITextComponent description;
    private final List<AbstractHamonSkill> skills = Lists.newArrayList();
    private long lastChanged;
    private boolean changed;

    private HamonSkillToast(Type type, AbstractHamonSkill skill) {
        this.type = type;
        this.description = new TranslationTextComponent("hamon_skill.toast." + type.skillType + "description", 
                new KeybindTextComponent("jojo.key.hamon_skills_window").withStyle(TextFormatting.BOLD));
        this.skills.add(skill);
    }

    @Override
    public IToast.Visibility render(MatrixStack matrixStack, ToastGui toastGui, long delta) {
        if (changed) {
            lastChanged = delta;
            changed = false;
        }

        if (skills.isEmpty()) {
            return IToast.Visibility.HIDE;
        } else {
            Minecraft mc = toastGui.getMinecraft();
            mc.getTextureManager().bind(TEXTURE);
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            toastGui.blit(matrixStack, 0, 0, 0, 32, 160, 32);
            mc.font.draw(matrixStack, NAME, 30.0F, 7.0F, -11534256);
            mc.font.draw(matrixStack, description, 30.0F, 18.0F, -16777216);
            AbstractHamonSkill skill = skills.get((int)(delta / Math.max(1L, 5000L / (long)skills.size()) % (long)skills.size()));
            TextureAtlasSprite textureAtlasSprite = CustomResources.getHamonSkillSprites().getSprite(skill);
            mc.getTextureManager().bind(textureAtlasSprite.atlas().location());
            ToastGui.blit(matrixStack, 8, 8, 0, 16, 16, textureAtlasSprite);
            return delta - this.lastChanged >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
        }
    }

    protected void addAction(AbstractHamonSkill skill) {
        if (skills.add(skill)) {
            changed = true;
        }
    }

    public static void addOrUpdate(ToastGui toastGui, Type type, AbstractHamonSkill skill) {
        HamonSkillToast toast = toastGui.getToast(HamonSkillToast.class, type);
        if (toast == null) {
            toastGui.addToast(new HamonSkillToast(type, skill));
        } else {
            toast.addAction(skill);
        }

    }

    @Override
    public Type getToken() {
        return type;
    }

    public static enum Type {
        STRENGTH("strength."),
        CONTROL("control."),
        TECHNIQUE("technique.");
        
        private final String skillType;
        
        private Type(String skillType) {
            this.skillType = skillType;
        }
    }
}
