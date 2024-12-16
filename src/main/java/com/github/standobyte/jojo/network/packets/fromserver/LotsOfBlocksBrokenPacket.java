package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.TriConsumer;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.google.common.collect.Streams;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class LotsOfBlocksBrokenPacket {
    public final List<BrokenBlock> brokenBlocks;
    
    public LotsOfBlocksBrokenPacket() {
        this(new ArrayList<>());
    }
    
    private LotsOfBlocksBrokenPacket(List<BrokenBlock> brokenBlocks) {
        this.brokenBlocks = brokenBlocks;
    }
    
    public void addBlock(BlockPos blockPos, BlockState blockState) {
        brokenBlocks.add(new BrokenBlock(blockPos, blockState));
    }
    
    
    private static class BrokenBlock {
        private final BlockPos blockPos;
        private final int blockStateData;
        private BlockState blockState;
        
        private BrokenBlock(BlockPos blockPos, int data) {
            this.blockPos = blockPos;
            this.blockStateData = data;
        }
        
        private BrokenBlock(BlockPos blockPos, BlockState blockState) {
            this(blockPos, Block.getId(blockState));
            this.blockState = blockState;
        }
        
        void toBuf(PacketBuffer buffer) {
            buffer.writeBlockPos(this.blockPos);
            buffer.writeInt(this.blockStateData);
        }
        
        static BrokenBlock fromBuf(PacketBuffer buffer) {
            BlockPos blockPos = buffer.readBlockPos();
            int data = buffer.readInt();
            return new BrokenBlock(blockPos, data);
        }
        
        void handleResolveBlockState() {
            this.blockState = Block.stateById(blockStateData);
        }
    }
    
    public void forEachBlock(boolean network, TriConsumer<BlockPos, BlockState, Long> action) {
        Stream<BrokenBlock> stream = brokenBlocks.stream();
        if (brokenBlocks.size() > 128) {
            Vector3d cameraPos = ClientUtil.getCameraPos();
            stream = stream
                    .sorted(Comparator.comparingDouble(block -> block.blockPos.distSqr(cameraPos.x, cameraPos.y, cameraPos.z, true)))
                    .limit(128);
        }
        Streams.mapWithIndex(stream, (block, index) -> {
            if (network) {
                block.handleResolveBlockState();
            }
            action.accept(block.blockPos, block.blockState, index);
            return block;
        }).forEach(block -> {});
    }
    
    public static void blockBreakVisuals(BlockPos blockPos, BlockState blockState, long i) {
        World world = ClientUtil.getClientWorld();
        if (!blockState.isAir(world, blockPos)) {
            CustomParticlesHelper.addBlockBreakParticles(blockPos, blockState);
            SoundType soundType = blockState.getSoundType(world, blockPos, null);
            if (i % 8 == 0) {
                world.playLocalSound(
                        blockPos.getX() + 0.5, 
                        blockPos.getY() + 0.5, 
                        blockPos.getZ() + 0.5, 
                        soundType.getBreakSound(), SoundCategory.BLOCKS, 
                        (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
            }
        }
    }
    
    
    
    public static class Handler implements IModPacketHandler<LotsOfBlocksBrokenPacket> {

        @Override
        public void encode(LotsOfBlocksBrokenPacket msg, PacketBuffer buf) {
            NetworkUtil.writeCollection(buf, msg.brokenBlocks, BrokenBlock::toBuf, false);
        }

        @Override
        public LotsOfBlocksBrokenPacket decode(PacketBuffer buf) {
            return new LotsOfBlocksBrokenPacket(NetworkUtil.readCollection(buf, BrokenBlock::fromBuf));
        }

        @Override
        public void handle(LotsOfBlocksBrokenPacket msg, Supplier<NetworkEvent.Context> ctx) {
            msg.forEachBlock(true, LotsOfBlocksBrokenPacket::blockBreakVisuals);
        }

        @Override
        public Class<LotsOfBlocksBrokenPacket> getPacketClass() {
            return LotsOfBlocksBrokenPacket.class;
        }
    }
}
