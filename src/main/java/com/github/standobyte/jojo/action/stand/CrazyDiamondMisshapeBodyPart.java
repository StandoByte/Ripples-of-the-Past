package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.JojoMod;
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
                    
                    hitPart.disfigure(targetEntity);
    
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
            return LEGS;
        }

        // FIXME !!!!! (combo heavy) determine the body part position
        Vector3d getPartCenter(LivingEntity target) {
            switch (this) {
            case HEAD:
                return target.getBoundingBox().getCenter().add(0, 1, 0);
            case TORSO_ARMS:
                return target.getBoundingBox().getCenter().add(0, 0.25, 0);
            case LEGS:
                return target.getBoundingBox().getCenter().add(0, -1, 0);
            default:
                return null;
            }
        }
        
        void disfigure(LivingEntity target) {
            JojoMod.LOGGER.debug(this);
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
