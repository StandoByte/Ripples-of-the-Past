package com.github.standobyte.jojo.entity;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class HamonProjectileShieldEntity extends Entity implements IEntityAdditionalSpawnData {
    private LivingEntity user;
    private INonStandPower power;
    private HamonData hamon;

    public HamonProjectileShieldEntity(World world, @Nonnull LivingEntity hamonUser) {
        this(ModEntityTypes.HAMON_PROJECTILE_SHIELD.get(), world);
        this.user = hamonUser;
        this.power = INonStandPower.getNonStandPowerOptional(hamonUser).orElse(null);
        if (power != null) {
            hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).orElse(null);
        }
        copyPosition(user);
    }

    public HamonProjectileShieldEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (user == null || !user.isAlive() || !level.isClientSide() && 
                (power == null || power.getHeldAction() != ModActions.HAMON_PROJECTILE_SHIELD.get() || hamon == null)) {
            remove();
            return;
        }
        copyPosition(user);
    }
    
    @Override
    public void push(Entity entity) {}

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }
    
    @Override
    public boolean hurt(DamageSource dmgSource, float amount) {
        Entity projectile = dmgSource.getDirectEntity();
        if (projectile instanceof ProjectileEntity) {
            if (!level.isClientSide()) {
                if (power != null && hamon != null) {
                    float energyCost = amount * 30;
                    if (power.consumeEnergy(energyCost)) {
                        if (projectile != null) {
                            DamageUtil.dealHamonDamage(projectile, 0.1F, this, user);
                            if (!(projectile instanceof AbstractArrowEntity)) {
                                projectile.setDeltaMovement(projectile.getDeltaMovement().reverse());
                                projectile.move(MoverType.SELF, projectile.getDeltaMovement());
                            }
                        }
                        hamon.hamonPointsFromAction(HamonStat.CONTROL, energyCost);
                    }
                    else {
                        power.setEnergy(0);
                        remove();
                    }
                }
            }
            else {
                HamonPowerType.createHamonSparkParticles(level, ClientUtil.getClientPlayer(), projectile.position(), amount / 5F);
            }
        }
        return false;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        // no save
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        // no save
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeInt(user.getId());
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        Entity entity = level.getEntity(additionalData.readInt());
        if (entity instanceof LivingEntity) {
            user = (LivingEntity) entity;
        }
    }

}
