package com.github.standobyte.jojo.action.stand;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.damaging.projectile.CDBlockBulletEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.util.general.LazySupplier;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class CrazyDiamondBlockBullet extends StandEntityAction {
    public static final StandPose BLOCK_BULLET_SHOT_POSE = new StandPose("CD_BLOCK_BULLET");
    public static final double PLAYER_TRACKING_RANGE = 64;
    private final StandRelativeOffset userOffsetLeftArm;

    public CrazyDiamondBlockBullet(StandEntityAction.Builder builder) {
        super(builder);
        this.userOffsetLeftArm = builder.userOffset.copyScale(-1, 1, 1);
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
        if (!hardMaterial(blockState)) {
            return conditionMessage("item_hard_material");
        }
        return super.checkSpecificConditions(user, power, target);
    }
    
    private boolean hardMaterial(BlockState blockState) {
        Material material = blockState.getMaterial();
        return 
                material == Material.BUILDABLE_GLASS || 
                material == Material.ICE_SOLID || 
                material == Material.WOOD || 
                material == Material.NETHER_WOOD || 
                material == Material.GLASS || 
                material == Material.ICE || 
                material == Material.STONE || 
                material == Material.METAL || 
                material == Material.HEAVY_METAL || 
                material == Material.CLAY && blockState.getBlock().getRegistryName().getPath().contains("infested");
    }

    @Override
    public void standTickWindup(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide() && userPower.getUser() != null && ClientUtil.canSeeStands()) {
            CustomParticlesHelper.createCDRestorationParticle(userPower.getUser(), Hand.OFF_HAND);
        }
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            if (user == null) return;
            ItemStack item = user.getOffhandItem();
            if (!(item.getItem() instanceof BlockItem)) return;
            CDBlockBulletEntity bullet = new CDBlockBulletEntity(standEntity, world);
            bullet.setShootingPosOf(user);
            bullet.setBlock(((BlockItem) item.getItem()).getBlock());
            standEntity.shootProjectile(bullet, 2.0F, 0.25F);
            if (!(user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild)) {
                item.shrink(1);
            }
            if (!JojoModUtil.useShiftVar(user)) {
                StandEffectsTracker.getTargetLookedAt(userPower, ModStandEffects.DRIED_BLOOD_DROPS.get(), PLAYER_TRACKING_RANGE, user).ifPresent(effect -> {
                    bullet.setTarget(effect.getTarget());
                });
            }
        }
    }
    
    @Override
    protected void playSoundAtStand(World world, StandEntity standEntity, SoundEvent sound, IStandPower standPower, Phase phase) {
        if (world.isClientSide() && phase == Phase.WINDUP) {
            ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, sound, this, phase, 1.0F, 1.0F, false);
        }
        else {
            super.playSoundAtStand(world, standEntity, sound, standPower, phase);
        }
    }
    
    @Override
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        if (!standEntity.isArmsOnlyMode()) {
            LivingEntity user = standEntity.getUser();
            if (user.getMainArm() == HandSide.LEFT) {
                return userOffsetLeftArm;
            }
        }
        return super.getOffsetFromUser(standPower, standEntity, task);
    }
    
    @Override
    public String getTranslationKey(IStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        if (isHoming(power)) {
            key += ".homing";
        }
        return key;
    }
    
    private final LazySupplier<ResourceLocation> homingTex = 
            new LazySupplier<>(() -> makeIconVariant(this, "_homing"));
    @Override
    public ResourceLocation getIconTexturePath(@Nullable IStandPower power) {
        if (power != null && isHoming(power)) {
            return homingTex.get();
        }
        else {
            return super.getIconTexturePath(power);
        }
    }
    
    private boolean isHoming(IStandPower power) {
        return power.getUser() != null && !JojoModUtil.useShiftVar(power.getUser())
                && StandEffectsTracker.getTargetLookedAt(power, ModStandEffects.DRIED_BLOOD_DROPS.get(), PLAYER_TRACKING_RANGE, power.getUser()).isPresent();
    }
    
    
    
    @Deprecated
    private ResourceLocation homingTexPath;
    @Deprecated
    @Override
    public ResourceLocation getTexture(IStandPower power) {
        ResourceLocation resLoc = getRegistryName();
        if (isHoming(power)) {
            if (homingTexPath == null) {
                homingTexPath = new ResourceLocation(resLoc.getNamespace(), resLoc.getPath() + "_homing");
            }
            resLoc = homingTexPath;
        }
        return resLoc;
    }

    @Deprecated
    @Override
    public Stream<ResourceLocation> getTexLocationstoLoad() {
        ResourceLocation resLoc = getRegistryName();
        return Stream.of(resLoc, new ResourceLocation(resLoc.getNamespace(), resLoc.getPath() + "_homing"));
    }
}
