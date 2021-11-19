package com.github.standobyte.jojo.util;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.util.damage.ModDamageSources;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class HamonCharge {
    private float charge;
    private int chargeTicks;
    private UUID hamonUserId;
    private boolean gavePoints;
    private float manaSpent;
    
    public HamonCharge(float charge, int chargeTicks, @Nullable LivingEntity hamonUser, float manaSpent) {
        this.charge = charge;
        this.chargeTicks = chargeTicks;
        if (hamonUser != null) {
            this.hamonUserId = hamonUser.getUUID();
        }
        this.manaSpent = manaSpent;
    }
    
    public HamonCharge(CompoundNBT nbt) {
        this.charge = nbt.getFloat("Charge");
        this.chargeTicks = nbt.getInt("ChargeTicks");
        if (nbt.contains("HamonUser")) {
            this.hamonUserId = nbt.getUUID("HamonUser");
        }
        this.gavePoints = nbt.getBoolean("GavePoints");
        this.manaSpent = nbt.getFloat("ManaSpent");
    }
    
    public CompoundNBT writeNBT() {
        CompoundNBT chargeNbt = new CompoundNBT();
        chargeNbt.putFloat("Charge", charge);
        chargeNbt.putInt("ChargeTicks", chargeTicks);
        if (hamonUserId != null) {
            chargeNbt.putUUID("HamonUser", hamonUserId);
        }
        chargeNbt.putBoolean("GavePoints", gavePoints);
        chargeNbt.putFloat("ManaSpent", manaSpent);
        return chargeNbt;
    }
    
    public float getCharge() {
        return charge;
    }
    
    public void tick(@Nullable Entity charged, World world, AxisAlignedBB aabb) {
        if (!world.isClientSide()) {
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, aabb, EntityPredicates.NO_CREATIVE_OR_SPECTATOR);
            for (LivingEntity target : entities) {
                if (!target.is(charged) && target.isAlive() && target.getUUID() != hamonUserId) {
                    if (ModDamageSources.dealHamonDamage(target, charge, charged, null)) {
                        if (!gavePoints) {
                            Entity user = ((ServerWorld) world).getEntity(hamonUserId);
                            if (user instanceof LivingEntity) {
                                INonStandPower.getNonStandPowerOptional((LivingEntity) user).ifPresent(power -> {
                                    power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, manaSpent);
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
    
    public boolean shouldBeRemoved() {
        return chargeTicks < 0;
    }

}
