package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGStringEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.MathUtil;

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
            int n = shift ? 4 : 7;
            for (int i = 0; i < n; i++) {
                Vector2f rotOffsets = i > 0 ? MathUtil.xRotYRotOffsets((double) i / (double) n * Math.PI * 2, 10) : Vector2f.ZERO;
                addProjectile(world, standEntity, rotOffsets.y, rotOffsets.x, shift);
            }
            addProjectile(world, standEntity, 0, 0, shift);
        }
    }

    private void addProjectile(World world, StandEntity standEntity, float yRotDelta, float xRotDelta, boolean shift) {
    	HGStringEntity string = new HGStringEntity(world, standEntity, yRotDelta, xRotDelta, shift);
    	if (!shift) {
    		string.addKnockback(standEntity.guardCounter());
    	}
    	standEntity.addProjectile(string);
    }
    
    protected boolean isCancelable(IStandPower standPower, StandEntity standEntity, @Nullable StandEntityAction newAction, Phase phase) {
    	return !this.hasShiftVariation() && 
    			(newAction == ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH.get()
    			|| newAction == ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED.get())
    			|| super.isCancelable(standPower, standEntity, newAction, phase);
    }
}
