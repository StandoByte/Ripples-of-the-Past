package com.github.standobyte.jojo.entity.itemprojectile;

import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class BladeHatEntity extends ItemNbtProjectileEntity implements IEntityAdditionalSpawnData {
    private static final DataParameter<Boolean> RETURNING_TO_OWNER = EntityDataManager.defineId(BladeHatEntity.class, DataSerializers.BOOLEAN);

    public BladeHatEntity(World world, double x, double y, double z, ItemStack thrownStack) {
        super(ModEntityTypes.BLADE_HAT.get(), world, x, y, z, thrownStack);
        this.setNoGravity(true);
        this.setBaseDamage(6.0F);
    }

    public BladeHatEntity(World world, LivingEntity thrower, ItemStack thrownStack) {
        super(ModEntityTypes.BLADE_HAT.get(), world, thrower, thrownStack);
        this.setNoGravity(true);
        this.setBaseDamage(6.0F);
    }
    
    public BladeHatEntity(EntityType<? extends ItemNbtProjectileEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!isReturningToOwner() && shouldReturn()) {
            changeMovementAfterHit();
        }
        if (tickCount > 100) {
            setNoGravity(false);
        }
        else if (!isInGround()) {
            if (!level.isClientSide()) {
                Vector3d motionVec = this.getDeltaMovement();
                Vector3d posVec = this.position();
                Vector3d nextPosVec = posVec.add(motionVec);
                RayTraceResult rayTraceResult = this.level.clip(new RayTraceContext(posVec, nextPosVec, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, this));
                if (rayTraceResult.getType() == Type.BLOCK) {
                    BlockPos blockPos = ((BlockRayTraceResult) rayTraceResult).getBlockPos();
                    Block block = level.getBlockState(blockPos).getBlock();
                    if (block == Blocks.COBWEB || block == Blocks.TRIPWIRE || block instanceof BushBlock) {
                        level.destroyBlock(blockPos, true);
                    }
                }
            }
        }
    }
    
    protected boolean shouldReturn() {
        return tickCount > 30;
    }
    
    protected void changeMovementAfterHit() {
        if (!isReturningToOwner()) {
            setDeltaMovement(getDeltaMovement().reverse());
            setReturningToOwner(true);
        }
    }
    
    @Override
    public boolean canHitEntity(Entity entity) {
        return !entity.is(getOwner()) && super.canHitEntity(entity);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(RETURNING_TO_OWNER, false);
    }
    
    private boolean isReturningToOwner() {
        return entityData.get(RETURNING_TO_OWNER);
    }
    
    private void setReturningToOwner(boolean returnToOwner) {
        entityData.set(RETURNING_TO_OWNER, returnToOwner);
    }
    
    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return ModSounds.BLADE_HAT_ENTITY_HIT.get();
    }
    
    @Override
    public double getBaseDamage() {
        return super.getBaseDamage() + EnchantmentHelper.getDamageBonus(thrownStack, CreatureAttribute.UNDEFINED);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        setReturningToOwner(compound.getBoolean("Returning"));
        // TODO remove the retroactive fix
        if (thrownStack.isEmpty()) {
            thrownStack = new ItemStack(ModItems.BLADE_HAT.get());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Returning", isReturningToOwner());
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {}

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        ClientTickingSoundsHelper.playBladeHatSound(this);
    }

}
