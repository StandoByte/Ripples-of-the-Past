package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.client.render.armor.ArmorModelRegistry;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CustomModelArmorItem extends ArmorItem {
    protected String textureStr;

    public CustomModelArmorItem(IArmorMaterial material, EquipmentSlotType slot, Properties builder) {
        super(material, slot, builder);
    }
    
    @Override
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlotType armorSlot, A _default) {
        A model = (A) ArmorModelRegistry.getModel(this);
        return model;
    }
    
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        if (textureStr == null) {
            textureStr = createTexturePath(getRegistryName());
        }
        return textureStr;
    }
    
    protected String createTexturePath(ResourceLocation regName) {
        return regName.getNamespace() + ":textures/armor/" + regName.getPath() + ".png";
    }
}
