package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.itemprojectile.StandArrowEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class StandArrowItem extends ArrowItem {

    public StandArrowItem(Properties properties) {
        super(properties);
        
        DispenserBlock.registerBehavior(this, new ProjectileDispenseBehavior() {
            @Override
            protected ProjectileEntity getProjectile(World world, IPosition position, ItemStack stack) {
                StandArrowEntity arrow = new StandArrowEntity(world, position.x(), position.y(), position.z(), stack);
                arrow.pickup = AbstractArrowEntity.PickupStatus.ALLOWED;
                return arrow;
            }
        });
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (playerPiercedByArrow(player, stack, world, true)) {
            return ActionResult.success(stack);
        }
        return ActionResult.fail(stack);
    }

    @Override
    public AbstractArrowEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
       return new StandArrowEntity(world, shooter, stack);
    }
    
    public static boolean onPiercedByArrow(Entity entity, ItemStack stack, World world) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.hurt(DamageUtil.STAND_VIRUS, Math.min(livingEntity.getHealth(), 20F) - 1F);
            stack.hurtAndBreak(1, livingEntity, pl -> {});
            if (livingEntity instanceof PlayerEntity) {
                return playerPiercedByArrow((PlayerEntity) livingEntity, stack, world, false);
            }
        }
        return false;
    }
    
    private static boolean playerPiercedByArrow(PlayerEntity player, ItemStack stack, World world, boolean dealVirusDamage) {
        IStandPower power = IStandPower.getPlayerStandPower(player);
        if (!world.isClientSide()) {
            if (!power.hasPower()) {
                if (dealVirusDamage) {
                    player.hurt(DamageUtil.STAND_VIRUS, Math.min(player.getMaxHealth(), 11F) - 1F);
                }
                StandType<?> stand = null;
                boolean checkTier = JojoModConfig.getCommonConfigInstance().standTiers.get();
                int tier = checkTier ? StandUtil.standTierFromXp(player.experienceLevel, true) : -1;
                if (!checkTier || tier > -1) {
                    stand = StandUtil.randomStandByTier(tier, player, random);
                }
                else {
                    player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.tmp_not_enough_xp"), true); // TODO remove the message after adding stands for each tier
                }
                if (stand != null && power.givePower(stand)) {
                    if (!player.abilities.instabuild) {
                        if (JojoModConfig.getCommonConfigInstance().standTiers.get()) {
                            player.giveExperienceLevels(-StandUtil.tierLowerBorder(stand.getTier()));
                        }
                        stack.hurtAndBreak(1, player, pl -> {});
                    }
                    return true;
                }
            }
            else {
                player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.already_have_stand"), true);
            }
        }
        else if (!power.hasPower()) {
            return true;
        }
        return false;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        IFormattableTextComponent text = null;
        if (JojoModConfig.getCommonConfigInstance().standTiers.get()) {
            PlayerEntity player = ClientUtil.getClientPlayer();
            if (player != null) {
                int currentTier = StandUtil.standTierFromXp(player.experienceLevel, true);
                int nextTier = StandUtil.arrowPoolNextTier(currentTier);
                if (currentTier > -1) {
                    if (nextTier > -1) {
                        text = new TranslationTextComponent("jojo.arrow.tier", currentTier, StandUtil.tierLowerBorder(nextTier));
                    }
                    else {
                        text = new TranslationTextComponent("jojo.arrow.max_tier", currentTier);
                    }
                }
                else {
                    if (nextTier > -1) {
                        text = new TranslationTextComponent("jojo.arrow.no_tier", StandUtil.tierLowerBorder(nextTier), nextTier);
                    }
                    else {
                        text = new TranslationTextComponent("jojo.arrow.no_stands").withStyle(TextFormatting.OBFUSCATED);
                    }
                }
            }
        }
        else {
            for (int i = 0; i < StandUtil.MAX_TIER; i++) {
                if (JojoModConfig.getCommonConfigInstance().tierHasUnbannedStands(i)) {
                    return;
                }
            }
            text = new TranslationTextComponent("jojo.arrow.no_stands").withStyle(TextFormatting.OBFUSCATED);
        }
        if (text != null) {
            tooltip.add(text.withStyle(TextFormatting.ITALIC, TextFormatting.GRAY));
        }
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment.equals(Enchantments.LOYALTY);
    }
}
