package com.github.standobyte.jojo.network.packets.fromclient;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClSetStandSkinPacket {
    private final Optional<ResourceLocation> standSkin;
    private final ResourceLocation standId;
    
    public ClSetStandSkinPacket(Optional<ResourceLocation> standSkin, ResourceLocation standId) {
        this.standSkin = standSkin;
        this.standId = standId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClSetStandSkinPacket> {
    
        @Override
        public void encode(ClSetStandSkinPacket msg, PacketBuffer buf) {
            NetworkUtil.writeOptional(buf, msg.standSkin, buf::writeResourceLocation);
            buf.writeResourceLocation(msg.standId);
        }

        @Override
        public ClSetStandSkinPacket decode(PacketBuffer buf) {
            return new ClSetStandSkinPacket(NetworkUtil.readOptional(buf, buf::readResourceLocation), 
                    buf.readResourceLocation());
        }

        @Override
        public void handle(ClSetStandSkinPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                if (power.hasPower() && msg.standId.equals(power.getType().getRegistryName())) {
                    power.getStandInstance().ifPresent(stand -> {
                        stand.setCustomSkin(msg.standSkin, power);
                        
                        
                        boolean isSingleplayer = player.server.isSingleplayer();
                        if (isSingleplayer) {
                            stand.syncIfDirty(player);
                            if (power.getStandManifestation() instanceof StandEntity) {
                                StandEntity standEntity = (StandEntity) power.getStandManifestation();
                                
                                SEntityMetadataPacket entityDataPacket = new SEntityMetadataPacket();
                                PacketBuffer data = new PacketBuffer(Unpooled.buffer());
                                data.writeVarInt(standEntity.getId());
                                DataParameter<Optional<ResourceLocation>> dataParameter = StandEntity.DATA_PARAM_STAND_SKIN;
                                int serializerId = DataSerializers.getSerializedId(dataParameter.getSerializer());
                                if (serializerId >= 0) {
                                    data.writeByte(dataParameter.getId());
                                    data.writeVarInt(serializerId);
                                    dataParameter.getSerializer().write(data, stand.getSelectedSkin());
                                    data.writeByte(255);
                                    try {
                                        entityDataPacket.read(data);
                                        player.connection.send(entityDataPacket);
                                    } catch (IOException e) {}
                                }
                            }
                        }
                    });
                }
            });
        }

        @Override
        public Class<ClSetStandSkinPacket> getPacketClass() {
            return ClSetStandSkinPacket.class;
        }
    }

}
