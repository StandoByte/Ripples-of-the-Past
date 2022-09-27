package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrStaminaPacket {
    private final int userId;
    private final float stamina;
    
    public TrStaminaPacket(int userId, float stamina) {
        this.userId = userId;
        this.stamina = stamina;
    }
    
    public static void encode(TrStaminaPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.userId);
        buf.writeFloat(msg.stamina);
    }
    
    public static TrStaminaPacket decode(PacketBuffer buf) {
        return new TrStaminaPacket(buf.readInt(), buf.readFloat());
    }

    public static void handle(TrStaminaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity userEntity = ClientUtil.getEntityById(msg.userId);
            if (userEntity instanceof LivingEntity) {
                LivingEntity userLiving = (LivingEntity) userEntity;
                IStandPower.getStandPowerOptional(userLiving).ifPresent(power -> {
                    power.setStamina(msg.stamina);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
