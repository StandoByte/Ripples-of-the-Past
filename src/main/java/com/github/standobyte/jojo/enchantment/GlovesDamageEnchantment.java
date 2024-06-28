package com.github.standobyte.jojo.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.inventory.EquipmentSlotType;

public class GlovesDamageEnchantment extends Enchantment {

    public GlovesDamageEnchantment(Enchantment.Rarity pRarity, 
            EnchantmentType type, EquipmentSlotType... pApplicableSlots) {
        super(pRarity, type, pApplicableSlots);
    }

    /**
     * Returns the minimal value of enchantability needed on the enchantment level passed.
     */
    @Override
    public int getMinCost(int pEnchantmentLevel) {
        return 1 + (pEnchantmentLevel - 1) * 11;
    }

    @Override
    public int getMaxCost(int pEnchantmentLevel) {
        return this.getMinCost(pEnchantmentLevel) + 20;
    }

    /**
     * Returns the maximum level that the enchantment can have.
     */
    @Override
    public int getMaxLevel() {
        return 5;
    }

    /**
     * Calculates the additional damage that will be dealt by an item with this enchantment. This alternative to
     * calcModifierDamage is sensitive to the targets EnumCreatureAttribute.
     * @param pLevel The level of the enchantment being used.
     */
    @Override
    public float getDamageBonus(int pLevel, CreatureAttribute pCreatureType) {
        return 1.0F + (float)Math.max(0, pLevel - 1) * 0.5F;
    }
//
//    /**
//     * Determines if the enchantment passed can be applyied together with this enchantment.
//     * @param pEnch The other enchantment to test compatibility with.
//     */
//    public boolean checkCompatibility(Enchantment pEnch) {
//        return !(pEnch instanceof DamageEnchantment);
//    }

}
