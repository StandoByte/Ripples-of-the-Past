package com.github.standobyte.jojo.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.HandSide;
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
}
