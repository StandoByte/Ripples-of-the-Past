package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.stream.IntStream;

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
import net.minecraft.util.text.KeybindTextComponent;
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
        if (!world.isClientSide() && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.hurt(DamageUtil.STAND_VIRUS, Math.min(livingEntity.getHealth(), 11F) - 1F);
            stack.hurtAndBreak(1, livingEntity, pl -> {});
            boolean gaveStand = false;
            if (livingEntity instanceof PlayerEntity) {
                gaveStand = playerPiercedByArrow((PlayerEntity) livingEntity, stack, world, false);
            }
            else {
                gaveStand = StandArrowEntity.EntityPierce.onArrowPierce(livingEntity);
            }
            if (!gaveStand) {
                livingEntity.hurt(DamageUtil.STAND_VIRUS, 10F);
            }
            return gaveStand;
        }
        return false;
    }
    
    private static boolean playerPiercedByArrow(PlayerEntity player, ItemStack stack, World world, boolean dealVirusDamage) {
        IStandPower power = IStandPower.getPlayerStandPower(player);
        if (!world.isClientSide()) {
            if (!power.hasPower()) {
                if (dealVirusDamage) {
                    player.hurt(DamageUtil.STAND_VIRUS, Math.min(player.getMaxHealth(), 6F) - 1F);
                }
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
            	mainText.withStyle(TextFormatting.ITALIC, TextFormatting.GRAY);
            }

            if (canBeUsed) {
    			boolean shift = ClientUtil.isShiftPressed();
    			if (shift) {
    				tooltip.add(new TranslationTextComponent("jojo.arrow.stands_list").withStyle(TextFormatting.DARK_GRAY));
    				StandUtil.availableStands(currentTiers, player).forEach(
    						stand -> tooltip.add(new TranslationTextComponent(stand.getTranslationKey())));
    			}
    			else {
    				tooltip.add(new TranslationTextComponent("jojo.arrow.stands_hint", new KeybindTextComponent("key.sneak"))
    						.withStyle(TextFormatting.DARK_GRAY));
    			}
            }
        }
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment.equals(Enchantments.LOYALTY);
    }
    
    public enum StandGivingMode {
    	RANDOM,
    	LEAST_TAKEN,
    	UNIQUE
    }
}
