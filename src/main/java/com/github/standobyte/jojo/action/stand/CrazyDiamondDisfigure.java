package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CrazyDiamondDisfigure extends StandEntityActionModifier {

    public CrazyDiamondDisfigure(Builder builder) {
        super(builder);
    }
    
    @Override
    public void standTickRecovery(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
    }
    
    enum TargetHitPart {
        HEAD,
        TORSO_ARMS,
        LEGS;
        
        TargetHitPart getHitTarget(EntityRayTraceResult rayTrace) {
            return HEAD;
        }
        
        Vector3d getPartCenter(LivingEntity target) {
            return null;
        }
    }
}
