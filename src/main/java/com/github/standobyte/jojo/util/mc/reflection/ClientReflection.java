package com.github.standobyte.jojo.util.mc.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.matrix.MatrixStack;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISoundEventAccessor;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ClientReflection {
    static {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> new DistExecutor.SafeRunnable() {
            
            @Override
            public void run() {
                FIRST_PERSON_RENDERER_OFF_HAND_HEIGHT = ObfuscationReflectionHelper.findField(FirstPersonRenderer.class, "field_187471_h");
                FIRST_PERSON_RENDERER_O_OFF_HAND_HEIGHT = ObfuscationReflectionHelper.findField(FirstPersonRenderer.class, "field_187472_i");
                FIRST_PERSON_RENDERER_RENDER_PLAYER_ARM = ObfuscationReflectionHelper.findMethod(FirstPersonRenderer.class, "func_228401_a_", 
                        MatrixStack.class, IRenderTypeBuffer.class, int.class, float.class, float.class, HandSide.class);
                MINECRAFT_PAUSE = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71445_n");
                MINECRAFT_TIMER = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71428_T");
                MAIN_MENU_SCREEN_SPLASH = ObfuscationReflectionHelper.findField(MainMenuScreen.class, "field_73975_c");
                RENDER_TYPE_BUFFER_IMPL_BUILDER = ObfuscationReflectionHelper.findField(IRenderTypeBuffer.Impl.class, "field_228457_a_");
                RENDER_TYPE_BUFFER_IMPL_FIXED_BUFFERS = ObfuscationReflectionHelper.findField(IRenderTypeBuffer.Impl.class, "field_228458_b_");
                MODEL_RENDERER_CUBES = ObfuscationReflectionHelper.findField(ModelRenderer.class, "field_78804_l");
                MODEL_BOX_POLYGONS = ObfuscationReflectionHelper.findField(ModelRenderer.ModelBox.class, "field_78254_i");
                SOUND_EVENT_ACCESSOR_LIST = ObfuscationReflectionHelper.findField(SoundEventAccessor.class, "field_188716_a");
                INGAME_MENU_SCREEN_SHOW_PAUSE_MENU = ObfuscationReflectionHelper.findField(IngameMenuScreen.class, "field_222813_a");
                PARTICLE_MANAGER_SPRITE_SETS = ObfuscationReflectionHelper.findField(ParticleManager.class, "field_215242_i");
                MODEL_BAKERY_UNREFERENCED_TEXTURES = ObfuscationReflectionHelper.findField(ModelBakery.class, "field_177602_b");
                TEXTURE_MANAGER_TICKABLE_TEXTURES = ObfuscationReflectionHelper.findField(TextureManager.class, "field_110583_b");
                MINECRAFT_PARTICLE_ENGINE = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71452_i");
                PARTICLE_MANAGER_TEXTURE_MANAGER = ObfuscationReflectionHelper.findField(ParticleManager.class, "field_78877_c");
                SHADER_GROUP_PASSES = ObfuscationReflectionHelper.findField(ShaderGroup.class, "field_148031_d");
                GAME_RENDERER_POST_EFFECT = ObfuscationReflectionHelper.findField(GameRenderer.class, "field_147707_d");
                GAME_RENDERER_EFFECT_ACTIVE = ObfuscationReflectionHelper.findField(GameRenderer.class, "field_175083_ad");
                GAME_RENDERER_EFFECT_INDEX = ObfuscationReflectionHelper.findField(GameRenderer.class, "field_147713_ae");
            }
        });
    }
    
    private static Field FIRST_PERSON_RENDERER_OFF_HAND_HEIGHT;
    public static float getOffHandHeight(FirstPersonRenderer renderer) {
        return ReflectionUtil.getFieldValue(FIRST_PERSON_RENDERER_OFF_HAND_HEIGHT, renderer);
    }

    private static Field FIRST_PERSON_RENDERER_O_OFF_HAND_HEIGHT;
    public static float getOffHandHeightPrev(FirstPersonRenderer renderer) {
        return ReflectionUtil.getFieldValue(FIRST_PERSON_RENDERER_O_OFF_HAND_HEIGHT, renderer);
    }

    private static Method FIRST_PERSON_RENDERER_RENDER_PLAYER_ARM;
    public static void renderPlayerArm(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, 
            float handHeight, float swingAnim, HandSide handSide, FirstPersonRenderer renderer) {
        ReflectionUtil.invokeMethod(FIRST_PERSON_RENDERER_RENDER_PLAYER_ARM, 
                renderer, matrixStack, buffer, packedLight, handHeight, swingAnim, handSide);
    }
    
    
    private static Field MINECRAFT_PAUSE;
    public static void pauseClient(Minecraft minecraft) {
        ReflectionUtil.setFieldValue(MINECRAFT_PAUSE, minecraft, true);
    }

    private static Field MINECRAFT_TIMER;
    public static Timer getTimer(Minecraft minecraft) {
        return ReflectionUtil.getFieldValue(MINECRAFT_TIMER, minecraft);
    }
    
    
    private static Field MAIN_MENU_SCREEN_SPLASH;
    public static void setSplash(MainMenuScreen screen, String splash) {
        ReflectionUtil.setFieldValue(MAIN_MENU_SCREEN_SPLASH, screen, splash);
    }
    
    
    private static Field RENDER_TYPE_BUFFER_IMPL_BUILDER;
    public static BufferBuilder getBuilder(IRenderTypeBuffer.Impl buffers) {
        return ReflectionUtil.getFieldValue(RENDER_TYPE_BUFFER_IMPL_BUILDER, buffers);
    }
    
    private static Field RENDER_TYPE_BUFFER_IMPL_FIXED_BUFFERS;
    public static Map<RenderType, BufferBuilder> getFixedBuffers(IRenderTypeBuffer.Impl buffers) {
        return ReflectionUtil.getFieldValue(RENDER_TYPE_BUFFER_IMPL_FIXED_BUFFERS, buffers);
    }
    
    
    private static Field MODEL_RENDERER_CUBES;
    public static void setCubes(ModelRenderer modelRenderer, ObjectList<ModelRenderer.ModelBox> cubes) {
        ReflectionUtil.setFieldValue(MODEL_RENDERER_CUBES, modelRenderer, cubes);
    }
    
    public static void addCube(ModelRenderer modelRenderer, ModelRenderer.ModelBox cube) {
        List<ModelRenderer.ModelBox> cubes = ReflectionUtil.getFieldValue(MODEL_RENDERER_CUBES, modelRenderer);
        cubes.add(cube);
    }
    
    
    private static Field MODEL_BOX_POLYGONS;
    @Deprecated
    public static ModelRenderer.TexturedQuad[] getPolygons(ModelRenderer.ModelBox modelBox) {
        return ReflectionUtil.getFieldValue(MODEL_BOX_POLYGONS, modelBox);
    }
    
    public static void setPolygons(ModelRenderer.ModelBox modelBox, ModelRenderer.TexturedQuad[] polygons) {
        ReflectionUtil.setFieldValue(MODEL_BOX_POLYGONS, modelBox, polygons);
    }
    
    
    private static Field SOUND_EVENT_ACCESSOR_LIST;
    public static List<ISoundEventAccessor<Sound>> getSubAccessorsList(SoundEventAccessor accessor) {
        return ReflectionUtil.getFieldValue(SOUND_EVENT_ACCESSOR_LIST, accessor);
    }
    
    
    private static Field INGAME_MENU_SCREEN_SHOW_PAUSE_MENU;
    public static boolean showsPauseMenu(IngameMenuScreen screen) {
        return ReflectionUtil.getFieldValue(INGAME_MENU_SCREEN_SHOW_PAUSE_MENU, screen);
    }
    
    
    private static Field PARTICLE_MANAGER_SPRITE_SETS;
    public static Map<ResourceLocation, ? extends IAnimatedSprite> getSpriteSets(ParticleManager particleManager) {
        return ReflectionUtil.getFieldValue(PARTICLE_MANAGER_SPRITE_SETS, particleManager);
    }
    
    
    private static Field MODEL_BAKERY_UNREFERENCED_TEXTURES;
    public static Set<RenderMaterial> getModelBakeryUnreferencedTextures() {
        return ReflectionUtil.getFieldValue(MODEL_BAKERY_UNREFERENCED_TEXTURES, null);
    }

    
    private static Field TEXTURE_MANAGER_TICKABLE_TEXTURES;
    public static Set<ITickable> getTickableTextures(TextureManager textureManager) {
        return ReflectionUtil.getFieldValue(TEXTURE_MANAGER_TICKABLE_TEXTURES, textureManager);
    }
    
    public static void setTickableTextures(TextureManager textureManager, Set<ITickable> textures) {
        ReflectionUtil.setFieldValue(TEXTURE_MANAGER_TICKABLE_TEXTURES, textureManager, textures);
    }
    
    
    private static Field MINECRAFT_PARTICLE_ENGINE;
    public static void setParticleEngine(Minecraft mc, ParticleManager particleEngine) {
        ReflectionUtil.setFieldValue(MINECRAFT_PARTICLE_ENGINE, mc, particleEngine);
    }
    
    private static Field PARTICLE_MANAGER_TEXTURE_MANAGER;
    public static void setTextureManager(ParticleManager particleManager, TextureManager textureManager) {
        ReflectionUtil.setFieldValue(PARTICLE_MANAGER_TEXTURE_MANAGER, particleManager, textureManager);
    }
    
    
    private static Field SHADER_GROUP_PASSES;
    public static List<Shader> getShaderGroupPasses(ShaderGroup shaderGroup) {
        return ReflectionUtil.getFieldValue(SHADER_GROUP_PASSES, shaderGroup);
    }
    
    private static Field GAME_RENDERER_POST_EFFECT;
    public static void setPostEffect(GameRenderer gameRenderer, ShaderGroup postEffect) {
        ReflectionUtil.setFieldValue(GAME_RENDERER_POST_EFFECT, gameRenderer, postEffect);
    }
    
    private static Field GAME_RENDERER_EFFECT_ACTIVE;
    public static void setEffectActive(GameRenderer gameRenderer, boolean effectActive) {
        ReflectionUtil.setFieldValue(GAME_RENDERER_EFFECT_ACTIVE, gameRenderer, effectActive);
    }
    
    private static Field GAME_RENDERER_EFFECT_INDEX;
    public static void setEffectIndex(GameRenderer gameRenderer, int effectIndex) {
        ReflectionUtil.setFieldValue(GAME_RENDERER_EFFECT_INDEX, gameRenderer, effectIndex);
    }
}
