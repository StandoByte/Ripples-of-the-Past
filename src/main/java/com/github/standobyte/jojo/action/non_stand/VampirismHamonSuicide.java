package com.github.standobyte.jojo.action.non_stand;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class VampirismHamonSuicide extends VampirismAction {

    public VampirismHamonSuicide(NonStandAction.Builder builder) {
        super(builder); 
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide()) {
            if (ticksHeld % 10 == 5) {
                DamageUtil.dealHamonDamage(user, 4, user, null);
            }
            if (ticksHeld == 30) {
                user.addEffect(new EffectInstance(ModEffects.HAMON_SPREAD.get(), 100, 1));
            }
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            DamageUtil.dealHamonDamage(user, 1024, user, null);
            hamonExplosion(world, user.getBoundingBox().getCenter(), user, 6, 200);
        }
    }
    
    // FIXME ! (hamon) hamon explosion particles
    // FIXME ! (hamon) charge nearby living blocks
    public static void hamonExplosion(World world, Vector3d pos, @Nullable LivingEntity sourceEntity, float radius, float damage) {
        if (!world.isClientSide()) {
            Predicate<LivingEntity> filter = EntityPredicates.LIVING_ENTITY_STILL_ALIVE.and(EntityPredicates.NO_CREATIVE_OR_SPECTATOR);
            if (sourceEntity != null) filter = filter.and(entity -> !entity.is(sourceEntity));
            world.getEntitiesOfClass(LivingEntity.class, 
                    new AxisAlignedBB(pos.subtract(radius, radius, radius), pos.add(radius, radius, radius)), filter)
            .forEach(target -> {
                DamageUtil.dealHamonDamage(target, damage, sourceEntity, null);
            });
        }
    }
}
