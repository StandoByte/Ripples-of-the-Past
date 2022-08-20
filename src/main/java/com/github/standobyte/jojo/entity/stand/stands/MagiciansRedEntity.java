package com.github.standobyte.jojo.entity.stand.stands;

import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class MagiciansRedEntity extends StandEntity {
    
    public MagiciansRedEntity(StandEntityType<MagiciansRedEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public boolean attackEntity(Entity target, StandEntityPunch punch, StandEntityTask task) {
        return DamageUtil.dealDamageAndSetOnFire(target, 
                entity -> super.attackEntity(target, punch, task), 10, true);
    }
    
    @Override
    public void playStandSummonSound() {
        if (!isArmsOnlyMode()) {
            super.playStandSummonSound();
        }
    }
}
