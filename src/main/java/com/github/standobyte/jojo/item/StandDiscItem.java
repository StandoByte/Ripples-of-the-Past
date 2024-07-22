package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.item.ItemEntity;
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
                if (validStandDisc(stack, false)) {
                    StandInstance stand = getStandFromStack(stack, false);
                    if (MCUtil.dispenseOnNearbyEntity(blockSource, stack, entity -> {
                        return IStandPower.getStandPowerOptional(entity).map(power -> {
                            return giveStandFromDisc(power, stand, stack);
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
                
                if (!player.abilities.instabuild) {
                    Optional<StandInstance> previousDiscStand = power.putOutStand();
                    previousDiscStand.ifPresent(prevStand -> {
                        ItemEntity discItemEntity = player.drop(withStand(new ItemStack(this), prevStand), false);
                        discItemEntity.setPickUpDelay(5);
                        discItemEntity.setOwner(player.getUUID());
                    });
                }
                else {
                    power.clear();
                }
                
                if (giveStandFromDisc(power, stand, stack)) {
                    if (!player.abilities.instabuild) {
                        stack.shrink(1);
                    }
                    return ActionResult.success(stack);
                }
                else {
                    return ActionResult.fail(stack);
                }
            } 
        }
        else if (!power.hasPower()) {
            return ActionResult.success(stack);
        }
        return ActionResult.fail(stack);
    }

//    private static boolean canGetStandFromDisc(LivingEntity entity, IStandPower entityStandPower, StandType<?> stand) {
//        if (entity instanceof PlayerEntity) {
//            PlayerEntity player = (PlayerEntity) entity;
//            return player.abilities.instabuild || entityStandPower.hadAnyStand();
//        }
//        return false;
//    }
    
    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            boolean isClientSide = Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT;
            for (StandType<?> standType : JojoCustomRegistries.STANDS.getRegistry()) {
                if (StandUtil.canPlayerGetFromArrow(standType, isClientSide)) {
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
                tooltip.add(stand.getName());
                ITextComponent partName = StandSkinsManager.getInstance()
                        .getStandSkin(stand.getSelectedSkin())
                        .map(skin -> skin.getPartName(stand.getType()))
                        .orElse(stand.getType().getPartName());
                tooltip.add(partName);
                for (StandPart standPart : StandPart.values()) {
                    if (!stand.hasPart(standPart)) {
                        tooltip.add(new TranslationTextComponent("jojo.disc.missing_part." + standPart.name().toLowerCase()).withStyle(TextFormatting.DARK_GRAY));
                    }
                }
            }
        }
        tooltip.add(new TranslationTextComponent("item.jojo.creative_only_tooltip").withStyle(TextFormatting.DARK_GRAY));
    }
    
    @Deprecated
    public static StandInstance getStandFromStack(ItemStack stack, boolean clientSide) {
        return getStandFromStack(stack);
    }
    
    @Nullable
    public static StandInstance getStandFromStack(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt == null || !nbt.contains(STAND_TAG, MCUtil.getNbtId(CompoundNBT.class))) {
            return null;
        }
        return StandInstance.fromNBT((CompoundNBT) nbt.get(STAND_TAG));
    }
    
    public static boolean validStandDisc(ItemStack stack, boolean clientSide) {
        StandInstance stand = getStandFromStack(stack, clientSide);
        return stand != null && stand.getType() != null;
    }

    public static int getColor(ItemStack itemStack) {
        if (!validStandDisc(itemStack, true)) {
            return 0xFFFFFF;
        } else {
            return StandSkinsManager.getUiColor(getStandFromStack(itemStack, true));
        }
    }
    
    @Override
    public String getCreatorModId(ItemStack itemStack) {
        ResourceLocation id;
        StandInstance stand = getStandFromStack(itemStack);
        if (stand != null) {
            id = stand.getType().getRegistryName();
        }
        else {
            id = this.getRegistryName();
        }
        return id.getNamespace();
    }
    
    
    
    public static boolean giveStandFromDisc(IStandPower standCap, StandInstance stand, ItemStack discItem) {
        boolean standExistedBefore = discItem.getTag().getBoolean(WS_TAG);
        return standCap.giveStandFromInstance(stand, standExistedBefore);
    }
}
