package com.github.standobyte.jojo.action.stand;

import java.util.List;
import java.util.OptionalInt;

import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class GoldExperienceLifeDetector extends StandEntityAction {

    public GoldExperienceLifeDetector(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        int tick = task.getTick();
        if (world.isClientSide() && userPower.getUser() == ClientUtil.getClientPlayer()) {
            double radius = (double) tick;
            double maxRadius = 32;
            List<LivingEntity> entitiesAround = MCUtil.entitiesAround(LivingEntity.class, standEntity, 
                    Math.min(radius, maxRadius), false, 
                    entity -> entity != userPower.getUser() && GoldExperienceHeal.isLiving(entity));
            entitiesAround.forEach(entity -> entity.getCapability(EntityUtilCapProvider.CAPABILITY).ifPresent(
                    cap -> cap.setClGlowingColor(OptionalInt.of(ActionsOverlayGui.getPowerUiColor(userPower)), 80)));
        }
    }

}
