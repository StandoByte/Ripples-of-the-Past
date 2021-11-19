package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.AfterimageEntity;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class SilverChariotTakeOffArmor extends StandEntityAction {

    public SilverChariotTakeOffArmor(Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IPower<?> power, ActionTarget target) {
        if (performer instanceof SilverChariotEntity && !((SilverChariotEntity) performer).hasArmor()) {
            return conditionMessage("chariot_armor");
        }
        return super.checkConditions(user, performer, power, target);
    }
    
    @Override
    public void perform(World world, LivingEntity user, IPower<?> power, ActionTarget target) {
        if (!world.isClientSide()) {
            LivingEntity performer = this.getPerformer(user, power);
            if (performer instanceof SilverChariotEntity) {
                SilverChariotEntity scEntity = (SilverChariotEntity) performer;
                scEntity.setArmor(!scEntity.hasArmor());
                for (int i = 1; i <= 10; i++) {
                    AfterimageEntity afterimage = new AfterimageEntity(world, scEntity, i);
                    afterimage.setLifeSpan(Integer.MAX_VALUE);
                    world.addFreshEntity(afterimage);
                }
                scEntity.playSound(ModSounds.SILVER_CHARIOT_ARMOR_OFF.get(), 1.0F, 1.0F);
            }
        }
    }
}
