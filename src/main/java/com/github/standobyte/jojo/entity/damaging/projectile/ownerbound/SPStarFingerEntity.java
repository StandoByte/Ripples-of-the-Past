package com.github.standobyte.jojo.entity.damaging.projectile.ownerbound;

import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModStandTypes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class SPStarFingerEntity extends OwnerBoundProjectileEntity {

    public SPStarFingerEntity(World world, LivingEntity entity) {
        super(ModEntityTypes.SP_STAR_FINGER.get(), entity, world);
    }
    
    public SPStarFingerEntity(EntityType<? extends SPStarFingerEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    @Override
    public float getBaseDamage() {
        LivingEntity owner = getOwner();
        float damage = owner != null ? (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) : 
            (float) ModStandTypes.STAR_PLATINUM.get().getStats().getBasePower();
        return damage *= 0.75F;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 5.0F;
    }

    @Override
    protected int ticksLifespan() {
        return ModActions.STAR_PLATINUM_STAR_FINGER.get().getStandActionTicks(null, null);
    }
    
    @Override
    protected float movementSpeed() {
        return 0.4F;
    }
    
    @Override
    protected float retractSpeed() {
        return movementSpeed() * 3F;
    }
    
    @Override
    protected boolean isBodyPart() {
        return true;
    }

    private static final Vector3d OFFSET = new Vector3d(-0.3, -0.2, 0.75);
    @Override
    protected Vector3d getOwnerRelativeOffset() {
        return OFFSET;
    }
}
