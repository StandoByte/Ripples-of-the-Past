package com.github.standobyte.jojo.action.non_stand;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.MRFlameEntity;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class PillarmanSelfDetonation extends PillarmanAction {

    public PillarmanSelfDetonation(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.HEAT;
    }
 
    @Override
    public void onHoldTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled && world.isClientSide()) {
            PillarmanDivineSandstorm.auraEffect(user, ModParticles.HAMON_AURA_RED.get(), 12);
            PillarmanDivineSandstorm.auraEffect(user, ModParticles.BOILING_BLOOD_POP.get(), 1);
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide) {
            PillarmanExplosion explosion = new PillarmanExplosion(world, user, 
                    DamageSource.ON_FIRE.setExplosion(), null, 
                    user.getX(), user.getY(), user.getZ(), 3.0F, 
                    true, Explosion.Mode.BREAK);
            CustomExplosion.explode(explosion);
            PlayerEntity playerentity = user instanceof PlayerEntity ? (PlayerEntity)user : null;
            if (playerentity == null || !playerentity.abilities.instabuild) {
                user.hurt(EntityDamageSource.explosion(user), 40F);
                user.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 200, 0));
            }
        }
    }
    
    
    
    public static class PillarmanExplosion extends CustomExplosion {

        public PillarmanExplosion(World pLevel, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius) {
            super(pLevel, pToBlowX, pToBlowY, pToBlowZ, pRadius);
        }

        public PillarmanExplosion(World pLevel, @Nullable Entity pSource, 
                @Nullable DamageSource pDamageSource, @Nullable ExplosionContext pDamageCalculator, 
                double pToBlowX, double pToBlowY, double pToBlowZ, 
                float pRadius, boolean pFire, Explosion.Mode pBlockInteraction) {
            super(pLevel, pSource, pDamageSource, pDamageCalculator, pToBlowX, pToBlowY, pToBlowZ, pRadius, pFire, pBlockInteraction);
        }
        
        @Override
        protected void filterEntities(List<Entity> entities) {
            LivingEntity acdc = getSourceMob();
            if (acdc != null) {
                Iterator<Entity> iter = entities.iterator();
                while (iter.hasNext()) {
                    Entity entity = iter.next();
                    if (entity == acdc || !MCUtil.canHarm(acdc, entity)) {
                        iter.remove();
                    }
                }
            }
        }
        
        @Override
        protected void spawnFire() {
            LivingEntity acdc = getSourceMob();
            if (acdc == null || ForgeEventFactory.getMobGriefingEvent(level, acdc)) {
                for (BlockPos pos : getToBlow()) {
                    if (level.isEmptyBlock(pos)) {
                        if (!level.isEmptyBlock(pos.below()) && Math.random() < 0.05f) {
                            level.setBlockAndUpdate(pos, ModBlocks.BOILING_BLOOD.get().defaultBlockState());
                        } else {
                            level.setBlockAndUpdate(pos, AbstractFireBlock.getState(level, pos));
                        }
                    }
                    else {
                        BlockState blockState = level.getBlockState(pos);
                        MRFlameEntity.meltIceAndSnow(level, blockState, pos);
                    }
                }
            }
        }
        
        @Override
        public ResourceLocation getExplosionType() {
            return CustomExplosion.Register.PILLAR_MAN_DETONATION;
        }
    }
    
}
