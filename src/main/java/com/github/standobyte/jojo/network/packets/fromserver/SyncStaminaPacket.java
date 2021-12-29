package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncStaminaPacket {
    private final float stamina;
    
    public SyncStaminaPacket(float stamina) {
        this.stamina = stamina;
    }
    
    public static void encode(SyncStaminaPacket msg, PacketBuffer buf) {
        buf.writeFloat(msg.stamina);
    }
    
    public static SyncStaminaPacket decode(PacketBuffer buf) {
        return new SyncStaminaPacket(buf.readFloat());
    }

    public static void handle(SyncStaminaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.setStamina(msg.stamina);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
