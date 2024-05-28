package com.github.standobyte.jojo.enchantment;

import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;

import com.github.standobyte.jojo.init.ModEnchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;

public class GlovesSpeedEnchantment extends Enchantment {
    protected final EquipmentSlotType[] slots;

    public GlovesSpeedEnchantment(Enchantment.Rarity pRarity, 
            EnchantmentType type, EquipmentSlotType... pApplicableSlots) {
        super(pRarity, type, pApplicableSlots);
        this.slots = pApplicableSlots;
    }

    /**
     * Returns the minimal value of enchantability needed on the enchantment level passed.
     */
    @Override
    public int getMinCost(int pEnchantmentLevel) {
        return 8 + (pEnchantmentLevel - 1) * 14;
    }

    @Override
    public int getMaxCost(int pEnchantmentLevel) {
        return this.getMinCost(pEnchantmentLevel) + 25;
    }

    /**
     * Returns the maximum level that the enchantment can have.
     */
    @Override
    public int getMaxLevel() {
        return 4;
    }
    
    public boolean appliesToSlot(EquipmentSlotType slot) {
        return ArrayUtils.contains(slots, slot);
    }

    private static final UUID ATTRIBUTE_ID = UUID.fromString("ed86a102-525f-4713-920a-6edfd72b83ac");
    public static void addAtrributeModifiersFromEvent(ItemStack item, ItemAttributeModifierEvent modifiers) {
        if (ModEnchantments.GLOVES_SPEED.get().appliesToSlot(modifiers.getSlotType())) {
            int glovesSpeedLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.GLOVES_SPEED.get(), item);
            if (glovesSpeedLevel > 0) {
                modifiers.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(
                        ATTRIBUTE_ID, "Gloves attack speed", 0.1 * glovesSpeedLevel, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }
}
