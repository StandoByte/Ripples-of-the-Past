package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockShardEntity extends Entity implements IEntityAdditionalSpawnData {
    private BlockState blockState;

    public BlockShardEntity(World world, BlockState blockState) {
        this(ModEntityTypes.BLOCK_SHARD.get(), world);
        this.blockState = blockState;
    }

    public BlockShardEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }
    
    public BlockState getBlock() {
        if (blockState == null) {
            blockState = Blocks.COBBLESTONE.defaultBlockState();
        }
        return blockState;
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return !canUpdate();
    }
    
    @Override
    public void tick() {
        super.tick();
        if (tickCount > 10 && !level.isClientSide()) {
            remove();
        }
    }
    

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT pCompound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT pCompound) {
    }

    
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(Block.getId(getBlock()));
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        this.blockState = Block.stateById(additionalData.readInt());
    }

}
