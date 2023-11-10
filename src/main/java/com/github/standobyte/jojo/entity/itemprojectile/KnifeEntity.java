package com.github.standobyte.jojo.entity.itemprojectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class KnifeEntity extends ItemProjectileEntity {
    private boolean timeStop = false;
    private Vector3d timeStopHitMotion;
    private int tsFlightTicks = 0;
    private TexVariant knifeTexVariant = TexVariant.KNIFE;

    public KnifeEntity(World world, LivingEntity shooter) {
       super(ModEntityTypes.KNIFE.get(), shooter, world);
    }

    public KnifeEntity(World world, double x, double y, double z) {
       super(ModEntityTypes.KNIFE.get(), x, y, z, world);
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
    
    public void setTimeStopFlightTicks(int ticks) {
        this.tsFlightTicks = ticks;
    }
    
    @Override
    public void tick() {
        if (timeStop) {
            if (tsFlightTicks > 0) {
                tsFlightTicks--;
            }
            else {
                super.canUpdate(false);
                return;
            }
        }
        
        super.tick();
        
        if (timeStopHitMotion != null) {
            setDeltaMovement(timeStopHitMotion);
            timeStopHitMotion = null;
        }
        
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

    @Override
    protected void onHit(RayTraceResult rayTraceResult) {
        if (timeStop && rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
            if (!level.isClientSide()) {
                timeStopHitMotion = getDeltaMovement();
                setDeltaMovement(Vector3d.ZERO);
            }
            tsFlightTicks = 0;
            super.canUpdate(false);
        }
        else {
            super.onHit(rayTraceResult);
        }
    }

    @Override
    public void canUpdate(boolean canUpdate) {
        this.timeStop = !canUpdate;
        if (canUpdate) {
            super.canUpdate(canUpdate);
        }
    }
//    
//    @Override
//    public void setSecondsOnFire(int seconds) {}
    
    @Override
    protected boolean hurtTarget(Entity target, Entity thrower) {
        float dmgAmount = getActualDamage();
        DamageSource damagesource = DamageSource.arrow(this, thrower == null ? this : thrower);
        return DamageUtil.hurtThroughInvulTicks(target, damagesource, dmgAmount);
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
    
    public void setKnifeType(TexVariant type) {
        this.knifeTexVariant = type;
    }
    
    public ResourceLocation getKnifeTexture() {
        return knifeTexVariant.texPath;
    }
    
    public static enum TexVariant {
        KNIFE(new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/knife.png")),
        SCALPEL(new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/knife_scalpel.png")),
        FISH(new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/knife_fish.png"));
        
        private final ResourceLocation texPath;
        private TexVariant(ResourceLocation texPath) {
            this.texPath = texPath;
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        timeStop = nbt.getBoolean("TimeStop");
        tsFlightTicks = nbt.getInt("TimeStopTicks");
        knifeTexVariant = MCUtil.nbtGetEnum(nbt, "KnifeType", TexVariant.class);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("TimeStop", timeStop);
        nbt.putInt("TimeStopTicks", tsFlightTicks);
        MCUtil.nbtPutEnum(nbt, "KnifeType", knifeTexVariant);
    }
    
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(timeStop);
        buffer.writeVarInt(tsFlightTicks);
        buffer.writeEnum(knifeTexVariant);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        timeStop = additionalData.readBoolean();
        tsFlightTicks = additionalData.readVarInt();
        knifeTexVariant = additionalData.readEnum(TexVariant.class);
    }
}
