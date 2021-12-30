package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MRRedBindEntity extends OwnerBoundProjectileEntity {
    private IStandPower userStandPower;

    public MRRedBindEntity(World world, LivingEntity entity, IStandPower userStand) {
        super(ModEntityTypes.MR_RED_BIND.get(), entity, world);
        this.userStandPower = userStand;
    }
    
    public MRRedBindEntity(EntityType<? extends MRRedBindEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    public void tick() {
        if (isInWaterOrRain()) {
            clearFire();
            return;
        }
        super.tick();
        if (!isAlive()) {
            return;
        }
        if (!level.isClientSide()) {
            if (userStandPower == null || userStandPower.getHeldAction() != ModActions.MAGICIANS_RED_RED_BIND.get()) {
                remove();
                return;
            }
            LivingEntity bound = getEntityAttachedTo();
            if (bound != null) {
                LivingEntity owner = getOwner();
                if (!bound.isAlive() || owner.distanceToSqr(bound) > 100) {
                    remove();
                }
                else {
                    bound.addEffect(new EffectInstance(ModEffects.STUN.get(), 60));
                    if (bound.getRemainingFireTicks() % 20 == 0 || bound.getRemainingFireTicks() <= 0) {
                        bound.setSecondsOnFire(3);
                    }
                    Vector3d vecToOwner = owner.position().subtract(bound.position());
                    if (vecToOwner.lengthSqr() > 2.25) {
                        bound.move(MoverType.PLAYER, vecToOwner.normalize().scale(0.15D));
                    }
                    else {
                        remove();
                    }
                }
            }
        }
    }
    
    @Override
    public void clearFire() {
        super.clearFire();
        if (!level.isClientSide()) {
            JojoModUtil.extinguishFieryStandEntity(this, (ServerWorld) level);
        }
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        if (getEntityAttachedTo() == null) {
            if (target instanceof LivingEntity) {
                attachToEntity((LivingEntity) target);
                return true;
            }
        }
        return false;
    }

    private static final Vector3d OFFSET = new Vector3d(0, -0.25, 0.5);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET;
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    @Override
    public float getBaseDamage() {
        return 0.0F;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }
    
    @Override
    protected int ticksLifespan() {
        return getEntityAttachedTo() == null ? 10 : 100;
    }
    
    @Override
    protected float movementSpeed() {
        return 0.75F;
    }
    
    @Override
    protected void checkRetract() {}
}
