package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.stand.ModStandActions;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HGStringEntity extends OwnerBoundProjectileEntity {
    private float yRotOffset;
    private float xRotOffset;
    private boolean isBinding;
    private boolean dealtDamage;
    private float knockback = 0;

    public HGStringEntity(World world, LivingEntity entity, float angleXZ, float angleYZ, boolean isBinding) {
        super(ModEntityTypes.HG_STRING.get(), entity, world);
        this.yRotOffset = angleXZ;
        this.xRotOffset = angleYZ;
        this.isBinding = isBinding;
    }
    
    public HGStringEntity(EntityType<? extends HGStringEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    public boolean isBinding() {
        return isBinding;
    }
    
    @Override
    public float getBaseDamage() {
        return isBinding ? 0.5F : 1F;
    }
    
    public void addKnockback(float knockback) {
        this.knockback = knockback;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        return !dealtDamage ? super.hurtTarget(target, owner) : false;
    }
    
    @Override
    protected boolean shouldHurtThroughInvulTicks() {
        return true;
    }
    
    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt) {
            dealtDamage = true;
            Entity target = entityRayTraceResult.getEntity();
            if (isBinding) {
                if (target instanceof LivingEntity) {
                    LivingEntity livingTarget = (LivingEntity) target;
                    attachToEntity(livingTarget);
                    livingTarget.addEffect(new EffectInstance(ModEffects.STUN.get(), ticksLifespan() - tickCount));
                }
            }
            else {
                if (target instanceof LivingEntity) {
                    DamageUtil.knockback((LivingEntity) target, knockback, yRot);
                }
                setIsRetracting(true);
            }
        }
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }

    @Override
    public int ticksLifespan() {
        return isBinding ? ModStandActions.HIEROPHANT_GREEN_STRING_BIND.get().getStandActionTicks(null, null) 
                + (isAttachedToAnEntity() ? 10 : 0)
                : ModStandActions.HIEROPHANT_GREEN_STRING_ATTACK.get().getStandActionTicks(null, null);
    }
    
    @Override
    protected float movementSpeed() {
        return isBinding ? 0.9F : 2.25F;
    }
    
    @Override
    public boolean isBodyPart() {
        return true;
    }
    
    private static final Vector3d FRONT_OFFSET = new Vector3d(0.0D, 0.0D, 0.25D);
    @Override
    protected Vector3d getXRotOffset() {
        return FRONT_OFFSET;
    }

    @Override
    protected Vector3d originOffset(float yRot, float xRot, double distance) {
        return super.originOffset(yRot + yRotOffset, xRot + xRotOffset, distance);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(yRotOffset);
        buffer.writeFloat(xRotOffset);
        buffer.writeBoolean(isBinding);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        this.yRotOffset = additionalData.readFloat();
        this.xRotOffset = additionalData.readFloat();
        this.isBinding = additionalData.readBoolean();
    }
}
