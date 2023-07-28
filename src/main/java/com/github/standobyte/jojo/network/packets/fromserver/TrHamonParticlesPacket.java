package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonParticlesPacket {
    private final Type type;
    private final int entityId;
    private final float soundVolume;
    
    // fields for emitter
    private final float intensity;
    private final IParticleData particleType;
    
    // fields for short sparks
    private final Vector3d pos;
    private final boolean followEntity;
    private final int count;
    
    
    
    public static TrHamonParticlesPacket emitter(int entityId, float intensity, float soundVolumeMultiplier, @Nullable IParticleData particleType) {
        return new TrHamonParticlesPacket(Type.EMITTER, entityId, intensity, soundVolumeMultiplier, particleType, null, false, -1);
    }
    
    public static TrHamonParticlesPacket shortSpark(int entityId, Vector3d pos, boolean followEntity, int particleCount, float soundVolume) {
        return new TrHamonParticlesPacket(Type.SHORT_SPARK, entityId, -1, soundVolume, null, pos, followEntity, particleCount);
    }
    
    private TrHamonParticlesPacket(Type type,
            int entityId, float intensity, float soundVolume, @Nullable IParticleData particleType,
            Vector3d pos, boolean followEntity, int count) {
        this.type = type;
        this.entityId = entityId;
        this.soundVolume = soundVolume;
        this.intensity = intensity;
        this.particleType = particleType;
        this.pos = pos;
        this.followEntity = followEntity;
        this.count = count;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonParticlesPacket> {

        @Override
        public void encode(TrHamonParticlesPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.type);
            buf.writeInt(msg.entityId);
            switch (msg.type) {
            case EMITTER:
                buf.writeFloat(msg.intensity);
                buf.writeFloat(msg.soundVolume);
                NetworkUtil.writeOptionally(buf, msg.particleType, particle -> writeParticle(particle, buf));
                break;
            case SHORT_SPARK:
                NetworkUtil.writeVecApproximate(buf, msg.pos);
                buf.writeBoolean(msg.followEntity);
                buf.writeVarInt(msg.count);
                buf.writeFloat(msg.soundVolume);
                break;
            }
        }

        @Override
        public TrHamonParticlesPacket decode(PacketBuffer buf) {
            Type type = buf.readEnum(Type.class);
            int entityId = buf.readInt();
            switch (type) {
            case EMITTER:
                return emitter(entityId, buf.readFloat(), buf.readFloat(), 
                        NetworkUtil.readOptional(buf, () -> readParticle(buf).orElse(ModParticles.HAMON_SPARK.get())).orElse(ModParticles.HAMON_SPARK.get()));
            case SHORT_SPARK:
                return shortSpark(entityId, NetworkUtil.readVecApproximate(buf), buf.readBoolean(), buf.readVarInt(), buf.readFloat());
            default:
                throw new NoSuchElementException();
            }
        }
        
        private void writeParticle(IParticleData particleData, PacketBuffer buf) {
            buf.writeInt(Registry.PARTICLE_TYPE.getId(particleData.getType()));
            particleData.writeToNetwork(buf);
        }
        
        private Optional<IParticleData> readParticle(PacketBuffer buf) {
            ParticleType<?> particleType = Registry.PARTICLE_TYPE.byId(buf.readInt());
            if (particleType == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(readParticle(buf, particleType));
        }
    
        private <T extends IParticleData> T readParticle(PacketBuffer buf, ParticleType<T> type) {
            return type.getDeserializer().fromNetwork(type, buf);
        }

        @Override
        public void handle(TrHamonParticlesPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
                switch (msg.type) {
                case EMITTER:
                    HamonUtil.createHamonSparkParticlesEmitter(entity, msg.intensity, msg.soundVolume, msg.particleType);
                    break;
                case SHORT_SPARK:
                    HamonSparksLoopSound.playSparkSound(entity, msg.pos, msg.soundVolume);
                    CustomParticlesHelper.createHamonSparkParticles(msg.followEntity ? entity : null, msg.pos.x, msg.pos.y, msg.pos.z, msg.count);
                    break;
                }
            }
        }

        @Override
        public Class<TrHamonParticlesPacket> getPacketClass() {
            return TrHamonParticlesPacket.class;
        }
    }
    
    private enum Type {
        EMITTER,
        SHORT_SPARK
    }
}
