package com.github.standobyte.jojo.entity;

import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class CrimsonBubbleEntity extends Entity {
    private int hamonStrengthPoints;
    private int hamonControlPoints;
    private Vector3d initialPoint;

    public CrimsonBubbleEntity(World world) {
        this(ModEntityTypes.CRIMSON_BUBBLE.get(), world);
        setDeltaMovement(new Vector3d(
                random.nextDouble() - 0.5D, 
                (random.nextDouble() - 0.5D) * 0.5D, 
                random.nextDouble() - 0.5D).normalize().scale(0.002D));
    }

    public CrimsonBubbleEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    public void setHamonPoints(int hamonStrengthPoints, int hamonControlPoints) {
        this.hamonStrengthPoints = hamonStrengthPoints;
        this.hamonControlPoints = hamonControlPoints;
    }
    
    public void putItem(ItemEntity item) {
        item.setPickUpDelay(2);
        item.setExtendedLifetime();
        item.startRiding(this);
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0;
    }

    @Override
    public void tick() {
        if (!level.isClientSide()) {
            if (hamonControlPoints == 0 && hamonStrengthPoints == 0) {
                remove();
                return;
            }
            for (Entity entity : getPassengers()) {
                if (entity.getType() == EntityType.ITEM) {
                    ((ItemEntity) entity).setPickUpDelay(2);
                }
            }

            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox(), EntityPredicates.NO_CREATIVE_OR_SPECTATOR);
            for (LivingEntity entity : entities) {
                if (entity.isAlive()) {
                    collideWithEntity(entity);
                }
            }
            
            if (hamonStrengthPoints > 0 && random.nextFloat() < 0.2F) {
                hamonStrengthPoints--;
            }
            if (hamonControlPoints > 0 && random.nextFloat() < 0.2F) {
                hamonControlPoints--;
            }

            if (initialPoint != null) {
                if (tickCount % 100 == 0 || position().distanceToSqr(initialPoint) > 2.25D) {
                    Vector3d vecToInitialPos = initialPoint.subtract(position());
                    setDeltaMovement(vecToInitialPos.add(
                            new Vector3d(random.nextDouble() - 0.5D, 
                            (random.nextDouble() - 0.5D) * 0.5D, 
                            random.nextDouble() - 0.5D)).normalize().scale(0.002D));
                }
            }
            else {
                initialPoint = position().add(Vector3d.ZERO);
            }
        }
        move(MoverType.SELF, getDeltaMovement());
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    private void collideWithEntity(LivingEntity entity) {
        Optional<HamonData> hamonOptional = INonStandPower.getNonStandPowerOptional(entity).map(power -> 
        power.getTypeSpecificData(ModPowers.HAMON.get())).orElse(Optional.empty());
        if (hamonOptional.isPresent()) {
            HamonData hamon = hamonOptional.get();
            hamon.setHamonStatPoints(HamonStat.STRENGTH, hamon.getHamonStrengthPoints() + (int) (hamonStrengthPoints * 0.8), true, false);
            hamon.setHamonStatPoints(HamonStat.CONTROL, hamon.getHamonControlPoints() + (int) (hamonControlPoints * 0.8), true, false);
            HamonUtil.createHamonSparkParticlesEmitter(entity, 2.0F);
            if (hamon.characterIs(ModHamonSkills.CHARACTER_JOSEPH.get())) {
                JojoModUtil.sayVoiceLine(entity, ModSounds.JOSEPH_CRIMSON_BUBBLE_REACTION.get());
            }
            if (entity instanceof ServerPlayerEntity) {
                ModCriteriaTriggers.LAST_HAMON.get().trigger((ServerPlayerEntity) entity, this);
            }
            hamonStrengthPoints = 0;
            hamonControlPoints = 0;
        }
        else {
            DamageUtil.dealHamonDamage(entity, getDamage(), this, null);
            hamonStrengthPoints = Math.max(hamonStrengthPoints - 1, 0);
            hamonControlPoints = Math.max(hamonControlPoints - 1, 0);
        }
    }

    @Override
    public boolean hurt(DamageSource dmgSource, float amount) {
        if (this.isInvulnerableTo(dmgSource)) {
            return false;
        }
        if (dmgSource.isCreativePlayer()) {
            hamonControlPoints = 0;
            hamonStrengthPoints = 0;
        }
        else {
            if (dmgSource.getDirectEntity() instanceof LivingEntity) {
                DamageUtil.dealHamonDamage((LivingEntity) dmgSource.getDirectEntity(), getDamage(), this, null);
            }
            hamonStrengthPoints = Math.max(hamonStrengthPoints - (int) amount, 0);
            hamonControlPoints = Math.max(hamonControlPoints - (int) amount, 0);
        }
        return true;
    }

    private float getDamage() {
        return ((float) (hamonStrengthPoints + hamonControlPoints)) * 20F / (float) HamonData.MAX_HAMON_POINTS;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundNBT compound) {
        this.hamonStrengthPoints = compound.getInt("StrengthPoints");
        this.hamonControlPoints = compound.getInt("ControlPoints");
        if (compound.contains("InitialPoint", MCUtil.getNbtId(ListNBT.class))) {
            ListNBT listNBT = compound.getList("InitialPoint", MCUtil.getNbtId(DoubleNBT.class));
            if (listNBT.size() >= 3) {
                this.initialPoint = new Vector3d(listNBT.getDouble(0), listNBT.getDouble(1), listNBT.getDouble(2));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {
        compound.putInt("StrengthPoints", hamonStrengthPoints);
        compound.putInt("ControlPoints", hamonControlPoints);
        if (initialPoint != null) {
            compound.put("InitialPoint", this.newDoubleList(initialPoint.x, initialPoint.y, initialPoint.z));
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
