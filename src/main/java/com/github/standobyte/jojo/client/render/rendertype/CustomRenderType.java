package com.github.standobyte.jojo.client.render.rendertype;

import java.util.Map;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
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
    
    
    private static final ResourceLocation GE_GLINT_PATH = new ResourceLocation(JojoMod.MOD_ID, "textures/item_imbued_with_life.png");
    public static RenderType goldExperienceLifeformAura() {
        RenderType.State renderType$state = RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(GE_GLINT_PATH, true, false))
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
    
    private static final RenderType GE_IMBUED_GLINT = RenderType.create("jojo_ge_glint", DefaultVertexFormats.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderState.TextureState(GE_GLINT_PATH, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .createCompositeState(false));
    public static RenderType geImbuedGlint() {
        return GE_IMBUED_GLINT;
    }
    
    private static final RenderType GE_IMBUED_GLINT_DIRECT = RenderType.create("jojo_ge_glint_direct", DefaultVertexFormats.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderState.TextureState(GE_GLINT_PATH, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .createCompositeState(false));
    public static RenderType geImbuedGlintDirect() {
        return GE_IMBUED_GLINT_DIRECT;
    }
    
    private static final RenderType GE_IMBUED_GLINT_TRANSLUCENT = create("jojo_ge_glint_translucent", DefaultVertexFormats.POSITION_TEX, 7, 256, 
            RenderType.State.builder()
            .setTextureState(new RenderState.TextureState(GE_GLINT_PATH, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .setOutputState(ITEM_ENTITY_TARGET)
            .createCompositeState(false));
    public static RenderType geImbuedGlintTranslucent() {
        return GE_IMBUED_GLINT_TRANSLUCENT;
    }
    
    public static void addExtraFixedBuffers(Minecraft mc) {
        Map<RenderType, BufferBuilder> fixedBuffers = ClientReflection.getFixedBuffers(mc.renderBuffers().bufferSource());
        fixedBuffers.put(GE_IMBUED_GLINT, new BufferBuilder(GE_IMBUED_GLINT.bufferSize()));
        fixedBuffers.put(GE_IMBUED_GLINT_DIRECT, new BufferBuilder(GE_IMBUED_GLINT_DIRECT.bufferSize()));
        fixedBuffers.put(GE_IMBUED_GLINT_TRANSLUCENT, new BufferBuilder(GE_IMBUED_GLINT_TRANSLUCENT.bufferSize()));
    }
    
}
