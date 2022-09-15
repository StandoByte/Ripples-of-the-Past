package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.punch.IPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.util.utils.MathUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CrazyDiamondMisshapeBodyPart extends StandEntityActionModifier {

    public CrazyDiamondMisshapeBodyPart(Builder builder) {
        super(builder);
    }
    
    // FIXME misshaping body parts mob effects
    
    @Override
    public void standTickRecovery(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        TargetHitPart hitPart = task.getAdditionalData().peekOrNull(TargetHitPart.class);
        if (hitPart == null) return;
        
        boolean triggerEffect = task.getTicksLeft() <= 1;
        if (task.getAdditionalData().isEmpty(TriggeredFlag.class) && task.getTarget().getType() == TargetType.ENTITY) {
            Entity entity = task.getTarget().getEntity();
            if (entity.isAlive() && entity instanceof LivingEntity) {
                if (world.isClientSide()) {
                    if (StandUtil.shouldStandsRender(ClientUtil.getClientPlayer())) {
                        CrazyDiamondHeal.addParticlesAround(entity);
                    }
                    if (task.getTick() == 0 && StandUtil.shouldHearStands(ClientUtil.getClientPlayer())) {
                        world.playLocalSound(entity.getX(), entity.getY(0.5), entity.getZ(), ModSounds.CRAZY_DIAMOND_FIX_STARTED.get(), 
                                standEntity.getSoundSource(), 1.0F, 1.0F, false);
                    }
                }
                else if (triggerEffect) {
                    LivingEntity targetEntity = StandUtil.getStandUser((LivingEntity) entity);
                    
                    hitPart.misshape(targetEntity);
    
                    IPunch punch = standEntity.getLastPunch();
                    float damageDealt = punch.getType() == TargetType.ENTITY ? ((StandEntityPunch) punch).getDamageDealtToLiving() : 0;
                    targetEntity.setHealth(targetEntity.getHealth() + damageDealt * 0.5F);
                }
            }
            if (triggerEffect) {
                task.getAdditionalData().push(TriggeredFlag.class, new TriggeredFlag());
            }
        }
    }
    
    public enum TargetHitPart {
        HEAD,
        TORSO_ARMS,
        LEGS;
        
        // FIXME !!!!! (combo heavy) determine the body part aimed at
        static TargetHitPart getHitTarget(EntityRayTraceResult rayTrace) {
            return TORSO_ARMS;
        }

        Vector3d getPartCenter(LivingEntity target) {
            switch (this) {
            case HEAD:
                return new Vector3d(target.getX(), target.getY(1.0), target.getZ());
            case TORSO_ARMS:
                return new Vector3d(target.getX(), target.getY(0.7), target.getZ())
                        .add(new Vector3d(target.getBbWidth() * 0.375F, 0, 0).yRot((180 - target.yRot) * MathUtil.DEG_TO_RAD));
            case LEGS:
                return new Vector3d(target.getX(), target.getY(0.0), target.getZ());
            default:
                return null;
            }
        }
        
        void misshape(LivingEntity target) {
            switch (this) {
            case HEAD:
                target.addEffect(new EffectInstance(Effects.CONFUSION, 60, 0, false, false, true));
                target.addEffect(new EffectInstance(ModEffects.MISSHAPEN_FACE.get(), 200, 0, false, false, true));
                break;
            case TORSO_ARMS:
                target.addEffect(new EffectInstance(Effects.WEAKNESS, 60, 0, false, false, true));
                target.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 60, 1, false, false, true));
                target.addEffect(new EffectInstance(ModEffects.MISSHAPEN_ARMS.get(), 200, 0, false, false, true));
                break;
            case LEGS:
                target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 60, 1, false, false, true));
                target.addEffect(new EffectInstance(ModEffects.MISSHAPEN_LEGS.get(), 200, 0, false, false, true));
                break;
            }
        }
    }
}
