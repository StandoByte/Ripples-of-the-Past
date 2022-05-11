package com.github.standobyte.jojo.entity.stand.stands;

import java.util.List;
import java.util.UUID;

import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.entity.damaging.projectile.SCRapierEntity;
import com.github.standobyte.jojo.entity.stand.StandAttackProperties;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class SilverChariotEntity extends StandEntity {
    private static final AttributeModifier NO_ARMOR_MOVEMENT_SPEED_BOOST = new AttributeModifier(
            UUID.fromString("a31ffbee-5a26-4022-a298-59c839e5048d"), "Movement speed boost with no armor", 1.5, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier NO_ARMOR_ATTACK_SPEED_BOOST = new AttributeModifier(
            UUID.fromString("c3e4ddb0-daa9-4cbb-acb9-dbc7eecad3f1"), "Attack speed boost with no armor", 1, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier NO_ARMOR = new AttributeModifier(
            UUID.fromString("d4987f5f-55e8-45db-9a5e-b2fd0a98c2ec"), "No armor", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier NO_ARMOR_TOUGHNESS = new AttributeModifier(
            UUID.fromString("8dfd5e42-a578-4f4a-aafd-b86ef965b9f3"), "No armor toughness", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier NO_ARMOR_DURABILITY_DECREASE = new AttributeModifier(
            UUID.fromString("47c93a42-b04f-44f3-97be-5f542d97c000"), "No durability without armor", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    
    public static final AttributeModifier NO_RAPIER_DAMAGE_DECREASE = new AttributeModifier(
            UUID.fromString("84331a3b-73f1-4461-b240-6d688897e3f4"), "Attack damage decrease without rapier", -0.25, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier NO_RAPIER_ATTACK_SPEED_DECREASE = new AttributeModifier(
            UUID.fromString("485642f9-5475-4d74-8b54-dea9c53fe62e"), "Attack speed decrease without rapier", -0.75, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final double RAPIER_RANGE = 1;
    private static final AttributeModifier NO_RAPIER_ATTACK_RANGE_DECREASE = new AttributeModifier(
            UUID.fromString("ba319644-fab3-4d4c-bcdf-26fd05dd62f5"), "Attack range decrease without rapier", -RAPIER_RANGE, AttributeModifier.Operation.ADDITION);
    
    private static final DataParameter<Boolean> HAS_RAPIER = EntityDataManager.defineId(SilverChariotEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_ARMOR = EntityDataManager.defineId(SilverChariotEntity.class, DataSerializers.BOOLEAN);
    
    private int ticksAfterArmorRemoval;
    private Vector3d dashVec = Vector3d.ZERO;

    public SilverChariotEntity(StandEntityType<SilverChariotEntity> type, World world) {
        super(type, world);
        getAttribute(Attributes.ARMOR).setBaseValue(15);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(HAS_RAPIER, true);
        entityData.define(HAS_ARMOR, true);
    }

    @Override
    public double getDefaultMeleeAttackRange() {
        return super.getDefaultMeleeAttackRange() + RAPIER_RANGE;
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
        updateModifier(getAttribute(Attributes.ATTACK_DAMAGE), NO_RAPIER_DAMAGE_DECREASE, !rapier);
        updateModifier(getAttribute(Attributes.ATTACK_SPEED), NO_RAPIER_ATTACK_SPEED_DECREASE, !rapier);
        updateModifier(getAttribute(ForgeMod.REACH_DISTANCE.get()), NO_RAPIER_ATTACK_RANGE_DECREASE, !rapier);
    }

    public boolean hasArmor() {
        return entityData.get(HAS_ARMOR);
    }

    public void setArmor(boolean armor) {
        entityData.set(HAS_ARMOR, armor);
        updateModifier(getAttribute(Attributes.MOVEMENT_SPEED), NO_ARMOR_MOVEMENT_SPEED_BOOST, !armor);
        updateModifier(getAttribute(Attributes.ATTACK_SPEED), NO_ARMOR_ATTACK_SPEED_BOOST, !armor);
        updateModifier(getAttribute(Attributes.ARMOR), NO_ARMOR, !armor);
        updateModifier(getAttribute(Attributes.ARMOR_TOUGHNESS), NO_ARMOR_TOUGHNESS, !armor);
        updateModifier(getAttribute(ModEntityAttributes.STAND_DURABILITY.get()), NO_ARMOR_DURABILITY_DECREASE, !armor);
    }
    
    @Override
    protected float getPhysicalResistance(float blockedRatio) {
        return StandStatFormulas.getPhysicalResistance(0, 0, blockedRatio);
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
            List<Entity> entities = level.getEntities(this, getBoundingBox());
            for (Entity entity : entities) {
                if (entity.isAlive() && entity.getType() == ModEntityTypes.SC_RAPIER.get()) {
                    ((SCRapierEntity) entity).takeRapier(this);
                }
            }
        }
    }
    
    public int getTicksAfterArmorRemoval() {
        return ticksAfterArmorRemoval;
    }
    
    public void setDashVec(Vector3d dashVec) {
        this.dashVec = dashVec;
    }
    
    public Vector3d getDashVec() {
        return dashVec;
    }
    
    @Override
    public HandSide getSwingingHand() {
        return hasRapier() ? HandSide.RIGHT : super.getSwingingHand();
    }
    
    @Override
    protected boolean canBreakBlock(float blockHardness, int blockHarvestLevel) {
        if (hasRapier()) {
            return blockHardness <= 1 && blockHarvestLevel <= 0;
        }
        return super.canBreakBlock(blockHardness, blockHarvestLevel);
    }

    @Override
    public boolean attackEntity(Entity target, PunchType punch, StandEntityAction action, int barrageHits) {
        if (target instanceof ProjectileEntity) {
            if (target.getType() != ModEntityTypes.SPACE_RIPPER_STINGY_EYES.get()) {
                JojoModUtil.deflectProjectile(target, getLookAngle());
                return true;
            }
            return false;
        }
        else {
            return super.attackEntity(target, punch, action, barrageHits);
        }
    }
    
    @Override
    protected StandAttackProperties standAttackProperties(PunchType punchType, Entity target, StandEntityAction action,
            double strength, double precision, double attackRange, double distance, double knockback, int barrageHits) {
        StandAttackProperties attack = super.standAttackProperties(punchType, target, action, 
                strength, precision, attackRange, distance, knockback, barrageHits);
        
        switch (punchType) {
        case HEAVY_NO_COMBO:
            if (hasRapier()) {
                if (getAttackSpeed() < 24) {
                    attack
                    .addKnockback(1.5F)
                    .knockbackYRotDeg((75F + random.nextFloat() * 30F) * (random.nextBoolean() ? -1 : 1));
                }
                else {
                    attack
                    .addKnockback(0.25F)
                    .knockbackXRot(-90F);
                }
            }
            break;
        case BARRAGE:
            if (hasRapier() && target instanceof SkeletonEntity) {
                attack.damage(attack.getDamage() * 0.75F);
            }
        default:
            break;
        }
        return attack;
    }
    
    @Override
    protected double leapBaseStrength() {
        return getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
    }
    
    @Override
    public float getUserMovementFactor() {
        float factor = super.getUserMovementFactor();
        if (getUserPower() != null && getUserPower().getResolveLevel() >= 4) {
            factor += (1 - factor) * (hasArmor() ? 0.5F : 0.75F);
        }
        return factor;
    }
}
