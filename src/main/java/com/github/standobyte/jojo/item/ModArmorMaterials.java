package com.github.standobyte.jojo.item;

import java.util.function.Supplier;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.LazyValue;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public enum ModArmorMaterials implements IArmorMaterial {
    STONE_MASK("stone_mask", 3, new int[]{0, 0, 0, 1}, 0, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.EMPTY),
    BREATH_CONTROL_MASK("breath_control_mask", 10, new int[]{0, 0, 0, 1}, 9, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT)),
    SATIPOROJA_SCARF("satiporoja_scarf", 0, new int[]{0, 0, 0, 1}, 25, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> Ingredient.EMPTY),
    WHITE_CLOTH(Items.WHITE_WOOL),
    ORANGE_CLOTH(Items.ORANGE_WOOL),
    MAGENTA_CLOTH(Items.MAGENTA_WOOL),
    LIGHT_BLUE_CLOTH(Items.LIGHT_BLUE_WOOL),
    YELLOW_CLOTH(Items.YELLOW_WOOL),
    LIME_CLOTH(Items.LIME_WOOL),
    PINK_CLOTH(Items.PINK_WOOL),
    GRAY_CLOTH(Items.GRAY_WOOL),
    LIGHT_GRAY_CLOTH(Items.LIGHT_GRAY_WOOL),
    CYAN_CLOTH(Items.CYAN_WOOL),
    PURPLE_CLOTH(Items.PURPLE_WOOL),
    BLUE_CLOTH(Items.BLUE_WOOL),
    BROWN_CLOTH(Items.BROWN_WOOL),
    GREEN_CLOTH(Items.GREEN_WOOL),
    RED_CLOTH(Items.RED_WOOL),
    BLACK_CLOTH(Items.BLACK_WOOL);
    
    private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};
    private final String name;
    private final int maxDamageFactor;
    private final int[] damageReductionAmountArray;
    private final int enchantability;
    private final SoundEvent soundEvent;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyValue<Ingredient> repairMaterial;
    
    private ModArmorMaterials(String name, int maxDamageFactor, int[] damageReductionAmounts, int enchantability, SoundEvent equipSound, 
            float toughness, float knockbackResistance, Supplier<Ingredient> repairMaterialSupplier) {
        this.name = name;
        this.maxDamageFactor = maxDamageFactor;
        this.damageReductionAmountArray = damageReductionAmounts;
        this.enchantability = enchantability;
        this.soundEvent = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairMaterial = new LazyValue<>(repairMaterialSupplier);
    }

    private ModArmorMaterials(IItemProvider woolItem) {
        this(woolItem.asItem().getDescriptionId() + "_cloth", 12, new int[]{1, 2, 3, 1}, 15, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, () -> {
            return Ingredient.of(woolItem);
        });
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlotType slot) {
        return MAX_DAMAGE_ARRAY[slot.getIndex()] * maxDamageFactor;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlotType slot) {
        return damageReductionAmountArray[slot.getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return soundEvent;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairMaterial.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
