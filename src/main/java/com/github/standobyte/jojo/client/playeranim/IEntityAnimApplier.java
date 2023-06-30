package com.github.standobyte.jojo.client.playeranim;

import com.github.standobyte.jojo.client.ClientTicking;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;

public interface IEntityAnimApplier<T extends LivingEntity, M extends BipedModel<T>> extends ClientTicking.ITicking {
    
    void onInit();
    void setEmote();
    void applyBodyTransforms(MatrixStack matrixStack, float partialTick);
    
    

    public static <T extends LivingEntity, M extends BipedModel<T>> IEntityAnimApplier<T, M> createDummy() {
        return new Dummy<>();
    }
    
    public static class Dummy<T extends LivingEntity, M extends BipedModel<T>> implements IEntityAnimApplier<T, M> {
        @Override public void onInit() {}
        @Override public void setEmote() {}
        @Override public void applyBodyTransforms(MatrixStack matrixStack, float partialTick) {}
        @Override public void tick() {}
    }
}
