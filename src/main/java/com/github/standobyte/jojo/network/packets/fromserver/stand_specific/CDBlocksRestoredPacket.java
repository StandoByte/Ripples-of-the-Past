package com.github.standobyte.jojo.network.packets.fromserver.stand_specific;

import java.util.Collection;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.stand.StandUtil;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class CDBlocksRestoredPacket {
    private final Collection<BlockPos> positions;

    public CDBlocksRestoredPacket(Collection<BlockPos> positions) {
        this.positions = positions;
    }

    public static void encode(CDBlocksRestoredPacket msg, PacketBuffer buf) {
        NetworkUtil.writeCollection(buf, msg.positions, (buffer, pos) -> buffer.writeBlockPos(pos), false);
    }

    public static CDBlocksRestoredPacket decode(PacketBuffer buf) {
        return new CDBlocksRestoredPacket(NetworkUtil.readCollection(buf, buffer -> buffer.readBlockPos()));
    }

    public static void handle(CDBlocksRestoredPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // FIXME do not send these packets to non-stand users at all
            if (StandUtil.shouldStandsRender(ClientUtil.getClientPlayer())) {
                World world = ClientUtil.getClientWorld();
                msg.positions.forEach(pos -> CrazyDiamondRestoreTerrain.addParticlesAroundBlock(world, pos, world.getRandom()));
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
