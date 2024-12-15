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
import com.mojang.datafixers.util.Either;

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
                    Either<StandType<?>, ITextComponent> standOrError;
                    if (standCap.hasPower()) {
                        standOrError = Either.right(new TranslationTextComponent("jojo.chat.message.already_have_stand"));
                    }
                    else {
                        standOrError = StandUtil.randomStandOrError(player, player.getRandom());
                    }
                    return GeneralUtil.merge(standOrError.mapBoth(
                            standToGive -> {
                                if (player.abilities.instabuild) { // instantly give a stand in creative
                                    return giveStandFromArrow(player, standCap, standToGive);
                                }
                                else {
                                    standCap.getStandArrowHandler().startArrowEffectSetStand(standToGive);

                                    int virusEffectDuration = StandVirusEffect.getEffectDurationToApply(player);
                                    if (virusEffectDuration > 0) {
                                        int inhibitionLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.VIRUS_INHIBITION.get(), stack);
                                        int effectLevel = StandVirusEffect.getEffectLevelToApply(inhibitionLevel);
                                        player.addEffect(new EffectInstance(ModStatusEffects.STAND_VIRUS.get(), 
                                                virusEffectDuration, effectLevel, false, false, true));
                                    }
                                    else { // instantly give a stand if there was no stand virus effect given
                                        return giveStandFromArrow(player, standCap, standToGive);
                                    }

                                    rememberArrowShooter(livingEntity, arrowShooter, stack);
                                }

                                return true;
                            }, error -> {
                                player.displayClientMessage(error, true);
                                return false;
                            }));
                });
            }
            else {
                int inhibitionLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.VIRUS_INHIBITION.get(), stack);
                int effectLevel = StandVirusEffect.getEffectLevelToApply(inhibitionLevel);
                livingEntity.addEffect(new EffectInstance(ModStatusEffects.STAND_VIRUS.get(), 
                        600, effectLevel, false, false, true));
                rememberArrowShooter(livingEntity, arrowShooter, stack);
            }
        }
        return false;
    }
    
    private static void rememberArrowShooter(LivingEntity target, Optional<Entity> arrowShooter, ItemStack arrow) {
        IStandPower.getStandPowerOptional(target).ifPresent(power -> {
            StandArrowHandler handler = power.getStandArrowHandler();
            arrowShooter.ifPresent(shooter -> {
                handler.setStandArrowShooter(shooter);
            });
            handler.setStandArrowItem(arrow);
        });
    }
    
    public static boolean giveStandFromArrow(LivingEntity entity, IStandPower standCap, StandType<?> standType) {
        if (standCap.givePower(standType)) {
            StandArrowHandler handler = standCap.getStandArrowHandler();
            handler.onGettingStandFromArrow(entity);
            return true;
        }
        
        return false;
    }
    
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
