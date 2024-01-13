package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class StandRemoverItem extends Item {
    private final boolean oneTimeUse;
    private final Mode mode;

    public StandRemoverItem(Properties properties, Mode mode, boolean oneTimeUse) {
        super(properties);

        this.mode = mode;
        this.oneTimeUse = oneTimeUse;
        
        DispenserBlock.registerBehavior(this, new DefaultDispenseItemBehavior() {
            protected ItemStack execute(IBlockSource blockSource, ItemStack stack) {
                if (MCUtil.dispenseOnNearbyEntity(blockSource, stack, entity -> {
                    return IStandPower.getStandPowerOptional(entity).map(power -> {
                        return useOn(entity, power);
                    }).orElse(false);
                }, oneTimeUse)) {
                    return stack;
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
            if (useOn(player, power)) {
                if (oneTimeUse && !player.abilities.instabuild) {
                    stack.shrink(1);
                }
                return ActionResult.success(stack);
            }
            return ActionResult.fail(stack);
        }
        else if (power.hasPower()) {
            return ActionResult.success(stack);
        }
        return ActionResult.fail(stack);
    }
    
    private boolean useOn(LivingEntity entity, IStandPower power) {
        if (power.hasPower()) {
            switch (mode) {
            case REMOVE:
                power.clear();
                break;
            case EJECT:
                Optional<StandInstance> previousDiscStand = power.putOutStand();
                previousDiscStand.ifPresent(prevStand -> MCUtil.giveItemTo(entity, 
                        StandDiscItem.withStand(new ItemStack(ModItems.STAND_DISC.get()), prevStand), true));
                break;
            case FULL_CLEAR:
                power.clear();
                power.fullStandClear();
                break;
            }
            return true;
        }
        return false;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (((StandRemoverItem) stack.getItem()).mode == Mode.FULL_CLEAR) {
            tooltip.add(new TranslationTextComponent("item.jojo.stand_full_clear.hint").withStyle(TextFormatting.GRAY));
        }
        tooltip.add(new TranslationTextComponent("item.jojo.creative_only_tooltip").withStyle(TextFormatting.DARK_GRAY));
    }
    
    public static enum Mode {
        REMOVE,
        EJECT,
        FULL_CLEAR
    }
}
