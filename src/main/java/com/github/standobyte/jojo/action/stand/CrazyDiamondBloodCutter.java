package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.CDBloodCutterEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class CrazyDiamondBloodCutter extends StandEntityAction {

    public CrazyDiamondBloodCutter(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (user.getHealth() >= user.getMaxHealth()
                && !(user instanceof PlayerEntity && ((PlayerEntity) user).abilities.invulnerable)) {
            return conditionMessage("full_health");
        }
        return super.checkSpecificConditions(user, power, target);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            CDBloodCutterEntity cutter = new CDBloodCutterEntity(user, world);
            cutter.shootFromRotation(user, 2.0F, standEntity.getProjectileInaccuracy(1.0F));
            standEntity.addProjectile(cutter);
        }
    }

}
