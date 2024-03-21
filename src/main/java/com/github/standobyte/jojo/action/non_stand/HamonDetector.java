package com.github.standobyte.jojo.action.non_stand;

import java.util.List;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class HamonDetector extends HamonAction {

    public HamonDetector(HamonAction.Builder builder) {
        super(builder.holdType());
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (stateRefreshed && requirementsFulfilled) {
            ClientTickingSoundsHelper.playHeldActionSound(ModSounds.HAMON_DETECTOR.get(), 
                    1.0F, 1.0F, true, user, power, this, 15);
        }
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            if (!world.isClientSide() && ticksHeld < 160 || ticksHeld % 20 == 0) {
                HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
                double controlRatio = (double) hamon.getHamonControlLevel() / (double) HamonData.MAX_STAT_LEVEL * hamon.getActionEfficiency(getHeldTickEnergyCost(power), false);
                double radius = (double) ticksHeld * (controlRatio * 0.8D + 0.2D);
                double maxRadius = 8D + controlRatio * 24D;
                List<LivingEntity> entitiesAround = MCUtil.entitiesAround(LivingEntity.class, user, Math.min(radius, maxRadius), false, null);
                entitiesAround.forEach(entity -> entity.addEffect(new EffectInstance(Effects.GLOWING, 80)));
                if (!entitiesAround.isEmpty()) {
                    hamon.hamonPointsFromAction(HamonStat.CONTROL, getHeldTickEnergyCost(power)); 
                }
            }
            HamonSparksLoopSound.playSparkSound(user, user.position(), 1.0F);
            CustomParticlesHelper.createHamonSparkParticles(user instanceof PlayerEntity ? (PlayerEntity) user : null, 
                    user.getX(), user.getY(0.5), user.getZ(), 1);
        }
    }

}
