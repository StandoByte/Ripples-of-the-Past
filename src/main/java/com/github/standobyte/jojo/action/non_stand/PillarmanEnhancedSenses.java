package com.github.standobyte.jojo.action.non_stand;

import java.util.List;
import java.util.OptionalInt;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanPowerType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class PillarmanEnhancedSenses extends PillarmanAction {

    public PillarmanEnhancedSenses(PillarmanAction.Builder builder) {
        super(builder.holdType());
        stage = 2;
        canBeUsedInStone = true;
    }
    
    private static final OptionalInt COLOR = OptionalInt.of(PillarmanPowerType.COLOR);

    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            if (ticksHeld < 160 || ticksHeld % 20 == 0) {
                double radius = (double) ticksHeld * 0.5;
                double maxRadius = 36;
                List<LivingEntity> entitiesAround = MCUtil.entitiesAround(LivingEntity.class, user, Math.min(radius, maxRadius), false, null);
                
                if (world.isClientSide()) {
                    if (user == ClientUtil.getClientPlayer()) {
                        entitiesAround.forEach(entity -> entity.getCapability(EntityUtilCapProvider.CAPABILITY).ifPresent(
                                cap -> cap.setClGlowingColor(COLOR, 80)));
                    }
                }
                
            }
        }
    }

}
