package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.UUID;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.polaroid.PhotosCache;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PhotoIdAssignedPacket {
    private final UUID photoSendId;
    private final long photoFinalId;
    private final boolean saveToFile;
    
    public PhotoIdAssignedPacket(UUID photoSendId, long photoFinalId, boolean saveToFile) {
        this.photoSendId = photoSendId;
        this.photoFinalId = photoFinalId;
        this.saveToFile = saveToFile;
    }
    
    
    
    public static class Handler implements IModPacketHandler<PhotoIdAssignedPacket> {

        @Override
        public void encode(PhotoIdAssignedPacket msg, PacketBuffer buf) {
            buf.writeUUID(msg.photoSendId);
            buf.writeLong(msg.photoFinalId);
            buf.writeBoolean(msg.saveToFile);
        }

        @Override
        public PhotoIdAssignedPacket decode(PacketBuffer buf) {
            UUID photoSendId = buf.readUUID();
            long photoFinalId = buf.readLong();
            boolean saveToFile = buf.readBoolean();
            return new PhotoIdAssignedPacket(photoSendId, photoFinalId, saveToFile);
        }

        @Override
        public void handle(PhotoIdAssignedPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PhotosCache.assignImageId(ClientUtil.getServerUUID(), msg.photoFinalId, msg.photoSendId, msg.saveToFile);
        }

        @Override
        public Class<PhotoIdAssignedPacket> getPacketClass() {
            return PhotoIdAssignedPacket.class;
        }
    }
}
