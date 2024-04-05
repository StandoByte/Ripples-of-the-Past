package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TieredItem;
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
		ItemStack weaponStack = player.getOffhandItem();
		ItemStack weaponStack2 = player.getMainHandItem();
        ItemStack stack = player.getItemInHand(hand);
        CompoundNBT mainNBT = new CompoundNBT();
		mainNBT.putInt("Oiled", MAX_USES);
		
        if(stack.getItem() instanceof OilItem && (weaponStack.getItem() instanceof TieredItem || weaponStack2.getItem() instanceof TieredItem)) {
        	if (!world.isClientSide()) {
        		if(weaponStack.getItem() instanceof TieredItem) {
        			weaponStack.getOrCreateTag().put("Oiled", mainNBT);
        		} else {
        			weaponStack2.getOrCreateTag().put("Oiled", mainNBT);
        		}
                if (player == null || !player.abilities.instabuild) {
        			player.inventory.add(new ItemStack(Items.GLASS_BOTTLE));
        		}
                world.playSound(null, player.getX(), player.getEyeY(), player.getZ(), SoundEvents.BOTTLE_EMPTY, player.getSoundSource(), 1F, 1F);
                stack.shrink(1);
            }
        }
        return ActionResult.fail(stack);
	}
    
     @Override
     public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
             tooltip.add(new TranslationTextComponent("item.jojo.oil.hint").withStyle(TextFormatting.GRAY)); 
     }
     
     public static Optional<String> isOiledTag(ItemStack stack) {
    	 if (stack.hasTag()) {
             CompoundNBT nbt = stack.getTag();
             if (nbt.contains("Oiled", MCUtil.getNbtId(CompoundNBT.class))) {
            	 CompoundNBT posNBT = nbt.getCompound("Oiled");
            	 String string = new String("Oiled: " + posNBT.getInt("Oiled") + " uses").toString();
            	 return Optional.of(string); 
             }
         }
         return Optional.empty();
     } 
         
}
