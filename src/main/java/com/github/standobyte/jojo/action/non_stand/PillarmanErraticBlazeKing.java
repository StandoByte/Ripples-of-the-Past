package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.PillarmanVeinEntity;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;

public class PillarmanErraticBlazeKing extends PillarmanAction {

    public PillarmanErraticBlazeKing(PillarmanAction.Builder builder) {
        super(builder);
        mode = Mode.HEAT;
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            int n = 5;
            for (int i = 0; i < n; i++) {
                Vector2f rotOffsets = MathUtil.xRotYRotOffsets((double) i / (double) n * Math.PI * 2, 10);
                //addVeinProjectile(world, power, user, rotOffsets.x, rotOffsets.y, rotOffsets.x, rotOffsets.y - 0.6D);
                addVeinProjectile(world, power, user, rotOffsets.x, rotOffsets.y, -0.4, -0.65);
                addVeinProjectile(world, power, user, rotOffsets.x, rotOffsets.y, 0.4, -0.65);
            }
        }
}

    public static void addVeinProjectile(World world, INonStandPower power, LivingEntity user, float xRotDelta, float yRotDelta, double offsetX, double offsetY) {
        PillarmanVeinEntity string = new PillarmanVeinEntity(world, user, xRotDelta, yRotDelta, offsetX, offsetY);
        string.setLifeSpan(25);
        world.addFreshEntity(string);
    }
    
}
