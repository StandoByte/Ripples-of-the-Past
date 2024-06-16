package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.mob.CocoJumboTurtleModel;
import com.github.standobyte.jojo.entity.mob.CocoJumboTurtleEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class CocoJumboTurtleRenderer extends MobRenderer<CocoJumboTurtleEntity, CocoJumboTurtleModel<CocoJumboTurtleEntity>> {
    private static final ResourceLocation TURTLE_LOCATION = new ResourceLocation("textures/entity/turtle/big_sea_turtle.png");
    private static final ResourceLocation TURTLE_LOCATION_2 = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/mob/turtle_extra.png");
    private static final ResourceLocation KEY_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/mob/turtle_key.png");
    
    public CocoJumboTurtleRenderer(EntityRendererManager renderManager) {
        super(renderManager, new CocoJumboTurtleModel<>(0.0F), 0.7F);
        addLayer(new CocoJumboExtraTextureStuff(this));
        addLayer(new MrPresidentKeyLayer(this));
    }
    
    @Override
    public void render(CocoJumboTurtleEntity entity, float yRot, float partialTick, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int light) {
        if (entity.isBaby()) {
            this.shadowRadius *= 0.5F;
        }
        model.hasKey = entity.hasKey();

        super.render(entity, yRot, partialTick, matrixStack, buffer, light);
    }
    
    @Override
    public ResourceLocation getTextureLocation(CocoJumboTurtleEntity pEntity) {
        return TURTLE_LOCATION;
    }
    
    
    
    public static class CocoJumboExtraTextureStuff extends LayerRenderer<CocoJumboTurtleEntity, CocoJumboTurtleModel<CocoJumboTurtleEntity>> {

        public CocoJumboExtraTextureStuff(IEntityRenderer<CocoJumboTurtleEntity, CocoJumboTurtleModel<CocoJumboTurtleEntity>> renderer) {
            super(renderer);
        }

        @Override
        public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight,
                CocoJumboTurtleEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks,
                float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            IVertexBuilder ivertexbuilder = pBuffer.getBuffer(RenderType.entityCutoutNoCull(TURTLE_LOCATION_2));
            this.getParentModel().renderToBuffer(pMatrixStack, ivertexbuilder, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        
    }
    
    public static class MrPresidentKeyLayer extends LayerRenderer<CocoJumboTurtleEntity, CocoJumboTurtleModel<CocoJumboTurtleEntity>> {

        public MrPresidentKeyLayer(IEntityRenderer<CocoJumboTurtleEntity, CocoJumboTurtleModel<CocoJumboTurtleEntity>> renderer) {
            super(renderer);
        }

        @Override
        public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight,
                CocoJumboTurtleEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks,
                float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            if (pLivingEntity.hasKey()) {
                IVertexBuilder ivertexbuilder = pBuffer.getBuffer(RenderType.entityCutoutNoCull(KEY_LOCATION));
                this.getParentModel().renderToBuffer(pMatrixStack, ivertexbuilder, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        
    }
}
