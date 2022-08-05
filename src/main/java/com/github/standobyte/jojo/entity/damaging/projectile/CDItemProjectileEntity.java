package com.github.standobyte.jojo.entity.damaging.projectile;

import java.util.Optional;
import java.util.UUID;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.network.NetworkUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CDItemProjectileEntity extends ModdedProjectileEntity {
    private Block block;
    private ResourceLocation blockTex = new ResourceLocation("textures/block/glass.png");
    private Optional<Entity> homingTarget;
    
    public CDItemProjectileEntity(LivingEntity shooter, World world) {
        super(ModEntityTypes.CD_ITEM_PROJECTILE.get(), shooter, world);
    }

    public CDItemProjectileEntity(EntityType<? extends CDItemProjectileEntity> type, World world) {
        super(type, world);
    }
    
    public void setTarget(Entity target) {
        this.homingTarget = Optional.ofNullable(target);
    }
    
    public void setBlock(Block block) {
        this.block = block;
        this.blockTex = block != null ? new ResourceLocation(
                block.getRegistryName().getNamespace(), 
                "textures/block/" + block.getRegistryName().getPath() + ".png")
                : new ResourceLocation("textures/block/glass.png");
    }
    
    public Block getBlock() {
        return block;
    }
    
    public ResourceLocation getBlockTex() {
        return blockTex;
    }

    @Override
    public int ticksLifespan() {
        return 6000;
    }

    @Override
    protected float getBaseDamage() {
     // FIXME !!!!!!!!!!!! projectile damage
        return 8;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return false;
    }

    private UUID targetUUID;
    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        homingTarget.ifPresent(target -> {
            nbt.putUUID("HomingTarget", target.getUUID());
        });
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.hasUUID("HomingTarget")) {
            targetUUID = nbt.getUUID("HomingTarget");
        }
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        if (targetUUID != null) {
            setTarget(((ServerWorld) level).getEntity(targetUUID));
        }
        super.writeSpawnData(buffer);
        NetworkUtil.writeOptional(buffer, block, () -> buffer.writeRegistryId(block));
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        setBlock(NetworkUtil.readOptional(additionalData, () -> additionalData.readRegistryIdSafe(Block.class)));
    }
}
