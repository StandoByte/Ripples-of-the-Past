package com.github.standobyte.jojo.entity;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

public class AngeloRockEntity extends Entity implements IEntityAdditionalSpawnData {
    protected static final DataParameter<Optional<BlockPos>> DATA_ATTACH_POS_ID = EntityDataManager.defineId(ShulkerEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private SoundEvent mobSound;
    
    
    public AngeloRockEntity(EntityType<?> pType, World pLevel) {
        super(pType, pLevel);
    }
    
    public static void turnIntoRock(Entity entity) {
        if (!entity.level.isClientSide()) {
            AngeloRockEntity angeloRock = new AngeloRockEntity(ModEntityTypes.ANGELO_ROCK.get(), entity.level);
            angeloRock.yRot = 90 * MathUtil.round(entity.yRot / 90);
            angeloRock.setPos(entity.getX(), entity.getY(), entity.getZ());
            if (entity instanceof MobEntity) {
                angeloRock.mobSound = CommonReflection.getAmbientSound((MobEntity) entity);
            }
            
            entity.level.addFreshEntity(angeloRock);
            if (entity instanceof IPlayerPossess) {
                ((IPlayerPossess) entity).jojoPossessEntity(angeloRock, false, null);
            }
            else {
                entity.remove();
            }
        }
    }
    
    @Override
    public ActionResultType interact(PlayerEntity pPlayer, Hand pHand) {
        // TODO Yo, Angelo
        return super.interact(pPlayer, pHand);
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return isAlive();
    }
    
    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ATTACH_POS_ID, Optional.empty());
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT pCompound) {
        BlockPos blockpos = this.getAttachPosition();
        if (blockpos != null) {
            pCompound.putInt("APX", blockpos.getX());
            pCompound.putInt("APY", blockpos.getY());
            pCompound.putInt("APZ", blockpos.getZ());
        }
        
        if (mobSound != null) {
            pCompound.putString("MobSound", mobSound.getRegistryName().toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT pCompound) {
        if (pCompound.contains("APX")) {
            int i = pCompound.getInt("APX");
            int j = pCompound.getInt("APY");
            int k = pCompound.getInt("APZ");
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(new BlockPos(i, j, k)));
        } else {
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.empty());
        }
        
        if (pCompound.contains("MobSound", Constants.NBT.TAG_STRING)) {
            ResourceLocation soundKey = new ResourceLocation(pCompound.getString("MobSound"));
            if (ForgeRegistries.SOUND_EVENTS.containsKey(soundKey)) {
                mobSound = ForgeRegistries.SOUND_EVENTS.getValue(soundKey);
            }
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        BlockPos attachPos = getAttachPosition();
        if (attachPos == null && !this.level.isClientSide) {
            attachPos = this.blockPosition();
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(attachPos));
        }

        if (this.isPassenger()) {
            attachPos = null;
            float f = this.getVehicle().yRot;
            this.yRot = f;
        } else if (!this.level.isClientSide) {
            Optional<BlockPos> moveWithPiston = moveWithPiston(attachPos);
            if (!moveWithPiston.isPresent()) moveWithPiston = moveWithPiston(attachPos.above());
            if (moveWithPiston.isPresent()) {
                this.entityData.set(DATA_ATTACH_POS_ID, moveWithPiston);
            }
        }


        if (attachPos != null) {
            setPosAndOldPos(attachPos.getX() + 0.5, attachPos.getY(), attachPos.getZ() + 0.5);
//            if (isAddedToWorld() && level instanceof ServerWorld) {
//                ((ServerWorld)level).updateChunkPos(this); // Forge - Process chunk registration after moving.
//            }
            setBoundingBox(new AxisAlignedBB(
                    getX() - 0.5, 
                    getY(), 
                    getZ() - 0.5, 
                    getX() + 0.5, 
                    getY() + 2, 
                    getZ() + 0.5));
        }
    }
    
    @Nullable
    private Optional<BlockPos> moveWithPiston(BlockPos pistonBlockPos) {
        Direction pistonHeadDir = null;
        BlockState blockState = level.getBlockState(pistonBlockPos);
        if (!blockState.isAir(this.level, pistonBlockPos) && (blockState.is(Blocks.MOVING_PISTON) || blockState.is(Blocks.PISTON_HEAD))) {
            pistonHeadDir = blockState.getValue(BlockStateProperties.FACING);
        }
        if (pistonHeadDir != null) {
            BlockPos attachPos = getAttachPosition();
            if (attachPos != null) {
                BlockPos newPos = attachPos.relative(pistonHeadDir);
                if (this.level.isEmptyBlock(newPos) && this.level.isEmptyBlock(newPos.above())) {
                    return Optional.of(newPos);
                }
            }
        }
        
        return Optional.empty();
    }
    
    public void setPos(double pX, double pY, double pZ) {
        super.setPos(pX, pY, pZ);
        if (this.entityData != null && this.tickCount != 0) {
            Optional<BlockPos> optional = this.entityData.get(DATA_ATTACH_POS_ID);
//            if (this.isAddedToWorld() && this.level instanceof ServerWorld) {
//                ((ServerWorld) level).updateChunkPos(this); // Forge - Process chunk registration after moving.
//            }
            Optional<BlockPos> optional1 = Optional.of(new BlockPos(pX, pY, pZ));
            if (!optional1.equals(optional)) {
                this.entityData.set(DATA_ATTACH_POS_ID, optional1);
                this.hasImpulse = true;
            }

        }
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> pKey) {
        if (DATA_ATTACH_POS_ID.equals(pKey) && level.isClientSide && !isPassenger()) {
            BlockPos blockpos = getAttachPosition();
            if (blockpos != null) {
                setPosAndOldPos(blockpos.getX() + 0.5, blockpos.getY(), blockpos.getZ() + 0.5);
            }
        }

        super.onSyncedDataUpdated(pKey);
    }

    @Nullable
    public BlockPos getAttachPosition() {
        return this.entityData.get(DATA_ATTACH_POS_ID).orElse(null);
    }

    public void setAttachPosition(@Nullable BlockPos pPos) {
        this.entityData.set(DATA_ATTACH_POS_ID, Optional.ofNullable(pPos));
    }


    @Override
    public void writeSpawnData(PacketBuffer buffer) {
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
