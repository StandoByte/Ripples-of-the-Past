package com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage;

import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.client.playeranim.IPlayerBarrageAnimation;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.BarrageSwingsHolder;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

public class BarrageFistAfterimagesLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {
    private IPlayerBarrageAnimation barrageAnim = null;
    private final PlayerModel<AbstractClientPlayerEntity> model;
    private final PlayerModel<AbstractClientPlayerEntity> modelSlim;

    public BarrageFistAfterimagesLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> renderer) {
        super(renderer);
        this.model = new PlayerModel<>(0.0F, false);
        this.modelSlim = new PlayerModel<>(0.0F, true);
    }
    
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, 
            AbstractClientPlayerEntity entity, float walkAnimPos, float walkAnimSpeed, float partialTick, 
            float ticks, float headYRotation, float headXRotation) {
        if (lazyInitBarrageAnim() != null) {
            if (isBarraging(entity)) {
                barrageAnim.addSwings(entity, entity.getMainArm(), ticks);
            }

            Minecraft mc = Minecraft.getInstance();
            BarrageSwingsHolder<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> swings = getSwings(entity);
            
            if (swings != null && swings.hasSwings()) {
                boolean visible = !entity.isInvisible();
                boolean spectatorVisibility = !visible && !entity.isInvisibleTo(mc.player);
                boolean glowing = mc.shouldEntityAppearGlowing(entity);
                RenderType renderType = null;
                ResourceLocation texture = getTextureLocation(entity);
                if (spectatorVisibility) {
                    renderType = RenderType.itemEntityTranslucentCull(texture);
                } else if (visible) {
                    renderType = model.renderType(texture);
                } else if (glowing) {
                    renderType = RenderType.outline(texture);
                }
                if (renderType == null) return;
                
                PlayerModel<AbstractClientPlayerEntity> model = getModel(entity);
                PlayerModel<AbstractClientPlayerEntity> parentModel = getParentModel();
                parentModel.copyPropertiesTo(model);
                model.prepareMobModel(entity, walkAnimPos, walkAnimSpeed, partialTick);
                model.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, headYRotation, headXRotation);
                
                matrixStack.pushPose();
                barrageAnim.beforeSwingsRender(matrixStack, this);
                
                IVertexBuilder vertexBuilder = buffer.getBuffer(renderType);
                swings.renderBarrageSwings(model, entity, matrixStack, vertexBuilder, 
                        packedLight, OverlayTexture.NO_OVERLAY, 
                        headYRotation * MathUtil.DEG_TO_RAD, headXRotation * MathUtil.DEG_TO_RAD, 
                        1.0F, 1.0F, 1.0F, spectatorVisibility ? 0.15F : 1.0F);
                
                matrixStack.popPose();
                
                swings.updateSwings(mc);
            }
        }
    }
    
    private PlayerModel<AbstractClientPlayerEntity> getModel(AbstractClientPlayerEntity entity) {
        boolean slim = "slim".equals(entity.getModelName());
        return slim ? modelSlim : model;
    }
    
    private IPlayerBarrageAnimation lazyInitBarrageAnim() {
        if (barrageAnim == null) {
            barrageAnim = PlayerAnimationHandler.getPlayerAnimator().createBarrageAfterimagesAnim(model, this);
        }
        return barrageAnim;
    }
    
    public void setArmsVisibility(PlayerModel<AbstractClientPlayerEntity> model, HandSide punchingHand) {
        setVisibility(model.head, false);
        setVisibility(model.hat, false);
        setVisibility(model.body, false);
        setVisibility(model.jacket, false);
        setVisibility(model.leftLeg, false);
        setVisibility(model.leftPants, false);
        setVisibility(model.rightLeg, false);
        setVisibility(model.rightPants, false);
        boolean rightSide = punchingHand == HandSide.RIGHT;
        setVisibility(model.leftArm, !rightSide);
        setVisibility(model.leftSleeve, false);
        setVisibility(model.rightArm, rightSide);
        setVisibility(model.rightSleeve, false);
    }
    
    private void setVisibility(ModelRenderer part, boolean visible) {
        part.visible = visible;
    }
    
    public static BarrageSwingsHolder<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> getSwings(AbstractClientPlayerEntity player) {
        return player.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).map(cap -> cap.getBarrageSwings()).orElse(null);
    }
    
    public static void setIsBarraging(AbstractClientPlayerEntity player, boolean isBarraging) {
        player.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setIsBarraging(isBarraging));
    }
    
    private static boolean isBarraging(AbstractClientPlayerEntity player) {
        return player.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).map(cap -> cap.isBarraging()).orElse(false);
    }

}
