package com.github.standobyte.jojo.action.non_stand;

import java.util.List;
import java.util.OptionalInt;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class HamonDetector extends HamonAction {

    public HamonDetector(HamonAction.Builder builder) {
        super(builder.holdType());
    }
    
    private static final OptionalInt COLOR = OptionalInt.of(HamonPowerType.COLOR);
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            if (ticksHeld < 160 || ticksHeld % 20 == 0) {
                HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
                double controlRatio = (double) hamon.getHamonControlLevel() / (double) HamonData.MAX_STAT_LEVEL * hamon.getActionEfficiency(getHeldTickEnergyCost(power), false);
                double radius = (double) ticksHeld * (controlRatio * 0.8D + 0.2D);
                double maxRadius = 8D + controlRatio * 24D;
                List<LivingEntity> entitiesAround = MCUtil.entitiesAround(LivingEntity.class, user, Math.min(radius, maxRadius), false, null);
                
                if (world.isClientSide()) {
                    entitiesAround.forEach(entity -> entity.getCapability(EntityUtilCapProvider.CAPABILITY).ifPresent(
                            cap -> cap.setClGlowingColor(COLOR, 80)));
                }
                else if (!entitiesAround.isEmpty()) {
                    hamon.hamonPointsFromAction(HamonStat.CONTROL, getHeldTickEnergyCost(power)); 
                }
            }
            if (ticksHeld % 3 == 0) {
                // FIXME !!!!!!!!!!!!!!!!!! sfx
                HamonUtil.emitHamonSparkParticles(world, user instanceof PlayerEntity ? (PlayerEntity) user : null, 
                        user.getX(), user.getY(0.5), user.getZ(), 0.1F);
            }
        }
    }

}
