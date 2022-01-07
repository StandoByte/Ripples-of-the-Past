package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.itemprojectile.StandArrowEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.util.damage.ModDamageSources;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class StandArrowItem extends ArrowItem {

    public StandArrowItem(Properties properties) {
        super(properties);
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
            livingEntity.hurt(ModDamageSources.STAND_VIRUS, Math.min(livingEntity.getMaxHealth(), 20F) - 1F);
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
                StandType<?> stand = null;
                boolean checkTier = JojoModConfig.COMMON.standTiers.get();
                int tier = checkTier ? StandUtil.standTierFromXp(player.experienceLevel, true) : -1;
                if (!checkTier || tier > -1) {
                    stand = StandUtil.randomStandByTier(tier, player, random);
                }
                if (stand != null && power.givePower(stand)) {
                    if (!player.abilities.instabuild) {
                        if (dealVirusDamage) {
                            player.hurt(ModDamageSources.STAND_VIRUS, Math.min(player.getHealth(), 11F) - 1F);
                        }
                        if (JojoModConfig.COMMON.standTiers.get()) {
                            player.giveExperienceLevels(-StandUtil.tierLowerBorder(stand.getTier()));
                        }
                        stack.hurtAndBreak(1, player, pl -> {});
                    }
                    return true;
                }
            }
            else {
                player.sendMessage(new TranslationTextComponent("jojo.chat.message.already_have_stand"), Util.NIL_UUID);
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
        if (JojoModConfig.COMMON.standTiers.get()) {
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
                if (JojoModConfig.COMMON.tierHasUnbannedStands(i)) {
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
