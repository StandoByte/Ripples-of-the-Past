package com.github.standobyte.jojo.item;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.command.configpack.standassign.PlayerStandAssignmentConfig;
import com.github.standobyte.jojo.entity.itemprojectile.StandArrowEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEnchantments;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.potion.StandVirusEffect;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandArrowHandler;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.StandUtil.StandRandomPoolFilter;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.GeneralUtil;

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
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;

public class StandArrowItem extends ArrowItem {
    private final int enchantability;
    private final boolean higherDurability;

    public StandArrowItem(Properties properties, int enchantability, boolean higherDurability) {
        super(properties);
        this.enchantability = enchantability;
        this.higherDurability = higherDurability;
        
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
        
        if (!world.isClientSide() && onPiercedByArrow(player, stack, world, Optional.empty())) {
            player.hurt(DamageSource.playerAttack(player), Math.min(1.0F, Math.max(player.getHealth() - 1.0F, 0)));
            stack.hurtAndBreak(1, player, pl -> {});
            return ActionResult.success(stack);
        }
        return ActionResult.fail(stack);
    }

    @Override
    public AbstractArrowEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
       return new StandArrowEntity(world, shooter, stack);
    }
    
    /** 
     * @return  if the entity got the Stand Virus effect or a Stand
     */
    public static boolean onPiercedByArrow(Entity target, ItemStack stack, World world, Optional<Entity> arrowShooter) {
        if (!world.isClientSide() && target instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) target;
            if (livingEntity.hasEffect(ModStatusEffects.STAND_VIRUS.get())) {
                return false;
            }
            
            if (livingEntity instanceof StandEntity) {
                return false;
            }
            else if (livingEntity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) livingEntity;
                return GeneralUtil.orElseFalse(IStandPower.getStandPowerOptional(livingEntity), standCap -> {
                    if (standCap.hasPower()) {
                        player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.already_have_stand"), true);
                        return false;
                    }
                    
                    if (player.abilities.instabuild) { // instantly give a stand in creative
                        StandType<?> stand = StandUtil.randomStand(player, player.getRandom());
                        return giveStandFromArrow(player, standCap, stand);
                    }
                    else {
                        StandType<?> standToGive = StandUtil.randomStand(player, player.getRandom());
                        if (standToGive == null) {
                            return false;
                        }
                        standCap.getStandArrowHandler().startArrowEffectSetStand(standToGive);
                        
                        int virusEffectDuration = StandVirusEffect.getEffectDurationToApply(player);
                        if (virusEffectDuration > 0) {
                            int inhibitionLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.VIRUS_INHIBITION.get(), stack);
                            int effectLevel = StandVirusEffect.getEffectLevelToApply(inhibitionLevel);
                            player.addEffect(new EffectInstance(ModStatusEffects.STAND_VIRUS.get(), 
                                    virusEffectDuration, effectLevel, false, false, true));
                        }
                        else { // instantly give a stand if there was no stand virus effect given
                            StandType<?> stand = StandUtil.randomStand(player, player.getRandom());
                            return giveStandFromArrow(player, standCap, stand);
                        }
                        
                        IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                            StandArrowHandler handler = power.getStandArrowHandler();
                            arrowShooter.ifPresent(shooter -> {
                                handler.setStandArrowShooter(shooter);
                            });
                            handler.setStandArrowItem(stack);
                        });
                    }
                    
                    return true;
                });
            }
        }
        return false;
    }
    
    public static boolean giveStandFromArrow(LivingEntity entity, IStandPower standCap, StandType<?> standType) {
        if (standCap.givePower(standType)) {
            StandArrowHandler handler = standCap.getStandArrowHandler();
            handler.onGettingStandFromArrow(entity);
            return true;
        }
        
        return false;
    }
    
//    public static boolean onPiercedByArrow(Entity entity, ItemStack stack, World world) {
//        if (!world.isClientSide() && entity instanceof LivingEntity) {
//            LivingEntity livingEntity = (LivingEntity) entity;
//            LazyOptional<IStandPower> standPower = IStandPower.getStandPowerOptional(livingEntity);
//            if (standPower.map(stand -> stand.hasPower() && !canPierceWithStand(stand)).orElse(false)) {
//                return false;
//            }
//            boolean wasStandUser = standPower.map(stand -> stand.hasPower() || stand.hadStand()).orElse(false);
//            DamageUtil.hurtThroughInvulTicks(livingEntity, DamageUtil.STAND_VIRUS, getVirusDamage(stack, livingEntity, !wasStandUser));
//            if (!livingEntity.isAlive()) {
//                return false;
//            }
//            boolean gaveStand = false;
//            if (livingEntity instanceof PlayerEntity) {
//                gaveStand = playerPiercedByArrow((PlayerEntity) livingEntity, stack, world);
//            }
//            else {
//                gaveStand = StandArrowEntity.EntityPierce.onArrowPierce(livingEntity);
//            }
//            return gaveStand;
//        }
//        return false;
//    }
//    
//    public static final int STAND_XP_REQUIREMENT = 40;
//    private static boolean playerPiercedByArrow(PlayerEntity player, ItemStack stack, World world) {
//        IStandPower power = IStandPower.getPlayerStandPower(player);
//        if (!world.isClientSide()) {
//            if (!power.hasPower()) {
//                StandType<?> stand = null;
//                if (player.experienceLevel < STAND_XP_REQUIREMENT) {
//                    player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.stand_not_enough_xp"), true);
//                    return false;
//                }
//                
//                stand = StandUtil.randomStand(player, random);
//                if (stand != null && power.givePower(stand)) {
//                    if (!player.abilities.instabuild) {
//                        player.giveExperienceLevels(-STAND_XP_REQUIREMENT);
//                    }
//                    return true;
//                }
//            }
//            else {
//                player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.already_have_stand"), true);
//            }
//        }
//        return false;
//    }
//    
//    private static float getVirusDamage(ItemStack arrow, LivingEntity entity, boolean canKill) {
//        float damage = entity.getRandom().nextFloat() * 8F + 8;
//        damage = Math.max(damage * (1 - 0.25F * EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.VIRUS_INHIBITION.get(), arrow)), 0);
//        damage *= entity.getMaxHealth() / 20F;
//        if (!canKill) {
//            damage = Math.min(damage, entity.getHealth() - 1);
//        }
//        return damage;
//    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        PlayerEntity player = ClientUtil.getClientPlayer();
        if (player != null) {
            IFormattableTextComponent mainText = null;
            
            Collection<StandType<?>> unbannedStands = StandUtil.arrowStands(true)
                    .sorted(Comparator.comparingInt(stand -> JojoCustomRegistries.STANDS.getNumericId(stand.getRegistryName())))
                    .collect(Collectors.toList());
            
            if (unbannedStands.isEmpty()) {
                mainText = new TranslationTextComponent("jojo.arrow.no_stands").withStyle(TextFormatting.GRAY, TextFormatting.OBFUSCATED);
                tooltip.add(mainText);
            }
            else {
                StandRandomPoolFilter poolFilter = JojoModConfig.getCommonConfigInstance(true).standRandomPoolFilter.get();
                boolean shift = ClientUtil.isShiftPressed();
                
                if (shift) {
                    tooltip.add(new TranslationTextComponent("jojo.arrow.stands_list"));

                    List<StandType<?>> assignedByDataConfig = PlayerStandAssignmentConfig.getInstance().getAssignedStands(player);
                    
                    IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                        unbannedStands.forEach(stand -> {
                            IFormattableTextComponent standName = stand.getName();
                            
                            if (assignedByDataConfig != null && !assignedByDataConfig.contains(stand)) {
                                standName.withStyle(TextFormatting.STRIKETHROUGH, TextFormatting.DARK_GRAY);
                            }
                            else {
                                standName.withStyle(TextFormatting.GRAY);
                            }
                            tooltip.add(standName);
                        });
                        
                        tooltip.add(StringTextComponent.EMPTY);
                    });
                }
                else {
                    tooltip.add(new TranslationTextComponent("jojo.arrow.stands_hint", new KeybindTextComponent("key.sneak")));
                }
                
                if (!player.abilities.instabuild) {
                    int levelsNeeded = IStandPower.getStandPowerOptional(player).map(power -> {
                        StandArrowHandler handler = power.getStandArrowHandler();
                        return handler.getStandXpLevelsRequirement(true, stack);
                    }).orElse(0);
                    if (levelsNeeded > 0) {
                        boolean playerHasStand = StandUtil.isEntityStandUser(player);
                        boolean playerHasLevels = player.experienceLevel >= levelsNeeded;
                        tooltip.add(new TranslationTextComponent("jojo.arrow.stand_arrow_xp", levelsNeeded).withStyle(
                                playerHasStand ? TextFormatting.DARK_GRAY : playerHasLevels ? TextFormatting.GREEN : TextFormatting.RED));
                    }
                }
                
                boolean isOnServer = !ClientUtil.isInSinglePlayer();
                if (isOnServer) {
                    switch (poolFilter) {
                    case LEAST_TAKEN:
                        tooltip.add(new TranslationTextComponent("jojo.arrow.least_taken_mode").withStyle(TextFormatting.GRAY));
                        break;
                    case NOT_TAKEN: 
                        tooltip.add(new TranslationTextComponent("jojo.arrow.not_taken_mode").withStyle(TextFormatting.GRAY));
                        break;
                    default:
                        break;
                    }
                }
            }
        }
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment)
                || enchantment == Enchantments.LOYALTY
                || enchantment == Enchantments.SHARPNESS;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantability;
    }

    @Override
    public boolean isValidRepairItem(ItemStack item, ItemStack repairItem) {
        return repairItem.getItem() == ModItems.METEORIC_INGOT.get();
    }
    
    @Override
    public int getMaxDamage(ItemStack stack) {
        boolean isClientSide = Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT;
        JojoModConfig.Common config = JojoModConfig.getCommonConfigInstance(isClientSide);
        ForgeConfigSpec.IntValue configOption = higherDurability ? config.arrowDurabilityBeetle : config.arrowDurability;
        return configOption.get();
    }
}
