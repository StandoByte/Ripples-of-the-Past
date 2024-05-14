package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class PillarmanLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
    private static final Map<PlayerRenderer, PillarmanLayer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> RENDERER_LAYERS = new HashMap<>();
    public static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/pillarman_layer.png");
    
    public PillarmanLayer(IEntityRenderer<T, M> renderer) {
        super(renderer);
        if (renderer instanceof PlayerRenderer) {
            RENDERER_LAYERS.put((PlayerRenderer) renderer, (PillarmanLayer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>) this);
        }
    }
    
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, 
            T entity, float walkAnimPos, float walkAnimSpeed, float partialTick, 
            float ticks, float headYRotation, float headXRotation) {
	        if (!entity.isInvisible()) {
		        M model = getParentModel();
		        ResourceLocation texture = getTexture(model, entity);
		        if (texture == null) return;
		        IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(texture));
		        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, LivingRenderer.getOverlayCoords(entity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
	        }
    }
    
    @Nullable
    private ResourceLocation getTexture(EntityModel<?> model, LivingEntity entity) {
        if (INonStandPower.getNonStandPowerOptional(entity).map(
                power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get())
                .map(pillarman -> pillarman.isStoneFormEnabled()).orElse(false)).orElse(false)) {
            return TEXTURE;
        }
        return null;
    }
    
}
