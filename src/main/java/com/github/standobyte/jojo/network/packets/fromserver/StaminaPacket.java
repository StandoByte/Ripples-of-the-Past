package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class StaminaPacket {
    private final float stamina;
    
    public StaminaPacket(float stamina) {
        this.stamina = stamina;
    }
    
    public static void encode(StaminaPacket msg, PacketBuffer buf) {
        buf.writeFloat(msg.stamina);
    }
    
    public static StaminaPacket decode(PacketBuffer buf) {
        return new StaminaPacket(buf.readFloat());
    }

    public static void handle(StaminaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.setStamina(msg.stamina);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
