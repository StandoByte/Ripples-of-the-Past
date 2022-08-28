package com.github.standobyte.jojo.action.stand;

import java.util.Optional;
import java.util.stream.Stream;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.entity.damaging.projectile.CDItemProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModStandEffects;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CrazyDiamondItemProjectile extends StandEntityAction {

    public CrazyDiamondItemProjectile(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ItemStack itemToShoot = user.getOffhandItem();
        if (itemToShoot == null || itemToShoot.isEmpty() || !(itemToShoot.getItem() instanceof BlockItem)) {
            return conditionMessage("block_offhand");
        }
        Block block = ((BlockItem) itemToShoot.getItem()).getBlock();
        BlockState blockState = block.defaultBlockState();
        if (!StandStatFormulas.isBlockBreakable(
                power.isActive() ? ((StandEntity) power.getStandManifestation()).getAttackDamage()
                        : power.getType().getStats().getBasePower() + power.getType().getStats().getDevPower(power.getStatsDevelopment()), 
                        blockState.getDestroySpeed(user.level, user.blockPosition()), blockState.getHarvestLevel())) {
            return conditionMessage("stand_cant_break_block");
        }
        if (!hardMaterial(blockState.getMaterial())) {
            return conditionMessage("item_hard_material");
        }
        return super.checkSpecificConditions(user, power, target);
    }
    
    private boolean hardMaterial(Material material) {
        return 
                material == Material.BUILDABLE_GLASS || 
                material == Material.ICE_SOLID || 
                material == Material.WOOD || 
                material == Material.NETHER_WOOD || 
                material == Material.GLASS || 
                material == Material.ICE || 
                material == Material.STONE || 
                material == Material.METAL || 
                material == Material.HEAVY_METAL;
    }

    @Override
    public void standTickWindup(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide() && userPower.getUser() != null) {
            CustomParticlesHelper.createCDRestorationParticle(userPower.getUser(), Hand.OFF_HAND);
        }
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            if (user != null) {
                CDItemProjectileEntity bullet = new CDItemProjectileEntity(standEntity, world);
                bullet.setBlock(((BlockItem) user.getOffhandItem().getItem()).getBlock());
                standEntity.shootProjectile(bullet, 2.0F, 0.25F);
                if (!(user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild)) {
                    user.getOffhandItem().shrink(1);
                }
                if (!user.isShiftKeyDown()) {
                    getTarget(targets(userPower), user).ifPresent(effect -> {
                        bullet.setTarget(effect.getTarget());
                    });
                }
            }
        }
    }
    
    public static Stream<StandEffectInstance> targets(IStandPower power) {
        return power.getContinuousEffects().getEffects(effect -> effect.effectType == ModStandEffects.DRIED_BLOOD_DROPS.get())
        .stream().filter(effect -> effect.getTarget() != null && effect.getTarget().distanceToSqr(power.getUser()) < 4096);
    }

    public static Optional<StandEffectInstance> getTarget(Stream<StandEffectInstance> targets, LivingEntity user) {
        Vector3d lookAngle = user.getLookAngle();
        return targets.max((e1, e2) -> 
        MathHelper.floor(
                (lookAngle.dot(e1.getTarget().getBoundingBox().getCenter().subtract(user.getEyePosition(1.0F)).normalize()) - 
                lookAngle.dot(e2.getTarget().getBoundingBox().getCenter().subtract(user.getEyePosition(1.0F)).normalize()))
                * 256));
    }
}
