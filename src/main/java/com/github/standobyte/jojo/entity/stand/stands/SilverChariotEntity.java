package com.github.standobyte.jojo.entity.stand.stands;

import java.util.UUID;

import com.github.standobyte.jojo.entity.damaging.projectile.SCRapierEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class SilverChariotEntity extends StandEntity {
    private static final UUID NO_ARMOR_MOVEMENT_SPEED_BOOST_ID = UUID.fromString("a31ffbee-5a26-4022-a298-59c839e5048d");
    private static final UUID NO_ARMOR_ATTACK_SPEED_BOOST_ID = UUID.fromString("c3e4ddb0-daa9-4cbb-acb9-dbc7eecad3f1");
    private static final UUID NO_RAPIER_DAMAGE_DECREASE_ID = UUID.fromString("84331a3b-73f1-4461-b240-6d688897e3f4");
    private static final UUID NO_RAPIER_ATTACK_SPEED_DECREASE_ID = UUID.fromString("485642f9-5475-4d74-8b54-dea9c53fe62e");
    private static final AttributeModifier NO_ARMOR_MOVEMENT_SPEED_BOOST = new AttributeModifier(NO_ARMOR_MOVEMENT_SPEED_BOOST_ID, "Movement speed boost with no armor", 2D, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier NO_ARMOR_ATTACK_SPEED_BOOST = new AttributeModifier(NO_ARMOR_ATTACK_SPEED_BOOST_ID, "Attack speed boost with no armor", 2D, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier NO_RAPIER_DAMAGE_DECREASE = new AttributeModifier(NO_RAPIER_DAMAGE_DECREASE_ID, "Attack damage decrease without rapier", -0.25D, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier NO_RAPIER_ATTACK_SPEED_DECREASE = new AttributeModifier(NO_RAPIER_ATTACK_SPEED_DECREASE_ID, "Attack speed decrease without rapier", -0.75D, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final DataParameter<Boolean> HAS_RAPIER = EntityDataManager.defineId(SilverChariotEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_ARMOR = EntityDataManager.defineId(SilverChariotEntity.class, DataSerializers.BOOLEAN);
    
    private int ticksAfterArmorRemoval;

    public SilverChariotEntity(StandEntityType<SilverChariotEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(HAS_RAPIER, true);
        entityData.define(HAS_ARMOR, true);
    }

    @Override
    public void rangedAttackTick(int ticks, boolean shift) {
        if (!level.isClientSide() && hasRapier()) {
            SCRapierEntity rapierEntity = new SCRapierEntity(this, level);
            rapierEntity.shootFromRotation(this, 1F, 0.0F);
            level.addFreshEntity(rapierEntity);
            setRapier(false);
        }
    }

    @Override
    public double getMeleeAttackRange() {
        return hasRapier() ? super.getMeleeAttackRange() + 1 : super.getMeleeAttackRange();
    }

    @Override
    public void swing(Hand hand) {
        if (hasRapier()) {
            super.swing(Hand.MAIN_HAND);
        }
        else {
            super.swing(hand);
        }
    }

    public boolean hasRapier() {
        return entityData.get(HAS_RAPIER);
    }

    public void setRapier(boolean rapier) {
        entityData.set(HAS_RAPIER, rapier);
        ModifiableAttributeInstance attackDamage = getAttribute(Attributes.ATTACK_DAMAGE);
        ModifiableAttributeInstance attackSpeed = getAttribute(Attributes.ATTACK_SPEED);
        if (attackDamage.getModifier(NO_RAPIER_DAMAGE_DECREASE_ID) != null) {
            attackDamage.removeModifier(NO_RAPIER_DAMAGE_DECREASE_ID);
        }
        if (attackSpeed.getModifier(NO_RAPIER_ATTACK_SPEED_DECREASE_ID) != null) {
            attackSpeed.removeModifier(NO_RAPIER_ATTACK_SPEED_DECREASE_ID);
        }

        if (!rapier) {
            attackDamage.addPermanentModifier(NO_RAPIER_DAMAGE_DECREASE);
            attackSpeed.addPermanentModifier(NO_RAPIER_ATTACK_SPEED_DECREASE);
        }
    }
    
    @Override
    public boolean canAttackRanged() {
        return hasRapier();
    }

    public boolean hasArmor() {
        return entityData.get(HAS_ARMOR);
    }

    public void setArmor(boolean armor) {
        entityData.set(HAS_ARMOR, armor);
        ModifiableAttributeInstance movementSpeed = getAttribute(Attributes.MOVEMENT_SPEED);
        ModifiableAttributeInstance attackSpeed = getAttribute(Attributes.ATTACK_SPEED);
        if (movementSpeed.getModifier(NO_ARMOR_MOVEMENT_SPEED_BOOST_ID) != null) {
            movementSpeed.removeModifier(NO_ARMOR_MOVEMENT_SPEED_BOOST_ID);
        }
        if (attackSpeed.getModifier(NO_ARMOR_ATTACK_SPEED_BOOST_ID) != null) {
            attackSpeed.removeModifier(NO_ARMOR_ATTACK_SPEED_BOOST_ID);
        }

        if (!armor) {
            movementSpeed.addPermanentModifier(NO_ARMOR_MOVEMENT_SPEED_BOOST);
            attackSpeed.addPermanentModifier(NO_ARMOR_ATTACK_SPEED_BOOST);
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide()) {
            if (!hasArmor()) {
                ticksAfterArmorRemoval++;
            }
            else {
                ticksAfterArmorRemoval = 0;
            }
        }
    }
    
    public int getTicksAfterArmorRemoval() {
        return ticksAfterArmorRemoval;
    }

    public int getArmorValue() {
        return hasArmor() ? super.getArmorValue() : 0;
    }
    
    @Override
    protected boolean canBreakBlock(float blockHardness, int blockHarvestLevel) {
        return blockHardness <= 1 && blockHarvestLevel <= 0;
    }

    @Override
    public boolean attackEntity(Entity target, boolean strongAttack, double attackDistance) {
        if (target instanceof ProjectileEntity) {
            target.setDeltaMovement(target.getDeltaMovement().reverse());
            target.move(MoverType.SELF, target.getDeltaMovement());
            return true;
        }
        else {
            return super.attackEntity(target, strongAttack, attackDistance);
        }
    }
    
    @Override
    protected double getAttackDamage(Entity target, boolean strongAttack, double rangeFactor, double attackDistance, double precision) {
        double damage = super.getAttackDamage(target, strongAttack, rangeFactor, attackDistance, precision);
        if (target instanceof SkeletonEntity) {
            damage *= 0.1;
        }
        return damage;
    }
}
