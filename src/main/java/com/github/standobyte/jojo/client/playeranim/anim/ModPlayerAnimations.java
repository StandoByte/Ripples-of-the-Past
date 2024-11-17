package com.github.standobyte.jojo.client.playeranim.anim;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.HamonMeditationPoseAnim;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.HamonSYOBAnim;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.PlayerBarrageAnim;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.WindupAttackAnim;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.WallClimbAnim;

import net.minecraft.util.ResourceLocation;

public class ModPlayerAnimations {
    public static HamonMeditationPoseAnim meditationPoseAnim;
    public static PlayerBarrageAnim playerBarrageAnim;
    public static BasicToggleAnim hamonBreath;
    public static BasicToggleAnim hamonBeat;
    public static WindupAttackAnim sunlightYellowOverdrive;
    public static WallClimbAnim wallClimbing;
    public static HamonSYOBAnim syoBarrage;
    public static BasicToggleAnim sendoWaveKick;
    public static WindupAttackAnim rebuffOverdrive;
    public static BasicToggleAnim divineSandstorm;
    public static BasicToggleAnim unnaturalAgility;
    public static BasicToggleAnim stoneForm;
    public static BasicToggleAnim bladeBarrage;
    public static BasicToggleAnim bladeDash;

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
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXMeditationPoseHandler",
                new ResourceLocation(JojoMod.MOD_ID, "meditation"), 1, 
                HamonMeditationPoseAnim.NoPlayerAnimator::new);
        
        playerBarrageAnim = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.barrage.KosmXBarrageAnimHandler",
                new ResourceLocation(JojoMod.MOD_ID, "barrage"), 1, 
                PlayerBarrageAnim.NoPlayerAnimator::new);
        
        hamonBreath = PlayerAnimationHandler.getPlayerAnimator().registerBasicAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXHamonBreathHandler",
                new ResourceLocation(JojoMod.MOD_ID, "hamon_breath"), 1);
        
        hamonBeat = PlayerAnimationHandler.getPlayerAnimator().registerBasicAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXHamonBeatHandler",
                new ResourceLocation(JojoMod.MOD_ID, "hamon_beat"), 1);
        
        sunlightYellowOverdrive = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXSYOHandler",
                new ResourceLocation(JojoMod.MOD_ID, "syo"), 1, 
                WindupAttackAnim.NoPlayerAnimator::new);
        
        syoBarrage = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXSYOBHandler",
                new ResourceLocation(JojoMod.MOD_ID, "syo_barrage"), 1, 
                HamonSYOBAnim.NoPlayerAnimator::new);
        
        wallClimbing = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXWallClimbHandler",
                new ResourceLocation(JojoMod.MOD_ID, "wall_climb"), 1, 
                WallClimbAnim.NoPlayerAnimator::new);
        
        sendoWaveKick = PlayerAnimationHandler.getPlayerAnimator().registerBasicAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXSendoWaveKickHandler",
                new ResourceLocation(JojoMod.MOD_ID, "sendo_wave_kick"), 1);
        
        rebuffOverdrive = PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXRebuffOverdriveHandler",
                new ResourceLocation(JojoMod.MOD_ID, "rebuff_overdrive"), 1, 
                WindupAttackAnim.NoPlayerAnimator::new);
        
        
        
        PlayerAnimationHandler.getPlayerAnimator().registerAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.KosmXTestAnimHandler",
                new ResourceLocation(JojoMod.MOD_ID, "test_anim"), 1, 
                Object::new);
        
        divineSandstorm = PlayerAnimationHandler.getPlayerAnimator().registerBasicAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.pillarman.KosmXDivineSandstormLayer",
                new ResourceLocation(JojoMod.MOD_ID, "divine_sandstorm"), 1);
        
        unnaturalAgility = PlayerAnimationHandler.getPlayerAnimator().registerBasicAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.pillarman.KosmXUnnaturalAgilityLayer",
                new ResourceLocation(JojoMod.MOD_ID, "unnatural_agility"), 1);
        
        stoneForm = PlayerAnimationHandler.getPlayerAnimator().registerBasicAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.pillarman.KosmXStoneFormLayer",
                new ResourceLocation(JojoMod.MOD_ID, "stone_form_1"), 1);
        
        bladeBarrage = PlayerAnimationHandler.getPlayerAnimator().registerBasicAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.pillarman.KosmXBladeBarrageLayer",
                new ResourceLocation(JojoMod.MOD_ID, "blade_barrage"), 1);
        
        bladeDash = PlayerAnimationHandler.getPlayerAnimator().registerBasicAnimLayer(
                "com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.pillarman.KosmXBladeDashLayer",
                new ResourceLocation(JojoMod.MOD_ID, "blade_dash"), 1);
    }

}
