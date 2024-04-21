package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.World;

public class PillarmanRegeneration extends PillarmanAction {

    public PillarmanRegeneration(PillarmanAction.Builder builder) {
        super(builder);
        stage = 2;
        canBeUsedInStone = true;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        
        if (!world.isClientSide()) {
            updateRegenEffect(user, 80, 3);
            world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), ModSounds.VAMPIRE_BLOOD_DRAIN.get(), user.getSoundSource(), 1.5F, 1.2F);
        }
    }
    
    // prevents the health regeneration being faster or slower when spamming the ability
    private void updateRegenEffect(LivingEntity entity, int duration, int level) {
        EffectInstance currentRegen = entity.getEffect(ModStatusEffects.UNDEAD_REGENERATION.get());
        if (currentRegen != null && currentRegen.getAmplifier() < 5 && currentRegen.getAmplifier() <= level) {
            int regenGap = 50 >> currentRegen.getAmplifier();
            if (regenGap > 0) {
                int oldRegenAppliesIn = currentRegen.getDuration() % (50 >> currentRegen.getAmplifier());
                int newRegenGap = 50 >> level;
                int newRegenAppliesIn = duration % newRegenGap;
                
                if (oldRegenAppliesIn > newRegenAppliesIn) {
                    int newDuration = duration + (oldRegenAppliesIn - newRegenAppliesIn);
                    while (newDuration > duration) {
                        newDuration -= newRegenGap;
                    }
                    if (newDuration > 0) {
                        duration = newDuration;
                    }
                }
                else {
                    duration -= (newRegenAppliesIn - oldRegenAppliesIn);
                }
            }
        }
        entity.addEffect(new EffectInstance(ModStatusEffects.UNDEAD_REGENERATION.get(), duration, level, false, false, true));
    }
}
