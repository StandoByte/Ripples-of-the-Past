package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClGEUiDataPacket {
    private final Type type;
    private final Optional<EntityType<?>> entityType;
    
    public static ClGEUiDataPacket chosenEntityType(Optional<EntityType<?>> entityType) {
        return new ClGEUiDataPacket(Type.CHOSEN_ENTITY_TYPE, entityType);
    }
    
    public static ClGEUiDataPacket hiddenEntry(EntityType<?> entityType) {
        return new ClGEUiDataPacket(Type.HIDDEN_ENTRY, Optional.of(entityType));
    }
    
    public static ClGEUiDataPacket shownEntry(EntityType<?> entityType) {
        return new ClGEUiDataPacket(Type.SHOWN_ENTRY, Optional.of(entityType));
    }
    
    private ClGEUiDataPacket(Type type, Optional<EntityType<?>> entityType) {
        this.type = type;
        this.entityType = entityType;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClGEUiDataPacket> {

        @Override
        public void encode(ClGEUiDataPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.type);
            NetworkUtil.writeOptional(buf, msg.entityType, entityType -> buf.writeRegistryId(entityType));
        }

        @Override
        public ClGEUiDataPacket decode(PacketBuffer buf) {
            Type packetType = buf.readEnum(Type.class);
            Optional<EntityType<?>> entityType = NetworkUtil.readOptional(buf, () -> buf.readRegistryIdSafe(EntityType.class));
            return new ClGEUiDataPacket(packetType, entityType);
        }

        @Override
        public void handle(ClGEUiDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                switch (msg.type) {
                case CHOSEN_ENTITY_TYPE:
                    cap.setGEChosenLifeformType(msg.entityType.orElse(null), false);
                    break;
                case HIDDEN_ENTRY:
                    cap.hideGELifeform(msg.entityType.get());
                    break;
                case SHOWN_ENTRY:
                    cap.showGELifeform(msg.entityType.get());
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
        HIDDEN_ENTRY,
        SHOWN_ENTRY
    }

}
