package com.github.standobyte.jojo.entity;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
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
            hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).orElse(null);
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
                (power == null || power.getHeldAction() != ModHamonActions.HAMON_PROJECTILE_SHIELD.get() || hamon == null)) {
            if (!level.isClientSide()) remove();
            return;
        }
        setPos(user.getX(), user.getY() - 0.25, user.getZ());
        level.getEntitiesOfClass(ProjectileEntity.class, getBoundingBox().inflate(4), 
                entity -> entity.isAlive()).forEach(projectile -> {
                    if (getBoundingBox().contains(projectile.position().add(projectile.getDeltaMovement()))) {
                        deflectProjectile(projectile);
                    }
                    else {
                        RayTraceResult rayTrace = ProjectileHelper.getHitResult(projectile, 
                                target -> !target.isSpectator() && target.isAlive() && !target.is(projectile.getOwner()));
                        if (rayTrace.getType() == RayTraceResult.Type.ENTITY && ((EntityRayTraceResult) rayTrace).getEntity() == this) {
                            deflectProjectile(projectile);
                        }
                    }
                });
    }
    
    @Override
    public void push(Entity entity) {}

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }
    
    @Override
    public boolean hurt(DamageSource dmgSource, float amount) {
        Entity projectile = dmgSource.getDirectEntity();
        if (projectile instanceof ProjectileEntity) {
            deflectProjectile((ProjectileEntity) projectile);
        }
        return false;
    }
    
    private void deflectProjectile(ProjectileEntity projectile) {
        if (projectile == null) return;
        float speed = (float) projectile.getDeltaMovement().length();
        if (power != null && hamon != null) {
            float energyCost = speed * 20;
            if (power.hasEnergy(energyCost)) {
                JojoModUtil.deflectProjectile(projectile, null);
            }
            if (!level.isClientSide()) {
                if (power.consumeEnergy(energyCost)) {
                    DamageUtil.dealHamonDamage(projectile, 0.1F, this, user);
                    hamon.hamonPointsFromAction(HamonStat.CONTROL, energyCost);
                }
                else {
                    power.setEnergy(0);
                    remove();
                }
            }
        }
        if (level.isClientSide()) {
            HamonPowerType.emitHamonSparkParticles(level, ClientUtil.getClientPlayer(), projectile.position(), speed / 2.5F);
        }
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
            this.power = INonStandPower.getNonStandPowerOptional(user).orElse(null);
            if (power != null) {
                hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).orElse(null);
            }
        }
    }

}
