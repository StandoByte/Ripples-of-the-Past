package com.github.standobyte.jojo.client.playeranim.playeranimator;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.playeranim.IPlayerBarrageAnimation;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.playeranimator.anim.EntityAnimApplier;
import com.github.standobyte.jojo.client.playeranim.playeranimator.anim.HeldActionAnimLayer;
import com.github.standobyte.jojo.client.playeranim.playeranimator.anim.TestAnimLayer;
import com.github.standobyte.jojo.client.playeranim.playeranimator.anim.hamon.BarrageAnimLayer;
import com.github.standobyte.jojo.client.playeranim.playeranimator.anim.hamon.MeditationPoseLayer;
import com.github.standobyte.jojo.client.playeranim.playeranimator.anim.hamon.PlayerBarrageAfterimagesAnim;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.BarrageFistAfterimagesLayer;
import com.github.standobyte.jojo.client.render.entity.model.mob.HamonMasterModel;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.SetableSupplier;
import dev.kosmx.playerAnim.impl.Helper;
import dev.kosmx.playerAnim.impl.IBendHelper;
import dev.kosmx.playerAnim.impl.IMutableModel;
import dev.kosmx.playerAnim.impl.IUpperPartHelper;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class PlayerAnimator implements PlayerAnimationHandler.IPlayerAnimator {
    private HeldActionAnimLayer heldAction;
    private BarrageAnimLayer barrage;
    private MeditationPoseLayer meditation;
    
    @Override
    public void initAnims() {
        heldAction = register(new ResourceLocation(JojoMod.MOD_ID, "hamon_breath"), 1, HeldActionAnimLayer::new);
        barrage = register(new ResourceLocation(JojoMod.MOD_ID, "barrage"), 1, BarrageAnimLayer::new);
        meditation = register(new ResourceLocation(JojoMod.MOD_ID, "meditation"), 1, MeditationPoseLayer::new);
        register(new ResourceLocation(JojoMod.MOD_ID, "test"), 1, TestAnimLayer::new);
    }
    
    
    
    @Override
    public boolean actionStartedHolding(PlayerEntity player, Action<?> action) {
        if (action == ModHamonActions.JONATHAN_OVERDRIVE_BARRAGE.get()) {
            setBarrageAnim((AbstractClientPlayerEntity) player, true);
            return true;
        }
        else {
            return heldAction.setActionAnim(player, action);
        }
    }
    
    @Override
    public void actionStoppedHolding(PlayerEntity player, Action<?> action) {
        if (action == ModHamonActions.JONATHAN_OVERDRIVE_BARRAGE.get()) {
            setBarrageAnim((AbstractClientPlayerEntity) player, false);
        }
        else {
            heldAction.setActionAnim(player, null);
        }
    }

    private static final ResourceLocation SYO_BARRAGE_FINISHER = new ResourceLocation(JojoMod.MOD_ID, "syo_barrage_finisher");
    @Override
    public void syoBarrageFinisherAnim(PlayerEntity player) {
        heldAction.setAnimFromName((AbstractClientPlayerEntity) player, SYO_BARRAGE_FINISHER);
    }
    
    @Override
    public void onMeditationSet(PlayerEntity player, boolean isMeditating) {
        meditation.setAnimEnabled((AbstractClientPlayerEntity) player, isMeditating);
    }
    
    @Override
    public IEntityAnimApplier<HamonMasterEntity, HamonMasterModel> initHamonMasterPose(HamonMasterModel model) {
        if (model instanceof IMutableModel) {
            IMutableModel modelWithMixin = ((IMutableModel) model);
            SetableSupplier<AnimationProcessor> animProcessor = modelWithMixin.getEmoteSupplier();
            IAnimation sittingAnim = meditation.getHamonMasterAnim();
            if (sittingAnim != null) {
                AnimationProcessor anim = new AnimationProcessor(sittingAnim);
                animProcessor.set(anim);
                return new EntityAnimApplier<HamonMasterEntity, HamonMasterModel>(model, modelWithMixin);
            }
        }
        return PlayerAnimationHandler.IPlayerAnimator.super.initHamonMasterPose(model);
    }
    
    
    
    @Override
    public float[] getBend(BipedModel<?> model, BendablePart part) {
        if (Helper.isBendEnabled() && model instanceof IMutableModel) {
            IBendHelper mutablePart = getMutablePart((IMutableModel) model, part);
            if (mutablePart != null) {
                return BendyLibHelper.getBend(mutablePart);
            }
        }
        return PlayerAnimationHandler.IPlayerAnimator.super.getBend(model, part);
    }
    
    private IBendHelper getMutablePart(IMutableModel model, BendablePart neededPart) {
        switch (neededPart) {
        case TORSO:
            return model.getTorso();
        case LEFT_ARM:
            return model.getLeftArm();
        case RIGHT_ARM:
            return model.getRightArm();
        case LEFT_LEG:
            return model.getLeftLeg();
        case RIGHT_LEG:
            return model.getRightLeg();
        }
        return null;
    }

    @Override
    public void setBarrageAnim(PlayerEntity player, boolean val) {
        barrage.setAnimEnabled((AbstractClientPlayerEntity) player, val);
    }
    
    @Override
    public IPlayerBarrageAnimation createBarrageAfterimagesAnim(
            PlayerModel<AbstractClientPlayerEntity> model, BarrageFistAfterimagesLayer layer) {
        return new PlayerBarrageAfterimagesAnim(model, barrage.createSwingAnim(model), layer);
    }
    
    
    
    @Override
    public <T extends LivingEntity, M extends BipedModel<T>> void onPlayerLayerInit(LayerRenderer<T, M> layer) {
        ((IUpperPartHelper) layer).setUpperPart(false);
    }
    
    
    
    public static abstract class AnimHandler<T extends IAnimation> {
        private final ResourceLocation id;
        
        public AnimHandler(ResourceLocation id) {
            this.id = id;
        }
        
        protected abstract T createAnimLayer(AbstractClientPlayerEntity player);
        
        public boolean isForgeEventHandler() {
            return false;
        }
        
        protected void preventCrouch(AbstractClientPlayerEntity player, PlayerRenderer renderer) {
            T animLayer = getAnimLayer(player);
            if (animLayer != null && animLayer.isActive()) {
                renderer.getModel().crouching = false;
            }
        }
        
        @Nullable
        protected final T getAnimLayer(AbstractClientPlayerEntity player) {
            return (T) PlayerAnimationAccess.getPlayerAssociatedData(player).get(id);
        }
    }
    
    public static abstract class AnimLayerHandler extends AnimHandler<ModifierLayer<IAnimation>> {

        public AnimLayerHandler(ResourceLocation id) {
            super(id);
        }
        
        protected boolean setAnimFromName(AbstractClientPlayerEntity player, ResourceLocation name) {
            return setAnimFromName(player, name, KeyframeAnimationPlayer::new);
        }
        
        protected boolean setAnimFromName(AbstractClientPlayerEntity player, ResourceLocation name, Function<KeyframeAnimation, IAnimation> createAnimPlayer) {
            if (name == null) return false;
            KeyframeAnimation keyframes = PlayerAnimationRegistry.getAnimation(name);
            if (keyframes == null) return false;
            return setAnim(player, createAnimPlayer.apply(keyframes));
        }
        
        protected boolean setAnim(AbstractClientPlayerEntity player, IAnimation anim) {
            if (player == null) return false;
            ModifierLayer<IAnimation> animLayer = getAnimLayer((AbstractClientPlayerEntity) player);
            if (animLayer == null) return false;
            animLayer.setAnimation(anim);
            return true;
        }
        
        protected boolean fadeOutAnim(AbstractClientPlayerEntity player, @Nullable AbstractFadeModifier fadeModifier, @Nullable IAnimation newAnimation) {
            if (player == null) return false;
            ModifierLayer<IAnimation> animLayer = getAnimLayer((AbstractClientPlayerEntity) player);
            if (animLayer != null) {
                if (fadeModifier != null) {
                    animLayer.replaceAnimationWithFade(fadeModifier, newAnimation);
                }
                else {
                    animLayer.setAnimation(newAnimation);
                }
                return true;
            }
            return false;
        }
        
    }
    
    private static <A extends IAnimation, T extends AnimHandler<A>> T register(ResourceLocation id, int priority, Function<ResourceLocation, T> handler) {
        T animHandler = handler.apply(id);
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(id, priority, player -> animHandler.createAnimLayer(player));
        if (animHandler.isForgeEventHandler()) {
            MinecraftForge.EVENT_BUS.register(animHandler);
        }
        return animHandler;
    }
}
