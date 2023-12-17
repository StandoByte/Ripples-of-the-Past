package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.WalkmanSoundHandler.CassetteSide;
import com.github.standobyte.jojo.container.WalkmanItemContainer;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.CassetteRecordedItem;
import com.github.standobyte.jojo.item.WalkmanItem;
import com.github.standobyte.jojo.item.WalkmanDataCap.PlaybackMode;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;

public class ClWalkmanControlsPacket {
    private final Type packetType;
    private final int walkmanId;
    private final float volume;
    private final CassetteSide side;
    private final int track;
    private final PlaybackMode mode;
    
    public static ClWalkmanControlsPacket setVolume(int walkmanId, float volume) {
        return new ClWalkmanControlsPacket(Type.VOLUME, walkmanId, volume, null, -1, null);
    }
    
    public static ClWalkmanControlsPacket cassettePosition(int walkmanId, CassetteSide side, int track) {
        return new ClWalkmanControlsPacket(Type.CASSETTE_POS, walkmanId, -1, side, track, null);
    }
    
    public static ClWalkmanControlsPacket playbackMode(int walkmanId, PlaybackMode mode) {
        return new ClWalkmanControlsPacket(Type.PLAYBACK_MODE, walkmanId, -1, null, -1, mode);
    }
    
    private ClWalkmanControlsPacket(Type packetType, int walkmanId, float volume, CassetteSide side, int track, PlaybackMode mode) {
        this.packetType = packetType;
        this.walkmanId = walkmanId;
        this.volume = volume;
        this.side = side;
        this.track = track;
        this.mode = mode;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClWalkmanControlsPacket> {

        @Override
        public void encode(ClWalkmanControlsPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.packetType);
            buf.writeInt(msg.walkmanId);
            switch (msg.packetType) {
            case VOLUME:
                buf.writeFloat(msg.volume);
                break;
            case CASSETTE_POS:
                buf.writeEnum(msg.side);
                buf.writeVarInt(msg.track);
                break;
            case PLAYBACK_MODE:
                buf.writeEnum(msg.mode);
                break;
            }
        }

        @Override
        public ClWalkmanControlsPacket decode(PacketBuffer buf) {
            Type packetType = buf.readEnum(Type.class);
            switch (packetType) {
            case VOLUME:
                return setVolume(buf.readInt(), buf.readFloat());
            case CASSETTE_POS:
                return cassettePosition(buf.readInt(), buf.readEnum(CassetteSide.class), buf.readVarInt());
            case PLAYBACK_MODE:
                return playbackMode(buf.readInt(), buf.readEnum(PlaybackMode.class));
            }
            throw new IllegalArgumentException();
        }

        @Override
        public void handle(ClWalkmanControlsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            findWalkman(player, msg.walkmanId).ifPresent(walkman -> {
                WalkmanItem.editWalkmanData(walkman, walkmanData -> {
                    switch (msg.packetType) {
                    case VOLUME:
                        if (!walkman.isEmpty() && walkman.getItem() == ModItems.WALKMAN.get()) {
                            walkmanData.setVolume(msg.volume);
                        }
                        break;
                    case CASSETTE_POS:
                        ItemStack cassette = walkman.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(
                                walkmanSlot -> walkmanSlot.getStackInSlot(0)).orElse(ItemStack.EMPTY);
                        if (!cassette.isEmpty() && cassette.getItem() == ModItems.CASSETTE_RECORDED.get()) {
                            CassetteRecordedItem.editCassetteData(cassette, cap -> {
                                cap.setSide(msg.side);
                                cap.setTrackOn(msg.track);
                            });
                        }
                        break;
                    case PLAYBACK_MODE:
                        if (!walkman.isEmpty() && walkman.getItem() == ModItems.WALKMAN.get()) {
                            walkmanData.setPlaybackMode(msg.mode);
                        }
                        break;
                    }
                });
            });
        }
        
        private Optional<ItemStack> findWalkman(ServerPlayerEntity player, int walkmanId) {
            if (player.containerMenu instanceof WalkmanItemContainer) {
                ItemStack walkmanOpen = ((WalkmanItemContainer) player.containerMenu).getWalkmanItem();
                if (checkId(walkmanOpen, walkmanId)) {
                    return Optional.of(walkmanOpen);
                }
            }
            for (int i = 0; i < player.inventory.getContainerSize(); i++) {
                ItemStack item = player.inventory.getItem(i);
                if (checkId(item, walkmanId)) {
                    return Optional.of(item);
                }
            }
            return Optional.empty();
        }
        
        private boolean checkId(ItemStack item, int id) {
            return WalkmanItem.getWalkmanData(item).map(walkman -> {
                return walkman.checkId(id);
            }).orElse(false);
        }

        @Override
        public Class<ClWalkmanControlsPacket> getPacketClass() {
            return ClWalkmanControlsPacket.class;
        }
    }

    
    
    private static enum Type {
        VOLUME,
        CASSETTE_POS,
        PLAYBACK_MODE
    }
}
