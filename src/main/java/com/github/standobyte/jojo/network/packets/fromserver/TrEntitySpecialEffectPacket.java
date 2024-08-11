package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class TrEntitySpecialEffectPacket {
    private final int entityId;
    private final SoundEvent sound;
    private final int playerId;
    
    public TrEntitySpecialEffectPacket(int entityId, SoundEvent sound, int playerId) {
        this.entityId = entityId;
        this.sound = sound;
        this.playerId = playerId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrEntitySpecialEffectPacket> {
    
        public void encode(TrEntitySpecialEffectPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS, msg.sound);
            buf.writeInt(msg.playerId);
        }
        
        public TrEntitySpecialEffectPacket decode(PacketBuffer buf) {
            return new TrEntitySpecialEffectPacket(buf.readInt(), buf.readRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS), buf.readInt());
        }
    
        public void handle(TrEntitySpecialEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
                Entity trigerringPlayer = ClientUtil.getEntityById(msg.playerId);
                if (trigerringPlayer == ClientUtil.getClientPlayer()) {
                    ClientUtil.playMusic(msg.sound, 1.0F, 1.0F);
                }
                CustomParticlesHelper.addMenacingParticleEmitter(entity, ModParticles.MENACING.get());
            }
        }

        @Override
        public Class<TrEntitySpecialEffectPacket> getPacketClass() {
            return TrEntitySpecialEffectPacket.class;
        }
    }
}
