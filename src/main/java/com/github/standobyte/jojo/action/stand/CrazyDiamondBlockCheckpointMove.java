package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CrazyDiamondBlockCheckpointMove extends StandEntityAction {

    public CrazyDiamondBlockCheckpointMove(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (getBlockItemToUse(user).isEmpty()) {
            return conditionMessage("item_block_origin");
        }
        return super.checkSpecificConditions(user, power, target);
    }
    
    public static ItemStack getBlockItemToUse(LivingEntity user) {
        ItemStack blockItem = user.getOffhandItem();
        if (!CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(user.level, blockItem).isPresent()) {
            blockItem = user.getMainHandItem();
        }
        if (!CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(user.level, blockItem).isPresent()) {
            return ItemStack.EMPTY;
        }
        return blockItem;
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        LivingEntity user = userPower.getUser();
        if (user != null) {
            ItemStack heldItem = getBlockItemToUse(user);
            CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(world, heldItem).ifPresent(pos -> {
                Vector3d posD = Vector3d.atCenterOf(pos);
                Entity entity = user.getRootVehicle();
                if (entity.distanceToSqr(posD) > 16) {
                    entity.setDeltaMovement(posD.subtract(entity.position()).normalize().scale(0.75));
                    entity.fallDistance = 0;
                }
                else {
                    BlockState blockState = null;
                    if (heldItem.getItem() instanceof BlockItem) {
                        blockState = CrazyDiamondBlockCheckpointMake.getBlockState(heldItem, (BlockItem) heldItem.getItem());
                    }
                    boolean willRestore = blockState == null || CrazyDiamondPreviousState.canReplaceBlock(world, pos, blockState);
                    
                    if (!world.isClientSide()) {
                        if (willRestore) {
                            heldItem.shrink(1);
                            if (blockState != null) {
                                if (!world.getBlockState(pos).isAir()) {
                                    world.destroyBlock(pos, true);
                                }
                                world.setBlockAndUpdate(pos, blockState);
                            }
                            standEntity.playSound(ModSounds.CRAZY_DIAMOND_FIX_ENDED.get(), 1.0F, 1.0F, null);
                        }
                    }
                    else if (willRestore) {
                        CrazyDiamondRestoreTerrain.addParticlesAroundBlock(world, pos, standEntity.getRandom());
                    }
                }
                if (world.isClientSide() && ClientUtil.canSeeStands()) {
                    CustomParticlesHelper.createCDRestorationParticle(user, user.getMainHandItem() == heldItem ? Hand.MAIN_HAND : Hand.OFF_HAND);
                }
            });
        }
    }
    
    @Override
    public void phaseTransition(World world, StandEntity standEntity, IStandPower standPower, 
            @Nullable Phase from, @Nullable Phase to, StandEntityTask task, int nextPhaseTicks) {
        if (world.isClientSide()) {
            if (to == Phase.PERFORM) {
                ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, 
                        ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), this, Phase.PERFORM, 1.0F, 1.0F, true);
            }
        }
    }
    
    @Override
    public String getTranslationKey(IStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        LivingEntity user = power.getUser();
        ItemStack blockItem = getBlockItemToUse(user);
        if (!CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(user.level, blockItem).isPresent()) {
            return key + ".empty";
        }
        return key + ".pos";
    }

    @Override
    public IFormattableTextComponent getTranslatedName(IStandPower power, String key) {
        return CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(power.getUser().level, getBlockItemToUse(power.getUser())).map(pos -> 
        (IFormattableTextComponent) new TranslationTextComponent(key, pos.getX(), pos.getY(), pos.getZ()))
                .orElse(super.getTranslatedName(power, key));
    }
}
