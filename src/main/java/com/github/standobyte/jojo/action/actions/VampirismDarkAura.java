package com.github.standobyte.jojo.action.actions;

import java.util.List;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class VampirismDarkAura extends VampirismAction {

    public VampirismDarkAura(AbstractBuilder<?> builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, INonStandPower power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        int difficulty = world.getDifficulty().getId();
        int range = 16 * difficulty - 8;
        if (!world.isClientSide()) {
            List<LivingEntity> entitiesAround = JojoModUtil.entitiesAround(LivingEntity.class, user, range, false, entity -> !JojoModUtil.isUndead(entity));
            for (LivingEntity entity : entitiesAround) {
                int amplifier = MathHelper.floor((difficulty - 1) * 1.5);
                entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 200, amplifier));
                entity.addEffect(new EffectInstance(Effects.WEAKNESS, 200, amplifier));
                entity.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 200, amplifier));
            }
        }
        user.playSound(ModSounds.VAMPIRE_DARK_AURA.get(), (float) (range + 16) / 16F, 1.0F);
    }
}
