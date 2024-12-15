package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

public class PillarmanBladesLayer<T extends LivingEntity, M extends EntityModel<T> & IHasArm> extends LayerRenderer<T, M> implements IFirstPersonHandLayer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/pillarman_blades.png");
    
    private final PillarmanBladesModel<T> bladesModel;
    public final boolean slim;
    
    public PillarmanBladesLayer(IEntityRenderer<T, M> renderer, boolean slim) {
        super(renderer);
        this.slim = slim;
        this.bladesModel = new PillarmanBladesModel<>(slim);
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity, 
            float limbSwing, float limbSwingAmount, float partialTick, float ticks, float yRot, float xRot) {
        if (INonStandPower.getNonStandPowerOptional(entity).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()))
                .filter(PillarmanData::getBladesVisible).isPresent()) {
            matrixStack.pushPose();
            if (getParentModel().young) {
                matrixStack.translate(0.0D, 0.75D, 0.0D);
                matrixStack.scale(0.5F, 0.5F, 0.5F);
            }

            renderBlade(entity, HandSide.RIGHT, matrixStack, buffer);
            renderBlade(entity, HandSide.LEFT, matrixStack, buffer);
            matrixStack.popPose();
        }
    }

    private void renderBlade(LivingEntity entity, HandSide side, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        matrixStack.pushPose();
        getParentModel().translateToHand(side, matrixStack);
        PlayerAnimationHandler.getPlayerAnimator().onItemLikeLayerRender(matrixStack, entity, side);
        
        IVertexBuilder vertexBuilder = buffer.getBuffer(bladesModel.renderType(TEXTURE));
        ModelRenderer blade;
        switch (side) {
        case LEFT:
            blade = bladesModel.bladeLeft;
            break;
        case RIGHT:
            blade = bladesModel.bladeRight;
            break;
        default:
            throw new AssertionError();
        }
        
        blade.render(matrixStack, vertexBuilder, ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        matrixStack.popPose();
    }

    @Override
    public void renderHandFirstPerson(HandSide side, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light,
            AbstractClientPlayerEntity player, PlayerRenderer playerRenderer) {
        // TODO render blades in 1st person
    }
}
