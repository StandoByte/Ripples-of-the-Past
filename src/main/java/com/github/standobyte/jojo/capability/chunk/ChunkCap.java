package com.github.standobyte.jojo.capability.chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.BrokenChunkBlocksPacket;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

// FIXME !!! (restore terrain) sync it to players on loading the chunk
// FIXME !!! (restore terrain) limit the storage
public class ChunkCap {
    private final Chunk chunk;
    
    private final Map<BlockPos, PrevBlockInfo> brokenBlocks = new HashMap<>();
    private final List<PrevBlockInfo> blocksToSync = new ArrayList<>();

    public ChunkCap(Chunk chunk) {
        this.chunk = chunk;
    }

    public void saveBrokenBlock(BlockPos pos, BlockState state, Optional<TileEntity> tileEntity, List<ItemStack> drops) {
        // FIXME remember blocks with inventory
        if (tileEntity.map(te -> te instanceof IInventory).orElse(false)) return;
        
        saveBrokenBlock(new PrevBlockInfo(pos, state, drops, false));
    }
    
    private void saveBrokenBlock(PrevBlockInfo prevBlock) {
        brokenBlocks.put(prevBlock.pos, prevBlock);
        if (!chunk.getLevel().isClientSide()) {
            blocksToSync.add(prevBlock);
        }
    }

    public void removeBrokenBlock(BlockPos blockPos) {
        brokenBlocks.remove(blockPos);
        if (!chunk.getLevel().isClientSide()) {
            blocksToSync.add(PrevBlockInfo.clientInstance(blockPos, Blocks.AIR.defaultBlockState()));
        }
    }

    // FIXME !!! (restore terrain) the packet size might be too large
    public void tick() {
        Iterator<Map.Entry<BlockPos, PrevBlockInfo>> it = brokenBlocks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, PrevBlockInfo> entry = it.next();
            if (entry.getValue().forget()) {
                it.remove();
                blocksToSync.add(PrevBlockInfo.clientInstance(entry.getKey(), Blocks.AIR.defaultBlockState()));
            }
        }
        
        if (!chunk.getLevel().isClientSide() && !blocksToSync.isEmpty()) {
            PacketManager.sendToTrackingChunk(new BrokenChunkBlocksPacket(blocksToSync), chunk);
        }
        blocksToSync.clear();
    }
    
    public Stream<PrevBlockInfo> getBrokenBlocks() {
        return brokenBlocks.values().stream();
    }
    
    
    CompoundNBT save() {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT blocksBroken = new ListNBT();
        for (PrevBlockInfo block : brokenBlocks.values()) {
            blocksBroken.add(block.toNBT());
        }
        nbt.put("Blocks", blocksBroken);
        return nbt;
    }
    
    void load(CompoundNBT nbt) {
        if (nbt.contains("Blocks", JojoModUtil.getNbtId(ListNBT.class))) {
            nbt.getList("Blocks", JojoModUtil.getNbtId(CompoundNBT.class)).forEach(blockNBT -> {
                PrevBlockInfo block = PrevBlockInfo.fromNBT((CompoundNBT) blockNBT);
                if (block != null) {
                    saveBrokenBlock(block);
                }
            });
        }
    }

    
    
    public static class PrevBlockInfo {
        public final BlockPos pos;
        public final BlockState state;
        public final List<ItemStack> drops;
        public final boolean keep;
        private int tickCount = 0;
        
        private PrevBlockInfo(BlockPos pos, BlockState state, List<ItemStack> drops, boolean keep) {
            this.pos = pos;
            this.state = state;
            this.drops = drops.stream().map(stack -> stack.copy()).collect(Collectors.toList());
            this.keep = keep;
        }
        
        public static PrevBlockInfo clientInstance(BlockPos pos, BlockState state) {
            return new PrevBlockInfo(pos, state, new ArrayList<>(), true);
        }
        
        private boolean forget() {
            return !keep && tickCount++ == 24000;
        }

        // FIXME !!! (restore terrain) nbt
        private CompoundNBT toNBT() {
            CompoundNBT nbt = new CompoundNBT();
//            nbt.put("Pos", pos);
//            nbt.put("State", state);
//            nbt.putBoolean("Keep", keep);
//            nbt.putInt("TickCount", tickCount);
//            CompoundNBT itemsNBT = new CompoundNBT();
//            int i = 0;
//            for (ItemStack stack : drops) {
//                itemsNBT.put(String.valueOf(i++), stack);
//            }
            return nbt;
        }

        // FIXME !!! (restore terrain) nbt
        @Nullable
        private static PrevBlockInfo fromNBT(CompoundNBT nbt) {
            return null;
        }
    }
}
