package com.github.standobyte.jojo.item;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

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
import net.minecraft.util.ResourceLocation;
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
                if (validStandDisc(stack)) {
                    StandType<?> stand = getStandFromStack(stack);
                    if (JojoModUtil.dispenseOnNearbyEntity(blockSource, stack, entity -> {
                        return IStandPower.getStandPowerOptional(entity).map(power -> {
                            return standFitsTier(entity, power.getUserTier(), stand) && power.givePower(stand);
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
            if (validStandDisc(stack)) {
                StandType<?> stand = getStandFromStack(stack);
                if (JojoModConfig.getCommonConfigInstance(false).isStandBanned(stand)) {
                    return ActionResult.fail(stack);
                }
                if (!standFitsTier(player, power.getUserTier(), stand)) {
                    player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.low_tier"), true);
                    return ActionResult.fail(stack);
                }
                if (power.wasGivenByDisc()) {
                    StandType<?> previousDiscStand = power.putOutStand();
                    if (previousDiscStand != null) {
                        player.drop(withStandType(new ItemStack(this), previousDiscStand), false);
                    }
                }
                if (power.givePower(stand, !stack.getTag().getBoolean(WS_TAG))) {
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
            return player.abilities.instabuild || !JojoModConfig.getCommonConfigInstance(entity.level.isClientSide()).standTiers.get()
                    || Arrays.stream(StandUtil.standTiersFromXp(player.experienceLevel, false, entity.level.isClientSide()))
                    .anyMatch(tier -> tier >= stand.getTier())
                    || playerTier >= stand.getTier();
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
                    items.add(withStandType(new ItemStack(this), standType));
                }
            }
        }
    }
    
    public static ItemStack withStandType(ItemStack discStack, StandType<?> standType) {
        discStack.getOrCreateTag().putString(STAND_TAG, ModStandTypes.Registry.getKeyAsString(standType));
        return discStack;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (validStandDisc(stack)) {
            String standRegistryName = stack.getTag().getString(STAND_TAG);
            StandType<?> stand = ModStandTypes.Registry.getRegistry().getValue(new ResourceLocation(standRegistryName));
            tooltip.add(new TranslationTextComponent(stand.getTranslationKey()));
            tooltip.add(stand.getPartName());
            if (JojoModConfig.getCommonConfigInstance(true).standTiers.get()) {
                tooltip.add(new TranslationTextComponent("jojo.disc.tier", stand.getTier()).withStyle(TextFormatting.GRAY));
            }
        }
    }
    
    public static StandType<?> getStandFromStack(ItemStack stack) {
        return ModStandTypes.Registry.getRegistry().getValue(getStandResLocFromStack(stack));
    }
    
    public static ResourceLocation getStandResLocFromStack(ItemStack stack) {
        return new ResourceLocation(stack.getTag().getString(STAND_TAG));
    }
    
    public static boolean validStandDisc(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt != null && ModStandTypes.Registry.getRegistry().containsKey(ResourceLocation.tryParse(nbt.getString(STAND_TAG)))) {
            return true;
        }
        return false;
    }

    public static int getColor(ItemStack itemStack) {
        if (!validStandDisc(itemStack)) {
            return 0xFFFFFF;
        } else {
            return getStandFromStack(itemStack).getColor();
        }
    }
}
