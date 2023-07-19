package com.github.standobyte.jojo.power.impl.nonstand.type.hamon;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil.HamonAttackProperties;

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
    private float tickDamage;
    private int chargeTicks;
    private UUID hamonUserId;
    private Entity hamonUser;
    private boolean gavePoints;
    private float energySpent;
    
    public HamonCharge(float tickDamage, int chargeTicks, @Nullable LivingEntity hamonUser, float energySpent) {
        this.tickDamage = tickDamage;
        this.chargeTicks = chargeTicks;
        if (hamonUser != null) {
            this.hamonUserId = hamonUser.getUUID();
        }
        this.energySpent = energySpent;
    }
    
    public float getTickDamage() {
        return tickDamage;
    }
    
    public void tick(@Nullable Entity chargedEntity, @Nullable BlockPos chargedBlock, World world, AxisAlignedBB aabb) {
        if (!world.isClientSide() && (chargedEntity == null || chargedEntity.canUpdate())) {
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, aabb, EntityPredicates.NO_CREATIVE_OR_SPECTATOR);
            for (LivingEntity target : entities) {
                if (!target.is(chargedEntity) && target.isAlive() && target.getUUID() != hamonUserId) {
                    if (DamageUtil.dealHamonDamage(target, tickDamage, chargedEntity, null, HamonAttackProperties::noSrcEntityHamonMultiplier)) {
                        Entity user = getUserServerSide(world);
                        if (!target.isAlive() && user instanceof ServerPlayerEntity) {
                            ModCriteriaTriggers.HAMON_CHARGE_KILL.get().trigger((ServerPlayerEntity) user, target, chargedEntity, chargedBlock);
                        }
                        if (!gavePoints) {
                            if (user instanceof LivingEntity) {
                                INonStandPower.getNonStandPowerOptional((LivingEntity) user).ifPresent(power -> {
                                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
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
    
    public Entity getUserServerSide(World world) {
        if (hamonUser == null && world instanceof ServerWorld) {
            hamonUser = ((ServerWorld) world).getEntity(hamonUserId);
        }
        return hamonUser;
    }
    
    public boolean shouldBeRemoved() {
        return chargeTicks < 0;
    }
    
    
    
    public static HamonCharge fromNBT(CompoundNBT nbt) {
        HamonCharge charge = new HamonCharge(nbt.getFloat("Charge"), nbt.getInt("ChargeTicks"), null, nbt.getFloat("EnergySpent"));
        if (nbt.hasUUID("HamonUser")) {
            charge.hamonUserId = nbt.getUUID("HamonUser");
        }
        charge.gavePoints = nbt.getBoolean("GavePoints");
        return charge;
    }
    
    public CompoundNBT toNBT() {
        CompoundNBT chargeNbt = new CompoundNBT();
        chargeNbt.putFloat("Charge", tickDamage);
        chargeNbt.putInt("ChargeTicks", chargeTicks);
        if (hamonUserId != null) {
            chargeNbt.putUUID("HamonUser", hamonUserId);
        }
        chargeNbt.putBoolean("GavePoints", gavePoints);
        chargeNbt.putFloat("EnergySpent", energySpent);
        return chargeNbt;
    }

}
