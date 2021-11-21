package com.github.standobyte.jojo.item;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.itemprojectile.StandArrowEntity;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.util.damage.ModDamageSources;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
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
                    player.hurt(ModDamageSources.STAND_VIRUS, Math.min(player.getMaxHealth(), 20F) - 1F);
                }
                int tier = StandUtil.standTierFromXp(player);
                StandType stand = randomStandByTier(tier, player, random);
                if (stand != null) {
                    tier = stand.getTier();
                    if (power.givePower(stand)) {
                        if (!player.abilities.instabuild) {
                            player.giveExperienceLevels(-StandUtil.tierLowerBorder(tier));
                            stack.hurtAndBreak(1, player, pl -> {});
                        }
                        return true;
                    }
                }
                player.sendMessage(new TranslationTextComponent("chat.message.tmp_not_enough_xp", tier), Util.NIL_UUID); // TODO remove the message after adding stands for each tier
                return false;
            } else {
                player.sendMessage(new TranslationTextComponent("chat.message.already_have_stand"), Util.NIL_UUID);
                return false;
            }
        }
        else if (!power.hasPower()) {
            return true;
        }
        return false;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        PlayerEntity player = ClientUtil.getClientPlayer();
        if (player != null) {
            int tier = StandUtil.standTierFromXp(player);
            tooltip.add(new TranslationTextComponent("jojo.arrow.tier", tier, StandUtil.tierLowerBorder(tier))
                    .withStyle(TextFormatting.ITALIC, TextFormatting.GRAY));
        }
    }
    
    public static StandType randomStandByTier(int tier, LivingEntity entity, Random random) {
        Collection<StandType> stands = ModStandTypes.Registry.getRegistry().getValues();

        List<StandType> filtered = stands.stream()
                .filter(stand -> stand.prioritizedCondition(entity) && stand.getTier() <= tier)
                .collect(Collectors.toList());
        if (!filtered.isEmpty()) {
            return filtered.get(random.nextInt(filtered.size()));
        }
        else {
            filtered = stands.stream()
                    .filter(stand -> stand.getTier() == tier)
                    .collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                return filtered.get(random.nextInt(filtered.size()));
            }
            else {
                return null;
            }
        }
    }
}
