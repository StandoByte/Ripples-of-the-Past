package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class HamonBubbleBarrierEntity extends ModdedProjectileEntity {
    private int barrierTicks;
    private int barrierMaxTicks;
    private boolean barrier;
    private boolean shot;
    private INonStandPower power;
    
    public HamonBubbleBarrierEntity(World world, LivingEntity shooter, INonStandPower power) {
        super(ModEntityTypes.HAMON_BUBBLE_BARRIER.get(), shooter, world);
        this.power = power;
        barrierMaxTicks = (int) (100F * power.getTypeSpecificData(ModPowers.HAMON.get())
                .map(hamon -> hamon.getActionEfficiency(0, true)).orElse(1F));
    }

    public HamonBubbleBarrierEntity(EntityType<? extends HamonBubbleBarrierEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) { 
            if (barrier && (barrierTicks++ >= barrierMaxTicks || !isVehicle()) || power == null) {
                remove();
            }
            else if (!shot) {
                if (power.getHeldAction() != ModHamonActions.CAESAR_BUBBLE_BARRIER.get()) {
                    remove();
                }
                else if (power.getHeldActionTicks() >= ModHamonActions.CAESAR_BUBBLE_BARRIER.get().getHoldDurationToFire(power) - 1) {
                    Entity owner = getOwner();
                    shootFromRotation(owner != null ? owner : this, 1.0F, 0.0F);
                    shot = true;
                }
            }
            else if (isVehicle() && tickCount % 5 % 2 == 0) {
                DamageUtil.dealHamonDamage(getPassengers().get(0), 0.002F, this, getOwner());
            }
        }
        else {
            Vector3d sparkVec = Vector3d.directionFromRotation(random.nextFloat() * 360F, random.nextFloat() * 360F)
                    .scale(getBbWidth() / 2).add(getX(), getY(0.5), getZ());
            // FIXME ! (hamon 2) sfx
            HamonUtil.emitHamonSparkParticles(level, ClientUtil.getClientPlayer(), sparkVec, 0.1F);
        }
    }
    
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (!level.isClientSide()) {
            getPassengers().forEach(entity -> {
                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).removeEffect(ModStatusEffects.STUN.get());
                }
            });
        }
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return DamageUtil.dealHamonDamage(target, 0.1F, this, owner);
    }

    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            Entity target = entityRayTraceResult.getEntity();
            if (target instanceof LivingEntity && target.startRiding(this)) {
                barrier = true;
                ((LivingEntity) target).addEffect(new EffectInstance(ModStatusEffects.STUN.get(), barrierMaxTicks));
                setDeltaMovement(new Vector3d(0, 0.05D, 0));
            }
            LivingEntity owner = getOwner();
            if (owner != null) {
                INonStandPower.getNonStandPowerOptional(owner).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, ModHamonActions.CAESAR_BUBBLE_BARRIER.get().getHeldTickEnergyCost(power) / 4F);
                    });
                });
            }
        }
    }
    
    @Override
    protected void onHitBlock(BlockRayTraceResult blockRayTraceResult) {
        super.onHitBlock(blockRayTraceResult);
        if (blockRayTraceResult.getDirection().getAxis() == Axis.Y) {
            setDeltaMovement(getDeltaMovement().subtract(0, getDeltaMovement().y, 0));
        }
        else {
            setDeltaMovement(getDeltaMovement().subtract(getDeltaMovement().x, 0, getDeltaMovement().z));
        }
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, RayTraceResult hitTarget) {
        if (targetType != TargetType.ENTITY && !isVehicle()) {
            super.breakProjectile(targetType, hitTarget);
        }
    }

    @Override
    public float getBaseDamage() {
        return 0;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    public float getSize(float partialTick) {
        return Math.min((tickCount + partialTick) / (float) ModHamonActions.CAESAR_BUBBLE_BARRIER.get().getHoldDurationToFire(null), 1);
    }

    @Override
    public void positionRider(Entity entity) {
       if (hasPassenger(entity)) {
           entity.setPos(getX(), getY() + (getBbHeight() - entity.getBbHeight()) / 2, getZ());
        }
    }
    
    @Override
    public double getPassengersRidingOffset() {
        return getBbHeight() / 2;
    }
    
    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (barrier) {
            nbt.putBoolean("Barrier", barrier);
            nbt.putInt("BarrierTicks", barrierTicks);
        }
        nbt.putInt("BarrierTicksMax", barrierMaxTicks);
        nbt.putBoolean("Shot", shot);
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        this.barrier = nbt.getBoolean("Barrier");
        this.barrierTicks = nbt.getInt("BarrierTicks");
        this.barrierMaxTicks = nbt.getInt("BarrierTicksMax");
        this.shot = nbt.getBoolean("Shot");
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public int ticksLifespan() {
        return barrier ? 100 : 100 + barrierMaxTicks;
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeVarInt(barrierMaxTicks);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        this.barrierMaxTicks = additionalData.readVarInt();
    }
}
