package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.item.GlovesItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

public class GlovesLayer<T extends LivingEntity, M extends PlayerModel<T>> extends LayerRenderer<T, M> {
    private static final Map<PlayerRenderer, GlovesLayer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> RENDERER_LAYERS = new HashMap<>();
    private final M glovesModel;
    private final boolean slim;
    private boolean playerAnimHandled = false;
    
    public GlovesLayer(IEntityRenderer<T, M> renderer, M glovesModel, boolean slim) {
        super(renderer);
        if (renderer instanceof PlayerRenderer) {
            RENDERER_LAYERS.put((PlayerRenderer) renderer, (GlovesLayer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>) this);
        }
        this.glovesModel = glovesModel;
        this.slim = slim;
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity, 
            float limbSwing, float limbSwingAmount, float partialTick, float ticks, float yRot, float xRot) {
        if (!playerAnimHandled) {
            PlayerAnimationHandler.getPlayerAnimator().onArmorLayerInit(this);
            playerAnimHandled = true;
        }

        ItemStack glovesItemStack = getRenderedGlovesItem(entity);
        if (!glovesItemStack.isEmpty()) {
            GlovesItem gloves = (GlovesItem) glovesItemStack.getItem();
            M playerModel = getParentModel();
            glovesModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
            playerModel.copyPropertiesTo(glovesModel);
            glovesModel.setupAnim(entity, limbSwing, limbSwingAmount, ticks, yRot, xRot);
            
            glovesModel.leftArm.visible = playerModel.leftArm.visible;
            glovesModel.leftSleeve.visible = playerModel.leftArm.visible;
            glovesModel.rightArm.visible = playerModel.rightArm.visible;
            glovesModel.rightSleeve.visible = playerModel.rightArm.visible;
            ResourceLocation texture = getTexture(gloves);
            IVertexBuilder vertexBuilder = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(texture), false, glovesItemStack.hasFoil());
            glovesModel.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        }
    }
    
    
    
    public static void renderFirstPerson(HandSide side, MatrixStack matrixStack, 
            IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player) {
        EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        if (renderer instanceof PlayerRenderer) {
            PlayerRenderer playerRenderer = (PlayerRenderer) renderer;
            if (RENDERER_LAYERS.containsKey(playerRenderer)) {
                GlovesLayer<?, ?> layer = RENDERER_LAYERS.get(playerRenderer);
                if (layer != null) {
                    layer.renderHandFirstPerson(side, matrixStack, 
                            buffer, light, player, playerRenderer);
                }
            }
        }
    }
    
    private void renderHandFirstPerson(HandSide side, MatrixStack matrixStack, 
            IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, 
            PlayerRenderer playerRenderer) {
        if (player.isSpectator()) return;
        
        ItemStack glovesItemStack = getRenderedGlovesItem(player);
        if (glovesItemStack.isEmpty()) return;
        
        PlayerModel<AbstractClientPlayerEntity> model = playerRenderer.getModel();
        ClientUtil.setupForFirstPersonRender(model, player);
        GlovesItem glovesItem = (GlovesItem) glovesItemStack.getItem();
        ClientUtil.setupForFirstPersonRender((PlayerModel<AbstractClientPlayerEntity>) glovesModel, player);
        ModelRenderer glove = ClientUtil.getArm(model, side);
        ModelRenderer gloveOuter = ClientUtil.getArmOuter(model, side);
        ResourceLocation texture = getTexture(glovesItem);
        IVertexBuilder vertexBuilder = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(texture), false, glovesItemStack.hasFoil());
        glove.xRot = 0.0F;
        glove.render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY);
        gloveOuter.xRot = 0.0F;
        gloveOuter.render(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY);
    }
    
    private ResourceLocation getTexture(GlovesItem gloves) {
        return new ResourceLocation(
                gloves.getRegistryName().getNamespace(), 
                "textures/entity/layer/" + gloves.getRegistryName().getPath() + (slim ? "_slim" : "") + ".png");
    }
    
    
    
    // if the returned stack isn't empty, the return result's (ItemStack#getItem() instanceof GlovesItem) is guaranteed to be true
    public static ItemStack getRenderedGlovesItem(LivingEntity entity) {
        ItemStack checkedItem = entity.getMainHandItem();
        if (areGloves(checkedItem)) return checkedItem;
        checkedItem = entity.getOffhandItem();
        if (areGloves(checkedItem)) return checkedItem;
        return ItemStack.EMPTY;
    }
    
    public static boolean areGloves(ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof GlovesItem;
    }
}
