package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.CrazyDiamondMisshapeBodyPart.TargetHitPart;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CrazyDiamondMisshapingPunch extends StandEntityHeavyAttack {

    public CrazyDiamondMisshapingPunch(Builder builder) {
        super(builder);
    }
    
    @Override
    protected StandEntityActionModifier getRecoveryFollowup(IStandPower standPower, StandEntity standEntity) {
        if (standEntity == null) return null;
        
        TargetHitPart hitPart = standEntity.getCurrentTask().map(task -> task.getAdditionalData().peekOrNull(TargetHitPart.class)).orElse(null);
        if (hitPart != null) {
            switch (hitPart) {
            case HEAD:
                return ModStandsInit.CRAZY_DIAMOND_MISSHAPE_FACE.get();
            case TORSO_ARMS:
                return ModStandsInit.CRAZY_DIAMOND_MISSHAPE_ARMS.get();
            case LEGS:
                return ModStandsInit.CRAZY_DIAMOND_MISSHAPE_LEGS.get();
            }
        }
        return null;
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {
        super.onTaskSet(world, standEntity, standPower, phase, task, ticks);
        if (!world.isClientSide() && task.getTarget().getType() == TargetType.ENTITY) {
            Entity target = task.getTarget().getEntity();
            if (target instanceof LivingEntity && StandUtil.getStandUser((LivingEntity) target) instanceof PlayerEntity) {
                LivingEntity aimingEntity = standPower.getUser();
                if (aimingEntity == null) aimingEntity = standEntity;
                
                TargetHitPart hitPart = TargetHitPart.getHitTarget(target, aimingEntity);
                task.getAdditionalData().push(TargetHitPart.class, hitPart);
            }
        }
    }
    
    @Override
    public void taskWriteAdditional(StandEntityTask task, PacketBuffer buffer) {
        NetworkUtil.writeOptionally(buffer, task.getAdditionalData().peekOrNull(TargetHitPart.class), part -> {
            buffer.writeEnum(part);
        });
    }

    @Override
    public void taskReadAdditional(StandEntityTask task, PacketBuffer buffer) {
        NetworkUtil.readOptional(buffer, () -> buffer.readEnum(TargetHitPart.class)).ifPresent(part -> {
            task.getAdditionalData().push(TargetHitPart.class, part);
        });;
    }
    
    @Override
    public void taskCopyAdditional(StandEntityTask task, StandEntityTask sourceTask) {
        TargetHitPart hitPart = sourceTask.getAdditionalData().peekOrNull(TargetHitPart.class);
        if (hitPart != null) {
            task.getAdditionalData().push(TargetHitPart.class, hitPart);
        }
    }

    @Override
    public void rotateStandTowardsTarget(StandEntity standEntity, ActionTarget target, StandEntityTask task) {
        if (task.getTarget().getType() == TargetType.ENTITY && !task.getAdditionalData().isEmpty(TargetHitPart.class)) {
            Entity entity = task.getTarget().getEntity();
            if (entity instanceof LivingEntity) {
                TargetHitPart hitPart = task.getAdditionalData().peek(TargetHitPart.class);
                if (hitPart != null) {
                    Vector3d pos = hitPart.getPartCenter((LivingEntity) entity);
                    if (pos != null) {
                        MCUtil.rotateTowards(standEntity, pos, 360F);
                        return;
                    }
                }
            }
        }
        super.rotateStandTowardsTarget(standEntity, target, task);
    }
}
