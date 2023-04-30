package com.github.standobyte.jojo.capability.chunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.BrokenChunkBlocksPacket;
import com.github.standobyte.jojo.util.mc.MCUtil;

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

public class ChunkCap {
    private final Chunk chunk;
    
    private boolean loadedNBT = false;
    private final Map<BlockPos, PrevBlockInfo> brokenBlocks = new HashMap<>();
    private final Map<BlockPos, Integer> brokenBlocksXp = new HashMap<>();
    private final List<PrevBlockInfo> blocksToSync = new ArrayList<>();
//    private Set<ServerPlayerEntity> syncedTo = new HashSet<>();

    public ChunkCap(Chunk chunk) {
        this.chunk = chunk;
    }

    public void saveBrokenBlock(BlockPos pos, BlockState state, Optional<TileEntity> tileEntity, List<ItemStack> drops) {
        // FIXME remember blocks with inventory
        if (tileEntity.map(te -> te instanceof IInventory).orElse(false)) return;
        
        saveBrokenBlock(new PrevBlockInfo(pos, state, drops, false));
    }
    
    private void saveBrokenBlock(PrevBlockInfo prevBlock) {
        if (!chunk.getLevel().isClientSide() && brokenBlocksXp.containsKey(prevBlock.pos)) {
            prevBlock.setDroppedXp(brokenBlocksXp.remove(prevBlock.pos));
        }
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
                    if (CrazyDiamondRestoreTerrain.blockCanBePlaced(chunk.getLevel(), entry.getKey(), entry.getValue().state)) {
                        blocksToSync.add(entry.getValue());
                    }
                    else {
                        it.remove();
                    }
                }
                loadedNBT = false;
            }
            
//            else {
//                Iterator<Map.Entry<BlockPos, PrevBlockInfo>> it = brokenBlocks.entrySet().iterator();
//                while (it.hasNext()) {
//                    Map.Entry<BlockPos, PrevBlockInfo> entry = it.next();
//                    if (entry.getValue().forget()) {
//                        it.remove();
//                        blocksToSync.add(PrevBlockInfo.clientInstance(entry.getKey(), Blocks.AIR.defaultBlockState()));
//                    }
//                }
//            }
    
            if (!blocksToSync.isEmpty()) {
                PacketManager.sendToTrackingChunk(new BrokenChunkBlocksPacket(blocksToSync, false), chunk);
//                syncedTo = ((ServerChunkProvider) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false)
//                        .collect(Collectors.toSet());
            }
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
    
    public boolean wasBlockBroken(BlockPos pos) {
        return brokenBlocks.containsKey(pos);
    }
    
    public void setDroppedXp(BlockPos blockPos, int xp) {
        brokenBlocksXp.put(blockPos, xp);
    }
    
    
    CompoundNBT save() {
        CompoundNBT nbt = new CompoundNBT();
        if (JojoModConfig.getCommonConfigInstance(false).saveDestroyedBlocks.get()) {
            ListNBT blocksBroken = new ListNBT();
            for (PrevBlockInfo block : brokenBlocks.values()) {
                blocksBroken.add(block.toNBT());
            }
            nbt.put("Blocks", blocksBroken);
        }
        return nbt;
    }
    
    void load(CompoundNBT nbt) {
        if (JojoModConfig.getCommonConfigInstance(false).saveDestroyedBlocks.get()
                && nbt.contains("Blocks", MCUtil.getNbtId(ListNBT.class))) {
            nbt.getList("Blocks", MCUtil.getNbtId(CompoundNBT.class)).forEach(blockNBT -> {
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
        private int xp = 0;
        
        private PrevBlockInfo(BlockPos pos, BlockState state, List<ItemStack> drops, boolean keep) {
            this.pos = pos;
            this.state = state;
            this.drops = drops.stream().map(stack -> stack.copy()).collect(Collectors.toList());
            this.keep = keep;
        }
        
        public static PrevBlockInfo clientInstance(BlockPos pos, BlockState state) {
            return new PrevBlockInfo(pos, state, new ArrayList<>(), true);
        }
        
        public void setDroppedXp(int xp) {
            this.xp = xp;
        }
        
        public int getDroppedXp() {
            return xp;
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
            nbt.putInt("Xp", xp);
            nbt.put("Drops", itemsNBT);
            
            return nbt;
        }

        @Nullable
        private static PrevBlockInfo fromNBT(CompoundNBT nbt) {
            if (!(
                    nbt.contains("Pos", MCUtil.getNbtId(CompoundNBT.class)) &&
                    nbt.contains("State", MCUtil.getNbtId(CompoundNBT.class)) && 
                    nbt.contains("Drops", MCUtil.getNbtId(ListNBT.class)))) {
                return null;
            }
            
            List<ItemStack> drops = new ArrayList<>();
            ListNBT dropsNBT = nbt.getList("Drops", MCUtil.getNbtId(CompoundNBT.class));
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
            block.xp = nbt.getInt("Xp");
            return block;
        }
    }
}
