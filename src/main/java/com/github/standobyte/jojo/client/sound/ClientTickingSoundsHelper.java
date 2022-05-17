package com.github.standobyte.jojo.client.sound;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.entity.MRDetectorEntity;
import com.github.standobyte.jojo.entity.itemprojectile.BladeHatEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.stand.StandUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.EntityTickableSound;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;

public abstract class ClientTickingSoundsHelper {
    
    public static boolean playVoiceLine(Entity entity, SoundEvent soundEvent, SoundCategory category, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        
        PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(mc.player, soundEvent, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) {
            voiceLineNotTriggered(entity);
            return false;
        }
        soundEvent = event.getSound();
        category = event.getCategory();
        volume = event.getVolume();
        pitch = event.getPitch();

        ISound sound = new EntityTickableSound(soundEvent, category, volume, pitch, entity);
        if (entity instanceof AbstractClientPlayerEntity && entity.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).map(cap -> {
            boolean alreadyPlaying = cap.isVoiceLinePlaying();
            if (alreadyPlaying) {
                cap.lastVoiceLineTriggered = false;
            }
            else {
                cap.lastVoiceLineTriggered = true;
                cap.setCurrentVoiceLine(sound);
            }
            return !alreadyPlaying;
        }).orElse(false)) {
            mc.getSoundManager().play(sound);
            return true;
        }
        else {
            return false;
        }
    }
    
    public static void voiceLineNotTriggered(Entity entity) {
        if (entity instanceof AbstractClientPlayerEntity) {
            entity.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.lastVoiceLineTriggered = false;
            });
        }
    }
    
    public static void playStandEntityCancelableActionSound(StandEntity stand, SoundEvent sound, 
            StandEntityAction action, @Nullable StandEntityAction.Phase phase, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (!stand.isVisibleForAll() && !StandUtil.isEntityStandUser(mc.player)) {
            return;
        }
        
        SoundCategory category = stand.getSoundSource();
        PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(stand, sound, category, pitch, volume);
        if (event.isCanceled() || event.getSound() == null) return;
        sound = event.getSound();
        category = event.getCategory();
        volume = event.getVolume();
        pitch = event.getPitch();
        
        mc.getSoundManager().play(new StoppableEntityTickableSound<StandEntity>(sound, category, 
                volume, pitch, false, stand, e -> e.getCurrentTaskAction() != action
                        || phase != null && e.getCurrentTaskPhase().map(stPhase -> stPhase != phase).orElse(true)));
    }
    
    public static void playStandEntityUnsummonSound(StandEntity stand, SoundEvent sound, float volume, float pitch) {
        if (stand.tickCount > 20) {
            LivingEntity user = stand.getUser();
            if (user != null) {
                Minecraft mc = Minecraft.getInstance();
                if (!stand.isVisibleForAll() && !StandUtil.isEntityStandUser(mc.player)) {
                    return;
                }

                SoundCategory category = stand.getSoundSource();
                PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(stand, sound, category, pitch, volume);
                if (event.isCanceled() || event.getSound() == null) return;
                sound = event.getSound();
                category = event.getCategory();
                volume = event.getVolume();
                pitch = event.getPitch();
                
                mc.getSoundManager().play(new StandUnsummonTickableSound(sound, category, volume, pitch, user, stand));
            }
        }
    }
    
    public static void playHeldActionSound(SoundEvent sound, float volume, float pitch, boolean looping, 
            LivingEntity entity, IPower<?, ?> power, Action<?> action) {
        Minecraft.getInstance().getSoundManager().play(new StoppableEntityTickableSound<LivingEntity>(sound, entity.getSoundSource(), 
                volume, pitch, looping, entity, 
                e -> power.getHeldAction(true) != action));
    }
    
    public static void playHamonSparksSound(Entity entity, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(new HamonSparksSound(entity, volume, pitch));
    }
    
    public static void playGliderFlightSound(LeavesGliderEntity entity) {
        Minecraft.getInstance().getSoundManager().play(new GliderFlightSound(entity));
    }
    
    public static void playBladeHatSound(BladeHatEntity entity) {
        Minecraft.getInstance().getSoundManager().play(new BladeHatSound(entity));
    }
    
    public static void playHamonConcentrationSound(LivingEntity hamonUser, Predicate<LivingEntity> stopCondition) {
        Minecraft.getInstance().getSoundManager().play(new StoppableEntityTickableSound<LivingEntity>(
                ModSounds.HAMON_CONCENTRATION.get(), hamonUser.getSoundSource(), 1.0F, 1.0F, false, hamonUser, stopCondition));
    }
    
    public static void playItemUseSound(LivingEntity entity, SoundEvent sound, float volume, float pitch, boolean looping, ItemStack stack) {
        Minecraft.getInstance().getSoundManager().play(new StoppableEntityTickableSound<LivingEntity>(sound, 
                entity.getSoundSource(), volume, pitch, looping, entity, 
                e -> !e.isUsingItem() || !e.getUseItem().sameItem(stack)));
    }
    
    public static void playMagiciansRedDetectorSound(MRDetectorEntity entity) {
        Minecraft.getInstance().getSoundManager().play(new MRDetectorSound(entity));
    }
}
