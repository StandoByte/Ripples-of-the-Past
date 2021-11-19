package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.util.damage.ModDamageSources;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class VampirismFreeze extends Action {

    public VampirismFreeze(AbstractBuilder<?> builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IPower<?> power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        if (performer.isOnFire()) {
            return conditionMessage("fire");
        }
        if (user.level.dimensionType().ultraWarm()) {
            return conditionMessage("ultrawarm");
        }
        if (!performer.getMainHandItem().isEmpty()) {
            return conditionMessage("hand");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void onHoldTickUser(World world, LivingEntity user, IPower<?> power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            Entity entityTarget = target.getEntity(world);
            if (entityTarget instanceof LivingEntity) {
                int difficulty = world.getDifficulty().getId();
                LivingEntity targetLiving = (LivingEntity) entityTarget;
                EffectInstance freezeInstance = targetLiving.getEffect(ModEffects.FREEZE.get());
                if (freezeInstance == null) {
                    targetLiving.addEffect(new EffectInstance(ModEffects.FREEZE.get(), difficulty * 30, 0));
                }
                else {
                    int additionalDuration = 1 << difficulty;
                    int duration = freezeInstance.getDuration() + additionalDuration;
                    int lvl = duration / 120;
                    targetLiving.addEffect(new EffectInstance(ModEffects.FREEZE.get(), duration, lvl));
                }
                ModDamageSources.dealColdDamage(targetLiving, 1.5F * difficulty, user, null);
            }
        }
    }

    @Override
    public boolean isHeldSentToTracking() {
        return true;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, IPower<?> power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (requirementsFulfilled) {
            if (ticksHeld % 5 == 0) {
                Vector3d particlePos = user.getEyePosition(1.0F).add(user.getForward().scale(0.75));
                user.level.addParticle(ParticleTypes.CLOUD, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
            }
            if (stateRefreshed) {
                ClientTickingSoundsHelper.playHeldActionSound(ModSounds.VAMPIRE_FREEZE.get(), 1.0F, 1.0F, false, getPerformer(user, power), power, this);
            }
        }
    }
}
