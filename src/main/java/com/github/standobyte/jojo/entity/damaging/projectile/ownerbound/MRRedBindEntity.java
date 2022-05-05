package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MRRedBindEntity extends OwnerBoundProjectileEntity {
    private IStandPower userStandPower;
    private EffectInstance stunEffect = null;
    private int ticksTargetClose = 0;

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
        }
        LivingEntity bound = getEntityAttachedTo();
        if (bound != null) {
            LivingEntity owner = getOwner();
            if (!bound.isAlive() || owner.distanceToSqr(bound) > 100) {
                if (!level.isClientSide()) {
                    remove();
                }
            }
            else {
                if (!level.isClientSide()) {
                    if (bound.getRemainingFireTicks() % 20 == 0 || bound.getRemainingFireTicks() <= 0) {
                        DamageUtil.setOnFire(bound, 3, true);
                    }
                }
                Vector3d vecToOwner = owner.position().subtract(bound.position());
                if (vecToOwner.lengthSqr() > 4) {
                    dragTarget(bound, vecToOwner.normalize().scale(0.2));
                    ticksTargetClose = 0;
                }
                else if (!level.isClientSide() && ticksTargetClose++ > 10) {
                    remove();
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
    public boolean isOnFire() {
        return false;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        if (getEntityAttachedTo() == null) {
            if (target instanceof LivingEntity && !target.isInvulnerableTo(getDamageSource(owner))) {
                LivingEntity targetLiving = (LivingEntity) target;
                attachToEntity(targetLiving);
                stunEffect = new EffectInstance(ModEffects.STUN.get(), ticksLifespan() - tickCount);
                targetLiving.addEffect(stunEffect);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void remove() {
        super.remove();
        if (!level.isClientSide() && stunEffect != null) {
            LivingEntity bound = getEntityAttachedTo();
            if (bound != null) {
                JojoModUtil.removeEffectInstance(bound, stunEffect);
            }
        }
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
        return isAttachedToAnEntity() ? 100 : 7;
    }
    
    @Override
    protected float movementSpeed() {
        return 1.0F;
    }
    
    @Override
    protected void updateMotionFlags() {}
}
