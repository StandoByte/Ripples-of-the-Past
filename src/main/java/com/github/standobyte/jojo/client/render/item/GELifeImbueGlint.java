package com.github.standobyte.jojo.client.render.item;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.effect.GEItemMarkEffect;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

public abstract class GELifeImbueGlint extends RenderType {
    private static final ResourceLocation GLINT_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/item_imbued_with_life.png");
    
    private static final RenderType GLINT_TRANSLUCENT = create("glint_translucent", DefaultVertexFormats.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderState.TextureState(GLINT_LOCATION, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .setOutputState(ITEM_ENTITY_TARGET)
            .createCompositeState(false));
    private static final RenderType GLINT = create("glint", DefaultVertexFormats.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderState.TextureState(GLINT_LOCATION, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .createCompositeState(false));
    private static final RenderType GLINT_DIRECT = create("glint_direct", DefaultVertexFormats.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderState.TextureState(GLINT_LOCATION, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .createCompositeState(false));
    private static final RenderType ENTITY_GLINT = create("entity_glint", DefaultVertexFormats.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderState.TextureState(GLINT_LOCATION, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setOutputState(ITEM_ENTITY_TARGET)
            .setTexturingState(ENTITY_GLINT_TEXTURING)
            .createCompositeState(false));
    private static final RenderType ENTITY_GLINT_DIRECT = create("entity_glint_direct", DefaultVertexFormats.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderState.TextureState(GLINT_LOCATION, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(ENTITY_GLINT_TEXTURING)
            .createCompositeState(false));
    
    private GELifeImbueGlint() {
        super(null, null, 0, 0, false, false, null, null);
    }
    
    
    @Nullable
    public static IVertexBuilder overrideVertexBuilder(ItemStack item, MatrixStack matrixStack, IRenderTypeBuffer buffer, 
            RenderType renderType, ItemCameraTransforms.TransformType transformType, boolean blockSheet) {
        IVertexBuilder builder = null;
        boolean goldEFoil = GEItemMarkEffect.isItemMarked(item, Minecraft.getInstance().player);
        if (goldEFoil) {
            if (item.getItem() == Items.COMPASS) {
                matrixStack.pushPose();
                MatrixStack.Entry matrixEntry = matrixStack.last();
                if (transformType == ItemCameraTransforms.TransformType.GUI) {
                    matrixEntry.pose().multiply(0.5F);
                } else if (transformType.firstPerson()) {
                    matrixEntry.pose().multiply(0.75F);
                }

                if (blockSheet) {
                    builder = getCompassFoilBufferDirect(buffer, renderType, matrixEntry);
                } else {
                    builder = getCompassFoilBuffer(buffer, renderType, matrixEntry);
                }

                matrixStack.popPose();
            } else if (blockSheet) {
                builder = getFoilBufferDirect(buffer, renderType, true);
            } else {
                builder = getFoilBuffer(buffer, renderType, true);
            }
        }
        return builder;
    }
    

    public static RenderType glintTranslucent() {
        return GLINT_TRANSLUCENT;
    }

    public static RenderType glint() {
        return GLINT;
    }

    public static RenderType glintDirect() {
        return GLINT_DIRECT;
    }

    public static RenderType entityGlint() {
        return ENTITY_GLINT;
    }

    public static RenderType entityGlintDirect() {
        return ENTITY_GLINT_DIRECT;
    }
    
    private static IVertexBuilder getFoilBuffer(IRenderTypeBuffer pBuffer, RenderType pRenderType, boolean pIsItem) {
        return Minecraft.useShaderTransparency() && pRenderType == Atlases.translucentItemSheet() ? 
                VertexBuilderUtils.create(pBuffer.getBuffer(GLINT_TRANSLUCENT), pBuffer.getBuffer(pRenderType))
                : VertexBuilderUtils.create(pBuffer.getBuffer(pIsItem ? glint() : entityGlint()), pBuffer.getBuffer(pRenderType));
    }
    
    private static IVertexBuilder getFoilBufferDirect(IRenderTypeBuffer pBuffer, RenderType pRenderType, boolean pNoEntity) {
        return VertexBuilderUtils.create(pBuffer.getBuffer(pNoEntity ? glintDirect() : entityGlintDirect()), pBuffer.getBuffer(pRenderType));
    }
    
    private static IVertexBuilder getCompassFoilBuffer(IRenderTypeBuffer pBuffer, RenderType pRenderType, MatrixStack.Entry pMatrixEntry) {
       return VertexBuilderUtils.create(new MatrixApplyingVertexBuilder(pBuffer.getBuffer(glint()), pMatrixEntry.pose(), pMatrixEntry.normal()), pBuffer.getBuffer(pRenderType));
    }
    
    private static IVertexBuilder getCompassFoilBufferDirect(IRenderTypeBuffer pBuffer, RenderType pRenderType, MatrixStack.Entry pMatrixEntry) {
       return VertexBuilderUtils.create(new MatrixApplyingVertexBuilder(pBuffer.getBuffer(glintDirect()), pMatrixEntry.pose(), pMatrixEntry.normal()), pBuffer.getBuffer(pRenderType));
    }
}
