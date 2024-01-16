package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.command.configpack.standassign.PlayerStandAssignmentConfig;
import com.github.standobyte.jojo.command.configpack.standassign.StandAssignmentEntry;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class StandAssignmentDataPacket {
    public final Optional<List<StandType<?>>> stands;
    
    public StandAssignmentDataPacket(@Nullable StandAssignmentEntry configListEntry) {
        this(configListEntry != null ? Optional.ofNullable(configListEntry.getAssignedStands()) : Optional.empty());
    }
    
    public StandAssignmentDataPacket(Optional<List<StandType<?>>> stands) {
        this.stands = stands;
    }
    
    
    
    public static class Handler implements IModPacketHandler<StandAssignmentDataPacket> {

        @Override
        public void encode(StandAssignmentDataPacket msg, PacketBuffer buf) {
            NetworkUtil.writeOptional(buf, msg.stands, 
                    list -> NetworkUtil.writeCollection(buf, list, stand -> buf.writeRegistryId(stand), false));
        }

        @Override
        public StandAssignmentDataPacket decode(PacketBuffer buf) {
            return new StandAssignmentDataPacket(NetworkUtil.readOptional(buf, 
                    () -> NetworkUtil.readCollection(ArrayList::new, buf, () -> buf.readRegistryIdSafe(StandType.class))));
        }

        @Override
        public void handle(StandAssignmentDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerStandAssignmentConfig.getInstance().handleClientPacket(msg);
        }

        @Override
        public Class<StandAssignmentDataPacket> getPacketClass() {
            return StandAssignmentDataPacket.class;
        }
    }
}
