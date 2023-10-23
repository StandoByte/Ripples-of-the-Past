package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClSetStandSkinPacket {
    private final Optional<ResourceLocation> standSkin;
    
    public ClSetStandSkinPacket(Optional<ResourceLocation> standSkin) {
        this.standSkin = standSkin;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClSetStandSkinPacket> {
    
        @Override
        public void encode(ClSetStandSkinPacket msg, PacketBuffer buf) {
            NetworkUtil.writeOptional(buf, msg.standSkin, buf::writeResourceLocation);
        }

        @Override
        public ClSetStandSkinPacket decode(PacketBuffer buf) {
            return new ClSetStandSkinPacket(NetworkUtil.readOptional(buf, buf::readResourceLocation));
        }

        @Override
        public void handle(ClSetStandSkinPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                power.getStandInstance().ifPresent(stand -> stand.setCustomSkin(msg.standSkin, power));
            });
        }

        @Override
        public Class<ClSetStandSkinPacket> getPacketClass() {
            return ClSetStandSkinPacket.class;
        }
    }

}
