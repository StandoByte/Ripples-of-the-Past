package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHamonInteractAskTeacherPacket {
    private final int entityId;
    
    public ClHamonInteractAskTeacherPacket(int entityId) {
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonInteractAskTeacherPacket> {
    
        @Override
        public void encode(ClHamonInteractAskTeacherPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public ClHamonInteractAskTeacherPacket decode(PacketBuffer buf) {
            return new ClHamonInteractAskTeacherPacket(buf.readInt());
        }

        @Override
        public void handle(ClHamonInteractAskTeacherPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            Entity targetEntity = player.level.getEntity(msg.entityId);
            HamonUtil.interactWithHamonTeacher(player.level, player, targetEntity);
        }

        @Override
        public Class<ClHamonInteractAskTeacherPacket> getPacketClass() {
            return ClHamonInteractAskTeacherPacket.class;
        }
    }
}
