package com.github.standobyte.jojo.action.stand.effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.standobyte.jojo.init.ModStandEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.CDBlocksBrokenPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandEffectPacket;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class CrazyDiamondRestorableBlocks extends StandEffectInstance {
    // FIXME ! (restore terrain) save & sync
    private RegistryKey<World> dimension;
    private final Map<ChunkPos, Map<BlockPos, PrevBlockInfo>> blocks = new HashMap<>();
    private final List<CDBlocksBrokenPacket.BlockInfo> blocksToSend = new ArrayList<>();
    private Optional<ServerPlayerEntity> serverPlayerUser = Optional.empty();

    public CrazyDiamondRestorableBlocks(RegistryKey<World> dimension) {
        this(ModStandEffects.CRAZY_DIAMOND_RESTORABLE_BLOCKS.get());
        this.dimension = dimension;
    }

    public CrazyDiamondRestorableBlocks(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    public StandEffectInstance withUser(LivingEntity user) {
        serverPlayerUser = user instanceof ServerPlayerEntity ? Optional.of((ServerPlayerEntity) user) : Optional.empty();
        return super.withUser(user);
    }
    
    public StandEffectInstance withStand(IStandPower stand) {
        LivingEntity user = stand.getUser();
        serverPlayerUser = user instanceof ServerPlayerEntity ? Optional.of((ServerPlayerEntity) user) : Optional.empty();
        return super.withStand(stand);
    }
    
    public void removeBlock(World world, BlockPos blockPos) {
        if (world.dimension() == dimension && getChunkBlocks(blockPos).remove(blockPos) != null && serverPlayerUser.isPresent()) {
            blocksToSend.add(new CDBlocksBrokenPacket.BlockInfo(blockPos, Blocks.AIR.defaultBlockState(), false));
        }
    }
    
    public void addBlock(World world, BlockPos blockPos, BlockState blockState, List<ItemStack> restoreCost, boolean keep) {
        if (world.dimension() == dimension && restoreCost != null && restoreCost.size() == 1) {
            PrevBlockInfo prevBlock = new PrevBlockInfo(blockState, restoreCost.get(0), keep);
            getChunkBlocks(blockPos).put(blockPos, prevBlock);
            if (serverPlayerUser.isPresent()) {
                blocksToSend.add(new CDBlocksBrokenPacket.BlockInfo(blockPos, prevBlock.state, keep));
            }
        }
    }
    
    private Map<BlockPos, PrevBlockInfo> getChunkBlocks(BlockPos pos) {
        return blocks.computeIfAbsent(new ChunkPos(pos), p -> new HashMap<>());
    }
    
    public RegistryKey<World> getDimension() {
        return dimension;
    }
    
    public Stream<Map.Entry<BlockPos, PrevBlockInfo>> getBlocksAround(BlockPos pos, int manhattanDist) {
        ChunkPos chunkPos = new ChunkPos(pos);
        int chunksRange = (manhattanDist - 1) / 16 + 2;
        return blocks.entrySet().stream()
                .filter(chunk -> chunk.getKey().getChessboardDistance(chunkPos) <= chunksRange)
                .flatMap(chunk -> chunk.getValue().entrySet().stream())
                .filter(block -> block.getKey().distManhattan(pos) <= manhattanDist);
    }

    @Override
    protected void start() {}

    @Override
    protected void tickTarget(LivingEntity target) {}

    @Override
    protected void tick() {
        for (Map<BlockPos, PrevBlockInfo> chunkMap : blocks.values()) {
            Iterator<Map.Entry<BlockPos, PrevBlockInfo>> it = chunkMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, PrevBlockInfo> entry = it.next();
                if (entry.getValue().forget()) {
                    it.remove();
                    blocksToSend.add(new CDBlocksBrokenPacket.BlockInfo(entry.getKey(), Blocks.AIR.defaultBlockState(), false));
                }
            }
        }
        if (!blocksToSend.isEmpty()) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new CDBlocksBrokenPacket(blocksToSend), player);
            });
            blocksToSend.clear();
        }
    }

    @Override
    protected void stop() {}
    
    @Override
    public boolean removeOnUserDeath() {
        return false;
    }

    @Override
    public boolean removeOnUserLogout() {
        return false;
    }
    
    public void syncWithTrackingAndUser() {
        serverPlayerUser.ifPresent(player -> syncWithUserOnly(player));
    }

    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        PacketManager.sendToClient(TrStandEffectPacket.add(this), user);
    }

    @Override
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {}
    
    @Override
    public void writeAdditionalPacketData(PacketBuffer buf) {}

    @Override
    public void readAdditionalPacketData(PacketBuffer buf) {}

    @Override
    protected void writeAdditionalSaveData(CompoundNBT nbt) {}

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {}


    
    public static CrazyDiamondRestorableBlocks getRestorableBlocksEffect(IStandPower power, World world) {
        List<StandEffectInstance> effects = 
        power.getContinuousEffects().getEffects(effect -> effect.effectType == ModStandEffects.CRAZY_DIAMOND_RESTORABLE_BLOCKS.get());
        if (!effects.isEmpty()) {
            for (StandEffectInstance effect : effects) {
                CrazyDiamondRestorableBlocks cdEffect = (CrazyDiamondRestorableBlocks) effect;
                if (cdEffect.dimension == world.dimension()) {
                    return cdEffect;
                }
            }
        }
        CrazyDiamondRestorableBlocks cdEffect = new CrazyDiamondRestorableBlocks(world.dimension());
        power.getContinuousEffects().addEffect(cdEffect);
        return cdEffect;
    }
    
    public static class PrevBlockInfo {
        public final BlockState state;
//        public final List<ItemStack> stacks;
        // FIXME (!) (restore terrain) turn it into stacks list
        public final ItemStack stack;
        public final boolean keep;
        private int tickCount = 0;
        
        private PrevBlockInfo(BlockState state, ItemStack stack, boolean keep) {
            this.state = state;
            this.stack = stack;
            this.keep = keep;
        }
        
//        private PrevBlockInfo(BlockState state, List<ItemStack> stacks, boolean keep) {
//            this.state = state;
//            this.stacks = stacks;
//            this.keep = keep;
//        }
        
        private boolean forget() {
            return !keep && tickCount++ == 1200;
        }
    }
}
