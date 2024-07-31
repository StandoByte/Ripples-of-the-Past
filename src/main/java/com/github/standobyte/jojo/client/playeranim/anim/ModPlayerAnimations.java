package com.github.standobyte.jojo.client.playeranim.anim;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.HamonMeditationPoseAnim;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.HamonSYOBAnim;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.PlayerBarrageAnim;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.WallClimbAnim;

import net.minecraft.util.ResourceLocation;

public class ModPlayerAnimations {
    public static HamonMeditationPoseAnim meditationPoseAnim;
    public static PlayerBarrageAnim playerBarrageAnim;
    public static BasicToggleAnim hamonBreath;
    public static WallClimbAnim wallClimbing;
    public static HamonSYOBAnim syoBarrage;
    public static BasicToggleAnim sendoWaveKick;
    public static BasicToggleAnim rebuffOverdrive;

    /** 
     * This string must match the full name of the class and the package it's in.<br>
     * 
     * That class uses code from the playerAnimator mod, so we're using Java Reflection here 
     * instead of directly referencing the class. 
     * This way we can create an instance of that class and not crash if the player does not have
     * the playerAnimator mod installed.<br>
     * 
     * I'm prefixing all classes that reference the playerAnimator code in any way by "KosmX", 
     * the username of the playerAnimator developer, 
     * to know which classes not to import in the rest of the classes, 
     * which do not have "KosmX" and therefore might run even if playerAnimator is not installed.
     */
    public static void init() {
        meditationPoseAnim = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXMeditationPoseLayer",
                new ResourceLocation(JojoMod.MOD_ID, "meditation"), 1, 
                HamonMeditationPoseAnim.NoPlayerAnimator::new);
        
        playerBarrageAnim = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.barrage.KosmXBarrageAnimLayer",
                new ResourceLocation(JojoMod.MOD_ID, "barrage"), 1, 
                PlayerBarrageAnim.NoPlayerAnimator::new);
        
        hamonBreath = PlayerAnimationHandler.getPlayerAnimator().registerBasicAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXHamonBreathLayer",
                new ResourceLocation(JojoMod.MOD_ID, "hamon_breath"), 1);
        
        syoBarrage = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXSYOBLayer",
                new ResourceLocation(JojoMod.MOD_ID, "syo_barrage"), 1, 
                HamonSYOBAnim.NoPlayerAnimator::new);
        
        wallClimbing = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXWallClimbLayer",
                new ResourceLocation(JojoMod.MOD_ID, "wall_climb"), 1, 
                WallClimbAnim.NoPlayerAnimator::new);
        
        sendoWaveKick = PlayerAnimationHandler.getPlayerAnimator().registerBasicAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXSendoWaveKickLayer",
                new ResourceLocation(JojoMod.MOD_ID, "sendo_wave_kick"), 1);
        
        rebuffOverdrive = PlayerAnimationHandler.getPlayerAnimator().registerBasicAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXRebuffOverdriveLayer",
                new ResourceLocation(JojoMod.MOD_ID, "rebuff_overdrive"), 1);
        
        
        
        PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.KosmXTestAnimLayer",
                new ResourceLocation(JojoMod.MOD_ID, "test_layer"), 1, 
                Object::new);
    }

}
