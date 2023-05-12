package com.github.standobyte.jojo.network.packets.fromserver.ability_specific;

import java.util.Collection;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class CDBlocksRestoredPacket {
    private final Collection<BlockPos> positions;

    public CDBlocksRestoredPacket(Collection<BlockPos> positions) {
        this.positions = positions;
    }
    
    
    
    public static class Handler implements IModPacketHandler<CDBlocksRestoredPacket> {

        @Override
        public void encode(CDBlocksRestoredPacket msg, PacketBuffer buf) {
            NetworkUtil.writeCollection(buf, msg.positions, pos -> buf.writeBlockPos(pos), false);
        }

        @Override
        public CDBlocksRestoredPacket decode(PacketBuffer buf) {
            return new CDBlocksRestoredPacket(NetworkUtil.readCollection(buf, () -> buf.readBlockPos()));
        }

        @Override
        public void handle(CDBlocksRestoredPacket msg, Supplier<Context> ctx) {
            // FIXME do not send these packets to non-stand users at all
            if (ClientUtil.canSeeStands()) {
                World world = ClientUtil.getClientWorld();
                msg.positions.forEach(pos -> CrazyDiamondRestoreTerrain.addParticlesAroundBlock(world, pos, world.getRandom()));
            }
        }

        @Override
        public Class<CDBlocksRestoredPacket> getPacketClass() {
            return CDBlocksRestoredPacket.class;
        }
    }
}
