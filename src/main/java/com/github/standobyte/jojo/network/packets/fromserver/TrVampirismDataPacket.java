package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrVampirismDataPacket {
    private final int entityId;
    private final Flag flag;
    private final boolean value;
    
    public TrVampirismDataPacket(int entityId, Flag flag, boolean value) {
        this.entityId = entityId;
        this.flag = flag;
        this.value = value;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrVampirismDataPacket> {

        @Override
        public void encode(TrVampirismDataPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeEnum(msg.flag);
            buf.writeBoolean(msg.value);
        }

        @Override
        public TrVampirismDataPacket decode(PacketBuffer buf) {
            return new TrVampirismDataPacket(buf.readInt(), buf.readEnum(Flag.class), buf.readBoolean());
        }

        @Override
        public void handle(TrVampirismDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    switch(msg.flag) {
                    case VAMPIRE_HAMON_USER:
                        power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(vampirism -> {
                            vampirism.setVampireHamonUser(msg.value);
                        });
                        break;
                    case VAMPIRE_FULL_POWER:
                        power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(vampirism -> {
                            vampirism.setVampireFullPower(msg.value);
                        });
                        break;
                    }
                });
            }
        }

        @Override
        public Class<TrVampirismDataPacket> getPacketClass() {
            return TrVampirismDataPacket.class;
        }
    }
    
    public static enum Flag {
        VAMPIRE_HAMON_USER,
        VAMPIRE_FULL_POWER
    }
}
