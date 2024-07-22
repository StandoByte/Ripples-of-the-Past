package com.github.standobyte.jojo.client.playeranim;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.BarrageFistAfterimagesLayer;
import com.github.standobyte.jojo.client.render.entity.model.mob.HamonMasterModel;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;
import com.github.standobyte.jojo.modcompat.OptionalDependencyHelper;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class PlayerAnimationHandler {

    private static IPlayerAnimator instance = null;
    
    public static interface IPlayerAnimator {
        default boolean kosmXAnimatorInstalled() { return false; }
        default void initAnims() {}
        
        default boolean actionStartedHolding(PlayerEntity player, Action<?> action) { return false; }
        default void actionStoppedHolding(PlayerEntity player, Action<?> action) {}
        default void syoBarrageFinisherAnim(PlayerEntity player) {}
        default void onMeditationSet(PlayerEntity player, boolean meditation) {}
        default IEntityAnimApplier<HamonMasterEntity, HamonMasterModel> initHamonMasterPose(HamonMasterModel model) { return IEntityAnimApplier.createDummy(); }
        
        static final float[] ZERO_BEND = new float[] {0, 0};
        default float[] getBend(BipedModel<?> model, BendablePart part) { return ZERO_BEND; }
        
        public static enum BendablePart {
            TORSO,
            LEFT_ARM,
            RIGHT_ARM,
            LEFT_LEG,
            RIGHT_LEG
        }
        
        @Nullable default void setBarrageAnim(PlayerEntity player, boolean val) { }
        @Nullable default IPlayerBarrageAnimation createBarrageAfterimagesAnim(
                PlayerModel<AbstractClientPlayerEntity> model, BarrageFistAfterimagesLayer layer) { return null; }
        
        default <T extends LivingEntity, M extends BipedModel<T>> void onArmorLayerInit(LayerRenderer<T, M> layer) {}
        default <T extends LivingEntity, M extends BipedModel<T>> void heldItemLayerRender(
                LivingEntity livingEntity, MatrixStack matrices, HandSide arm) {}
        default <T extends LivingEntity, M extends BipedModel<T>> void heldItemLayerChangeItemLocation(
                LivingEntity livingEntity, MatrixStack matrices, HandSide arm) {}
        default void setupLayerFirstPersonRender(PlayerModel<?> layerModel) {}
        
        default void onVehicleMount(AbstractClientPlayerEntity player, @Nullable EntityType<?> vehicleType) {}
    }
    
    public static IPlayerAnimator getPlayerAnimator() {
        return instance;
    }
    
    
    
    public static void initAnimator() {
        if (instance != null) {
            JojoMod.getLogger().error("Player animation interface is already initialized!");
            return;
        }
        instance = OptionalDependencyHelper.initModHandlingInterface(
                "playeranimator", 
                "com.github.standobyte.jojo.modcompat.mod.client.playeranimator.PlayerAnimatorInstalled", 
                EmptyPlayerAnimator::new, "Player Animator lib");
        instance.initAnims();
    }
}
