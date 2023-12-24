package com.github.standobyte.jojo.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class ObjectEntity extends Entity implements IEntityAdditionalSpawnData {
    private Type objectType;
    private UUID owner;

    public ObjectEntity(EntityType<?> pType, World pLevel) {
        super(pType, pLevel);
    }

    public ObjectEntity(World world, Type objectType) {
        this(ModEntityTypes.OBJECT.get(), world);
        this.objectType = objectType;
    }

    public Type getObjectType() {
        return objectType;
    }

    @Nullable
    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(@Nullable UUID pOwner) {
        this.owner = pOwner;
    }

    @Override
    protected ITextComponent getTypeName() {
        if (objectType != null) {
            return new TranslationTextComponent(getType().getDescriptionId() + '.' + objectType.name().toLowerCase());
        }
        return super.getTypeName();
    }



    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        
        if (objectType == null) {
            if (!level.isClientSide()) {
                remove();
            }
            return;
        }
        
        if ((isOnGround() || fluidHeight.values().stream().anyMatch(height -> height > 0)) && tickCount > 100) {
            if (!level.isClientSide()) {
                remove();
            }
            return;
        }
        
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        Vector3d vector3d = this.getDeltaMovement();
        float f = this.getEyeHeight() - 0.11111111F;
        if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > (double)f) {
            this.setUnderwaterMovement();
        } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double)f) {
            this.setUnderLavaMovement();
        } else if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }

        if (this.level.isClientSide) {
            this.noPhysics = false;
        } else {
            this.noPhysics = !this.level.noCollision(this);
            if (this.noPhysics) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
            }
        }

        if (!this.onGround || getHorizontalDistanceSqr(this.getDeltaMovement()) > (double)1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            float f1 = 0.98F;
            if (this.onGround) {
                f1 = this.level.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ())).getSlipperiness(level, new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ()), this) * 0.98F;
            }

            this.setDeltaMovement(this.getDeltaMovement().multiply((double)f1, 0.98D, (double)f1));
            if (this.onGround) {
                Vector3d vector3d1 = this.getDeltaMovement();
                if (vector3d1.y < 0.0D) {
                    this.setDeltaMovement(vector3d1.multiply(1.0D, -0.5D, 1.0D));
                }
            }
        }

        boolean flag = MathHelper.floor(this.xo) != MathHelper.floor(this.getX()) || MathHelper.floor(this.yo) != MathHelper.floor(this.getY()) || MathHelper.floor(this.zo) != MathHelper.floor(this.getZ());
        int i = flag ? 2 : 40;
        if (this.tickCount % i == 0) {
            if (this.level.getFluidState(this.blockPosition()).is(FluidTags.LAVA) && !this.fireImmune()) {
                this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
            }
        }

        this.hasImpulse |= this.updateInWaterStateAndDoFluidPushing();
        if (!this.level.isClientSide) {
            double d0 = this.getDeltaMovement().subtract(vector3d).lengthSqr();
            if (d0 > 0.01D) {
                this.hasImpulse = true;
            }
        }
    }

    private void setUnderwaterMovement() {
        Vector3d vector3d = this.getDeltaMovement();
        this.setDeltaMovement(vector3d.x * (double)0.99F, vector3d.y + (double)(vector3d.y < (double)0.06F ? 5.0E-4F : 0.0F), vector3d.z * (double)0.99F);
    }

    private void setUnderLavaMovement() {
        Vector3d vector3d = this.getDeltaMovement();
        this.setDeltaMovement(vector3d.x * (double)0.95F, vector3d.y + (double)(vector3d.y < (double)0.06F ? 5.0E-4F : 0.0F), vector3d.z * (double)0.95F);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }



    @Override
    protected void defineSynchedData() {}

    @Override
    protected void addAdditionalSaveData(CompoundNBT pCompound) {
        if (objectType != null) {
            MCUtil.nbtPutEnum(pCompound, "ObjType", objectType);
        }
        pCompound.putInt("Age", tickCount);
        if (getOwner() != null) {
            pCompound.putUUID("Owner", getOwner());
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT pCompound) {
        objectType = MCUtil.nbtGetEnum(pCompound, "ObjType", Type.class);
        tickCount = pCompound.getInt("Age");
        if (pCompound.hasUUID("Owner")) {
            owner = pCompound.getUUID("Owner");
        }
    }


    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        NetworkUtil.writeOptionally(buffer, objectType, t -> buffer.writeEnum(t));
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        objectType = NetworkUtil.readOptional(additionalData, () -> additionalData.readEnum(Type.class)).orElse(null);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    public enum Type {
        TOOTH
    }
}
