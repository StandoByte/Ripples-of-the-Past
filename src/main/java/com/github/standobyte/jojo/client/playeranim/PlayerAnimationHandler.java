package com.github.standobyte.jojo.client.playeranim;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.modcompat.OptionalDependencyHelper;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class PlayerAnimationHandler {

    private static PlayerAnimator instance = null;
    
    public static class PlayerAnimator {
        @Deprecated
        public void onVehicleMount(AbstractClientPlayerEntity player, @Nullable EntityType<?> vehicleType) {
            boolean isGlider = vehicleType == ModEntityTypes.LEAVES_GLIDER.get();
            JojoMod.LOGGER.error("Missing glider riding animation");
        }
        
        protected Map<ResourceLocation, Object> animationLayers = new HashMap<>();
        
        public boolean kosmXAnimatorInstalled() { return false; }
        
        public BasicToggleAnim registerBasicAnimLayer(String classNameWithKosmXMod, ResourceLocation id, int priority) {
            return registerAnimLayer(classNameWithKosmXMod, id, priority, BasicToggleAnim.NoPlayerAnimator::new);
        }
        
        public <I> I registerAnimLayer(String classNameWithKosmXMod, ResourceLocation id, int priority, 
                Supplier<? extends I> fallbackEmptyConstructor) {
            if (animationLayers.containsKey(id)) {
                JojoMod.getLogger().error("An animation layer with id {} is already present!", id);
                IllegalArgumentException e = new IllegalArgumentException();
                e.printStackTrace();
                throw e;
            }
            
            I animationHandler = null;
            if (kosmXAnimatorInstalled()) {
                I instance;
                try {
                    Class<? extends I> animatorClass = (Class<? extends I>) Class.forName(classNameWithKosmXMod);
                    Constructor<? extends I> constructor = animatorClass.getConstructor(ResourceLocation.class);
                    instance = constructor.newInstance(id);
                    animationHandler = instance;
                    registerWithAnimatorMod(animationHandler, id, priority);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | ClassNotFoundException | NoSuchMethodException | SecurityException e) {
                    JojoMod.getLogger().error("Failed to create player animation layer of class {}", classNameWithKosmXMod);
                    e.printStackTrace();
                }
            }
            if (animationHandler == null) {
                animationHandler = fallbackEmptyConstructor.get();
            }
            
            animationLayers.put(id, animationHandler);
            return animationHandler;
        }
        
        protected void registerWithAnimatorMod(Object animLayer, ResourceLocation id, int priority) {}
        
        public <T> T getAnimLayer(Class<T> layerInterface, ResourceLocation id) {
            Object layer = animationLayers.get(id);
            if (layer == null) {
                JojoMod.getLogger().error("An animation layer with id {} was not registered!", id);
                throw new IllegalArgumentException();
            }
            return (T) layer;
        }
        
        
        public <T extends LivingEntity, M extends BipedModel<T>> void onArmorLayerInit(LayerRenderer<T, M> layer) {}
        
        
        static final float[] ZERO_BEND = new float[] {0, 0};
        public float[] getBend(BipedModel<?> model, BendablePart part) { return ZERO_BEND; }
        
        public <T extends LivingEntity, M extends BipedModel<T>> void heldItemLayerRender(
                LivingEntity livingEntity, MatrixStack matrices, HandSide arm) {}
        
        public <T extends LivingEntity, M extends BipedModel<T>> void heldItemLayerChangeItemLocation(
                LivingEntity livingEntity, MatrixStack matrices, HandSide arm) {}
        
        public void setupLayerFirstPersonRender(PlayerModel<?> layerModel) {}
    }
    
    public static enum BendablePart {
        TORSO,
        LEFT_ARM,
        RIGHT_ARM,
        LEFT_LEG,
        RIGHT_LEG
    }
    
    public static PlayerAnimator getPlayerAnimator() {
        return instance;
    }
    
    
    
    public static void initAnimator() {
        if (instance != null) {
            JojoMod.getLogger().error("Player animation interface is already initialized!");
            Exception e = new RedundantAddonCodeException();
            e.printStackTrace();
            return;
        }
        instance = OptionalDependencyHelper.initModHandlingInterface(
                "playeranimator", 
                "com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled", 
                PlayerAnimator::new, "Player Animator lib");
    }
    
    private static class RedundantAddonCodeException extends Exception {}
}
