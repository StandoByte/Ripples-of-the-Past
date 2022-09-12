package com.github.standobyte.jojo.capability.chunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.BrokenChunkBlocksPacket;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;

// FIXME !!! (restore terrain) sync it to players on loading the chunk
// FIXME !!! (restore terrain) limit the storage
public class ChunkCap {
    private final Chunk chunk;
    
    private boolean loadedNBT = false;
    private final Map<BlockPos, PrevBlockInfo> brokenBlocks = new HashMap<>();
    private final List<PrevBlockInfo> blocksToSync = new ArrayList<>();
    private Set<ServerPlayerEntity> syncedTo = new HashSet<>();

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
    
    public void reset() {
        brokenBlocks.clear();
        if (!chunk.getLevel().isClientSide()) {
            PacketManager.sendToTrackingChunk(new BrokenChunkBlocksPacket(Collections.emptyList(), true), chunk);
        }
    }

    public void tick() {
        if (!chunk.getLevel().isClientSide()) {
            if (loadedNBT) {
                Iterator<Map.Entry<BlockPos, PrevBlockInfo>> it = brokenBlocks.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<BlockPos, PrevBlockInfo> entry = it.next();
                    if (chunk.getBlockState(entry.getKey()).isAir(chunk.getLevel(), entry.getKey())) {
                        blocksToSync.add(entry.getValue());
                    }
                    else {
                        it.remove();
                    }
                }
                loadedNBT = false;
            }
            
            else {
                Iterator<Map.Entry<BlockPos, PrevBlockInfo>> it = brokenBlocks.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<BlockPos, PrevBlockInfo> entry = it.next();
                    if (entry.getValue().forget()) {
                        it.remove();
                        blocksToSync.add(PrevBlockInfo.clientInstance(entry.getKey(), Blocks.AIR.defaultBlockState()));
                    }
                }
            }
    
            // FIXME !!! (restore terrain) the packet size might be too large
            if (!blocksToSync.isEmpty()) {
                PacketManager.sendToTrackingChunk(new BrokenChunkBlocksPacket(blocksToSync, false), chunk);
                syncedTo = ((ServerChunkProvider) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false)
                        .collect(Collectors.toSet());
            }
            blocksToSync.clear();
        }
    }

    // FIXME fix the blocks resetting on client after being synced
    public void onChunkLoad(ServerPlayerEntity player) {
//        if (!chunk.getLevel().isClientSide() && !syncedTo.contains(player) && !brokenBlocks.isEmpty()) {
//            PacketManager.sendToClient(new BrokenChunkBlocksPacket(brokenBlocks.values(), true), player);
//            syncedTo.add(player);
//        }
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
                    brokenBlocks.put(block.pos, block);
                }
            });
        }
        loadedNBT = true;
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

        private CompoundNBT toNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.put("Pos", NBTUtil.writeBlockPos(pos));
            nbt.put("State", NBTUtil.writeBlockState(state));
            nbt.putBoolean("Keep", keep);
            nbt.putInt("TickCount", tickCount);
            
            ListNBT itemsNBT = new ListNBT();
            for (ItemStack stack : drops) {
                itemsNBT.add(stack.save(new CompoundNBT()));
            }
            nbt.put("Drops", itemsNBT);
            
            return nbt;
        }

        @Nullable
        private static PrevBlockInfo fromNBT(CompoundNBT nbt) {
            if (!(
                    nbt.contains("Pos", JojoModUtil.getNbtId(CompoundNBT.class)) &&
                    nbt.contains("State", JojoModUtil.getNbtId(CompoundNBT.class)) && 
                    nbt.contains("Drops", JojoModUtil.getNbtId(ListNBT.class)))) {
                return null;
            }
            
            List<ItemStack> drops = new ArrayList<>();
            ListNBT dropsNBT = nbt.getList("Drops", JojoModUtil.getNbtId(CompoundNBT.class));
            for (INBT nbtElement : dropsNBT) {
                CompoundNBT itemNBT = (CompoundNBT) nbtElement;
                ItemStack item = ItemStack.of(itemNBT);
                if (!item.isEmpty()) {
                    drops.add(item);
                }
            }
            
            PrevBlockInfo block = new PrevBlockInfo(
                    NBTUtil.readBlockPos(nbt.getCompound("Pos")), 
                    NBTUtil.readBlockState(nbt.getCompound("State")), 
                    drops, 
                    nbt.getBoolean("Keep"));
            block.tickCount = nbt.getInt("TickCount");
            return block;
        }
    }
}
