package com.github.standobyte.jojo.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGBarrierEntity;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.util.math.vector.Vector3d;

public class HGBarriersNet {
	private List<HGBarrierEntity> placedBarriers = new LinkedList<HGBarrierEntity>();

	public void tick() {
        Iterator<HGBarrierEntity> iter = placedBarriers.iterator();
        while (iter.hasNext()) {
            HGBarrierEntity barrier = iter.next();
            if (!barrier.isAlive() || barrier.wasRipped()) {
                iter.remove();
            }
        }
	}
	
	public void add(HGBarrierEntity barrier) {
		placedBarriers.add(barrier);
	}
	
	public int getSize() {
		return placedBarriers.size();
	}
    
    public void shootEmeraldsFromBarriers(IStandPower standPower, HierophantGreenEntity stand, Vector3d pos, int multiplier) {
    	// FIXME (!) emeralds from barriers change
    	float staminaCost = (ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED.get()).getStaminaCostTicking(standPower);
    	int barrierEmeralds = Math.max(stand.getPlacedBarriersCount() * multiplier / 10, 1);
    	for (int i = 0; i < barrierEmeralds && standPower.consumeStamina(staminaCost); i++) {
    		placedBarriers.get(stand.getRandom().nextInt(placedBarriers.size())).shootEmeralds(pos, 1);
    	}
    }
}
