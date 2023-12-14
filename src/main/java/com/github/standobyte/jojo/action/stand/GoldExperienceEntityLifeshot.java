package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class GoldExperienceEntityLifeshot extends StandEntityActionModifier {
    
    public GoldExperienceEntityLifeshot(Builder builder) {
        super(builder);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            Entity targetEntity = task.getTarget().getEntity();
            if (targetEntity instanceof LivingEntity) {
                LivingEntity targetLiving = (LivingEntity) targetEntity;
                targetLiving = StandUtil.getStandUser(targetLiving);
                if (targetLiving.getMobType() != CreatureAttribute.UNDEAD) {
                    if (!targetLiving.hasEffect(ModStatusEffects.SENSORY_OVERLOAD.get())) {
                        giveEffectWithResist(targetLiving);
                    }
                }
                else if (targetLiving instanceof ZombieVillagerEntity) {
                    CommonReflection.startConverting((ZombieVillagerEntity) targetEntity, 
                            userPower.getUser().getUUID(), 
                            targetLiving.getRandom().nextInt(2401) + 3600);
                }
            }
        }
    }
    
    public static final int MAX_DURATION = 100;
    public static final int RESIST_TICKS = 400;
    public static final float RESIST_TICK_DOWN = 0.125F;
    public static final int REDUCTION_SHORT_DELAY = 40;
    public static final int REDUCTION_LONG_DELAY = 20;
    public static void giveEffectWithResist(LivingEntity targetEntity) {
        int duration = targetEntity.getCapability(LivingUtilCapProvider.CAPABILITY)
                .map(cap -> cap.onLifeShot(MAX_DURATION)).orElse(MAX_DURATION);
        if (duration > 0) {
            targetEntity.addEffect(new EffectInstance(ModStatusEffects.SENSORY_OVERLOAD.get(), duration, 0));
        }
        else {
            targetEntity.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 40, 1, false, false, true));
            targetEntity.addEffect(new EffectInstance(Effects.DIG_SPEED, 40, 0, false, false, true));
            targetEntity.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 40, 0, false, false, true));
            targetEntity.addEffect(new EffectInstance(Effects.REGENERATION, 40, 0, false, false, true));
        }
    }
}
