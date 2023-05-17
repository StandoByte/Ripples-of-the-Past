package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.itemprojectile.StandArrowEntity;
import com.github.standobyte.jojo.init.ModEnchantments;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
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
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

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
        if (onPiercedByArrow(player, stack, world)) {
            stack.hurtAndBreak(1, player, pl -> {});
            return ActionResult.success(stack);
        }
        return ActionResult.fail(stack);
    }

    @Override
    public AbstractArrowEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
       return new StandArrowEntity(world, shooter, stack);
    }
    
    public static boolean onPiercedByArrow(Entity entity, ItemStack stack, World world) {
        if (!world.isClientSide() && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            LazyOptional<IStandPower> standPower = IStandPower.getStandPowerOptional(livingEntity);
            if (standPower.map(stand -> stand.hasPower() && !canPierceWithStand(stand)).orElse(false)) {
                return false;
            }
            boolean wasStandUser = standPower.map(stand -> stand.hasPower() || stand.getUserTier() > 0).orElse(false);
            DamageUtil.hurtThroughInvulTicks(livingEntity, DamageUtil.STAND_VIRUS, getVirusDamage(stack, livingEntity, !wasStandUser));
            if (!livingEntity.isAlive()) {
                return false;
            }
            boolean gaveStand = false;
            if (livingEntity instanceof PlayerEntity) {
                gaveStand = playerPiercedByArrow((PlayerEntity) livingEntity, stack, world);
            }
            else {
                gaveStand = StandArrowEntity.EntityPierce.onArrowPierce(livingEntity);
            }
            return gaveStand;
        }
        return false;
    }
    
    private static boolean canPierceWithStand(IStandPower stand) {
        return false;
    }
    
    private static boolean playerPiercedByArrow(PlayerEntity player, ItemStack stack, World world) {
        IStandPower power = IStandPower.getPlayerStandPower(player);
        if (!world.isClientSide()) {
            if (!power.hasPower()) {
                StandType<?> stand = null;
                boolean checkTier = JojoModConfig.getCommonConfigInstance(false).standTiers.get();
                if (checkTier) {
                    int[] tiers = StandUtil.standTiersFromXp(player.experienceLevel, true, false);
                    if (tiers.length > 0) {
                        stand = StandUtil.randomStandFromTiers(tiers, player, random);
                    }
                    else {
                        player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.stand_not_enough_xp"), true);
                        return false;
                    }
                }
                else {
                    stand = StandUtil.randomStand(player, random);
                }
                
                if (stand != null && power.givePower(stand)) {
                    if (!player.abilities.instabuild) {
                        if (JojoModConfig.getCommonConfigInstance(world.isClientSide()).standTiers.get()) {
                            player.giveExperienceLevels(-StandUtil.tierLowerBorder(stand.getTier(), false));
                        }
                    }
                    return true;
                }
            }
            else {
                player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.already_have_stand"), true);
            }
        }
        return false;
    }
    
    private static float getVirusDamage(ItemStack arrow, LivingEntity entity, boolean canKill) {
        float damage = entity.getRandom().nextFloat() * 8F + 8;
        damage = Math.max(damage * (1 - 0.25F * EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.VIRUS_INHIBITION.get(), arrow)), 0);
        damage *= entity.getMaxHealth() / 20F;
        if (!canKill) {
            damage = Math.min(damage, entity.getHealth() - 1);
        }
        return damage;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        PlayerEntity player = ClientUtil.getClientPlayer();
        if (player != null) {
            IFormattableTextComponent mainText = null;
            int[] currentTiers = null;
            boolean canBeUsed = false;
            if (JojoModConfig.getCommonConfigInstance(true).standTiers.get()) {
                currentTiers = StandUtil.standTiersFromXp(player.experienceLevel, true, true);
                int minTier = IntStream.of(currentTiers).min().orElse(-1);
                int maxTier = IntStream.of(currentTiers).max().orElse(-1);
                int nextTier = StandUtil.arrowPoolNextTier(maxTier + 1, true);
                if (currentTiers.length > 0) {
                    if (nextTier > -1) {
                        if (currentTiers.length == 1) {
                            mainText = new TranslationTextComponent("jojo.arrow.tier", minTier, 
                                    StandUtil.tierLowerBorder(nextTier, true));
                        }
                        else {
                            mainText = new TranslationTextComponent("jojo.arrow.tiers", minTier, maxTier, 
                                    StandUtil.tierLowerBorder(nextTier, true));
                        }
                    }
                    else {
                        if (currentTiers.length == 1) {
                            mainText = new TranslationTextComponent("jojo.arrow.max_tier", minTier);
                        }
                        else {
                            mainText = new TranslationTextComponent("jojo.arrow.max_tiers", minTier, maxTier);
                        }
                    }

                    tooltip.add(mainText);
                    canBeUsed = true;
                }
                else {
                    if (nextTier > -1) {
                        mainText = new TranslationTextComponent("jojo.arrow.no_tier", 
                                StandUtil.tierLowerBorder(nextTier, true), nextTier);
                    }
                    else {
                        mainText = new TranslationTextComponent("jojo.arrow.no_stands").withStyle(TextFormatting.OBFUSCATED);
                    }
                    tooltip.add(mainText);
                }
            }
            else {
                for (int i = 0; i < StandUtil.getMaxTier(true); i++) {
                    if (JojoModConfig.getCommonConfigInstance(true).tierHasUnbannedStands(i)) {
                        canBeUsed = true;
                    }
                }
                if (!canBeUsed) {
                    mainText = new TranslationTextComponent("jojo.arrow.no_stands").withStyle(TextFormatting.OBFUSCATED);
                    tooltip.add(mainText);
                }
            }
            if (mainText != null) {
                mainText.withStyle(TextFormatting.GRAY);
            }

            if (canBeUsed) {
                boolean shift = ClientUtil.isShiftPressed();
                if (shift) {
                    tooltip.add(new TranslationTextComponent("jojo.arrow.stands_list"));
                    StandUtil.availableStands(currentTiers, player).forEach(
                            stand -> tooltip.add(stand.getName().withStyle(TextFormatting.GRAY)));
                }
                else {
                    tooltip.add(new TranslationTextComponent("jojo.arrow.stands_hint", new KeybindTextComponent("key.sneak"))
                            .withStyle(TextFormatting.GRAY));
                }
            }
        }
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment.equals(Enchantments.LOYALTY);
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public boolean isValidRepairItem(ItemStack item, ItemStack repairItem) {
        return repairItem.getItem() == ModItems.METEORIC_INGOT.get();
    }
    
    public enum StandGivingMode {
        RANDOM,
        LEAST_TAKEN,
        UNIQUE
    }
}
