package com.github.standobyte.jojo.entity;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.CrazyDiamondHeal;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.GameData;

// TODO (angelo) render the entity during the creation anim
public class AngeloRockEntity extends Entity implements IEntityAdditionalSpawnData {
    protected static final DataParameter<Optional<BlockPos>> DATA_ATTACH_POS_ID = EntityDataManager.defineId(ShulkerEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final int CREATION_ANIM_LEN = 30;
    private int creationAnimTicks;
    private boolean startedSound;
    /* fuck it, sure, yeah */ private MobEntity mob;
    private boolean useMobHurtSound;
    
    
    public AngeloRockEntity(EntityType<?> pType, World pLevel) {
        super(pType, pLevel);
    }
    
    // TODO (angelo) item drops (+save them in NBT)
    public static void turnIntoRock(Entity entity, @Nullable BlockState upperBlock, @Nullable BlockState lowerBlock, @Nullable List<ItemStack> drops) {
        if (!entity.level.isClientSide()) {
            AngeloRockEntity angeloRock = new AngeloRockEntity(ModEntityTypes.ANGELO_ROCK.get(), entity.level);
            int rotation = MathUtil.round(entity.yRot / 90);
            angeloRock.yRot = 90 * rotation;
            angeloRock.setPos(entity.getX(), entity.getY(), entity.getZ());
            if (entity instanceof MobEntity) {
                angeloRock.mob = (MobEntity) entity;
                angeloRock.useMobHurtSound = CommonReflection.getAmbientSound(angeloRock.mob) == null;
            }
            
            angeloRock.setUpperBlock(upperBlock);
            angeloRock.setLowerBlock(lowerBlock);
            angeloRock.creationAnimTicks = CREATION_ANIM_LEN;
            
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
        if (level.isClientSide()) {
            return ActionResultType.SUCCESS;
        }
        else {
            if (mob != null) {
                mob.setPos(getX(), getY(), getZ());
            }
            SoundEvent voiceline = IStandPower.getStandPowerOptional(pPlayer).resolve().map(power -> {
                StandType<?> stand = power.getType();
                if (stand == ModStands.CRAZY_DIAMOND.getStandType()) {
                    return ModSounds.JOSUKE_YO_ANGELO.get();
                }
                if (stand != null && stand.getRegistryName().getPath().contains("echoes")) {
                    return ModSounds.KOICHI_YO_ANGELO.get();
                }
                return null;
            }).orElse(null);
            if (voiceline != null && JojoModUtil.sayVoiceLine(pPlayer, voiceline, null, 1, 1, 0, false)) {
            }
            else {
                playMobResponseSound();
            }
            return ActionResultType.CONSUME;
        }
    }
    
    private void playMobResponseSound() {
        if (mob != null) {
            if (useMobHurtSound) {
                CommonReflection.playHurtSound(mob, DamageSource.GENERIC);
            }
            else {
                mob.playAmbientSound();
            }
        }
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
        pCompound.putInt("CreationAnim", creationAnimTicks);
        
        if (mob != null) {
            String s = mob.getEncodeId();
            if (s != null) {
                CompoundNBT mobNBT = new CompoundNBT();
                mobNBT.putString("id", s);
                mob.saveWithoutId(mobNBT);
                mobNBT.remove("Passengers");
                pCompound.put("AngeloMob", mobNBT);
                pCompound.putBoolean("NoAmbient", useMobHurtSound);
            }
        }
        
        ListNBT stonePiecesNbt = new ListNBT();
        for (BlockState blockState : stonePieces) {
            stonePiecesNbt.add(NBTUtil.writeBlockState(blockState));
        }
        pCompound.put("RockPieces", stonePiecesNbt);
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
        this.creationAnimTicks = pCompound.getInt("CreationAnim");
        
        Entity mobEntity = MCUtil.nbtGetCompoundOptional(pCompound, "AngeloMob").flatMap(mobNBT -> {
            try {
                return EntityType.create(mobNBT, level);
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        }).orElse(null);
        this.mob = mobEntity instanceof MobEntity ? (MobEntity) mobEntity : null;
        this.useMobHurtSound = pCompound.getBoolean("NoAmbient");
        
        MCUtil.getNbtElement(pCompound, "RockPieces", ListNBT.class).ifPresent(stonePiecesNbt -> {
            if (stonePiecesNbt.getElementType() == Constants.NBT.TAG_COMPOUND) {
                for (int i = 0; i < STONE_PIECES_COUNT; i++) {
                    CompoundNBT blockNbt = stonePiecesNbt.getCompound(i);
                    BlockState blockState = NBTUtil.readBlockState(blockNbt);
                    if (blockState != null) {
                        stonePieces.set(i, blockState);
                    }
                }
            }
        });
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (creationAnimTicks > 0) {
            if (level.isClientSide()) {
                if (ClientUtil.canSeeStands()) {
                    CrazyDiamondHeal.addParticlesAround(this);
                }
                if (ClientUtil.canHearStands() && !this.isSilent()) {
                    if (!startedSound) {
                        ClientTickingSoundsHelper.playStoppableEntitySound(this, 
                                ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), 1, 1, true, entity -> entity.creationAnimTicks > 0);
                        startedSound = true;
                    }
                    if (creationAnimTicks == 1) {
                        level.playSound(ClientUtil.getClientPlayer(), getX(), getY(), getZ(), 
                                ModSounds.CRAZY_DIAMOND_FIX_ENDED.get(), getSoundSource(), 1, 1);
                    }
                }
            }
            --creationAnimTicks;
        }
        
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
    
    @Override
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
    
    public float getCreationAnimProgress(float partialTick) {
        return creationAnimTicks == 0 ? 1 : (CREATION_ANIM_LEN - creationAnimTicks + partialTick) / CREATION_ANIM_LEN;
    }
    
    
    private static final int STONE_PIECES_COUNT = 2;
    private List<BlockState> stonePieces = NonNullList.withSize(2, Blocks.STONE.defaultBlockState());
    
    public BlockState getUpperBlock() {
        return stonePieces.get(0);
    }
    
    public BlockState getLowerBlock() {
        return stonePieces.get(1);
    }
    
    private void setUpperBlock(BlockState block) {
        if (block != null) stonePieces.set(0, block);
    }
    
    private void setLowerBlock(BlockState block) {
        if (block != null) stonePieces.set(1, block);
    }
    
    
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeVarInt(creationAnimTicks);
        for (int i = 0; i < STONE_PIECES_COUNT; i++) {
            BlockState block = stonePieces.get(i);
            buffer.writeVarInt(Block.getId(block));
        }
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        creationAnimTicks = additionalData.readVarInt();
        for (int i = 0; i < STONE_PIECES_COUNT; i++) {
            int blockId = additionalData.readVarInt();
            BlockState block = GameData.getBlockStateIDMap().byId(blockId);
            if (block != null) {
                stonePieces.set(i, block);
            }
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
