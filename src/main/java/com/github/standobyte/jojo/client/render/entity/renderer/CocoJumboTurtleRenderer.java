package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.client.render.entity.model.mob.CocoJumboTurtleModel;
import com.github.standobyte.jojo.entity.mob.CocoJumboTurtleEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class CocoJumboTurtleRenderer extends MobRenderer<CocoJumboTurtleEntity, CocoJumboTurtleModel<CocoJumboTurtleEntity>> {
    private static final ResourceLocation TURTLE_LOCATION = new ResourceLocation("textures/entity/turtle/big_sea_turtle.png");
    
    public CocoJumboTurtleRenderer(EntityRendererManager renderManager) {
        super(renderManager, new CocoJumboTurtleModel<>(0.0F), 0.7F);
    }
    
    @Override
    public void render(CocoJumboTurtleEntity pEntity, float pEntityYaw, float pPartialTicks, 
            MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
        if (pEntity.isBaby()) {
            this.shadowRadius *= 0.5F;
        }
        model.hasKey = pEntity.hasKey();

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
    
    @Override
    public ResourceLocation getTextureLocation(CocoJumboTurtleEntity pEntity) {
        return TURTLE_LOCATION;
    }
}
