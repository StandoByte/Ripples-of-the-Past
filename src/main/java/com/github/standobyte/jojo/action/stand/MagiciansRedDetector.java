package com.github.standobyte.jojo.action.stand;

import java.util.List;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.MRDetectorEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class MagiciansRedDetector extends StandAction {

    public MagiciansRedDetector(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            List<MRDetectorEntity> summonedDetector = world.getEntities(ModEntityTypes.MR_DETECTOR.get(), 
                    user.getBoundingBox().inflate(5), detector -> detector.getOwner() == user);
            if (!summonedDetector.isEmpty()) {
                summonedDetector.forEach(detector -> detector.remove());
            }
            else {
                MRDetectorEntity detector = new MRDetectorEntity(user, world);
                detector.copyPosition(user);
                world.addFreshEntity(detector);
            }
        }
    }

}
