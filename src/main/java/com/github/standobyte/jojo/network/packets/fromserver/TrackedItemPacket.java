package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.itemtracking.SidedItemTrackerMap;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrackedItemPacket {
    private final UUID trackerId;
    private final ItemStack itemStack;
    private final OptionalInt entityId;
    private final Optional<BlockPos> blockPos;
    
    public TrackedItemPacket(UUID trackerId, ItemStack itemStack, OptionalInt entityId, Optional<BlockPos> blockPos) {
        this.trackerId = trackerId;
        this.itemStack = itemStack;
        this.entityId = entityId;
        this.blockPos = blockPos;
    }
    
//    public static TrackedItemPacket entity(UUID trackerId, int entityId) {
//        return new TrackedItemPacket(trackerId, OptionalInt.of(entityId), Optional.empty());
//    }
//    
//    public static TrackedItemPacket blockPos(UUID trackerId, BlockPos blockPos) {
//        return new TrackedItemPacket(trackerId, OptionalInt.empty(), Optional.of(blockPos));
//    }
//    
//    public static TrackedItemPacket unknown(UUID trackerId) {
//        return new TrackedItemPacket(trackerId, OptionalInt.empty(), Optional.empty());
//    }
    
    
    
    public static class Handler implements IModPacketHandler<TrackedItemPacket> {

        @Override
        public void encode(TrackedItemPacket msg, PacketBuffer buf) {
            buf.writeUUID(msg.trackerId);
            buf.writeItem(msg.itemStack);
            NetworkUtil.writeOptionalInt(buf, msg.entityId, false);
            NetworkUtil.writeOptional(buf, msg.blockPos, buf::writeBlockPos);
        }

        @Override
        public TrackedItemPacket decode(PacketBuffer buf) {
            TrackedItemPacket packet = new TrackedItemPacket(
                    buf.readUUID(), 
                    buf.readItem(),
                    NetworkUtil.readOptionalInt(buf, false),
                    NetworkUtil.readOptional(buf, buf::readBlockPos));
            return packet;
        }

        @Override
        public void handle(TrackedItemPacket msg, Supplier<NetworkEvent.Context> ctx) {
            SidedItemTrackerMap trackerMap = ClientUtil.clientTrackedItems;
            if (msg.entityId.isPresent()) {
                TrackerItemStack tracker = new TrackerItemStack(msg.itemStack, msg.trackerId);
                tracker.setAtEntity(msg.entityId.getAsInt(), ClientUtil.getClientWorld(), null);
                trackerMap.updateTracker(msg.trackerId, tracker, ClientUtil.getClientWorld());
            }
            else if (msg.blockPos.isPresent()) {
                TrackerItemStack tracker = new TrackerItemStack(msg.itemStack, msg.trackerId);
                tracker.setAtBlockPos(msg.blockPos.get(), ClientUtil.getClientWorld(), null);
                trackerMap.updateTracker(msg.trackerId, tracker, ClientUtil.getClientWorld());
            }
            else {
                trackerMap.removeTracker(msg.trackerId);
            }
        }

        @Override
        public Class<TrackedItemPacket> getPacketClass() {
            return TrackedItemPacket.class;
        }
    }
}
