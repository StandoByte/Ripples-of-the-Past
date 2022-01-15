package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGStringEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.MathUtil;

import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;

public class HierophantGreenStringAttack extends StandEntityAction {

    public HierophantGreenStringAttack(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide() && ticks == 0) {
            boolean shift = isShiftVariation();
            int n = shift ? 4 : 7;
            for (int i = 0; i < n; i++) {
                Vector2f rotOffsets = i > 0 ? MathUtil.xRotYRotOffsets((double) i / (double) n * Math.PI * 2, 10) : Vector2f.ZERO;
                HGStringEntity string = new HGStringEntity(world, standEntity, rotOffsets.y, rotOffsets.x, shift);
                world.addFreshEntity(string);
            }
            HGStringEntity string = new HGStringEntity(world, standEntity, 0, 0, shift);
            world.addFreshEntity(string);
        }
    }

}
