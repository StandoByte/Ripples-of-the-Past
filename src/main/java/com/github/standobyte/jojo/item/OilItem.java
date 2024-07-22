package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.OptionalInt;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class OilItem extends Item {

    public OilItem(Properties properties) {
        super(properties);
    }
    
    public static final int MAX_USES = 120;
     

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        Hand opposite = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack oilStack = player.getItemInHand(hand);
        ItemStack weaponStack = player.getItemInHand(opposite);
        
        if (MCUtil.isItemWeapon(weaponStack)) {
            if (!world.isClientSide()) {
                setWeaponOilUses(weaponStack, MAX_USES);
                if (!player.abilities.instabuild) {
                    oilStack.shrink(1);
                    player.inventory.add(new ItemStack(Items.GLASS_BOTTLE));
                }
            }
            world.playSound(player, player.getX(), player.getEyeY(), player.getZ(), SoundEvents.BOTTLE_EMPTY, player.getSoundSource(), 1F, 1F);
            return ActionResult.consume(oilStack);
        }
        
        return ActionResult.fail(oilStack);
    }
    
     @Override
     public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
             tooltip.add(new TranslationTextComponent("item.jojo.oil.hint").withStyle(TextFormatting.GRAY)); 
     }
     
     public static OptionalInt remainingOiledUses(ItemStack stack) {
         if (!stack.isEmpty() && stack.hasTag()) {
             CompoundNBT nbt = stack.getTag();
             if (nbt.contains("HamonOiled")) {
                 int usesLeft = nbt.getInt("HamonOiled");
                 return OptionalInt.of(usesLeft); 
             }
         }
         return OptionalInt.empty();
     } 
     
     public static void setWeaponOilUses(ItemStack weaponStack, int uses) {
         if (weaponStack.isEmpty()) return;
         
         if (uses > 0) {
             weaponStack.getOrCreateTag().putInt("HamonOiled", uses);
         }
         else if (weaponStack.hasTag()) {
             weaponStack.getTag().remove("HamonOiled");
         }
     }
         
}
