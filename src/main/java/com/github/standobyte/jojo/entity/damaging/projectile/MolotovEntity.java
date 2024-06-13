package com.github.standobyte.jojo.entity.damaging.projectile;

import java.util.List;

import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCap;
import com.github.standobyte.jojo.entity.HamonSendoOverdriveEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

@OnlyIn(value = Dist.CLIENT, _interface = IRendersAsItem.class)
public class MolotovEntity extends ProjectileItemEntity implements IRendersAsItem {

    public MolotovEntity(EntityType<? extends MolotovEntity> type, World world) {
        super(type, world);
    }

    public MolotovEntity(World world, LivingEntity shooter) {
        super(ModEntityTypes.MOLOTOV.get(), shooter, world);
    }

    public MolotovEntity(World world, double x, double y, double z) {
        super(ModEntityTypes.MOLOTOV.get(), x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MOLOTOV.get();
    }
    
    @Override
    protected float getGravity() {
        return 0.05F;
    }
    
    @Override
    protected void onHit(RayTraceResult pResult) {
        super.onHit(pResult);
        if (!level.isClientSide) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.SPLASH_POTION_BREAK, 
                    SoundCategory.NEUTRAL, 1.0F, random.nextFloat() * 0.1F + 0.9F);
            remove();
        }
    }
    
    @Override
    protected void onHitBlock(BlockRayTraceResult pResult) {
        super.onHitBlock(pResult);
        if (!level.isClientSide) {
            setBlocksOnFire(pResult.getBlockPos(), 3);
            setEntitiesOnFire(pResult.getBlockPos(), 3);
        }
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult pResult) {
        super.onHitEntity(pResult);
        if (!level.isClientSide) {
            Entity entity = pResult.getEntity();
            Entity owner = getOwner();
            entity.hurt(owner != null ? new EntityDamageSource(DamageSource.IN_FIRE.msgId, owner) : DamageSource.IN_FIRE, 2);
            entity.setSecondsOnFire(10);
            setBlocksOnFire(this.blockPosition(), 2);
            setEntitiesOnFire(this.blockPosition(), 2);
        }
    }
    
    protected void setBlocksOnFire(BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) + Math.abs(y) + Math.abs(z) <= radius) {
                        BlockPos pos = center.offset(x, y, z);
                        if (level.isEmptyBlock(pos)) {
                            level.setBlockAndUpdate(pos, AbstractFireBlock.getState(level, pos));
                        }
                    }
                }
            }
        }
    }
    
    protected void setEntitiesOnFire(BlockPos center, double radius) {
        List<Entity> targets = level.getEntities(this, new AxisAlignedBB(center).inflate(radius));
        for (Entity target : targets) {
            if (target.distanceToSqr(center.getX(), center.getY(), center.getZ()) < radius * radius) {
                target.setSecondsOnFire(4);
            }
        }
    }
    
    public void onHitWithHamonCharge(RayTraceResult target, ProjectileHamonChargeCap bottleCharge) {
        if (!level.isClientSide()) {
            if (target.getType() == RayTraceResult.Type.BLOCK) {
                LivingEntity owner = getOwner() instanceof LivingEntity ? (LivingEntity) getOwner() : null;
                BlockRayTraceResult blockTarget = (BlockRayTraceResult) target;
                
                HamonSendoOverdriveEntity sendoOverdrive1 = new HamonSendoOverdriveEntity(level, 
                        owner, blockTarget.getDirection().getAxis())
                        .setRadius(3f)
                        .setWaveDamage(bottleCharge.getHamonDamage() / 4)
                        .setWavesCount(2);
                sendoOverdrive1.moveTo(Vector3d.atCenterOf(blockTarget.getBlockPos())
                        .subtract(0, sendoOverdrive1.getDimensions(null).height * 0.5, 0));
                sendoOverdrive1.setBlockTarget(blockTarget.getBlockPos(), blockTarget.getDirection());
                level.addFreshEntity(sendoOverdrive1);
                
                HamonSendoOverdriveEntity sendoOverdrive2 = new HamonSendoOverdriveEntity(level, 
                        owner, blockTarget.getDirection().getAxis())
                        .setRadius(2)
                        .setWaveDamage(bottleCharge.getHamonDamage() / 4)
                        .setWavesCount(4);
                sendoOverdrive2.moveTo(Vector3d.atCenterOf(blockTarget.getBlockPos())
                        .subtract(0, sendoOverdrive2.getDimensions(null).height * 0.5, 0));
                sendoOverdrive2.setBlockTarget(blockTarget.getBlockPos(), blockTarget.getDirection());
                level.addFreshEntity(sendoOverdrive2);
            }
        }
    }
    
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
