package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class VampireEyesLayer<T extends LivingEntity, M extends PlayerModel<T>> extends LayerRenderer<T, M> {
	public static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/vampire_eyes.png");
    public VampireEyesLayer(IEntityRenderer<T, M> renderer) {
        super(renderer);
        PlayerAnimationHandler.getPlayerAnimator().onArmorLayerInit(this);
    }
    
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, 
            T player, float walkAnimPos, float walkAnimSpeed, float partialTick, 
            float ticks, float headYRotation, float headXRotation) {
    	if (!player.isInvisible()) {
            M model = getParentModel();
            ResourceLocation texture = getTexture(model, player);
            if (texture == null) return;
            IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(texture));
            model.renderToBuffer(matrixStack, vertexBuilder, ClientUtil.MAX_MODEL_LIGHT, LivingRenderer.getOverlayCoords(player, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    @Nullable
    private ResourceLocation getTexture(EntityModel<?> model, LivingEntity entity) {
        if (INonStandPower.getNonStandPowerOptional(entity).resolve().flatMap(
                power -> power.getTypeSpecificData(ModPowers.ZOMBIE.get())
                .map(zombie -> !zombie.isDisguiseEnabled())).orElse(false) || 
        		INonStandPower.getNonStandPowerOptional(entity).resolve().flatMap(
                        power -> power.getTypeSpecificData(ModPowers.VAMPIRISM.get())
                        .map(vampire -> power.getEnergy() >= 400)).orElse(false)) {
            return TEXTURE;
        }
        return null;
    } 
}
