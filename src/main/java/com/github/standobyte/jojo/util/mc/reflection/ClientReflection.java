package com.github.standobyte.jojo.util.mc.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.matrix.MatrixStack;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.audio.ISoundEventAccessor;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ClientReflection {
    private static final Field FIRST_PERSON_RENDERER_MAIN_HAND_HEIGHT = ObfuscationReflectionHelper.findField(FirstPersonRenderer.class, "field_187469_f");
    public static float getMainHandHeight(FirstPersonRenderer renderer) {
        return ReflectionUtil.getFloatFieldValue(FIRST_PERSON_RENDERER_MAIN_HAND_HEIGHT, renderer);
    }

    private static final Field FIRST_PERSON_RENDERER_O_MAIN_HAND_HEIGHT = ObfuscationReflectionHelper.findField(FirstPersonRenderer.class, "field_187470_g");
    public static float getMainHandHeightPrev(FirstPersonRenderer renderer) {
        return ReflectionUtil.getFloatFieldValue(FIRST_PERSON_RENDERER_O_MAIN_HAND_HEIGHT, renderer);
    }
    
    private static final Field FIRST_PERSON_RENDERER_OFF_HAND_HEIGHT = ObfuscationReflectionHelper.findField(FirstPersonRenderer.class, "field_187471_h");
    public static float getOffHandHeight(FirstPersonRenderer renderer) {
        return ReflectionUtil.getFloatFieldValue(FIRST_PERSON_RENDERER_OFF_HAND_HEIGHT, renderer);
    }

    private static final Field FIRST_PERSON_RENDERER_O_OFF_HAND_HEIGHT = ObfuscationReflectionHelper.findField(FirstPersonRenderer.class, "field_187472_i");
    public static float getOffHandHeightPrev(FirstPersonRenderer renderer) {
        return ReflectionUtil.getFloatFieldValue(FIRST_PERSON_RENDERER_O_OFF_HAND_HEIGHT, renderer);
    }

    private static final Method FIRST_PERSON_RENDERER_RENDER_PLAYER_ARM = ObfuscationReflectionHelper.findMethod(FirstPersonRenderer.class, "func_228401_a_", 
            MatrixStack.class, IRenderTypeBuffer.class, int.class, float.class, float.class, HandSide.class);
    public static void renderPlayerArm(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, 
            float handHeight, float swingAnim, HandSide handSide, FirstPersonRenderer renderer) {
        ReflectionUtil.invokeMethod(FIRST_PERSON_RENDERER_RENDER_PLAYER_ARM, 
                renderer, matrixStack, buffer, packedLight, handHeight, swingAnim, handSide);
    }
    
    
    private static final Field MINECRAFT_PAUSE = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71445_n");
    public static void pauseClient(Minecraft minecraft) {
        ReflectionUtil.setFieldValue(MINECRAFT_PAUSE, minecraft, true);
    }

    private static final Field MINECRAFT_TIMER = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71428_T");
    public static Timer getTimer(Minecraft minecraft) {
        return ReflectionUtil.getFieldValue(MINECRAFT_TIMER, minecraft);
    }
    
    
    private static final Field MAIN_MENU_SCREEN_SPLASH = ObfuscationReflectionHelper.findField(MainMenuScreen.class, "field_73975_c");
    public static void setSplash(MainMenuScreen screen, String splash) {
        ReflectionUtil.setFieldValue(MAIN_MENU_SCREEN_SPLASH, screen, splash);
    }
    
    
    private static final Field RENDER_TYPE_BUFFER_IMPL_BUILDER = ObfuscationReflectionHelper.findField(IRenderTypeBuffer.Impl.class, "field_228457_a_");
    public static BufferBuilder getBuilder(IRenderTypeBuffer.Impl buffers) {
        return ReflectionUtil.getFieldValue(RENDER_TYPE_BUFFER_IMPL_BUILDER, buffers);
    }
    
    private static final Field RENDER_TYPE_BUFFER_IMPL_FIXED_BUFFERS = ObfuscationReflectionHelper.findField(IRenderTypeBuffer.Impl.class, "field_228458_b_");
    public static Map<RenderType, BufferBuilder> getFixedBuffers(IRenderTypeBuffer.Impl buffers) {
        return ReflectionUtil.getFieldValue(RENDER_TYPE_BUFFER_IMPL_FIXED_BUFFERS, buffers);
    }
    
    
    private static final Field MODEL_RENDERER_CUBES = ObfuscationReflectionHelper.findField(ModelRenderer.class, "field_78804_l");
    public static void setCubes(ModelRenderer modelRenderer, ObjectList<ModelRenderer.ModelBox> cubes) {
        ReflectionUtil.setFieldValue(MODEL_RENDERER_CUBES, modelRenderer, cubes);
    }
    
    public static void addCube(ModelRenderer modelRenderer, ModelRenderer.ModelBox cube) {
        List<ModelRenderer.ModelBox> cubes = ReflectionUtil.getFieldValue(MODEL_RENDERER_CUBES, modelRenderer);
        cubes.add(cube);
    }
    
    public static ObjectList<ModelRenderer.ModelBox> getCubes(ModelRenderer modelRenderer) {
        return ReflectionUtil.getFieldValue(MODEL_RENDERER_CUBES, modelRenderer);
    }
    
    
    private static final Field MODEL_RENDERER_CHILDREN = ObfuscationReflectionHelper.findField(ModelRenderer.class, "field_78805_m");
    public static ObjectList<ModelRenderer> getChildren(ModelRenderer modelRenderer) {
        return ReflectionUtil.getFieldValue(MODEL_RENDERER_CHILDREN, modelRenderer);
    }
    
    public static void setChildren(ModelRenderer modelRenderer, ObjectList<ModelRenderer> children) {
        ReflectionUtil.setFieldValue(MODEL_RENDERER_CHILDREN, modelRenderer, children);
    }
    
    private static final Method AGEABLE_MODEL_HEAD_PARTS = ObfuscationReflectionHelper.findMethod(AgeableModel.class, "func_225602_a_");
    public static Iterable<ModelRenderer> getHeadParts(AgeableModel<?> model) {
        return ReflectionUtil.invokeMethod(AGEABLE_MODEL_HEAD_PARTS, model);
    }
    
    private static final Method AGEABLE_MODEL_BODY_PARTS = ObfuscationReflectionHelper.findMethod(AgeableModel.class, "func_225600_b_");
    public static Iterable<ModelRenderer> getBodyParts(AgeableModel<?> model) {
        return ReflectionUtil.invokeMethod(AGEABLE_MODEL_BODY_PARTS, model);
    }

    private static final Method LIVING_RENDERER_SCALE = ObfuscationReflectionHelper.findMethod(LivingRenderer.class, "func_225620_a_", 
            LivingEntity.class, MatrixStack.class, float.class);
    public static void scale(LivingRenderer<?, ?> renderer, LivingEntity entity, MatrixStack matrixStack, float partialTick) {
        ReflectionUtil.invokeMethod(LIVING_RENDERER_SCALE, renderer, entity, matrixStack, partialTick);
    }

    private static final Field ENTITY_RENDERER_SHADOW_RADIUS = ObfuscationReflectionHelper.findField(EntityRenderer.class, "field_76989_e");
    public static float getShadowRadius(EntityRenderer<?> renderer) {
        return ReflectionUtil.getFloatFieldValue(ENTITY_RENDERER_SHADOW_RADIUS, renderer);
    }

    private static final Field LIVING_RENDERER_LAYERS = ObfuscationReflectionHelper.findField(LivingRenderer.class, "field_177097_h");
    public static <T extends LivingEntity, M extends EntityModel<T>> List<LayerRenderer<T, M>> getLayers(LivingRenderer<T, M> renderer) {
        return ReflectionUtil.getFieldValue(LIVING_RENDERER_LAYERS, renderer);
    }
    
    
    private static final Field MODEL_BOX_POLYGONS = ObfuscationReflectionHelper.findField(ModelRenderer.ModelBox.class, "field_78254_i");
    @Deprecated
    public static ModelRenderer.TexturedQuad[] getPolygons(ModelRenderer.ModelBox modelBox) {
        return ReflectionUtil.getFieldValue(MODEL_BOX_POLYGONS, modelBox);
    }
    
    public static void setPolygons(ModelRenderer.ModelBox modelBox, ModelRenderer.TexturedQuad[] polygons) {
        ReflectionUtil.setFieldValue(MODEL_BOX_POLYGONS, modelBox, polygons);
    }
    
    
    private static final Field TEXTURED_QUAD_VERTICES = ObfuscationReflectionHelper.findField(ModelRenderer.TexturedQuad.class, "field_78239_a");
    public static void setVertices(ModelRenderer.TexturedQuad quad, ModelRenderer.PositionTextureVertex[] vertices) {
        ReflectionUtil.setFieldValue(TEXTURED_QUAD_VERTICES, quad, vertices);
    }
    
    private static final Field TEXTURED_QUAD_NORMAL = ObfuscationReflectionHelper.findField(ModelRenderer.TexturedQuad.class, "field_228312_b_");
    public static void setNormal(ModelRenderer.TexturedQuad quad, Vector3f normal) {
        ReflectionUtil.setFieldValue(TEXTURED_QUAD_NORMAL, quad, normal);
    }
    
    
    private static final Field SOUND_EVENT_ACCESSOR_LIST = ObfuscationReflectionHelper.findField(SoundEventAccessor.class, "field_188716_a");
    public static List<ISoundEventAccessor<Sound>> getSubAccessorsList(SoundEventAccessor accessor) {
        return ReflectionUtil.getFieldValue(SOUND_EVENT_ACCESSOR_LIST, accessor);
    }
    
    
    private static final Field INGAME_MENU_SCREEN_SHOW_PAUSE_MENU = ObfuscationReflectionHelper.findField(IngameMenuScreen.class, "field_222813_a");
    public static boolean showsPauseMenu(IngameMenuScreen screen) {
        return ReflectionUtil.getBooleanFieldValue(INGAME_MENU_SCREEN_SHOW_PAUSE_MENU, screen);
    }
    
    
    private static final Field PARTICLE_MANAGER_SPRITE_SETS = ObfuscationReflectionHelper.findField(ParticleManager.class, "field_215242_i");
    public static Map<ResourceLocation, ? extends IAnimatedSprite> getSpriteSets(ParticleManager particleManager) {
        return ReflectionUtil.getFieldValue(PARTICLE_MANAGER_SPRITE_SETS, particleManager);
    }
    
    
    private static final Field MODEL_BAKERY_UNREFERENCED_TEXTURES = ObfuscationReflectionHelper.findField(ModelBakery.class, "field_177602_b");
    public static Set<RenderMaterial> getModelBakeryUnreferencedTextures() {
        return ReflectionUtil.getFieldValue(MODEL_BAKERY_UNREFERENCED_TEXTURES, null);
    }

    
    private static final Field TEXTURE_MANAGER_TICKABLE_TEXTURES = ObfuscationReflectionHelper.findField(TextureManager.class, "field_110583_b");
    public static Set<ITickable> getTickableTextures(TextureManager textureManager) {
        return ReflectionUtil.getFieldValue(TEXTURE_MANAGER_TICKABLE_TEXTURES, textureManager);
    }
    
    public static void setTickableTextures(TextureManager textureManager, Set<ITickable> textures) {
        ReflectionUtil.setFieldValue(TEXTURE_MANAGER_TICKABLE_TEXTURES, textureManager, textures);
    }
    
    
    private static final Field MINECRAFT_PARTICLE_ENGINE = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71452_i");
    public static void setParticleEngine(Minecraft mc, ParticleManager particleEngine) {
        ReflectionUtil.setFieldValue(MINECRAFT_PARTICLE_ENGINE, mc, particleEngine);
    }
    
    private static final Field PARTICLE_MANAGER_TEXTURE_MANAGER = ObfuscationReflectionHelper.findField(ParticleManager.class, "field_78877_c");
    public static void setTextureManager(ParticleManager particleManager, TextureManager textureManager) {
        ReflectionUtil.setFieldValue(PARTICLE_MANAGER_TEXTURE_MANAGER, particleManager, textureManager);
    }
    
    private static final Field MINECRAFT_MOUSE_HANDLER = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71417_B");
    public static void setMouseHandler(Minecraft mc, MouseHelper mouseHandler) {
        ReflectionUtil.setFieldValue(MINECRAFT_MOUSE_HANDLER, mc, mouseHandler);
    }
    
    
    private static final Field SHADER_GROUP_PASSES = ObfuscationReflectionHelper.findField(ShaderGroup.class, "field_148031_d");
    public static List<Shader> getShaderGroupPasses(ShaderGroup shaderGroup) {
        return ReflectionUtil.getFieldValue(SHADER_GROUP_PASSES, shaderGroup);
    }
    
    private static final Field GAME_RENDERER_POST_EFFECT = ObfuscationReflectionHelper.findField(GameRenderer.class, "field_147707_d");
    public static void setPostEffect(GameRenderer gameRenderer, ShaderGroup postEffect) {
        ReflectionUtil.setFieldValue(GAME_RENDERER_POST_EFFECT, gameRenderer, postEffect);
    }
    
    private static final Field GAME_RENDERER_EFFECT_ACTIVE = ObfuscationReflectionHelper.findField(GameRenderer.class, "field_175083_ad");
    public static void setEffectActive(GameRenderer gameRenderer, boolean effectActive) {
        ReflectionUtil.setBooleanFieldValue(GAME_RENDERER_EFFECT_ACTIVE, gameRenderer, effectActive);
    }
    
    private static final Field GAME_RENDERER_EFFECT_INDEX = ObfuscationReflectionHelper.findField(GameRenderer.class, "field_147713_ae");
    public static void setEffectIndex(GameRenderer gameRenderer, int effectIndex) {
        ReflectionUtil.setIntFieldValue(GAME_RENDERER_EFFECT_INDEX, gameRenderer, effectIndex);
    }
    
    
    private static final Field CLIENT_PLAYER_ENTITY_HANDS_BUSY = ObfuscationReflectionHelper.findField(ClientPlayerEntity.class, "field_184844_co");
    public static void setHandsBusy(ClientPlayerEntity player, boolean handsBusy) {
        ReflectionUtil.setBooleanFieldValue(CLIENT_PLAYER_ENTITY_HANDS_BUSY, player, handsBusy);
    }
}
