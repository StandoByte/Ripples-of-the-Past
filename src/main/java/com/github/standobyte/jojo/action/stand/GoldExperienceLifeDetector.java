package com.github.standobyte.jojo.action.stand;

import java.util.List;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class GoldExperienceLifeDetector extends StandEntityAction {

    public GoldExperienceLifeDetector(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        int tick = task.getTick();
        if (!world.isClientSide()) {
            double radius = (double) tick;
            double maxRadius = 32;
            List<LivingEntity> entitiesAround = MCUtil.entitiesAround(LivingEntity.class, standEntity, 
                    Math.min(radius, maxRadius), false, 
                    entity -> entity != userPower.getUser() && GoldExperienceHeal.isLiving(entity));
            entitiesAround.forEach(entity -> entity.addEffect(new EffectInstance(Effects.GLOWING, 80)));
        }
    }

}
