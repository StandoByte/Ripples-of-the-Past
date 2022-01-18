package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.client.util.ITooltipFlag;
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

public class StandDiscItem extends Item {
    private static final String STAND_TAG = "Stand";

    public StandDiscItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        IStandPower power = IStandPower.getPlayerStandPower(player);
        if (!world.isClientSide()) {
            if (validStandDisc(stack)) {
                StandType<?> stand = getStandFromStack(stack);
                if (!canGainStand(player, power.getTier(), stand)) {
                    player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.low_tier"), true);
                    return ActionResult.fail(stack);
                }
                if (power.givePower(stand)) {
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

    private boolean canGainStand(PlayerEntity player, int playerTier, StandType<?> stand) {
        return player.abilities.instabuild || !JojoModConfig.COMMON.standTiers.get()
                || StandUtil.standTierFromXp(player.experienceLevel, false) >= stand.getTier() || playerTier >= stand.getTier();
    }
    
    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            for (StandType<?> standType : ModStandTypes.Registry.getRegistry()) {
                if (!JojoModConfig.COMMON.isConfigLoaded() || !JojoModConfig.COMMON.isStandBanned(standType)) {
                    ItemStack item = new ItemStack(this);
                    setStandType(item, standType);
                    items.add(item);
                }
            }
        }
    }
    
    public static void setStandType(ItemStack discStack, StandType<?> standType) {
        discStack.getOrCreateTag().putString(STAND_TAG, ModStandTypes.Registry.getKeyAsString(standType));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (validStandDisc(stack)) {
            String standRegistryName = stack.getTag().getString(STAND_TAG);
            StandType<?> stand = ModStandTypes.Registry.getRegistry().getValue(new ResourceLocation(standRegistryName));
            tooltip.add(new TranslationTextComponent(stand.getTranslationKey()));
            tooltip.add(stand.getPartName());
            if (JojoModConfig.COMMON.standTiers.get()) {
                tooltip.add(new TranslationTextComponent("jojo.disc.tier", stand.getTier()).withStyle(TextFormatting.GRAY));
            }
        }
    }
    
    private static StandType<?> getStandFromStack(ItemStack stack) {
        return ModStandTypes.Registry.getRegistry().getValue(new ResourceLocation(stack.getTag().getString(STAND_TAG)));
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
