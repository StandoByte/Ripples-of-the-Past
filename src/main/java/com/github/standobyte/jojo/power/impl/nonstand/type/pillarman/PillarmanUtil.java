package com.github.standobyte.jojo.power.impl.nonstand.type.pillarman;

import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrPillarmanParticlesPacket;

import net.minecraft.entity.Entity;

public class PillarmanUtil {
	
	public static void sparkEffect(Entity entity, int quantity) {
		if (!entity.level.isClientSide()) {
			PacketManager.sendToClientsTrackingAndSelf(TrPillarmanParticlesPacket.emitter(entity.getId(), quantity,
                    ModParticles.LIGHT_SPARK.get()), entity);
        }
    }
}
