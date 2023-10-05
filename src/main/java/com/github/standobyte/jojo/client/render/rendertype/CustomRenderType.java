package com.github.standobyte.jojo.client.render.rendertype;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

public class CustomRenderType extends RenderType {
    
    private CustomRenderType(String name, VertexFormat format, int mode, int bufferSize,
            boolean affectCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectCrumbling, sortOnUpload, setupState, clearState);
    }
    
    public static RenderType hamonProjectileShield(ResourceLocation glintTexture) { // it just works
        RenderType.State renderType$state = RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(glintTexture, true, false))
                .setWriteMaskState(COLOR_WRITE)
                .setFogState(NO_FOG)
                .setCullState(NO_CULL)
                .setTransparencyState(GLINT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setTexturingState(ENTITY_GLINT_TEXTURING)
                .createCompositeState(false);
        return RenderType.create("jojo_proj_shield", DefaultVertexFormats.BLOCK, 7, 256, false, true, renderType$state);
    }
    
    public static RenderType goldExperienceLifeformAura() {
        RenderType.State renderType$state = RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectile_shield.png"), true, false))
                .setWriteMaskState(COLOR_WRITE)
                .setFogState(NO_FOG)
                .setCullState(NO_CULL)
                .setTransparencyState(GLINT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setTexturingState(ENTITY_GLINT_TEXTURING)
                .createCompositeState(false);
        return RenderType.create("jojo_ge_lifeform", DefaultVertexFormats.BLOCK, 7, 256, false, true, renderType$state);
    }
    
    public static RenderType goldExperienceLifeformOverlay(ResourceLocation overlayTexture, float xScale, float yScale) {
        RenderType.State rendertype$state = RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(overlayTexture, false, false))
                .setTexturingState(new ScaledTexturingState(xScale, yScale))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDiffuseLightingState(DIFFUSE_LIGHTING)
                .setAlphaState(DEFAULT_ALPHA)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(false);
        return create("jojo_ge_lifeform_overlay", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, true, rendertype$state);
    }
}
