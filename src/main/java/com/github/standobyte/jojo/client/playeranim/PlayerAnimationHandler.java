package com.github.standobyte.jojo.client.playeranim;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.living.LivingWallClimbing;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.modcompat.OptionalDependencyHelper;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class PlayerAnimationHandler {

    private static IPlayerAnimator instance = null;
    
    public static interface IPlayerAnimator {
        
        boolean kosmXAnimatorInstalled();
        
        public void onRenderFrameStart(float partialTick);
        
        public void onRenderFrameEnd(float partialTick);
        
        BasicToggleAnim registerBasicAnimLayer(String classNameWithKosmXMod, ResourceLocation id, int priority);
        
        <I> I registerAnimLayer(String classNameWithKosmXMod, ResourceLocation id, int priority, Supplier<? extends I> fallbackEmptyConstructor);
        
        <T> T getAnimLayer(Class<T> layerInterface, ResourceLocation id);
        
        <T extends LivingEntity, M extends BipedModel<T>> void onArmorLayerInit(LayerRenderer<T, M> layer);
        
        float[] getBend(BipedModel<?> model, BendablePart part);
        
        void setBend(BipedModel<?> model, BendablePart part, float axis, float angle);
        
        Vector3d getBodyPos(AbstractClientPlayerEntity player, float partialTick);
        
        <T extends LivingEntity, M extends BipedModel<T>> void heldItemLayerRender(LivingEntity livingEntity, MatrixStack matrices, HandSide arm);
        
        <T extends LivingEntity, M extends BipedModel<T>> void heldItemLayerChangeItemLocation(LivingEntity livingEntity, MatrixStack matrices, HandSide arm);
        
        void setupLayerFirstPersonRender(BipedModel<?> layerModel);
        
        void onItemLikeLayerRender(MatrixStack matrixStack, LivingEntity entity, HandSide side);
        
        @Deprecated
        default void setBarrageAnim(PlayerEntity player, boolean val) {
            ModPlayerAnimations.playerBarrageAnim.setAnimEnabled(player, val);
        }
    }
    
    
    public static boolean canAnimate(PlayerEntity player) {
        return !player.isPassenger() && !LivingWallClimbing.getHandler(player).map(wallClimb -> wallClimb.isWallClimbing()).orElse(false);
    }
    
    
    public static class PlayerAnimator implements IPlayerAnimator {
        protected Map<ResourceLocation, Object> animationLayers = new HashMap<>();
        
        @Override
        public boolean kosmXAnimatorInstalled() { return false; }
        
        @Override
        public void onRenderFrameStart(float partialTick) {}
        
        @Override
        public void onRenderFrameEnd(float partialTick) {}

        @Override
        public BasicToggleAnim registerBasicAnimLayer(String classNameWithKosmXMod, ResourceLocation id, int priority) {
            return registerAnimLayer(classNameWithKosmXMod, id, priority, () -> BasicToggleAnim.NoPlayerAnimator.DUMMY);
        }

        @Override
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
                    JojoMod.getLogger().error("Failed to create player animation layer of class " + classNameWithKosmXMod, e);
                }
            }
            if (animationHandler == null) {
                animationHandler = fallbackEmptyConstructor.get();
            }
            
            animationLayers.put(id, animationHandler);
            return animationHandler;
        }
        
        protected void registerWithAnimatorMod(Object animLayer, ResourceLocation id, int priority) {}

        @Override
        public <T> T getAnimLayer(Class<T> layerInterface, ResourceLocation id) {
            Object layer = animationLayers.get(id);
            if (layer == null) {
                JojoMod.getLogger().error("An animation layer with id {} was not registered!", id);
                throw new IllegalArgumentException();
            }
            return (T) layer;
        }
        

        @Override
        public <T extends LivingEntity, M extends BipedModel<T>> void onArmorLayerInit(LayerRenderer<T, M> layer) {}
        
        
        static final float[] ZERO_BEND = new float[] {0, 0};
        @Override
        public float[] getBend(BipedModel<?> model, BendablePart part) { return ZERO_BEND; }
        
        @Override
        public void setBend(BipedModel<?> model, BendablePart part, float axis, float angle) {}
        
        @Override
        public Vector3d getBodyPos(AbstractClientPlayerEntity player, float partialTick) {
            return Vector3d.ZERO;
        }
        
        @Override
        public <T extends LivingEntity, M extends BipedModel<T>> void heldItemLayerRender(
                LivingEntity livingEntity, MatrixStack matrices, HandSide arm) {}

        @Override
        public <T extends LivingEntity, M extends BipedModel<T>> void heldItemLayerChangeItemLocation(
                LivingEntity livingEntity, MatrixStack matrices, HandSide arm) {}

        @Override
        public void setupLayerFirstPersonRender(BipedModel<?> layerModel) {}
        
        @Override
        public void onItemLikeLayerRender(MatrixStack matrixStack, LivingEntity entity, HandSide side) {}
    }
    
    public static enum BendablePart {
        TORSO,
        LEFT_ARM,
        RIGHT_ARM,
        LEFT_LEG,
        RIGHT_LEG
    }
    
    public static IPlayerAnimator getPlayerAnimator() {
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
