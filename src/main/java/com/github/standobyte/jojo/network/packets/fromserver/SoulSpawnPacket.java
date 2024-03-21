package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ControllerSoul;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SoulSpawnPacket {
    private final boolean failedSpawnPacket;
    private final boolean flag2;
    
    private SoulSpawnPacket(boolean failedSpawnPacket, boolean flag2) {
        this.failedSpawnPacket = failedSpawnPacket;
        this.flag2 = flag2;
    }
    
    public static SoulSpawnPacket noSoulSpawned() {
        return new SoulSpawnPacket(true, false);
    }
    
    public static SoulSpawnPacket spawnFlag(boolean soulCanSpawnFlag) {
        return new SoulSpawnPacket(false, soulCanSpawnFlag);
    }
    
    
    
    public static class Handler implements IModPacketHandler<SoulSpawnPacket> {

        @Override
        public void encode(SoulSpawnPacket msg, PacketBuffer buf) {
            byte flags = 0;
            if (msg.failedSpawnPacket) flags  = 1;
            if (msg.flag2)             flags |= 2;
            buf.writeByte(flags);
        }

        @Override
        public SoulSpawnPacket decode(PacketBuffer buf) {
            byte flags = buf.readByte();
            return new SoulSpawnPacket((flags & 1) > 0, (flags & 2) > 0);
        }

        @Override
        public void handle(SoulSpawnPacket msg, Supplier<NetworkEvent.Context> ctx) {
            if (msg.failedSpawnPacket) {
                ControllerSoul.getInstance().onSoulFailedSpawn();
            }
            else {
                IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                    power.clSetSoulSpawnFlag(msg.flag2);
                });
            }
        }

        @Override
        public Class<SoulSpawnPacket> getPacketClass() {
            return SoulSpawnPacket.class;
        }
    }
}
