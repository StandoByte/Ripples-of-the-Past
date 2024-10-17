package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class BloodParticlesPacket {
    private final Vector3d posSource;
    private final Optional<Vector3d> posDest;
    private final float speed;
    private final int count;
    private final int entityId;

    public BloodParticlesPacket(Vector3d posSource, float speed, int count, int entityId) {
        this(posSource, Optional.empty(), speed, count, entityId);
    }

    public BloodParticlesPacket(Vector3d posSource, Vector3d posDest, float speed, int count, int entityId) {
        this(posSource, Optional.of(posDest), speed, count, entityId);
    }

    public BloodParticlesPacket(Vector3d posSource, Optional<Vector3d> posDest, float speed, int count, int entityId) {
        this.posSource = posSource;
        this.posDest = posDest;
        this.speed = speed;
        this.count = count;
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<BloodParticlesPacket> {

        @Override
        public void encode(BloodParticlesPacket msg, PacketBuffer buf) {
            buf.writeDouble(msg.posSource.x);
            buf.writeDouble(msg.posSource.y);
            buf.writeDouble(msg.posSource.z);
            NetworkUtil.writeOptional(buf, msg.posDest, (vec, buffer) -> {
                buffer.writeDouble(vec.x);
                buffer.writeDouble(vec.y);
                buffer.writeDouble(vec.z);
            });
            buf.writeFloat(msg.speed);
            buf.writeVarInt(msg.count);
            buf.writeInt(msg.entityId);
        }

        @Override
        public BloodParticlesPacket decode(PacketBuffer buf) {
            return new BloodParticlesPacket(
                    new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()), 
                    NetworkUtil.readOptional(buf, vec -> new Vector3d(
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble())
                            ),
                    buf.readFloat(), buf.readVarInt(), buf.readInt());
        }

        private static final Random RANDOM = new Random();
        @Override
        public void handle(BloodParticlesPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Optional<Vector3d> diff = msg.posDest.map(vec -> vec.subtract(msg.posSource).normalize().scale(msg.speed));
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            for (int i = 0; i < msg.count; i++) {
                Vector3d speedVec = diff.orElseGet(() -> {
                    float xRot = (RANDOM.nextFloat() - 0.5f) * (float) Math.PI;
                    float yRot = RANDOM.nextFloat() * (float) Math.PI * 2;
                    return MathUtil.vecFromAngles(xRot, yRot).scale(msg.speed);
                });
                CustomParticlesHelper.createBloodParticle(ModParticles.BLOOD.get(), entity, 
                        msg.posSource.x, msg.posSource.y, msg.posSource.z, 
                        speedVec.x, speedVec.y, speedVec.z);
            }
        }

        @Override
        public Class<BloodParticlesPacket> getPacketClass() {
            return BloodParticlesPacket.class;
        }
    }

}
