package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.zombie.ZombieData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrZombieDataPacket {
    private final int entityId;
    private final boolean disguiseEnabled;
    
    public TrZombieDataPacket(int entityId, ZombieData zombieData) {
        this(entityId, zombieData.isDisguiseEnabled());
    }
    
    public TrZombieDataPacket(int entityId, boolean disguiseEnabled) {
        this.entityId = entityId;
        this.disguiseEnabled = disguiseEnabled;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrZombieDataPacket> {

        @Override
        public void encode(TrZombieDataPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeBoolean(msg.disguiseEnabled);
        }

        @Override
        public TrZombieDataPacket decode(PacketBuffer buf) {
            return new TrZombieDataPacket(buf.readInt(), buf.readBoolean());
        }

        @Override
        public void handle(TrZombieDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.ZOMBIE.get()))
                .ifPresent(zombie -> {
                    zombie.setDisguiseEnabled(msg.disguiseEnabled);
                });
            }
        }

        @Override
        public Class<TrZombieDataPacket> getPacketClass() {
            return TrZombieDataPacket.class;
        }
    }

}
