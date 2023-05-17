package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import java.util.Optional;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.HandSide;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

// FIXME ! (hamon 2) sparks wave anim on hamon damage
// FIXME ! (hamon) dislocated bones sound
public class ZoomPunchEntity extends OwnerBoundProjectileEntity {
    private HandSide side;
    private float speed;
    private float hamonDamage;
    private float hamonDamageCost;
    private float points;
    private boolean gaveHamonPoints;
    private int lifeSpan;

    public ZoomPunchEntity(World world, LivingEntity entity, 
            float speed, int lifeSpan, 
            float hamonDamage, float hamonDamageCost, 
            float points) {
        super(ModEntityTypes.ZOOM_PUNCH.get(), entity, world);
        this.side = entity.getMainArm();
        this.speed = speed;
        this.lifeSpan = lifeSpan;
        this.hamonDamage = hamonDamage;
        this.hamonDamageCost = hamonDamageCost;
        this.points = points;
    }

    public ZoomPunchEntity(EntityType<? extends ZoomPunchEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean standDamage() {
        return false;
    }

    private static final Vector3d RIGHT_HAND_OFFSET = new Vector3d(-0.35D, -0.47D, 0.0D);
    private static final Vector3d LEFT_HAND_OFFSET = new Vector3d(-RIGHT_HAND_OFFSET.x, RIGHT_HAND_OFFSET.y, RIGHT_HAND_OFFSET.z);
    @Override
    public Vector3d getOwnerRelativeOffset() {
        return side == HandSide.LEFT ? LEFT_HAND_OFFSET : RIGHT_HAND_OFFSET;
    }
    
    public HandSide getSide() {
        return side;
    }

    @Override
    public boolean isBodyPart() {
        return true;
    }

    @Override
    public int ticksLifespan() {
        return lifeSpan;
    }
    
    @Override
    protected float movementSpeed() {
        return speed;
    }
    
    @Override
    public void remove() {
        super.remove();
        if (tickCount < ticksLifespan()) {
            INonStandPower.getNonStandPowerOptional(getOwner()).ifPresent(power -> {
                power.updateCooldownTimer(ModHamonActions.HAMON_ZOOM_PUNCH.get(), 0, ModHamonActions.HAMON_ZOOM_PUNCH.get().getCooldownTechnical(null));
            });
        }
    }

    @Override
    protected Vector3d originOffset(float yRot, float xRot, double distance) {
        return super.originOffset(yRot, xRot, distance + 0.75);
    }

    @Override
    public float getBaseDamage() {
        LivingEntity owner = getOwner();
        return owner != null ? DamageUtil.getDamageWithoutHeldItem(owner)
                : (float) Attributes.ATTACK_DAMAGE.getDefaultValue();
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }

    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        boolean regularAttack = super.hurtTarget(target, owner);
        boolean hamonAttack = getUserHamon().map(hamon -> {
            Boolean dealtDamage = hamon.consumeHamonEnergyTo(eff -> DamageUtil.dealHamonDamage(target, hamonDamage * eff, this, owner), hamonDamageCost);
            return dealtDamage != null && dealtDamage;
        }).orElse(false);
        
        if (regularAttack) {
            float knockback = (float) owner.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
            if (knockback > 0) {
                if (target instanceof LivingEntity) {
                    ((LivingEntity) target).knockback(knockback * 0.5F, 
                            (double) MathHelper.sin(owner.yRot * MathUtil.DEG_TO_RAD), 
                            (double)(-MathHelper.cos(owner.yRot * MathUtil.DEG_TO_RAD)));
                } else {
                    target.push(
                            (double)(-MathHelper.sin(owner.yRot * MathUtil.DEG_TO_RAD) * knockback * 0.5F), 
                            0.1D, 
                            (double)(MathHelper.cos(owner.yRot * MathUtil.DEG_TO_RAD) * knockback * 0.5F));
                }

                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                this.setSprinting(false);
            }
        }
        
        return regularAttack || hamonAttack;
    }

    @Override
    protected DamageSource getDamageSource(LivingEntity owner) {
        return new IndirectEntityDamageSource(owner instanceof PlayerEntity ? "player" : "mob", this, owner);
    }

    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt && !gaveHamonPoints) {
            getUserHamon().ifPresent(hamon -> {
                gaveHamonPoints = true;
                hamon.hamonPointsFromAction(HamonStat.STRENGTH, points);
            });
        }
    }
    
    private Optional<HamonData> hamon = Optional.empty();
    private Optional<HamonData> getUserHamon() {
        if (!hamon.isPresent()) {
            hamon = INonStandPower.getNonStandPowerOptional(getOwner())
                    .map(power -> power.getTypeSpecificData(ModPowers.HAMON.get())).orElse(hamon);
        }
        return hamon;
    }

    @Override
    public boolean isOnFire() {
        return getOwner() == null ? super.isOnFire() : getOwner().isOnFire();
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("LeftArm", side == HandSide.LEFT);
        nbt.putFloat("Speed", speed);
        nbt.putInt("LifeSpan", lifeSpan);
        nbt.putFloat("HamonDamage", hamonDamage);
        nbt.putFloat("HamonDamageCost", hamonDamageCost);
        nbt.putBoolean("PointsGiven", gaveHamonPoints);
        nbt.putFloat("Points", points);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        side = nbt.getBoolean("LeftArm") ? HandSide.LEFT : HandSide.RIGHT;
        speed = nbt.getFloat("Speed");
        lifeSpan = nbt.getInt("LifeSpan");
        hamonDamage = nbt.getFloat("HamonDamage");
        hamonDamageCost = nbt.getFloat("HamonDamageCost");
        gaveHamonPoints = nbt.getBoolean("PointsGiven");
        points = nbt.getFloat("Points");
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(side == HandSide.LEFT);
        buffer.writeFloat(speed);
        buffer.writeVarInt(lifeSpan);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        side = additionalData.readBoolean() ? HandSide.LEFT : HandSide.RIGHT;
        speed = additionalData.readFloat();
        lifeSpan = additionalData.readVarInt();
    }

}
