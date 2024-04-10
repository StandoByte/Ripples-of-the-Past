package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.github.standobyte.jojo.power.impl.nonstand.type.zombie.ZombieData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrPillarmanFlagsPacket {
    private final int entityId;
    private final boolean stoneFormEnabled;
    private final int stage;
    
    public TrPillarmanFlagsPacket(int entityId, PillarmanData pillarmanData) {
        this(entityId, pillarmanData.isStoneFormEnabled(), pillarmanData.getEvolutionStage());
    }
    
    public TrPillarmanFlagsPacket(int entityId, boolean stoneFormEnabled, int stage) {
        this.entityId = entityId;
        this.stoneFormEnabled = stoneFormEnabled;
        this.stage = stage;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrPillarmanFlagsPacket> {

        @Override
        public void encode(TrPillarmanFlagsPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeBoolean(msg.stoneFormEnabled);
            buf.writeInt(msg.stage);
        }

        @Override
        public TrPillarmanFlagsPacket decode(PacketBuffer buf) {
            return new TrPillarmanFlagsPacket(buf.readInt(), buf.readBoolean(), buf.readInt());
        }

        @Override
        public void handle(TrPillarmanFlagsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).ifPresent(pillarman -> {
                    	pillarman.setStoneFormEnabled(msg.stoneFormEnabled);
                    });
                });
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).ifPresent(pillarman -> {
                    	pillarman.setEvolutionStage(msg.stage);
                    });
                });
            }
        }

        @Override
        public Class<TrPillarmanFlagsPacket> getPacketClass() {
            return TrPillarmanFlagsPacket.class;
        }
    }

}
