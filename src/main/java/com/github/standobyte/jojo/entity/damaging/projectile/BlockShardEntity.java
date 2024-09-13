package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

public class BlockShardEntity extends ModdedProjectileEntity {
    private BlockState blockState;

    public BlockShardEntity(LivingEntity shooter, World world, BlockState blockState) {
        super(ModEntityTypes.BLOCK_SHARD.get(), shooter, world);
        this.blockState = blockState;
    }

    public BlockShardEntity(EntityType<? extends BlockShardEntity> entityType, World world) {
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
    public int ticksLifespan() {
        return 100;
    }

    // TODO damage based on the block hardness
    @Override
    protected float getBaseDamage() {
        return 2.5f;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    protected boolean constVelocity() {
        return false;
    }
    
    @Override
    protected double getGravityAcceleration() {
        return 0.05;
    }
    
    @Override
    protected boolean hasGravity() {
        return true;
    }
    


    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (blockState != null) {
            nbt.put("Block", NBTUtil.writeBlockState(blockState));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        blockState = NBTUtil.readBlockState(nbt.getCompound("Block"));
        if (blockState.getBlock() == Blocks.AIR) {
            blockState = Blocks.COBBLESTONE.defaultBlockState();
        }
    }

    

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(Block.getId(getBlock()));
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        this.blockState = Block.stateById(additionalData.readInt());
    }

}
