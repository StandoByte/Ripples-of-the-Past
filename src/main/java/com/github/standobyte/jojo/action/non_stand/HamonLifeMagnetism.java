package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonLifeMagnetism extends HamonAction {

    public HamonLifeMagnetism(HamonAction.Builder builder) {
        super(builder.needsFreeMainHand());
    }
    
    @Override
    public ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (target.getType() == TargetType.BLOCK && user.level.getBlockState(target.getBlockPos()).getBlock() instanceof LeavesBlock
                ||
            useItemForGlider(user.getMainHandItem()) || useItemForGlider(user.getOffhandItem()) 
                ||
            user instanceof PlayerEntity && !MCUtil.findInInventory(((PlayerEntity) user).inventory, 
                    item -> !item.isEmpty() && item.getItem() instanceof BlockItem && 
                    ((BlockItem) item.getItem()).getBlock() instanceof LeavesBlock).isEmpty()) {
            return super.checkTarget(target, user, power);
        }
        return conditionMessage("leaves");
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (useItemForGlider(user.getMainHandItem()) || useItemForGlider(user.getOffhandItem())) {
            return ActionConditionResult.POSITIVE;
        }
        return super.checkHeldItems(user, power);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            if (target.getType() == TargetType.BLOCK) {
                BlockPos blockPos = target.getBlockPos();
                BlockState blockState = user.level.getBlockState(blockPos);
                if (blockState.getBlock() instanceof LeavesBlock) {
                    world.destroyBlock(blockPos, false);
                    summonGlider(world, user, power, Vector3d.atBottomCenterOf(blockPos), false, blockState);
                    return;
                }
            }
            
            if (user instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) user;
                IInventory inventory = player.inventory;
                ItemStack leavesItem = user.getMainHandItem();
                if (!useItemForGlider(leavesItem)) leavesItem = user.getOffhandItem();
                if (!useItemForGlider(leavesItem)) leavesItem = MCUtil.findInInventory(inventory, item -> useItemForGlider(item));
                if (!leavesItem.isEmpty()) {
                    summonGlider(world, user, power, user.position(), true, ((BlockItem) leavesItem.getItem()).getBlock().defaultBlockState());
                    if (!player.abilities.instabuild) {
                        leavesItem.shrink(1);
                    }
                }
            }
        }
    }
    
    private boolean useItemForGlider(ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof BlockItem && 
                ((BlockItem) item.getItem()).getBlock() instanceof LeavesBlock;
    }
    
    private void summonGlider(World world, LivingEntity user, INonStandPower power, Vector3d pos, boolean mount, BlockState leavesBlock) {
        LeavesGliderEntity glider = new LeavesGliderEntity(world);
        glider.moveTo(pos.x, pos.y, pos.z, user.xRot, user.yRot);
        glider.setLeavesBlock(leavesBlock);
        world.addFreshEntity(glider);
        if (mount) {
            user.startRiding(glider);
        }
        glider.setEnergy(Math.min(power.getEnergy(), LeavesGliderEntity.MAX_ENERGY));
        HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
        hamon.hamonPointsFromAction(HamonStat.CONTROL, getEnergyCost(power, ActionTarget.EMPTY));
        HamonUtil.emitHamonSparkParticles(world, null, pos.x, glider.getY(1.0F), pos.z, 0.1F);
    }
    
    @Override
    public boolean renderHamonAuraOnItem(ItemStack item, HandSide handSide) {
        return item.getItem() instanceof BlockItem && ((BlockItem) item.getItem()).getBlock() instanceof LeavesBlock;
    }
}
