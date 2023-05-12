package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.hamon.skill.CharacterHamonTechnique;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonCharacterTechniquePacket {
    private final int entityId;
    private final Optional<CharacterHamonTechnique> technique;
    
    public static TrHamonCharacterTechniquePacket reset(int entityId) {
        return new TrHamonCharacterTechniquePacket(entityId, Optional.empty());
    }
    
    public TrHamonCharacterTechniquePacket(int entityId, Optional<CharacterHamonTechnique> technique) {
        this.entityId = entityId;
        this.technique = technique;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonCharacterTechniquePacket> {

        @Override
        public void encode(TrHamonCharacterTechniquePacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            NetworkUtil.writeOptional(buf, msg.technique, technique -> buf.writeRegistryId(technique));
        }

        @Override
        public TrHamonCharacterTechniquePacket decode(PacketBuffer buf) {
            return new TrHamonCharacterTechniquePacket(
                    buf.readInt(), 
                    NetworkUtil.readOptional(buf, () -> buf.readRegistryIdSafe(CharacterHamonTechnique.class)));
        }

        @Override
        public void handle(TrHamonCharacterTechniquePacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                LivingEntity user = (LivingEntity) entity;
                INonStandPower.getNonStandPowerOptional(user).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.getTechniqueData().setCharacterTechnique(msg.technique);
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