package com.github.standobyte.jojo.client.ui.toasts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.client.ui.screen.stand.ge.EntityTypeIcon;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

@SuppressWarnings("deprecation")
public class MetEntityTypeToast implements IToast {
    private static final ITextComponent NAME = new TranslationTextComponent("ge_new_lifeform.toast.title");
    private final List<EntityType<?>> entityTypes = new ArrayList<>();
    private long lastChanged;
    private boolean changed;

    private MetEntityTypeToast(EntityType<?> entityType) {
        this.entityTypes.add(entityType);
    }

    @Override
    public IToast.Visibility render(MatrixStack matrixStack, ToastGui toastGui, long delta) {
        if (changed) {
            lastChanged = delta;
            changed = false;
        }

        if (entityTypes.isEmpty()) {
            return IToast.Visibility.HIDE;
        } else {
            Minecraft mc = toastGui.getMinecraft();
            mc.getTextureManager().bind(TEXTURE);
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            toastGui.blit(matrixStack, 0, 0, 0, 32, 160, 32);
            mc.font.draw(matrixStack, NAME, 30.0F, 7.0F, -0xAFFFB0);
            
            EntityType<?> entityType = entityTypes.get((int)(delta / Math.max(1L, 5000L / (long)entityTypes.size()) % (long)entityTypes.size()));
            ITextComponent description = entityType.getDescription();
            mc.font.draw(matrixStack, description, 30.0F, 18.0F, -0x1000000);
            EntityTypeIcon.renderIcon(entityType, matrixStack, 8, 8);

            matrixStack.pushPose();
            matrixStack.scale(0.5F, 0.5F, 1.0F);
            ResourceLocation standIcon = IStandPower.getStandPowerOptional(mc.player).resolve().flatMap(
                    power -> power.getType() == ModStands.GOLD_EXPERIENCE.getStandType() ? 
                            Optional.of(power.clGetPowerTypeIcon())
                            : Optional.empty())
                    .orElse(ModStands.GOLD_EXPERIENCE.getStandType().getIconTexture(null));
            mc.getTextureManager().bind(standIcon);
            ToastGui.blit(matrixStack, 3, 3, 0, 0, 16, 16, 16, 16);
            matrixStack.popPose();
            
            return delta - this.lastChanged >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
        }
    }

    protected void addMetType(EntityType<?> entityType) {
        if (entityTypes.add(entityType)) {
            changed = true;
        }
    }

    public static void addOrUpdate(ToastGui toastGui, EntityType<?> entityType) {
        MetEntityTypeToast toast = toastGui.getToast(MetEntityTypeToast.class, NO_TOKEN);
        if (toast == null) {
            toastGui.addToast(new MetEntityTypeToast(entityType));
        } else {
            toast.addMetType(entityType);
        }

    }
}
