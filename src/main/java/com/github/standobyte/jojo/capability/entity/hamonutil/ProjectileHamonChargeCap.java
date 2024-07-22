package com.github.standobyte.jojo.capability.entity.hamonutil;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.entity.damaging.projectile.MolotovEntity;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonEntityChargePacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil.HamonAttackProperties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.EggEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ProjectileHamonChargeCap {
    private static final Random RANDOM = new Random();
    
    @Nonnull private final Entity projectile;
    private float hamonBaseDmg;
    private int tickCount;
    private int maxChargeTicks;
    private boolean hasCharge;
    
    private boolean multiplyWithUserStrength;
    private float spentEnergy;
    
    public ProjectileHamonChargeCap(Entity projectile) {
        this.projectile = projectile;
    }
    
    public void setBaseDmg(float damage) {
        this.hamonBaseDmg = damage;
        setHasCharge(damage > 0);
    }
    
    public void setMaxChargeTicks(int ticks) {
        this.maxChargeTicks = ticks;
    }
    
    public void setInfiniteChargeTime() {
        this.maxChargeTicks = -1;
    }
    
    public void setSpentEnergy(float spentEnergy) {
        this.spentEnergy = spentEnergy;
    }
    
    public void setMultiplyWithUserStrength(boolean doMultipy) {
        this.multiplyWithUserStrength = doMultipy;
    }
    
    
    
    public void tick() {
        if (!hasCharge || !projectile.canUpdate()) return;
        
        if (!projectile.level.isClientSide()) {
            if (chargeWearsOff() && tickCount++ > maxChargeTicks) {
                setHasCharge(false);
            }
        }
        else {
            if (!chargeWearsOff() || tickCount++ <= maxChargeTicks) {
                float chargeWearOff = chargeWearOffMultiplier();
                Vector3d pos = projectile.position();
                HamonSparksLoopSound.playSparkSound(projectile, pos, chargeWearOff);
                if (chargeWearOff == 1 || RANDOM.nextFloat() < chargeWearOff) {
                    CustomParticlesHelper.createHamonSparkParticles(projectile, pos.x, pos.y, pos.z, 1);
                }
            }
        }
    }
    
    public float getHamonDamage() {
        return 3 * hamonBaseDmg * chargeWearOffMultiplier();
    }
    
    private float chargeWearOffMultiplier() {
        if (chargeWearsOff()) {
            return MathHelper.clamp((float) (maxChargeTicks - tickCount) / (float) maxChargeTicks, 0.25F, 1F);
        }
        else {
            return 1;
        }
    }
    
    private boolean chargeWearsOff() {
        return maxChargeTicks >= 0;
    }
    
    public void onTargetHit(RayTraceResult target) {
        World world = projectile.level;
        if (hasCharge && !world.isClientSide()) {
            // adds hamon damage to splash water bottle
            if (projectile instanceof PotionEntity) {
                Vector3d pos = projectile.getBoundingBox().getCenter();
                PotionEntity potionEntity = (PotionEntity) projectile;
                if (MCUtil.isPotionWaterBottle(potionEntity)) {
                    AxisAlignedBB waterSplashArea = projectile.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
                    List<LivingEntity> splashedEntity = world.getEntitiesOfClass(LivingEntity.class, waterSplashArea, 
                            EntityPredicates.LIVING_ENTITY_STILL_ALIVE.and(EntityPredicates.NO_SPECTATORS).and(entity -> !entity.is(potionEntity.getOwner())));
                    if (!splashedEntity.isEmpty()) {
                        for (LivingEntity targetEntity : splashedEntity) {
                            double distSqr = targetEntity.distanceToSqr(pos);
                            if (distSqr < 16.0) {
                                dealHamonDamageToTarget(targetEntity, getHamonDamage() * (1 - (float) (distSqr / 16)));
                            }
                        }
                    }
                    
                    world.playSound(null, pos.x, pos.y, pos.z, ModSounds.HAMON_SPARK.get(), 
                            SoundCategory.AMBIENT, 0.1F, 1.0F + (world.random.nextFloat() - 0.5F) * 0.15F);
                    ((ServerWorld) world).sendParticles(ModParticles.HAMON_SPARK.get(), 
                            pos.x, pos.y, pos.z, 32, 0.75, 0.05, 0.75, 0.25);
                }
                
                return;
            }
            
            // add hamon damage on direct hit
            if (target.getType() == RayTraceResult.Type.ENTITY) {
                dealHamonDamageToTarget(((EntityRayTraceResult) target).getEntity(), getHamonDamage());
            }
            
            // memorize charged egg entity to potentially charge the chicken(s) coming out of it
            if (projectile instanceof EggEntity) {
                world.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.addChargedEggEntity((EggEntity) projectile));
            }
            else if (projectile instanceof MolotovEntity) {
                ((MolotovEntity) projectile).onHitWithHamonCharge(target, this);
            }
        }
    }
    
    private boolean dealHamonDamageToTarget(Entity entity, float damage) {
        if (damage > 0) {
            Entity owner = getProjectileOwner();
            if (multiplyWithUserStrength) {
                if (owner instanceof LivingEntity) {
                    LivingEntity hamonUser = (LivingEntity) owner;
                    return GeneralUtil.orElseFalse(INonStandPower.getNonStandPowerOptional(hamonUser), power -> {
                        return GeneralUtil.orElseFalse(power.getTypeSpecificData(ModPowers.HAMON.get()), hamon -> {
                            if (DamageUtil.dealHamonDamage(entity, getHamonDamage(), projectile, owner)) {
                                hamon.hamonPointsFromAction(HamonStat.STRENGTH, spentEnergy);
                                return true;
                            }
                            return false;
                        });
                    });
                }
            }
            
            return DamageUtil.dealHamonDamage(entity, getHamonDamage(), projectile, owner, 
                    HamonAttackProperties::noSrcEntityHamonMultiplier);
        }
        
        return false;
    }
    
    @Nullable
    private Entity getProjectileOwner() {
        if (projectile instanceof ProjectileEntity) {
            return ((ProjectileEntity) projectile).getOwner();
        }
        return null;
    }
    
    private void setHasCharge(boolean hasCharge) {
        this.hasCharge = hasCharge;
        PacketManager.sendToClientsTracking(TrHamonEntityChargePacket
                .projectileCharge(projectile.getId(), hasCharge, tickCount, maxChargeTicks), projectile);
    }
    
    public void onTracking(ServerPlayerEntity tracking) {
        if (hasCharge) {
            PacketManager.sendToClient(TrHamonEntityChargePacket
                    .projectileCharge(projectile.getId(), hasCharge, tickCount, maxChargeTicks), tracking);
        }
    }
    
    public void handleMsgFromServer(TrHamonEntityChargePacket msg) {
        this.hasCharge = msg.hasCharge;
        this.tickCount = msg.tickCount;
        this.maxChargeTicks = msg.maxTicks;
    }
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("HamonDamage", hamonBaseDmg);
        nbt.putInt("ChargeAge", tickCount);
        nbt.putInt("ChargeTicks", maxChargeTicks);
        nbt.putFloat("SpentEnergy", spentEnergy);
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        this.hamonBaseDmg = nbt.getFloat("HamonDamage");
        this.tickCount = nbt.getInt("ChargeAge");
        this.maxChargeTicks = nbt.getInt("ChargeTicks");
        this.spentEnergy = nbt.getFloat("SpentEnergy");
        this.hasCharge = hamonBaseDmg > 0 && tickCount > maxChargeTicks;
    }
}
