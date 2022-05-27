package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrNonStandFlagPacket {
    private final int entityId;
    private final Flag flag;
    private final boolean value;
    
    public TrNonStandFlagPacket(int entityId, Flag flag, boolean value) {
        this.entityId = entityId;
        this.flag = flag;
        this.value = value;
    }
    
    public static void encode(TrNonStandFlagPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeEnum(msg.flag);
        buf.writeBoolean(msg.value);
    }
    
    public static TrNonStandFlagPacket decode(PacketBuffer buf) {
        return new TrNonStandFlagPacket(buf.readInt(), buf.readEnum(Flag.class), buf.readBoolean());
    }

    public static void handle(TrNonStandFlagPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    switch(msg.flag) {
                    case VAMPIRE_HAMON_USER:
                        power.getTypeSpecificData(ModNonStandPowers.VAMPIRISM.get()).ifPresent(vampirism -> {
                            vampirism.setVampireHamonUser(msg.value);
                        });
                        break;
                    case VAMPIRE_FULL_POWER:
                        power.getTypeSpecificData(ModNonStandPowers.VAMPIRISM.get()).ifPresent(vampirism -> {
                            vampirism.setVampireFullPower(msg.value);
                        });
                        break;
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static enum Flag {
        VAMPIRE_HAMON_USER,
        VAMPIRE_FULL_POWER
    }
}
