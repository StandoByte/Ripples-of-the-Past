package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGStringEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;

public class HierophantGreenStringAttack extends StandEntityAction {

    public HierophantGreenStringAttack(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            boolean shift = isShiftVariation();
            int n = shift ? 3 : 7;
            for (int i = 0; i < n; i++) {
                Vector2f rotOffsets = MathUtil.xRotYRotOffsets((double) i / (double) n * Math.PI * 2, 10);
                addProjectile(world, userPower, standEntity, rotOffsets.y, rotOffsets.x, shift);
            }
            addProjectile(world, userPower, standEntity, 0, 0, shift);
        }
    }

    private void addProjectile(World world, IStandPower userPower, StandEntity standEntity, float yRotDelta, float xRotDelta, boolean shift) {
        HGStringEntity string = new HGStringEntity(world, standEntity, yRotDelta, xRotDelta, shift);
        if (!shift) {
            string.addKnockback(standEntity.guardCounter());
        }
        string.setLifeSpan(getStandActionTicks(userPower, standEntity));
        string.withStandSkin(standEntity.getStandSkin());
        standEntity.addProjectile(string);
    }
    
    protected boolean isCancelable(IStandPower standPower, StandEntity standEntity, @Nullable StandEntityAction newAction, Phase phase) {
        return !this.hasShiftVariation() && 
                (newAction == ModStandsInit.HIEROPHANT_GREEN_EMERALD_SPLASH.get()
                || newAction == ModStandsInit.HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED.get())
                || super.isCancelable(standPower, standEntity, newAction, phase);
    }
    
    @Override
    protected boolean standKeepsTarget(ActionTarget target) {
        return true;
    }
    
    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed() / 8;
        return MathHelper.ceil(super.getStandActionTicks(standPower, standEntity) / Math.max(speed, 0.125));
    }
}
