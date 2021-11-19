package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGStringEntity;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.util.MathUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;

public class HierophantGreenStringAttack extends StandEntityAction {

    public HierophantGreenStringAttack(Builder builder) {
        super(builder);
    }
    
    @Override
    public void perform(World world, LivingEntity user, IPower<?> power, ActionTarget target) {
        if (!world.isClientSide()) {
            LivingEntity stand = getPerformer(user, power);
            boolean shift = isShiftVariation();
            int n = shift ? 3 : 7;
            for (int i = 0; i < n; i++) {
                Vector2f rotOffsets = MathUtil.xRotYRotOffsets((double) i / (double) n * Math.PI * 2, 10);
                HGStringEntity string = new HGStringEntity(world, stand, rotOffsets.y, rotOffsets.x, shift);
                world.addFreshEntity(string);
            }
            HGStringEntity string = new HGStringEntity(world, stand, 0, 0, shift);
            world.addFreshEntity(string);
        }
    }

}
