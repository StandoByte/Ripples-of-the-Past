package com.github.standobyte.jojo.util.mc.damage.explosion;

import java.util.List;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil.HamonAttackProperties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.World;

public class HamonBlastExplosion extends CustomExplosion {
    private float hamonDamage;
    
    public HamonBlastExplosion(World pLevel, Entity pSource, 
            ExplosionContext pDamageCalculator, 
            double pToBlowX, double pToBlowY, double pToBlowZ, 
            float pRadius) {
        super(pLevel, pSource, 
                null, pDamageCalculator, 
                pToBlowX, pToBlowY, pToBlowZ, 
                pRadius, false, Explosion.Mode.NONE);
    }
    
    public void setHamonDamage(float hamonDamage) {
        this.hamonDamage = hamonDamage;
    }
    
    @Override
    protected List<Entity> getAffectedEntities(AxisAlignedBB area) {
        return level.getEntitiesOfClass(LivingEntity.class, area, 
                EntityPredicates.LIVING_ENTITY_STILL_ALIVE.and(EntityPredicates.NO_SPECTATORS).and(entity -> !entity.is(getExploder())))
                .stream().collect(Collectors.toList());
    }
    
    @Override
    protected float calcDamage(double impact, double diameter) {
        return super.calcDamage(impact, diameter) * hamonDamage;
    }
    
    @Override
    protected void hurtEntity(Entity entity, float damage, double knockback, Vector3d vecToEntityNorm) {
        DamageUtil.dealHamonDamage(entity, damage, getSourceMob(), null, HamonAttackProperties::noSrcEntityHamonMultiplier);
    }
    
    @Override
    public void finalizeExplosion(boolean pSpawnParticles) {
        if (level.isClientSide) {
            playSound();
        }
        
        if (pSpawnParticles) {
            spawnParticles();
        }
        
        chargeBlocksWithHamon();
    }
    
    protected void chargeBlocksWithHamon() {
        
    }
    
    // FIXME !!! hamon blast visuals & sound
    @Override
    protected void playSound() {
        Vector3d pos = getPosition();
//        level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 
//                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F, false);
    }
    
    @Override
    protected void spawnParticles() {
        Vector3d pos = getPosition();
//        if (radius >= 2.0F && blockInteraction != Explosion.Mode.NONE) {
//            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);
//        } else {
//            level.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);
//        }
    }
    
    // FIXME !!! (hamon) charge nearby living blocks
    
    @Deprecated // not the actual DamageSource object
    @Override
    public DamageSource getDamageSource() {
        return DamageUtil.HAMON;
    }
}
