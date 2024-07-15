package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.WallClimbAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXHeadRotationModifier;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.EnergyRippleLayer;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.util.general.MathUtil;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KosmXWallClimbLayer extends AnimLayerHandler implements WallClimbAnim {
    // FIXME the AnimLayerHandler object is effectively a singleton, 
    //       while ModifierLayer objects are created for every player (so modifiers are too)
    KosmXWallClimbAnimPlayer animStuff;
    SpeedModifier speedModifier;
    
    public KosmXWallClimbLayer(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        ModifierLayer<IAnimation> anim = new ModifierLayer<>(null);
        anim.addModifierLast(new KosmXHeadRotationModifier());
        anim.addModifierLast(getOrCreateSpeedModifier(player));
        return anim;
    }
    
    
    private static final ResourceLocation CLIMB_UP = new ResourceLocation("jojo", "wall_climb_up");
    private static final ResourceLocation CLIMB_DOWN = new ResourceLocation("jojo", "wall_climb_down");
    private static final ResourceLocation CLIMB_LEFT = new ResourceLocation("jojo", "wall_climb_left");
    private static final ResourceLocation CLIMB_RIGHT = new ResourceLocation("jojo", "wall_climb_right");
    @Override
    public boolean setAnimEnabled(PlayerEntity player, boolean enabled) {
        if (enabled) {
            KeyframeAnimation up = PlayerAnimationRegistry.getAnimation(CLIMB_UP);
            KeyframeAnimation down = PlayerAnimationRegistry.getAnimation(CLIMB_DOWN);
            KeyframeAnimation left = PlayerAnimationRegistry.getAnimation(CLIMB_LEFT);
            KeyframeAnimation right = PlayerAnimationRegistry.getAnimation(CLIMB_RIGHT);
            if (up == null || down == null || left == null || right == null) return false;
            
            KosmXWallClimbAnimPlayer keyframePlayer = new KosmXWallClimbAnimPlayer(up, down, left, right);
            animStuff = keyframePlayer;
            ModifierLayer<?> modifierLayer = getAnimLayer((AbstractClientPlayerEntity) player);
            animStuff.onInit(player, modifierLayer, getOrCreateSpeedModifier(player));
            return setAnim(player, keyframePlayer);
        }
        else {
            animStuff = null;
            return setAnim(player, null);
        }
    }

    @Override
    public void tickAnimProperties(PlayerEntity player, boolean isMoving, 
            double movementUp, double movementLeft, float speed) {
        getWallClimbAnimPlayer(player).tickProperties(isMoving, movementUp, movementLeft, speed);
    }
    
    @Nullable
    private KosmXWallClimbAnimPlayer getWallClimbAnimPlayer(PlayerEntity player) {
        return animStuff;
    }
    
    private SpeedModifier getOrCreateSpeedModifier(PlayerEntity player) {
        if (speedModifier == null) {
            speedModifier = new SpeedModifier(1);
        }
        return speedModifier;
    }
    
    
    @Override
    public boolean isForgeEventHandler() {
        return true;
    }
    
    @SubscribeEvent
    public void onEntityRender(RenderPlayerEvent.Post event) {
        PlayerEntity player = event.getPlayer();
        KosmXWallClimbAnimPlayer animStuff = getWallClimbAnimPlayer(player);
        if (animStuff != null && animStuff.isActive()) {
            animStuff.onRender();
            
            HandSide handTouch = animStuff.handTouchFrame();
            if (handTouch != null) {
                Vector3d particlesOffset = EnergyRippleLayer.handTipPos(event.getRenderer().getModel(), handTouch, Vector3d.ZERO, player.yBodyRot);
                Vector3d particlesPos = player.position().add(particlesOffset);
                HamonUtil.emitHamonSparkParticles(player.level, ClientUtil.getClientPlayer(), 
                        particlesPos.x, particlesPos.y, particlesPos.z, 0.25f, 0.125f);
            }
        }
    }
    
    @SubscribeEvent
    public void onRenderFirstPerson(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType().isFirstPerson()) {
            PlayerEntity player = mc.player;
            KosmXWallClimbAnimPlayer animStuff = getWallClimbAnimPlayer(player);
            if (animStuff != null && animStuff.isActive()) {
                animStuff.onRender();
                
                HandSide handTouch = animStuff.handTouchFrame();
                if (handTouch != null) {
                    ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
                    Vector3d particlesOffset = new Vector3d(handTouch == HandSide.LEFT ? 0.25 : -0.25, 0, 0.25)
                            .yRot((180 + mc.player.yBodyRot) * MathUtil.DEG_TO_RAD);
                    Vector3d particlesPos = camera.getPosition().add(particlesOffset);
                    HamonUtil.emitHamonSparkParticles(player.level, ClientUtil.getClientPlayer(), 
                            particlesPos.x, particlesPos.y, particlesPos.z, 0.25f, 0.125f);
                }
            }
        }
    }
    
}
