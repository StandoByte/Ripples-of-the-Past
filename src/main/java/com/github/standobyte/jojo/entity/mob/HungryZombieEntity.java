package com.github.standobyte.jojo.entity.mob;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.actions.VampirismBloodDrain;
import com.github.standobyte.jojo.entity.ai.ZombieFollowOwnerGoal;
import com.github.standobyte.jojo.entity.ai.ZombieOwnerHurtByTargetGoal;
import com.github.standobyte.jojo.entity.ai.ZombieOwnerHurtTargetGoal;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public class HungryZombieEntity extends ZombieEntity {
    protected static final DataParameter<Optional<UUID>> OWNER_UUID = EntityDataManager.defineId(HungryZombieEntity.class, DataSerializers.OPTIONAL_UUID);
    private boolean isOwnerNearby;

    public HungryZombieEntity(World world) {
        this(ModEntityTypes.HUNGRY_ZOMBIE.get(), world);
    }

    public HungryZombieEntity(EntityType<? extends HungryZombieEntity> type, World world) {
        super(type, world);
        xpReward *= 1.5;
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER_UUID, Optional.empty());
    }
    
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return ZombieEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(6, new ZombieFollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.targetSelector.addGoal(1, new ZombieOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new ZombieOwnerHurtTargetGoal(this));
    }

    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        if (getOwnerUUID() != null) {
            compound.putUUID("Owner", getOwnerUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        UUID ownerId = compound.hasUUID("Owner") ? compound.getUUID("Owner") : null;
        if (ownerId != null) {
            setOwnerUUID(ownerId);
        }
    }

    @Nullable
    private UUID getOwnerUUID() {
        return entityData.get(OWNER_UUID).orElse(null);
    }

    private void setOwnerUUID(@Nullable UUID uuid) {
        entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public void setOwner(LivingEntity owner) {
        setOwnerUUID(owner != null ? owner.getUUID() : null);
    }

    @Nullable
    public LivingEntity getOwner() {
        try {
            UUID uuid = this.getOwnerUUID();
            return uuid == null ? null : this.level.getPlayerByUUID(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    @Override
    protected int getExperienceReward(PlayerEntity player) {
        return getOwnerUUID() == null ? super.getExperienceReward(player) : 0;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) {
            LivingEntity owner = getOwner();
            isOwnerNearby = owner != null && distanceToSqr(owner) <= 144;
        }
    }
    
    public boolean isOwnerNearby() {
        return isOwnerNearby;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target.is(getOwner()) ? false : super.canAttack(target);
    }

    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity) owner).canHarmPlayer((PlayerEntity) target)) {
            return false;
        }
        return true;
    }

    @Override
    public Team getTeam() {
        LivingEntity owner = getOwner();
        if (owner != null) {
            return owner.getTeam();
        }
        return super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        LivingEntity owner = getOwner();
        if (entity == owner) {
            return true;
        }
        if (owner != null) {
            return owner.isAlliedTo(entity);
        }
        return super.isAlliedTo(entity);
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficulty) {}

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (getMainHandItem().isEmpty() && target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                if (livingTarget.getArmorCoverPercentage() <= random.nextFloat() && JojoModUtil.canBleed(livingTarget)) {
                    float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
                    VampirismBloodDrain.drainBlood(this, livingTarget, damage / 5F, damage / 2F);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void killed(ServerWorld world, LivingEntity entityDead) {
        createZombie(world, getOwner(), entityDead, isPersistenceRequired());
    }
    
    public static boolean createZombie(ServerWorld world, LivingEntity owner, LivingEntity dead, boolean makePersistent) {
        if ((world.getDifficulty() == Difficulty.NORMAL && dead.getRandom().nextBoolean() || world.getDifficulty() == Difficulty.HARD)) {
            HungryZombieEntity zombie;
            if ((dead instanceof VillagerEntity || dead instanceof AbstractIllagerEntity) 
                    && ForgeEventFactory.canLivingConvert(dead, ModEntityTypes.HUNGRY_ZOMBIE.get(), (timer) -> {})) {
                zombie = ((MobEntity) dead).convertTo(ModEntityTypes.HUNGRY_ZOMBIE.get(), true);
            }
            else {
                return false;
            }
            zombie.finalizeSpawn(
                    world, 
                    world.getCurrentDifficultyAt(zombie.blockPosition()), 
                    SpawnReason.CONVERSION, 
                    new ZombieEntity.GroupData(false, true), 
                    null);
            zombie.setOwner(owner);
            ForgeEventFactory.onLivingConvert(dead, zombie);
            if (!dead.isSilent()) {
                world.levelEvent(null, 1026, dead.blockPosition(), 0);
            }
            return true;
        }
        return false;
    }
}
