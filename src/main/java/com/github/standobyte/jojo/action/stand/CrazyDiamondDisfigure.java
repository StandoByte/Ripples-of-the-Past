package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CrazyDiamondDisfigure extends StandEntityActionModifier {

    public CrazyDiamondDisfigure(Builder builder) {
        super(builder);
    }
    
    @Override
    public void standTickRecovery(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        // FIXME !!!!! (combo heavy) heal for half the amount of damage that was dealt
        // FIXME !!!!! (combo heavy) could be input later
        if (
//                !world.isClientSide() && 
                task.getTarget().getType() == TargetType.ENTITY && !task.getAdditionalData().isEmpty(TargetHitPart.class)) {
            TargetHitPart hitPart = task.getAdditionalData().peek(TargetHitPart.class);
            if (hitPart != null) {
                JojoMod.LOGGER.debug(hitPart);
            }
        }
    }
    
    enum TargetHitPart {
        HEAD,
        TORSO_ARMS,
        LEGS;
        
        static int i = 0;
        // FIXME !!!!! (combo heavy) determine the body part aimed at
        static TargetHitPart getHitTarget(EntityRayTraceResult rayTrace) {
            return TargetHitPart.values()[i++ % 3];
        }

        // FIXME !!!!! (combo heavy) determine the body part position
        Vector3d getPartCenter(LivingEntity target) {
            switch (this) {
            case HEAD:
                return target.getBoundingBox().getCenter().add(0, 1, 0);
            case TORSO_ARMS:
                return target.getBoundingBox().getCenter();
            case LEGS:
                return target.getBoundingBox().getCenter().add(0, -1, 0);
            default:
                return null;
            }
        }
        
        void disfigure(LivingEntity target) {
            switch (this) {
                // FIXME !!!!! (combo heavy) player effect
                // FIXME !!!!! (combo heavy) mob effect
            case HEAD:
                target.addEffect(new EffectInstance(Effects.CONFUSION, 60, 0, false, false, true));
                
                break;
                // FIXME !!!!! (combo heavy) player effect
                // FIXME !!!!! (combo heavy) mob effect
            case TORSO_ARMS:
                target.addEffect(new EffectInstance(Effects.WEAKNESS, 60, 0, false, false, true));
                target.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 60, 1, false, false, true));
                
                break;
                // FIXME !!!!! (combo heavy) mob effect
            case LEGS:
                target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 60, 1, false, false, true));
                target.addEffect(new EffectInstance(ModEffects.DISFIGURED_LEGS.get(), 200, 0, false, false, true));
                break;
            }
        }
    }
}
