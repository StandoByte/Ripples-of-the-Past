package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.entity.itemprojectile.BladeHatEntity;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BladeHatItem extends CustomModelArmorItem {
    
    public BladeHatItem(IArmorMaterial material, EquipmentSlotType slot, Properties builder) {
        super(material, slot, builder);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (player.isShiftKeyDown()) {
            return super.use(world, player, hand);
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            BladeHatEntity hat = new BladeHatEntity(world, player, stack);
            hat.shootFromRotation(player, 0.75F, 0.5F);
            world.addFreshEntity(hat);
        }
        player.playSound(ModSounds.BLADE_HAT_THROW.get(), 1.0F, 0.75F + random.nextFloat() * 0.5F);
        if (!player.abilities.instabuild) {
            stack.shrink(1);
        }
        return ActionResult.success(stack);
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment == Enchantments.SHARPNESS;
    }
}
