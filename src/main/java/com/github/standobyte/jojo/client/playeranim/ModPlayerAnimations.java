package com.github.standobyte.jojo.client.playeranim;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.client.playeranim.interfaces.HamonMeditationPoseAnim;
import com.github.standobyte.jojo.client.playeranim.interfaces.HamonSYOBAnim;
import com.github.standobyte.jojo.client.playeranim.interfaces.PlayerBarrageAnim;
import com.github.standobyte.jojo.client.playeranim.interfaces.WallClimbAnim;

import net.minecraft.util.ResourceLocation;

public class ModPlayerAnimations {
    public static HamonMeditationPoseAnim meditationPoseAnim;
    public static PlayerBarrageAnim playerBarrageAnim;
    public static BasicToggleAnim hamonBreath;
    public static WallClimbAnim wallClimbing;
    public static HamonSYOBAnim syoBarrage;
    
    public static void init() {
        meditationPoseAnim = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.kosmx.anim.layers.hamon.KosmXMeditationPoseLayer",
                new ResourceLocation(JojoMod.MOD_ID, "meditation"), 1, 
                HamonMeditationPoseAnim.NoPlayerAnimator::new);
        
        playerBarrageAnim = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.kosmx.anim.layers.barrage.KosmXBarrageAnimLayer",
                new ResourceLocation(JojoMod.MOD_ID, "barrage"), 1, 
                PlayerBarrageAnim.NoPlayerAnimator::new);
        
        hamonBreath = PlayerAnimationHandler.getPlayerAnimator().registerBasicAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.kosmx.anim.layers.hamon.KosmXHamonBreathLayer",
                new ResourceLocation(JojoMod.MOD_ID, "hamon_breath"), 1);
        
        syoBarrage = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.kosmx.anim.layers.hamon.KosmXSYOBLayer",
                new ResourceLocation(JojoMod.MOD_ID, "syo_barrage"), 1, 
                HamonSYOBAnim.NoPlayerAnimator::new);
        
        wallClimbing = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.kosmx.anim.layers.hamon.KosmXWallClimbLayer",
                new ResourceLocation(JojoMod.MOD_ID, "wall_climb"), 1, 
                WallClimbAnim.NoPlayerAnimator::new);
        
        PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.kosmx.anim.layers.KosmXTestAnimLayer",
                new ResourceLocation(JojoMod.MOD_ID, "test_layer"), 1, 
                Object::new);
    }
    
//  heldAction = register(new ResourceLocation(JojoMod.MOD_ID, "hamon_breath"), 1, HeldActionAnimLayer::new);
//  busyArms = register(new ResourceLocation(JojoMod.MOD_ID, "busy_arms"), 10, BusyArmsLayer::new);
//  register(new ResourceLocation(JojoMod.MOD_ID, "test"), 1, TestAnimLayer::new);

}
