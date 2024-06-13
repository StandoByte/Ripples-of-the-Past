package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.general.OptionalFloat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonWallClimbingPacket {
    private final int userId;
    private final boolean wallClimbing;
    private final boolean hamon;
    private final float climbSpeed;
    private final OptionalFloat bodyRot;
    
    public TrHamonWallClimbingPacket(int userId, boolean wallClimbing, boolean hamon, float climbSpeed, OptionalFloat bodyRot) {
        this.userId = userId;
        this.wallClimbing = wallClimbing;
        this.hamon = hamon;
        this.climbSpeed = climbSpeed;
        this.bodyRot = bodyRot;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonWallClimbingPacket> {

        @Override
        public void encode(TrHamonWallClimbingPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.userId);
            buf.writeBoolean(msg.wallClimbing);
            buf.writeBoolean(msg.hamon);
            buf.writeFloat(msg.climbSpeed);
            buf.writeBoolean(msg.bodyRot.isPresent());
            if (msg.bodyRot.isPresent()) {
                buf.writeFloat(msg.bodyRot.getAsFloat());
            }
        }

        @Override
        public TrHamonWallClimbingPacket decode(PacketBuffer buf) {
            return new TrHamonWallClimbingPacket(buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readFloat(), 
                    buf.readBoolean() ? OptionalFloat.of(buf.readFloat()) : OptionalFloat.empty());
        }

        @Override
        public void handle(TrHamonWallClimbingPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.userId);
            if (entity instanceof PlayerEntity) {
                entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.setWallClimbing(msg.wallClimbing, msg.hamon, msg.climbSpeed, msg.bodyRot);
                });
            }
        }

        @Override
        public Class<TrHamonWallClimbingPacket> getPacketClass() {
            return TrHamonWallClimbingPacket.class;
        }
    }
}
