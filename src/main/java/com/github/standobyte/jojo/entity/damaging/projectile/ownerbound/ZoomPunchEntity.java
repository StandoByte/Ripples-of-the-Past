package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import java.util.Collection;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.util.damage.ModDamageSources;
import com.google.common.collect.Multimap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.HandSide;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class ZoomPunchEntity extends OwnerBoundProjectileEntity {
    private HandSide side;
    private int hamonControlLvl;
    private boolean gaveHamonPoints;

    public ZoomPunchEntity(World world, LivingEntity entity, int hamonControlLvl) {
        super(ModEntityTypes.ZOOM_PUNCH.get(), entity, world);
        this.side = entity.getMainArm();
        this.hamonControlLvl = hamonControlLvl;
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
    protected boolean isBodyPart() {
        return true;
    }

    @Override
    protected int ticksLifespan() {
        return ModActions.HAMON_ZOOM_PUNCH.get().getCooldownValue();
    }
    
    @Override
    protected float movementSpeed() {
        return (4 + (float) hamonControlLvl * 0.05F) / 7F;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide()) {
            HamonPowerType.createHamonSparkParticles(level, ClientUtil.getClientPlayer(), getX(), getY(0.5), getZ(), 0.1F);
        }
    }
    
    @Override
    public void remove() {
        super.remove();
        if (tickCount < ticksLifespan()) {
            INonStandPower.getNonStandPowerOptional(getOwner()).ifPresent(power -> {
                power.setCooldownTimer(ModActions.HAMON_ZOOM_PUNCH.get(), 0, ModActions.HAMON_ZOOM_PUNCH.get().getCooldownValue());
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
        ItemStack heldItem = owner.getMainHandItem();
        if (!heldItem.isEmpty()) {
            Multimap<Attribute, AttributeModifier> itemModifiers = heldItem.getAttributeModifiers(EquipmentSlotType.MAINHAND);
            if (itemModifiers.containsKey(Attributes.ATTACK_DAMAGE)) {
                ModifiableAttributeInstance attackDamageAttribute = owner.getAttribute(Attributes.ATTACK_DAMAGE);
                Collection<AttributeModifier> attackDamageModifiers = itemModifiers.get(Attributes.ATTACK_DAMAGE);
                attackDamageModifiers.forEach(attackDamageAttribute::removeModifier);
                float damage = (float) attackDamageAttribute.getValue();
                attackDamageModifiers.forEach(attackDamageAttribute::addTransientModifier);
                return damage;
            }
        }
        return (float) getOwner().getAttributeValue(Attributes.ATTACK_DAMAGE);
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 0.0F;
    }

    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        boolean regularAttack = super.hurtTarget(target, owner);
        boolean hamonAttack = ModDamageSources.dealHamonDamage(target, 0.125F, this, owner);
        return regularAttack || hamonAttack;
    }

    @Override
    protected DamageSource getDamageSource(LivingEntity owner) {
        return new IndirectEntityDamageSource(owner instanceof PlayerEntity ? "player" : "mob", this, owner);
    }

    @Override
    protected void afterEntityHit(EntityRayTraceResult entityRayTraceResult, boolean entityHurt) {
        if (entityHurt && !gaveHamonPoints) {
            INonStandPower.getNonStandPowerOptional(getOwner()).ifPresent(power -> {
                power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                    gaveHamonPoints = true;
                    hamon.hamonPointsFromAction(HamonStat.STRENGTH, ModActions.HAMON_ZOOM_PUNCH.get().getEnergyCost(power));
                });
            });
        }
    }

    @Override
    public boolean isOnFire() {
        return getOwner().isOnFire();
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("LeftArm", side == HandSide.LEFT);
        nbt.putInt("HamonControl", hamonControlLvl);
        nbt.putBoolean("PointsGiven", gaveHamonPoints);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        side = nbt.getBoolean("LeftArm") ? HandSide.LEFT : HandSide.RIGHT;
        hamonControlLvl = nbt.getInt("HamonControl");
        gaveHamonPoints = nbt.getBoolean("PointsGiven");
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(side == HandSide.LEFT);
        buffer.writeVarInt(hamonControlLvl);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        super.readSpawnData(additionalData);
        side = additionalData.readBoolean() ? HandSide.LEFT : HandSide.RIGHT;
        hamonControlLvl = additionalData.readVarInt();
    }

}
