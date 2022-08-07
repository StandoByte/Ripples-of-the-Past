package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CrazyDiamondBlockCheckpointMove extends StandEntityAction {

    public CrazyDiamondBlockCheckpointMove(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ItemStack blockItem = user.getOffhandItem();
        if (blockItem == null || !CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(user.level, blockItem).isPresent()) {
            return conditionMessage("item_block_origin");
        }
        return super.checkSpecificConditions(user, power, target);
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        LivingEntity user = userPower.getUser();
        if (user != null) {
            ItemStack heldItem = user.getOffhandItem();
            CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(world, heldItem).ifPresent(pos -> {
                Vector3d posD = Vector3d.atCenterOf(pos);
                if (user.distanceToSqr(posD) > 16) {
                    user.setDeltaMovement(posD.subtract(user.position()).normalize().scale(0.75));
                    user.fallDistance = 0;
                }
                else {
                    if (heldItem.getItem() instanceof BlockItem && !world.isClientSide()) {
                        world.setBlockAndUpdate(pos, CrazyDiamondBlockCheckpointMake.getBlockState(heldItem, 
                                (BlockItem) heldItem.getItem()));
                    }
                    CrazyDiamondRestoreTerrain.addParticlesAroundBlock(world, pos, standEntity.getRandom());
                    heldItem.shrink(1);
                }
                if (world.isClientSide()) {
                    // FIXME ! (fast travel) CD restore sound
                    CustomParticlesHelper.createCDRestorationParticle(user, Hand.OFF_HAND);
                }
            });
        }
    }
    
    @Override
    public String getTranslationKey(IStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        LivingEntity user = power.getUser();
        ItemStack blockItem = user.getOffhandItem();
        if (blockItem == null || !CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(user.level, blockItem).isPresent()) {
            return key + ".empty";
        }
        return key + ".pos";
    }

    
    public TranslationTextComponent getTranslatedName(IStandPower power, String key) {
        return CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(power.getUser().level, power.getUser().getOffhandItem()).map(pos -> 
        new TranslationTextComponent(key, pos.getX(), pos.getY(), pos.getZ())).orElse(super.getTranslatedName(power, key));
    }
}
