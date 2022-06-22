package com.github.standobyte.jojo.power.nonstand.type;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class HamonCharge {
    private float charge;
    private int chargeTicks;
    private UUID hamonUserId;
    private Entity hamonUser;
    private boolean gavePoints;
    private float energySpent;
    
    public HamonCharge(float charge, int chargeTicks, @Nullable LivingEntity hamonUser, float energySpent) {
        this.charge = charge;
        this.chargeTicks = chargeTicks;
        if (hamonUser != null) {
            this.hamonUserId = hamonUser.getUUID();
        }
        this.energySpent = energySpent;
    }
    
    public HamonCharge(CompoundNBT nbt) {
        this.charge = nbt.getFloat("Charge");
        this.chargeTicks = nbt.getInt("ChargeTicks");
        if (nbt.contains("HamonUser")) {
            this.hamonUserId = nbt.getUUID("HamonUser");
        }
        this.gavePoints = nbt.getBoolean("GavePoints");
        this.energySpent = nbt.getFloat("EnergySpent");
    }
    
    public CompoundNBT writeNBT() {
        CompoundNBT chargeNbt = new CompoundNBT();
        chargeNbt.putFloat("Charge", charge);
        chargeNbt.putInt("ChargeTicks", chargeTicks);
        if (hamonUserId != null) {
            chargeNbt.putUUID("HamonUser", hamonUserId);
        }
        chargeNbt.putBoolean("GavePoints", gavePoints);
        chargeNbt.putFloat("EnergySpent", energySpent);
        return chargeNbt;
    }
    
    public float getCharge() {
        return charge;
    }
    
    public void tick(@Nullable Entity chargedEntity, @Nullable BlockPos chargedBlock, World world, AxisAlignedBB aabb) {
        if (!world.isClientSide() && (chargedEntity == null || chargedEntity.canUpdate())) {
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, aabb, EntityPredicates.NO_CREATIVE_OR_SPECTATOR);
            for (LivingEntity target : entities) {
                if (!target.is(chargedEntity) && target.isAlive() && target.getUUID() != hamonUserId) {
                    if (DamageUtil.dealHamonDamage(target, charge, chargedEntity, null)) {
                        Entity user = getUser(world);
                        if (!target.isAlive() && user instanceof ServerPlayerEntity) {
                            ModCriteriaTriggers.HAMON_CHARGE_KILL.get().trigger((ServerPlayerEntity) user, target, chargedEntity, chargedBlock);
                        }
                        if (!gavePoints) {
                            if (user instanceof LivingEntity) {
                                INonStandPower.getNonStandPowerOptional((LivingEntity) user).ifPresent(power -> {
                                    power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, energySpent);
                                    });
                                });
                            }
                        }
                        gavePoints = true;
                    }
                }
            }
            chargeTicks--;
        }
    }
    
    private Entity getUser(World world) {
        if (hamonUser == null && world instanceof ServerWorld) {
            hamonUser = ((ServerWorld) world).getEntity(hamonUserId);
        }
        return hamonUser;
    }
    
    public boolean shouldBeRemoved() {
        return chargeTicks < 0;
    }

}
