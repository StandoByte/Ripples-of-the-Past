package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanRegeneration extends PillarmanAction {

    public PillarmanRegeneration(PillarmanAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        
        if (!world.isClientSide()) {
            int regenDuration = 20 * power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().getEvolutionStage() * world.getDifficulty().getId();
            int regenLvl = 1 + power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().getEvolutionStage();
            updateRegenEffect(user, regenDuration, regenLvl);
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
