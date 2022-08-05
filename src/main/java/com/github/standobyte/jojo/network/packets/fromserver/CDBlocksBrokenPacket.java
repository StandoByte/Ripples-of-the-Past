package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.stand.effect.CrazyDiamondRestorableBlocks;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class CDBlocksBrokenPacket {
    private final List<BlockInfo> blocks;

    public CDBlocksBrokenPacket(List<BlockInfo> blocks) {
        this.blocks = blocks;
    }

    public static void encode(CDBlocksBrokenPacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.blocks.size());
        msg.blocks.forEach(block -> block.encode(buf));
    }

    public static CDBlocksBrokenPacket decode(PacketBuffer buf) {
        int size = buf.readVarInt();
        List<BlockInfo> blocks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            blocks.add(BlockInfo.decode(buf));
        }
        return new CDBlocksBrokenPacket(blocks);
    }

    public static void handle(CDBlocksBrokenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                World world = ClientUtil.getClientWorld();
                msg.blocks.forEach(block -> {
                    BlockState state = block.state;
                    if (state != Blocks.AIR.defaultBlockState()) {
                        CrazyDiamondRestorableBlocks.getRestorableBlocksEffect(power, world)
                        .addBlock(world, block.pos, block.state, null, block.keep);
                    }
                    else {
                        CrazyDiamondRestorableBlocks.getRestorableBlocksEffect(power, world)
                        .removeBlock(world, block.pos);
                    }
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }

    public static class BlockInfo {
        private final BlockPos pos;
        private final BlockState state;
        private final boolean keep;

        public BlockInfo(BlockPos pos, BlockState state, boolean keep) {
            this.pos = pos;
            this.state = state;
            this.keep = keep;
        }

        private void encode(PacketBuffer buf) {
            buf.writeBlockPos(pos);
            buf.writeVarInt(Block.getId(state));
            buf.writeBoolean(keep);
        }

        private static BlockInfo decode(PacketBuffer buf) {
            return new BlockInfo(buf.readBlockPos(), Block.stateById(buf.readVarInt()), buf.readBoolean());
        }
    }
}
