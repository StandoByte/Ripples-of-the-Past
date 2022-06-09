package com.github.standobyte.jojo.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.damaging.projectile.HGEmeraldEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGBarrierEntity;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class HGBarriersNet {
	private Map<HGBarrierEntity, ShootingPoints> placedBarriers = new HashMap<>();
	private GraphAdjacencyList<Vector3d> closePoints = new GraphAdjacencyList<>();
	private boolean isDirty = true;

	public void tick() {
        Iterator<Map.Entry<HGBarrierEntity, ShootingPoints>> iter = placedBarriers.entrySet().iterator();
        while (iter.hasNext()) {
            HGBarrierEntity barrier = iter.next().getKey();
            if (!barrier.isAlive() || barrier.wasRipped()) {
            	onRemoved(barrier);
                iter.remove();
            }
        }
	}
	
	public void add(HGBarrierEntity barrier) {
		placedBarriers.put(barrier, generateShootingPoints(barrier));
		onUpdate();
	}
	
	private static final double SHOOTING_POINTS_GAP = 12;
	private ShootingPoints generateShootingPoints(HGBarrierEntity entity) {
		Vector3d posA = entity.position();
		Vector3d posB = entity.getOriginPoint(1.0F);
		Vector3d vecAToB = posB.subtract(posA);
		
		List<Vector3d> shootingPoints = new ArrayList<>();
		if (vecAToB.lengthSqr() <= SHOOTING_POINTS_GAP * SHOOTING_POINTS_GAP * 4) {
			shootingPoints.add(posA.add(vecAToB.scale(0.5)));
		}
		else {
			int steps = MathHelper.floor(vecAToB.length() / SHOOTING_POINTS_GAP);
			Vector3d nextPoint = posA;
			Vector3d stepVec = vecAToB.normalize().scale(SHOOTING_POINTS_GAP);
			for (int i = 0; i < steps; i++) {
				nextPoint = nextPoint.add(stepVec);
				shootingPoints.add(nextPoint);
			}
		}

		return new ShootingPoints(shootingPoints);
	}
	
	private void onRemoved(HGBarrierEntity barrier) {
		
		onUpdate();
	}
	
	private void onUpdate() {
		if (!isDirty) {
			closePoints.clear();
			isDirty = true;
		}
	}
	
	public int getSize() {
		return placedBarriers.size();
	}

    // FIXME (!!) emeralds limit, stamina cost and shooting points choice strategy
    public void shootEmeraldsFromBarriers(IStandPower standPower, HierophantGreenEntity stand, 
    		Vector3d targetPos, int tick, int maxEmeralds, PointsChoice choose) {
    	List<Vector3d> shootingPoints = placedBarriers.values().stream().flatMap(points -> 
    	points.shootingPoints.stream()).collect(Collectors.toCollection(LinkedList::new));
    	if (isDirty) {
    		closePoints.create((pointA, pointB) -> pointA.distanceToSqr(pointB) < 36, shootingPoints);
    		isDirty = false;
    	}

    	choose = PointsChoice.CLOSEST;
    	Set<Vector3d> pointsToShootThisTick = new HashSet<>();
    	Random random = stand.getRandom();
    	for (int i = 0; i < maxEmeralds && !shootingPoints.isEmpty(); i++) {
    		Vector3d point;
    		switch (choose) {
    		case RANDOM:
    			point = shootingPoints.get(random.nextInt(shootingPoints.size()));
    			break;
    		case CLOSEST:
        		point = shootingPoints.stream().min(Comparator.comparingDouble(p -> p.distanceToSqr(targetPos))).get();
    			break;
    		default:
    			return;
    		}
    		pointsToShootThisTick.add(point);
    		shootingPoints.remove(point);
    		closePoints.getAllAdjacent(point).forEach(closePoint -> shootingPoints.remove(closePoint));
    	}
//    	float staminaCost = (ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED.get()).getStaminaCostTicking(standPower);
//    	int barrierEmeralds = Math.max(stand.getPlacedBarriersCount() * multiplier / 10, 1);
//    	for (int i = 0; i < barrierEmeralds && standPower.consumeStamina(staminaCost); i++) {
//    		placedBarriers.get(stand.getRandom().nextInt(placedBarriers.size())).shootEmeralds(pos, 1);
//    	}
    	JojoMod.LOGGER.debug(pointsToShootThisTick.size());
    	pointsToShootThisTick.forEach(point -> {
    		shootEmerald(stand, point, targetPos, 1, tick == 0);
    	});
    }
    
    public enum PointsChoice {
    	RANDOM,
    	CLOSEST
    }
    
    private void shootEmerald(HierophantGreenEntity stand, Vector3d shootingPos, Vector3d targetPos, double count, boolean playSound) {
    	if (!stand.level.isClientSide()) {
	    	JojoModUtil.doFractionTimes(() -> {
	            HGEmeraldEntity emeraldEntity = new HGEmeraldEntity(stand, stand.level, null);
	            emeraldEntity.setPos(shootingPos.x, shootingPos.y, shootingPos.z);
	            emeraldEntity.setConcentrated(true);
	            Vector3d shootVec = targetPos.subtract(shootingPos);
	            emeraldEntity.shoot(shootVec.x, shootVec.y, shootVec.z, 1F, stand.getProjectileInaccuracy(2.0F));
	            stand.addProjectile(emeraldEntity);
	            if (playSound) {
	            	JojoModUtil.playSound(stand.level, null, shootingPos.x, shootingPos.y, shootingPos.z, 
	            			ModSounds.HIEROPHANT_GREEN_EMERALD_SPLASH.get(), 
	            			stand.getSoundSource(), 1.0F, 1.0F, StandUtil::shouldHearStands);
	            }
	    	}, count);
    	}
    }
    
    private class ShootingPoints {
    	private final List<Vector3d> shootingPoints;
    	
    	private ShootingPoints(List<Vector3d> shootingPoints) {
    		this.shootingPoints = shootingPoints;
    	}
    }
}
