package com.github.standobyte.jojo.entity.damaging.projectile;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.CrazyDiamondHeal;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.entity.EntityMadeFromBlock;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class BlockShardEntity extends ModdedProjectileEntity implements EntityMadeFromBlock {
    private static final DataParameter<Boolean> CRAZY_D_RESTORED = EntityDataManager.defineId(BlockShardEntity.class, DataSerializers.BOOLEAN);
    private BlockState blockState;
    private Optional<BlockPos> originBlockPos = Optional.empty();
    private int crazyDRestoreTick = 1;
    
    public BlockShardEntity(LivingEntity shooter, World world, BlockState blockState, BlockPos originBlockPos) {
        super(ModEntityTypes.BLOCK_SHARD.get(), shooter, world);
        this.blockState = blockState;
        this.originBlockPos = Optional.ofNullable(originBlockPos);
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
    
    public boolean isGlass() {
        return isGlassBlock(getBlock());
    }
    
    @Override
    protected boolean hurtTarget(Entity target, @Nullable LivingEntity owner) {
        if (super.hurtTarget(target, owner)) {
            if (isGlass() && target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                if (random.nextFloat() < glassShardBleedingChance(livingTarget)) {
                    glassShardBleeding(livingTarget);
                }
            }
            
            return true;
        }
        return false;
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
    public boolean crazyDRestore(BlockPos blockPos) {
        entityData.set(CRAZY_D_RESTORED, true);
        return true;
    }
    
    protected boolean isCrazyDRestored() {
        return entityData.get(CRAZY_D_RESTORED);
    }
    
    @Override
    protected void moveProjectile() {
        if (isCrazyDRestored()) {
            originBlockPos.ifPresent(target -> {
                if (crazyDRestoreTick-- == 0 && !level.isClientSide()) {
                    remove();
                    return;
                }
                
                Vector3d targetPos = Vector3d.atCenterOf(target);
                Vector3d vecToTarget = targetPos.subtract(this.position());
                setDeltaMovement(vecToTarget.scale(0.5));
                getUserStandPower().ifPresent(stand -> {
                    stand.consumeStamina(stand.getStaminaTickGain() + ModStandsInit.CRAZY_DIAMOND_BLOCK_BULLET.get().getStaminaCostTicking(stand), true);
                });
                if (level.isClientSide()) {
                    if (ClientUtil.canSeeStands()) {
                        CrazyDiamondHeal.addParticlesAround(this);
                    }
                }
            });
        }
        super.moveProjectile();
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, RayTraceResult hitTarget) {
        if (level.isClientSide() && blockState != null) {
            Vector3d position = position();
            SoundType soundType = blockState.getSoundType();
            SoundEvent sound = soundType.getBreakSound();
            if (sound != null) {
                level.playLocalSound(position.x, position.y, position.z, 
                        sound, SoundCategory.BLOCKS, 
                        (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
            }
            
            CustomParticlesHelper.addBlockShardBreakParticles(position, blockState);
        }
        super.breakProjectile(targetType, hitTarget);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CRAZY_D_RESTORED, false);
    }
    
    
    
    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (blockState != null) {
            nbt.put("Block", NBTUtil.writeBlockState(blockState));
        }
        originBlockPos.ifPresent(pos -> nbt.put("OriginPos", NBTUtil.writeBlockPos(pos)));
        nbt.putBoolean("CDRestore", isCrazyDRestored());
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        blockState = NBTUtil.readBlockState(nbt.getCompound("Block"));
        if (blockState.getBlock() == Blocks.AIR) {
            blockState = Blocks.COBBLESTONE.defaultBlockState();
        }
        originBlockPos = MCUtil.nbtGetCompoundOptional(nbt, "OriginPos").map(NBTUtil::readBlockPos);
        entityData.set(CRAZY_D_RESTORED, nbt.getBoolean("CDRestore"));
    }

    

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(Block.getId(getBlock()));
        NetworkUtil.writeOptional(buffer, originBlockPos, buffer::writeBlockPos);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        this.blockState = Block.stateById(additionalData.readInt());
        this.originBlockPos = NetworkUtil.readOptional(additionalData, PacketBuffer::readBlockPos);
    }
    
    
    public static boolean isGlassBlock(BlockState blockState) {
        return blockState.getMaterial() == Material.GLASS;
    }
    
    public static float glassShardBleedingChance(LivingEntity entity) {
        float armorCover = entity.getArmorCoverPercentage();
        return Math.max(1 - armorCover, 0.05f);
    }
    
    public static void glassShardBleeding(LivingEntity entity) {
        entity.addEffect(new EffectInstance(ModStatusEffects.BLEEDING.get(), 100, 0, false, false, true));
    }

}
