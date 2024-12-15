package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.PillarmanRibEntity;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;

public class PillarmanRibsBlades extends PillarmanAction {

    public PillarmanRibsBlades(PillarmanAction.Builder builder) {
        super(builder);
        stage = 2;
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            Vector2f rotOffsets = MathUtil.xRotYRotOffsets(Math.PI * 2, 10);
            addRibProjectile(world, power, user, rotOffsets.x, rotOffsets.y, -0.18D, -0.50D);
            addRibProjectile(world, power, user, rotOffsets.x, rotOffsets.y, -0.22D, -0.60D);
            addRibProjectile(world, power, user, rotOffsets.x, rotOffsets.y, -0.22D, -0.70D);
            addRibProjectile(world, power, user, rotOffsets.x, rotOffsets.y, -0.18D, -0.80D);
            addRibProjectile(world, power, user, rotOffsets.x, rotOffsets.y, 0.18D, -0.50D);
            addRibProjectile(world, power, user, rotOffsets.x, rotOffsets.y, 0.22D, -0.65D);
            addRibProjectile(world, power, user, rotOffsets.x, rotOffsets.y, 0.22D, -0.85D);
            addRibProjectile(world, power, user, rotOffsets.x, rotOffsets.y, 0.18D, -0.95D);
        }
    }

    public static void addRibProjectile(World world, INonStandPower power, LivingEntity user, float xRotDelta, float yRotDelta, double offsetX, double offsetY) {
        PillarmanRibEntity string = new PillarmanRibEntity(world, user, xRotDelta, yRotDelta, offsetX, offsetY);
        string.setLifeSpan(21);
        world.addFreshEntity(string);
    }
    
}
