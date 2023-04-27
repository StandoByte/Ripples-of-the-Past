package com.github.standobyte.jojo.client.render.entity.renderer.mob;

import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsKidEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerLevelPendantLayer;
import net.minecraft.client.renderer.entity.model.VillagerModel;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

public class RockPaperScissorsKidRenderer extends MobRenderer<RockPaperScissorsKidEntity, VillagerModel<RockPaperScissorsKidEntity>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/villager/villager.png");

    public RockPaperScissorsKidRenderer(EntityRendererManager manager) {
        super(manager, new VillagerModel<>(0.0F), 0.5F);
        addLayer(new HeadLayer<>(this));
        addLayer(new VillagerLevelPendantLayer<>(this, (IReloadableResourceManager) Minecraft.getInstance().getResourceManager(), "villager"));
        addLayer(new CrossedArmsItemLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(RockPaperScissorsKidEntity entity) {
        return VILLAGER_BASE_SKIN;
    }

    @Override
    protected void scale(RockPaperScissorsKidEntity entity, MatrixStack matrixStack, float partialTick) {
        float f = 0.9375F;
        if (entity.isBaby()) {
            f = (float)((double)f * 0.5D);
            this.shadowRadius = 0.25F;
        } else {
            this.shadowRadius = 0.5F;
        }

        matrixStack.scale(f, f, f);
    }
}
