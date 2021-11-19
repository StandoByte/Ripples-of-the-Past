package com.github.standobyte.jojo.entity.itemprojectile;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.damage.ModDamageSources;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class KnifeEntity extends ItemProjectileEntity {
    private boolean timeStop;
    private Vector3d timeStopHitMotion;

    public KnifeEntity(World world, LivingEntity shooter) {
       super(ModEntityTypes.KNIFE.get(), shooter, world);
    }

    public KnifeEntity(World worldIn, double x, double y, double z) {
       super(ModEntityTypes.KNIFE.get(), x, y, z, worldIn);
    }

    public KnifeEntity(EntityType<? extends KnifeEntity> type, World world) {
       super(type, world);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ModItems.KNIFE.get());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
       return ModSounds.KNIFE_HIT.get();
    }

    @Override
    protected SoundEvent getActualHitGroundSound(BlockState blockState, BlockPos blockPos) {
       return ModSounds.KNIFE_HIT.get();
    }
    
    @Override
    public void tick() {
        if (!timeStop || timeStopHitMotion == null && tickCount < 5) {
            super.tick();
            if (!inGround && !level.isClientSide()) {
                Vector3d posVec = position();
                RayTraceResult rayTraceResult = level.clip(new RayTraceContext(posVec, posVec.add(getDeltaMovement()), RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, this));
                if (rayTraceResult.getType() == Type.BLOCK) {
                    BlockPos blockPos = ((BlockRayTraceResult) rayTraceResult).getBlockPos();
                    Block block = level.getBlockState(blockPos).getBlock();
                    if (block == Blocks.COBWEB) {
                        level.destroyBlock(blockPos, true);
                        setDeltaMovement(getDeltaMovement().scale(0.8D));
                    }
                    if (block == Blocks.TRIPWIRE) {
                        level.destroyBlock(blockPos, true);
                    }
                }
            }
        }
    }
    
    @Override
    protected void onHit(RayTraceResult rayTraceResult) {
        if (!(timeStop && rayTraceResult.getType() == RayTraceResult.Type.ENTITY)) {
            super.onHit(rayTraceResult);
        }
        else {
            timeStopHitMotion = getDeltaMovement();
            setDeltaMovement(Vector3d.ZERO);
        }
    }
    
    @Override
    public void setSecondsOnFire(int seconds) {}
    
    @Override
    protected boolean hurtTarget(Entity target, Entity thrower) {
        float dmgAmount = getActualDamage();
        DamageSource damagesource = DamageSource.arrow(this, thrower == null ? this : thrower);
        return ModDamageSources.hurtThroughInvulTicks(target, damagesource, dmgAmount);
    }
    
    @Override
    protected void doPostHurtEffects(LivingEntity entity) {
        if (!level.isClientSide()) {
            entity.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.addKnife();
            });
        }
    }

    @Override
    public boolean throwerCanCatch() {
        return false;
    }
    
    @Override
    public void canUpdate(boolean canUpdate) {
        if (canUpdate) {
            timeStop = false;
            if (timeStopHitMotion != null) {
                setDeltaMovement(timeStopHitMotion);
            }
        }
        else if (tickCount == 0) {
            timeStop = true;
        }
        else {
            super.canUpdate(canUpdate);
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("TSHitMotion", 9)) {
            ListNBT listNBT = nbt.getList("TSHitMotion", 6);
            timeStopHitMotion = new Vector3d(listNBT.getDouble(0), listNBT.getDouble(1), listNBT.getDouble(2));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        if (timeStopHitMotion != null) {
            nbt.putBoolean("TimeStopHit", true);
            nbt.put("TSHitMotion", newDoubleList(timeStopHitMotion.x, timeStopHitMotion.y, timeStopHitMotion.z));
        }
    }
}
