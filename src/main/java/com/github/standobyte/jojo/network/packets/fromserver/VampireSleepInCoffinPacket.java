package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class VampireSleepInCoffinPacket {
    private final boolean isRespawning;
    
    public VampireSleepInCoffinPacket(boolean isRespawning) {
        this.isRespawning = isRespawning;
    }
    
    
    
    public static class Handler implements IModPacketHandler<VampireSleepInCoffinPacket> {

        @Override
        public void encode(VampireSleepInCoffinPacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.isRespawning);
        }

        @Override
        public VampireSleepInCoffinPacket decode(PacketBuffer buf) {
            boolean isRespawning = buf.readBoolean();
            return new VampireSleepInCoffinPacket(isRespawning);
        }

        @Override
        public void handle(VampireSleepInCoffinPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ClientUtil.getClientPlayer();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(playerData -> {
                playerData.onSleepingInCoffin(msg.isRespawning);
            });
        }

        @Override
        public Class<VampireSleepInCoffinPacket> getPacketClass() {
            return VampireSleepInCoffinPacket.class;
        }
    }
}
