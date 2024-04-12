package com.github.standobyte.jojo.network.packets.fromserver.ability_specific;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class GEUiDataPacket {
    @Deprecated
    private final Collection<ResourceLocation> hidden;
    private final Collection<ResourceLocation> favorites;
    private final Collection<ResourceLocation> newMobs;
    private final Optional<EntityType<?>> selected;
    
    public GEUiDataPacket(Collection<ResourceLocation> hidden, 
            Collection<ResourceLocation> favorites, Collection<ResourceLocation> newMobs, 
            Optional<EntityType<?>> selected) {
        this.hidden = hidden;
        this.favorites = favorites;
        this.newMobs = newMobs;
        this.selected = selected;
    }
    
    
    
    public static class Handler implements IModPacketHandler<GEUiDataPacket> {

        @Override
        public void encode(GEUiDataPacket msg, PacketBuffer buf) {
            NetworkUtil.writeCollection(buf, msg.hidden, id -> buf.writeResourceLocation(id), false);
            NetworkUtil.writeCollection(buf, msg.favorites, id -> buf.writeResourceLocation(id), false);
            NetworkUtil.writeCollection(buf, msg.newMobs, id -> buf.writeResourceLocation(id), false);
            NetworkUtil.writeOptional(buf, msg.selected, type -> buf.writeRegistryId(type));
        }

        @Override
        public GEUiDataPacket decode(PacketBuffer buf) {
            return new GEUiDataPacket(
                    NetworkUtil.readCollection(buf, () -> buf.readResourceLocation()),
                    NetworkUtil.readCollection(buf, () -> buf.readResourceLocation()),
                    NetworkUtil.readCollection(buf, () -> buf.readResourceLocation()),
                    NetworkUtil.readOptional(buf, () -> buf.readRegistryIdSafe(EntityType.class)));
        }

        @Override
        public void handle(GEUiDataPacket msg, Supplier<Context> ctx) {
            PlayerEntity player = ClientUtil.getClientPlayer();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.setGEHiddenLifeforms(msg.hidden);
                cap.setGELifeformFavs(msg.favorites);
                cap.setGELifeformsNew(msg.newMobs);
                cap.setGEChosenLifeformType(msg.selected.orElse(null), false);
            });
        }

        @Override
        public Class<GEUiDataPacket> getPacketClass() {
            return GEUiDataPacket.class;
        }
    }
}
