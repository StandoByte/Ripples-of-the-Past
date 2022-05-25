package com.github.standobyte.jojo.action.actions;

import java.util.List;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class HamonDetector extends HamonAction {

    public HamonDetector(HamonAction.Builder builder) {
        super(builder);
    }

    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            if (!world.isClientSide() && ticksHeld < 160 || ticksHeld % 20 == 0) {
                HamonData hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
                double controlRatio = (double) hamon.getHamonControlLevel() / (double) HamonData.MAX_STAT_LEVEL * hamon.getBloodstreamEfficiency();
                double radius = (double) ticksHeld * (controlRatio * 0.8D + 0.2D);
                double maxRadius = 8D + controlRatio * 24D;
                List<LivingEntity> entitiesAround = JojoModUtil.entitiesAround(LivingEntity.class, user, Math.min(radius, maxRadius), false, null);
                entitiesAround.forEach(entity -> entity.addEffect(new EffectInstance(Effects.GLOWING, 80)));
                if (!entitiesAround.isEmpty()) {
                    hamon.hamonPointsFromAction(HamonStat.CONTROL, getHeldTickEnergyCost()); 
                }
            }
            if (ticksHeld % 3 == 0) {
                HamonPowerType.createHamonSparkParticles(world, user instanceof PlayerEntity ? (PlayerEntity) user : null, 
                        user.getX(), user.getY(0.5), user.getZ(), 0.1F);
            }
        }
    }

}
