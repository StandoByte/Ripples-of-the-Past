package com.github.standobyte.jojo.action.stand;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class CrazyDiamondRepairItem extends StandEntityAction {
    public static final StandPose ITEM_FIX_POS = new StandPose("CD_ITEM_FIX");
    private final StandRelativeOffset userOffsetLeftArm;

    public CrazyDiamondRepairItem(StandEntityAction.Builder builder) {
        super(builder);
        this.userOffsetLeftArm = builder.userOffset.copyScale(-1, 1, 1);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ItemStack itemToRepair = itemToRepair(user);
        if (itemToRepair == null || itemToRepair.isEmpty()) {
            return conditionMessage("item_offhand");
        }
        if (!canBeRepaired(itemToRepair)) {
            return conditionMessage("no_repair");
        }
        return super.checkSpecificConditions(user, power, target);
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        LivingEntity user = userPower.getUser();
        if (user != null) {
            if (!world.isClientSide()) {
                ItemStack itemToRepair = itemToRepair(user);
                if (!itemToRepair.isEmpty()) {
                    float points = repairTick(user, itemToRepair, task.getTick());
                    if (points > 0) {
                        userPower.addLearningProgressPoints(this, points);
                    }
                }
            }
            else if (StandUtil.shouldStandsRender(ClientUtil.getClientPlayer())) {
                CustomParticlesHelper.createCDRestorationParticle(user, Hand.OFF_HAND);
            }
        }
    }

    @Override
    public void appendWarnings(List<ITextComponent> list, IStandPower power, PlayerEntity clientPlayerUser) {
        ItemStack itemToRepair = itemToRepair(clientPlayerUser);
        if (!itemToRepair.isEmpty() && itemToRepair.isEnchanted()) {
            list.add(new TranslationTextComponent("jojo.crazy_diamond_fix.warning", itemToRepair.getDisplayName()));
        }
    }
    
    private ItemStack itemToRepair(LivingEntity entity) {
        return entity.getOffhandItem();
    }
    
    public float repairTick(LivingEntity user, ItemStack itemStack, int taskTicks) {
        int damage = 0;

        ItemStack newStack = null;
        if (itemStack.getItem() == Items.CHIPPED_ANVIL)                             newStack = new ItemStack(Items.ANVIL);
        else if (itemStack.getItem() == Items.DAMAGED_ANVIL)                        newStack = new ItemStack(Items.CHIPPED_ANVIL);
        else if (itemStack.getItem() == Items.COBBLESTONE)                          newStack = new ItemStack(Items.STONE);
        else if (itemStack.getItem().getRegistryName().getPath().contains("cracked")) {
            ResourceLocation uncracked = new ResourceLocation(
                    itemStack.getItem().getRegistryName().getNamespace(), 
                    itemStack.getItem().getRegistryName().getPath().replace("cracked_", ""));
            if (ForgeRegistries.ITEMS.containsKey(uncracked)) {
                newStack = new ItemStack(ForgeRegistries.ITEMS.getValue(uncracked));
            }
        }
        
        if (newStack != null && user instanceof PlayerEntity) {
            if (taskTicks % 10 == 9) {
                user.setItemInHand(Hand.OFF_HAND, DrinkHelper.createFilledResult(itemStack, (PlayerEntity) user, newStack, false));
                damage += 500;
            }
            else {
                damage = -1;
            }
        }
        
        itemStack.removeTagKey("Enchantments");
        itemStack.removeTagKey("StoredEnchantments");
        damage = Math.min(itemStack.getDamageValue(), 50);
        itemStack.setDamageValue(itemStack.getDamageValue() - damage);
        itemStack.setRepairCost(0);
        
        return (float) damage * 0.0001F;
    }
    
    private boolean canBeRepaired(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && 
                (itemStack.isDamaged() || itemStack.isEnchanted()
                        || itemStack.getItem() == Items.CHIPPED_ANVIL || itemStack.getItem() == Items.DAMAGED_ANVIL
                        || itemStack.getItem() == Items.COBBLESTONE
                        || itemStack.getItem().getRegistryName().getPath().contains("cracked") && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(
                                itemStack.getItem().getRegistryName().getNamespace(), 
                                itemStack.getItem().getRegistryName().getPath().replace("cracked_", "")))
                        );
    }
    
    @Override
    public void onMaxTraining(IStandPower power) {
        power.unlockAction(getShiftVariationIfPresent());
    }
    
    @Override
    protected void onPhaseTransition(World world, StandEntity standEntity, IStandPower standPower, 
            @Nullable Phase from, @Nullable Phase to, StandEntityTask task, int nextPhaseTicks) {
        if (world.isClientSide()) {
            if (to == Phase.PERFORM) {
                ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, 
                        ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), this, Phase.PERFORM, 1.0F, 1.0F, true);
            }
            else if (from == Phase.PERFORM) {
                standEntity.playSound(ModSounds.CRAZY_DIAMOND_FIX_ENDED.get(), 1.0F, 1.0F, ClientUtil.getClientPlayer());
            }
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
    public float yRotForOffset(LivingEntity user, StandEntityTask task) {
        return user.yBodyRot;
    }
    
    @Override
    public void rotateStand(StandEntity standEntity, StandEntityTask task) {
        if (!standEntity.isRemotePositionFixed()) {
            LivingEntity user = standEntity.getUser();
            if (user != null) {
                float rotationOffset = user.getMainArm() == HandSide.RIGHT ? 15 : -15;
                standEntity.setRot(user.yBodyRot + rotationOffset, user.xRot);
                standEntity.setYHeadRot(user.yBodyRot + rotationOffset);
            }
        }
    }
}
