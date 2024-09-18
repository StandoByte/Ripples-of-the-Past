package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.AnvilUpdateEvent;

public class GlovesItem extends Item {
    protected Multimap<Attribute, AttributeModifier> defaultModifiers;
    protected int enchantability = 15;
    
    public GlovesItem(Properties properties) {
        super(properties);

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        this.defaultModifiers = builder
                .put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 
                        1, AttributeModifier.Operation.ADDITION))
                .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
//        if (openFingers()) {
//            tooltip.add(new TranslationTextComponent("item.jojo.gloves.hint").withStyle(TextFormatting.GRAY));
//        }
    }
    
    public boolean openFingers() {
        return true;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType slot) {
        return slot == EquipmentSlotType.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }
    
    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return this.getItemStackLimit(itemStack) == 1;
    }
    
    @Override
    public int getEnchantmentValue() {
        return enchantability;
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment == Enchantments.KNOCKBACK;
    }
    
    
    public static void combineInAnvil(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (left.getItem() == right.getItem() && left.getItem() instanceof GlovesItem) {
            combineEnchantments(event);
        }
    }
    
    private static void combineEnchantments(AnvilUpdateEvent event) {
        ItemStack item = event.getLeft();
        ItemStack item1 = item.copy();
        ItemStack item2 = event.getRight();
        PlayerEntity player = event.getPlayer();
        
        int i = 0;
        int cost = event.getCost();
        int k = 0;
        Map<Enchantment, Integer> enchants1 = EnchantmentHelper.getEnchantments(item);
        boolean flag = false;

        if (!item2.isEmpty()) {
            flag = !EnchantedBookItem.getEnchantments(item2).isEmpty();
            if (!flag && (item1.getItem() != item2.getItem())) {
                return;
            }
            
            Map<Enchantment, Integer> enchants2 = EnchantmentHelper.getEnchantments(item2);
            boolean flag2 = false;
            boolean flag3 = false;


            for(Enchantment enchantment2 : enchants2.keySet()) {
               if (enchantment2 != null) {
                  int i2 = enchants1.getOrDefault(enchantment2, 0);
                  int j2 = enchants2.get(enchantment2);
                  j2 = i2 == j2 ? j2 + 1 : Math.max(j2, i2);
                  boolean flag1 = player.abilities.instabuild || enchantment2.canEnchant(item);
                  
                  for(Enchantment enchantment1 : enchants1.keySet()) {
                     if (enchantment1 != enchantment2 && !enchantment2.isCompatibleWith(enchantment1)) {
                        flag1 = false;
                        ++i;
                     }
                  }

                  if (!flag1) {
                     flag3 = true;
                  } else {
                     flag2 = true;
                     if (j2 > enchantment2.getMaxLevel()) {
                        j2 = enchantment2.getMaxLevel();
                     }

                     enchants1.put(enchantment2, j2);
                     int k3 = 0;
                     switch(enchantment2.getRarity()) {
                     case COMMON:
                        k3 = 1;
                        break;
                     case UNCOMMON:
                        k3 = 2;
                        break;
                     case RARE:
                        k3 = 4;
                        break;
                     case VERY_RARE:
                        k3 = 8;
                     }

                     if (flag) {
                        k3 = Math.max(1, k3 / 2);
                     }

                     i += k3 * j2;
                     if (item.getCount() > 1) {
                        i = 40;
                     }
                  }
               }
            }

            if (flag3 && !flag2) {
                return;
            }
        }

        String itemName = event.getName();
        if (StringUtils.isBlank(itemName)) {
           if (item.hasCustomHoverName()) {
              k = 1;
              i += k;
              item1.resetHoverName();
           }
        } else if (!itemName.equals(item.getHoverName().getString())) {
           k = 1;
           i += k;
           item1.setHoverName(new StringTextComponent(itemName));
        }
        if (flag && !item1.isBookEnchantable(item2)) item1 = ItemStack.EMPTY;

        cost = cost + i;
        if (i <= 0) {
           item1 = ItemStack.EMPTY;
        }

        if (k == i && k > 0 && cost >= 40) {
           cost = 39;
        }

        if (cost >= 40 && !event.getPlayer().abilities.instabuild) {
           item1 = ItemStack.EMPTY;
        }

        if (!item1.isEmpty()) {
           int k2 = item1.getBaseRepairCost();
           if (!item2.isEmpty() && k2 < item2.getBaseRepairCost()) {
              k2 = item2.getBaseRepairCost();
           }

           if (k != i || k == 0) {
              k2 = RepairContainer.calculateIncreasedRepairCost(k2);
           }

           item1.setRepairCost(k2);
           EnchantmentHelper.setEnchantments(enchants1, item1);
        }

        event.setOutput(item1);
        event.setCost(cost);
    }

}
