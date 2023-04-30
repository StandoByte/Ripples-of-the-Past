package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonPowerType;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonParticlesPacket {
    private final int entityId;
    private final float intensity;
    private final float soundVolumeMultiplier;
    private final IParticleData particleType;

    public TrHamonParticlesPacket(int entityId, float intensity, float soundVolumeMultiplier, @Nullable IParticleData particleType) {
        this.entityId = entityId;
        this.intensity = intensity;
        this.soundVolumeMultiplier = soundVolumeMultiplier;
        this.particleType = particleType;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonParticlesPacket> {

        @Override
        public void encode(TrHamonParticlesPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeFloat(msg.intensity);
            buf.writeFloat(msg.soundVolumeMultiplier);
    
            NetworkUtil.writeOptionally(buf, msg.particleType, (buffer, particle) -> writeParticle(particle, buffer));
        }

        @Override
        public TrHamonParticlesPacket decode(PacketBuffer buf) {
            return new TrHamonParticlesPacket(buf.readInt(), buf.readFloat(), buf.readFloat(), 
                    NetworkUtil.readOptional(buf, buffer -> readParticle(buffer).orElse(ModParticles.HAMON_SPARK.get())).orElse(ModParticles.HAMON_SPARK.get()));
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
                HamonPowerType.createHamonSparkParticlesEmitter(entity, msg.intensity, msg.soundVolumeMultiplier, msg.particleType);
            }
        }

        @Override
        public Class<TrHamonParticlesPacket> getPacketClass() {
            return TrHamonParticlesPacket.class;
        }
    }
}
