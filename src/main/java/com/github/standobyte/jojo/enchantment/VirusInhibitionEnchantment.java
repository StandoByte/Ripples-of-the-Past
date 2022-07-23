package com.github.standobyte.jojo.enchantment;

import com.github.standobyte.jojo.init.ModEnchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;

public class VirusInhibitionEnchantment extends Enchantment {

    public VirusInhibitionEnchantment(Rarity rarity, EquipmentSlotType... slots) {
        super(rarity, ModEnchantments.STAND_ARROW, slots);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 1 + level * 7;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 15;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }
    
    @Override
    public boolean isAllowedOnBooks() {
        return false;
    }

}
