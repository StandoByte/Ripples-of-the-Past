package com.github.standobyte.jojo.action.non_stand;

import java.util.List;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class PillarmanEnhancedSenses extends PillarmanAction {

    public PillarmanEnhancedSenses(PillarmanAction.Builder builder) {
        super(builder.holdType());
        stage = 2;
        canBeUsedInStone = true;
    }

    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            if (!world.isClientSide() && ticksHeld < 160 || ticksHeld % 20 == 0) {
                double radius = (double) ticksHeld * 0.5D;
                double maxRadius = 36D;
                List<LivingEntity> entitiesAround = MCUtil.entitiesAround(LivingEntity.class, user, Math.min(radius, maxRadius), false, null);
                entitiesAround.forEach(entity -> entity.addEffect(new EffectInstance(Effects.GLOWING, 80)));
                
            }
        }
    }

}
