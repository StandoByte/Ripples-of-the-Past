package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonCharacterTechniquePacket {
    private final int entityId;
    private final Optional<CharacterHamonTechnique> technique;
    private final boolean playPickSound;
    
    public static TrHamonCharacterTechniquePacket reset(int entityId) {
        return new TrHamonCharacterTechniquePacket(entityId, Optional.empty(), false);
    }
    
    public TrHamonCharacterTechniquePacket(int entityId, CharacterHamonTechnique technique) {
        this(entityId, technique, false);
    }
    
    public TrHamonCharacterTechniquePacket(int entityId, CharacterHamonTechnique technique, boolean playPickSound) {
        this(entityId, Optional.of(technique), playPickSound);
    }
    
    private TrHamonCharacterTechniquePacket(int entityId, Optional<CharacterHamonTechnique> technique, boolean playPickSound) {
        this.entityId = entityId;
        this.technique = technique;
        this.playPickSound = playPickSound;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonCharacterTechniquePacket> {

        @Override
        public void encode(TrHamonCharacterTechniquePacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            NetworkUtil.writeOptional(buf, msg.technique, technique -> buf.writeRegistryId(technique));
            buf.writeBoolean(msg.playPickSound);
        }

        @Override
        public TrHamonCharacterTechniquePacket decode(PacketBuffer buf) {
            return new TrHamonCharacterTechniquePacket(
                    buf.readInt(), 
                    NetworkUtil.readOptional(buf, () -> buf.readRegistryIdSafe(CharacterHamonTechnique.class)), 
                    buf.readBoolean());
        }

        @Override
        public void handle(TrHamonCharacterTechniquePacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                LivingEntity user = (LivingEntity) entity;
                INonStandPower.getNonStandPowerOptional(user).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        if (msg.technique.isPresent()) {
                            hamon.pickHamonTechnique(user, msg.technique.get());
                            if (msg.playPickSound && user.is(ClientUtil.getClientPlayer())) {
                                SoundEvent techniquePickMusic = msg.technique.get().getMusicOnPick();
                                if (techniquePickMusic != null) {
                                    ClientUtil.playMusic(techniquePickMusic, 1.0F, 1.0F);
                                }
                            }
                        }
                        else {
                            hamon.resetCharacterTechnique(user);
                        }
                        if (user.is(ClientUtil.getClientPlayer())) {
                            HamonScreen.updateTabs();
                        }
                    });
                });
            }
        }

        @Override
        public Class<TrHamonCharacterTechniquePacket> getPacketClass() {
            return TrHamonCharacterTechniquePacket.class;
        }
    }
}