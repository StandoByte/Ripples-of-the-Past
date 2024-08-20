package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.LifeformsUIState;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;
import com.github.standobyte.jojo.util.mc.entitysubtype.SubtypeResourceLocation;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class ClGEUiDataPacket {
    private final Type type;
    private final Optional<SubtypeResourceLocation> resLoc;
    
    public static ClGEUiDataPacket chosenEntityType(Optional<EntitySubtype<?>> entityType) {
        return new ClGEUiDataPacket(Type.CHOSEN_ENTITY_TYPE, entityType.map(EntitySubtype::getId));
    }
    
    public static ClGEUiDataPacket favoriteAdded(EntityType<?> entityType) {
        return new ClGEUiDataPacket(Type.FAVORITE_ADDED, Optional.of(entityType)
                .map(EntityType::getRegistryName).map(SubtypeResourceLocation::new));
    }

    public static ClGEUiDataPacket favoriteRemoved(EntityType<?> entityType) {
        return new ClGEUiDataPacket(Type.FAVORITE_REMOVED, Optional.of(entityType)
                .map(EntityType::getRegistryName).map(SubtypeResourceLocation::new));
    }

    public static ClGEUiDataPacket clearUnseen() {
        return new ClGEUiDataPacket(Type.CLEAR_UNSEEN, Optional.empty());
    }
    
    private ClGEUiDataPacket(Type type, Optional<SubtypeResourceLocation> resLoc) {
        this.type = type;
        this.resLoc = resLoc;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClGEUiDataPacket> {

        @Override
        public void encode(ClGEUiDataPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.type);
            NetworkUtil.writeOptional(buf, msg.resLoc, id -> buf.writeUtf(id.toString()));
        }

        @Override
        public ClGEUiDataPacket decode(PacketBuffer buf) {
            Type packetType = buf.readEnum(Type.class);
            Optional<SubtypeResourceLocation> id = NetworkUtil.readOptional(buf, () -> new SubtypeResourceLocation(buf.readUtf()));
            return new ClGEUiDataPacket(packetType, id);
        }

        @Override
        public void handle(ClGEUiDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                LifeformsUIState state = cap.getGELifeformsUIState();
                switch (msg.type) {
                case CHOSEN_ENTITY_TYPE:
                    EntitySubtype<?> chosenType = msg.resLoc.map(EntitySubtype::getSubtype).orElse(null);
                    state.setGEChosenLifeformType(chosenType, false);
                    break;
                case FAVORITE_ADDED:
                    msg.resLoc.ifPresent(id -> {
                        if (ForgeRegistries.ENTITIES.containsKey(id)) {
                            state.GELifeformAddFav(ForgeRegistries.ENTITIES.getValue(id));
                        }
                    });
                    break;
                case FAVORITE_REMOVED:
                    msg.resLoc.ifPresent(id -> {
                        if (ForgeRegistries.ENTITIES.containsKey(id)) {
                            state.GELifeformRemoveFav(ForgeRegistries.ENTITIES.getValue(id));
                        }
                    });
                    break;
                case CLEAR_UNSEEN:
                    state.clearGENewMobs();
                    break;
                }
            });
        }

        @Override
        public Class<ClGEUiDataPacket> getPacketClass() {
            return ClGEUiDataPacket.class;
        }
    }
    
    
    
    private enum Type {
        CHOSEN_ENTITY_TYPE,
        FAVORITE_ADDED,
        FAVORITE_REMOVED,
        CLEAR_UNSEEN
    }

}
