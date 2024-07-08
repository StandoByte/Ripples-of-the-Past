package com.github.standobyte.jojo.client.playeranim.kosmx;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler.BendablePart;
import com.mojang.blaze3d.matrix.MatrixStack;

import dev.kosmx.playerAnim.api.AnimUtils;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.Helper;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.IBendHelper;
import dev.kosmx.playerAnim.impl.IMutableModel;
import dev.kosmx.playerAnim.impl.IPlayerModel;
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
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KosmXPlayerAnimatorInstalled extends PlayerAnimationHandler.PlayerAnimator {
    private static final List<AnimHandler<? extends IAnimation>> PREVENT_CROUCH = new ArrayList<>();
    
    public KosmXPlayerAnimatorInstalled() {
        super();
        MinecraftForge.EVENT_BUS.register(new KosmXPlayerAnimatorInstalled.EventHandler());
    }
    
    @Override
    public boolean kosmXAnimatorInstalled() {
        return true;
    }
    
    @Override
    protected void registerWithAnimatorMod(Object animLayer, ResourceLocation id, int priority) {
        register((AnimHandler<?>) animLayer, id, priority);
    }
    
    private static <A extends IAnimation, T extends AnimHandler<A>> void register(T animLayer, ResourceLocation id, int priority) {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(id, priority, player -> animLayer.createAnimLayer(player));
        if (animLayer.isForgeEventHandler()) {
            MinecraftForge.EVENT_BUS.register(animLayer);
        }
        if (animLayer.preventsCrouch()) {
            PREVENT_CROUCH.add(animLayer);
        }
    }
    
    
    public static class EventHandler {
        
        @SubscribeEvent
        public void preRender(RenderPlayerEvent.Pre event) {
            if (event.getRenderer().getModel().crouching) {
                AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) event.getPlayer();
                for (AnimHandler<?> animHandler : PREVENT_CROUCH) {
                    if (animHandler.preventCrouch(player, event.getRenderer())) {
                        break;
                    }
                }
            }
        }
    }
    
    
    @Override
    public float[] getBend(BipedModel<?> model, BendablePart part) {
        if (Helper.isBendEnabled() && model instanceof IMutableModel) {
            IBendHelper mutablePart = getMutablePart((IMutableModel) model, part);
            if (mutablePart != null) {
                return KosmXBendyLibHelper.getBend(mutablePart);
            }
        }
        return super.getBend(model, part);
    }
    
    @Override
    public void setBend(BipedModel<?> model, BendablePart part, float axis, float angle) {
        if (Helper.isBendEnabled() && model instanceof IMutableModel) {
            IBendHelper mutablePart = getMutablePart((IMutableModel) model, part);
            if (mutablePart != null) {
                mutablePart.bend(axis, angle);
            }
        }
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
    public <T extends LivingEntity, M extends BipedModel<T>> void onArmorLayerInit(LayerRenderer<T, M> layer) {
        ((IUpperPartHelper) layer).setUpperPart(false);
    }
    
    
    @Override
    public <T extends LivingEntity, M extends BipedModel<T>> void heldItemLayerRender(
            LivingEntity livingEntity, MatrixStack matrices, HandSide arm) {
        if(Helper.isBendEnabled() && livingEntity instanceof IAnimatedPlayer){
            IAnimatedPlayer player = (IAnimatedPlayer) livingEntity;
            if(player.playerAnimator_getAnimation().isActive()){
                AnimationProcessor anim = player.playerAnimator_getAnimation();

                Vec3f data = anim.get3DTransform(arm == HandSide.LEFT ? "leftArm" : "rightArm", TransformType.BEND, new Vec3f(0f, 0f, 0f));

                Pair<Float, Float> pair = new Pair<>(data.getX(), data.getY());

                float offset = 0.25f;
                matrices.translate(0, offset, 0);
                float bend = pair.getRight();
                float axisf = - pair.getLeft();
                Vector3f axis = new Vector3f((float) Math.cos(axisf), 0, (float) Math.sin(axisf));
                //return this.setRotation(axis.getRadialQuaternion(bend));
                matrices.mulPose(axis.rotation(bend));
                matrices.translate(0, - offset, 0);

            }
        }
    }
    
    @Override
    public <T extends LivingEntity, M extends BipedModel<T>> void heldItemLayerChangeItemLocation(
            LivingEntity livingEntity, MatrixStack matrices, HandSide arm) {
        if (livingEntity instanceof IAnimatedPlayer) {
            IAnimatedPlayer player = (IAnimatedPlayer) livingEntity;
            if (player.playerAnimator_getAnimation().isActive()) {
                AnimationProcessor anim = player.playerAnimator_getAnimation();

                Vec3f rot = anim.get3DTransform(arm == HandSide.LEFT ? "leftItem" : "rightItem", TransformType.ROTATION, Vec3f.ZERO);
                Vec3f pos = anim.get3DTransform(arm == HandSide.LEFT ? "leftItem" : "rightItem", TransformType.POSITION, Vec3f.ZERO).scale(1/16f);

                matrices.translate(pos.getX(), pos.getY(), pos.getZ());

                matrices.mulPose(Vector3f.ZP.rotation(rot.getZ()));    //roll
                matrices.mulPose(Vector3f.YP.rotation(rot.getY()));    //pitch
                matrices.mulPose(Vector3f.XP.rotation(rot.getX()));    //yaw
            }
        }
    }
    
    @Override
    public void setupLayerFirstPersonRender(PlayerModel<?> layerModel) {
        if (layerModel instanceof IPlayerModel && AnimUtils.disableFirstPersonAnim) {
            ((IPlayerModel) layerModel).playerAnimator_prepForFirstPersonRender();
        }
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
        
        public boolean preventsCrouch() {
            return true;
        }
        
        public ResourceLocation getId() {
            return id;
        }
        
        protected boolean preventCrouch(AbstractClientPlayerEntity player, PlayerRenderer renderer) {
            T animLayer = getAnimLayer(player);
            if (animLayer != null && animLayer.isActive()) {
                renderer.getModel().crouching = false;
                return true;
            }
            return false;
        }
        
        @Nullable
        protected final T getAnimLayer(AbstractClientPlayerEntity player) {
            PlayerAnimationAccess.getPlayerAnimLayer(player);
            return (T) PlayerAnimationAccess.getPlayerAssociatedData(player).get(id);
        }
    }
    
    public static abstract class AnimLayerHandler extends AnimHandler<ModifierLayer<IAnimation>> {

        public AnimLayerHandler(ResourceLocation id) {
            super(id);
        }
        
        protected boolean setAnimFromName(PlayerEntity player, ResourceLocation name) {
            return setAnimFromName(player, name, KeyframeAnimationPlayer::new);
        }
        
        protected boolean setAnimFromName(PlayerEntity player, ResourceLocation name, Function<KeyframeAnimation, IAnimation> createAnimPlayer) {
            if (name == null) return false;
            KeyframeAnimation keyframes = PlayerAnimationRegistry.getAnimation(name);
            if (keyframes == null) return false;
            return setAnim(player, createAnimPlayer.apply(keyframes));
        }
        
        protected boolean setAnim(PlayerEntity player, IAnimation anim) {
            if (player == null) return false;
            ModifierLayer<IAnimation> animLayer = getAnimLayer((AbstractClientPlayerEntity) player);
            if (animLayer == null) return false;
            animLayer.setAnimation(anim);
            return true;
        }
        
        protected boolean fadeOutAnim(PlayerEntity player, @Nullable AbstractFadeModifier fadeModifier, @Nullable IAnimation newAnimation) {
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
}
