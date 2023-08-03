package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class VampirismDarkAura extends VampirismAction {

    public VampirismDarkAura(NonStandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        int difficulty = world.getDifficulty().getId();
        int range = 16 * difficulty - 8;
        if (!world.isClientSide()) {
            for (LivingEntity entity : MCUtil.entitiesAround(
                    LivingEntity.class, user, range, false, entity -> 
                    !JojoModUtil.isUndead(entity) && !(entity instanceof StandEntity && user.is(((StandEntity) entity).getUser())))) {
                boolean passive = entity instanceof AgeableEntity;
                int amplifier = MathHelper.floor((difficulty - 1) * 1.5);
                int duration = passive ? 600 : 200;
                entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, duration, amplifier));
                entity.addEffect(new EffectInstance(Effects.WEAKNESS, duration, amplifier));
                entity.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, duration, amplifier));
                if (passive) {
                    entity.addEffect(new EffectInstance(ModStatusEffects.STUN.get(), duration));
                }
            }
            if (world.getDifficulty() == Difficulty.HARD) {
                for (HungryZombieEntity zombie : MCUtil.entitiesAround(
                        HungryZombieEntity.class, user, range, false, zombie -> zombie.isEntityOwner(user))) {
                    zombie.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 300, 1));
                    zombie.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 300, 0));
                    zombie.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 300, 0));
                }
            }
        }
        user.playSound(ModSounds.VAMPIRE_EVIL_ATMOSPHERE.get(), (float) (range + 16) / 16F, 1.0F);
    }
    
    @Override
    protected int maxCuringStage() {
        return 3;
    }
}
