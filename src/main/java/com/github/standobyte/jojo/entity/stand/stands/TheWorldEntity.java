package com.github.standobyte.jojo.entity.stand.stands;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;

import net.minecraft.world.World;

public class TheWorldEntity extends StandEntity {
    
    public TheWorldEntity(StandEntityType<TheWorldEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    protected float getHeavyAttackArmorPiercing(double strength) {
        return super.getHeavyAttackArmorPiercing(strength) * 2F;
    }
}
