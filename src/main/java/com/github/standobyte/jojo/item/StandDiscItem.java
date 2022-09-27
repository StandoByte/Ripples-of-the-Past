package com.github.standobyte.jojo.item;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.util.utils.JojoModUtil;
import com.github.standobyte.jojo.util.utils.LegacyUtil;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;

public class StandDiscItem extends Item {
    private static final String STAND_TAG = "Stand";
    public static final String WS_TAG = "WSPutOut";

    public StandDiscItem(Properties properties) {
        super(properties);

        DispenserBlock.registerBehavior(this, new DefaultDispenseItemBehavior() {
            protected ItemStack execute(IBlockSource blockSource, ItemStack stack) {
                if (validStandDisc(stack, false)) {
                    StandInstance stand = getStandFromStack(stack, false);
                    if (JojoModUtil.dispenseOnNearbyEntity(blockSource, stack, entity -> {
                        return IStandPower.getStandPowerOptional(entity).map(power -> {
                            return standFitsTier(entity, power.getUserTier(), stand.getType()) && power.giveStand(stand, false);
                        }).orElse(false);
                    }, true)) {
                        return stack;
                    }
                }
                return super.execute(blockSource, stack);
            }
        });
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        IStandPower power = IStandPower.getPlayerStandPower(player);
        if (!world.isClientSide()) {
            if (validStandDisc(stack, false)) {
                StandInstance stand = getStandFromStack(stack, false);
                if (JojoModConfig.getCommonConfigInstance(false).isStandBanned(stand.getType())) {
                    return ActionResult.fail(stack);
                }
                if (!standFitsTier(player, power.getUserTier(), stand.getType())) {
                    player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.low_tier"), true);
                    return ActionResult.fail(stack);
                }
                if (power.wasGivenByDisc()) {
                	if (!player.abilities.instabuild) {
	                    Optional<StandInstance> previousDiscStand = power.putOutStand();
	                    previousDiscStand.ifPresent(prevStand -> player.drop(withStand(new ItemStack(this), prevStand), false));
                	}
                	else {
                		power.clear();
                	}
                }
                if (power.giveStand(stand, !stack.getTag().getBoolean(WS_TAG))) {
                    power.setGivenByDisc();
                    if (!player.abilities.instabuild) {
                        stack.shrink(1);
                    }
                    return ActionResult.success(stack);
                }
                else {
                    player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.already_have_stand"), true);
                    return ActionResult.fail(stack);
                }
            } 
        }
        else if (!power.hasPower()) {
            return ActionResult.success(stack);
        }
        return ActionResult.fail(stack);
    }

    private static boolean standFitsTier(LivingEntity entity, int playerTier, StandType<?> stand) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            return player.abilities.instabuild
            		|| !JojoModConfig.getCommonConfigInstance(entity.level.isClientSide()).standTiers.get()
                    || playerTier >= stand.getTier()
                    || Arrays.stream(StandUtil.standTiersFromXp(player.experienceLevel, false, entity.level.isClientSide()))
                    .anyMatch(tier -> tier >= stand.getTier());
        }
        return false;
    }
    
    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            boolean isClientSide = Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT;
            for (StandType<?> standType : ModStandTypes.Registry.getRegistry()) {
                if (!JojoModConfig.getCommonConfigInstance(isClientSide).isConfigLoaded()
                        || !JojoModConfig.getCommonConfigInstance(isClientSide).isStandBanned(standType)) {
                    items.add(withStand(new ItemStack(this), new StandInstance(standType)));
                }
            }
        }
    }
    
    public static ItemStack withStand(ItemStack discStack, StandInstance standInstance) {
        discStack.getOrCreateTag().put(STAND_TAG, standInstance.writeNBT());
        return discStack;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        PlayerEntity player = ClientUtil.getClientPlayer();
        if (player != null) {
        	if (validStandDisc(stack, true)) {
        	    StandInstance stand = getStandFromStack(stack, true);
        		tooltip.add(stand.getCustomName().orElse(new TranslationTextComponent(stand.getType().getTranslationKey())));
        		tooltip.add(stand.getType().getPartName());
        		if (JojoModConfig.getCommonConfigInstance(true).standTiers.get()) {
        			int standTier = stand.getType().getTier();
        			int playerXpLevel = ClientUtil.getClientPlayer().experienceLevel;
        			int xpForTier = JojoModUtil.getOrLast(JojoModConfig.getCommonConfigInstance(true).standTierXpLevels.get(), standTier).intValue();
        			tooltip.add(new TranslationTextComponent("jojo.disc.tier", standTier, 
        					new TranslationTextComponent("jojo.disc.tier_level", xpForTier)
        					.withStyle(playerXpLevel < xpForTier ? TextFormatting.RED : TextFormatting.GREEN)).withStyle(TextFormatting.GRAY));
        		}
        		for (StandPart standPart : StandPart.values()) {
        		    if (!stand.hasPart(standPart)) {
                        tooltip.add(new TranslationTextComponent("jojo.disc.missing_part." + standPart.name().toLowerCase()).withStyle(TextFormatting.DARK_GRAY));
        		    }
        		}
        	}
        }
    }
    
    @Nullable
    public static StandInstance getStandFromStack(ItemStack stack, boolean clientSide) {
        return LegacyUtil.oldStandDiscInstance(stack, clientSide).orElseGet(() -> {
            CompoundNBT nbt = stack.getTag();
            if (nbt == null || !nbt.contains(STAND_TAG, JojoModUtil.getNbtId(CompoundNBT.class))) {
                return null;
            }
            return StandInstance.fromNBT((CompoundNBT) nbt.get(STAND_TAG));
        });
    }
    
    public static boolean validStandDisc(ItemStack stack, boolean clientSide) {
        StandInstance stand = getStandFromStack(stack, clientSide);
        return stand != null && stand.getType() != null;
    }

    public static int getColor(ItemStack itemStack) {
        if (!validStandDisc(itemStack, true)) {
            return 0xFFFFFF;
        } else {
            return getStandFromStack(itemStack, true).getType().getColor();
        }
    }
}
