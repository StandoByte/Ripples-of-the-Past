package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanGiantCarthwheelPrison extends PillarmanErraticBlazeKing {

    public PillarmanGiantCarthwheelPrison(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.HEAT;
    }
 
    @Override
    public void onHoldTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            PillarmanDivineSandstorm.auraEffect(user, ModParticles.HAMON_AURA_RED.get());
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            int n = 20;
            for (int i = 0; i < n; i++) {
                Vector2f rotOffsets = MathUtil.xRotYRotOffsets((double) i / (double) n * Math.PI * 2, 5);
                addVeinProjectile(world, power, user, rotOffsets.y, rotOffsets.x);
            }
            for (int i = 0; i < n; i++) {
                Vector2f rotOffsets = MathUtil.xRotYRotOffsets((double) i / (double) n * Math.PI * 2, 1);
                addVeinProjectile(world, power, user, rotOffsets.y, rotOffsets.x);
            }
            for (int i = 0; i < n; i++) {
                Vector2f rotOffsets = MathUtil.xRotYRotOffsets((double) i / (double) n * Math.PI * 2, 0.1);
                addVeinProjectile(world, power, user, rotOffsets.y, rotOffsets.x);
            }
            addVeinProjectile(world, power, user, 0, 0);
            }
        }
    
    @Override
    public boolean isHeldSentToTracking() {
        return true;
    }
    
}
