package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.HamonBurnLayer.TextureSize;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

public class FrozenLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> implements IFirstPersonHandLayer {
    public static final ResourceLocation BIPED_PATH = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/vampire_freeze/biped");
    public static final ResourceLocation NON_BIPED_PATH = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/layer/vampire_freeze");
    
    public FrozenLayer(IEntityRenderer<T, M> renderer, ResourceLocation texturesPath) {
        super(renderer);
        initTextures(texturesPath);
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
        EffectInstance freeze = entity.getEffect(ModStatusEffects.FREEZE.get());
        if (freeze != null) {
            int freezelvl = Math.min(freeze.getAmplifier(), 3);
            TextureSize freezesize = TextureSize.getClosestTexSize(model);
            return LAYER_TEXTURES_FREEZE.get(freezesize)[freezelvl];
        }
        return null;
    }
    
    @Override
    public void renderHandFirstPerson(HandSide side, MatrixStack matrixStack, 
            IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, 
            PlayerRenderer playerRenderer) {
        PlayerModel<AbstractClientPlayerEntity> model = playerRenderer.getModel();
        IFirstPersonHandLayer.defaultRender(side, matrixStack, buffer, light, player, playerRenderer, 
                model, getTexture(model, player));
    }

    
    private Map<TextureSize, ResourceLocation[]> LAYER_TEXTURES_FREEZE;
    private void initTextures(ResourceLocation texturesPath) {
        LAYER_TEXTURES_FREEZE = Util.make(new EnumMap<>(TextureSize.class), map -> {
            String id = texturesPath.getNamespace();
            String path = texturesPath.getPath();
            map.put(TextureSize._64x32, new ResourceLocation[] {
                    new ResourceLocation(id, path + "/t64x32/1.png"),
                    new ResourceLocation(id, path + "/t64x32/2.png"),
                    new ResourceLocation(id, path + "/t64x32/3.png"),
                    new ResourceLocation(id, path + "/t64x32/4.png")
            });
            map.put(TextureSize._64x64, new ResourceLocation[] {
                    new ResourceLocation(id, path + "/t64x64/1.png"),
                    new ResourceLocation(id, path + "/t64x64/2.png"),
                    new ResourceLocation(id, path + "/t64x64/3.png"),
                    new ResourceLocation(id, path + "/t64x64/4.png")
            });
            map.put(TextureSize._128x64, new ResourceLocation[] {
                    new ResourceLocation(id, path + "/t128x64/1.png"),
                    new ResourceLocation(id, path + "/t128x64/2.png"),
                    new ResourceLocation(id, path + "/t128x64/3.png"),
                    new ResourceLocation(id, path + "/t128x64/4.png")
            });
            map.put(TextureSize._128x128, new ResourceLocation[] {
                    new ResourceLocation(id, path + "/t128x128/1.png"),
                    new ResourceLocation(id, path + "/t128x128/2.png"),
                    new ResourceLocation(id, path + "/t128x128/3.png"),
                    new ResourceLocation(id, path + "/t128x128/4.png")
            });
            map.put(TextureSize._256x128, new ResourceLocation[] {
                    new ResourceLocation(id, path + "/t256x128/1.png"),
                    new ResourceLocation(id, path + "/t256x128/2.png"),
                    new ResourceLocation(id, path + "/t256x128/3.png"),
                    new ResourceLocation(id, path + "/t256x128/4.png")
            });
            map.put(TextureSize._256x256, new ResourceLocation[] {
                    new ResourceLocation(id, path + "/t256x256/1.png"),
                    new ResourceLocation(id, path + "/t256x256/2.png"),
                    new ResourceLocation(id, path + "/t256x256/3.png"),
                    new ResourceLocation(id, path + "/t256x256/4.png")
            });
        });
    }
    
}
