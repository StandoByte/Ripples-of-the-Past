package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModParticles;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class BloodParticlesPacket {
    private final Vector3d posSource;
    private final Vector3d posDest;
    private final int count;

    public BloodParticlesPacket(Vector3d posSource, Vector3d posDest, int count) {
        this.posSource = posSource;
        this.posDest = posDest;
        this.count = count;
    }

    public static void encode(BloodParticlesPacket msg, PacketBuffer buf) {
        buf.writeDouble(msg.posSource.x);
        buf.writeDouble(msg.posSource.y);
        buf.writeDouble(msg.posSource.z);
        buf.writeDouble(msg.posDest.x);
        buf.writeDouble(msg.posDest.y);
        buf.writeDouble(msg.posDest.z);
        buf.writeVarInt(msg.count);
    }

    public static BloodParticlesPacket decode(PacketBuffer buf) {
        return new BloodParticlesPacket(
                new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()), 
                new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readVarInt());
    }

    public static void handle(BloodParticlesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Vector3d diff = msg.posDest.subtract(msg.posSource).normalize().scale(0.5);
            for (int i = 0; i < msg.count; i++) {
                ClientUtil.getClientWorld().addParticle(ModParticles.BLOOD.get(), 
                        msg.posSource.x, msg.posSource.y, msg.posSource.z, 
                        diff.x, diff.y, diff.z);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
