package com.github.standobyte.jojo.item;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.SweepingEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SledgehammerItem extends PickaxeItem {

    public SledgehammerItem(IItemTier tier, int attackDamage, float attackSpeed, Properties builder) {
        super(tier, attackDamage, attackSpeed, builder);
    }
    
    @Override
    public boolean mineBlock(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (!world.isClientSide() && state.getDestroySpeed(world, pos) != 0.0F) {
            stack.hurtAndBreak(2, entityLiving, entity -> {
                entity.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
            });
        }
        return true;
     }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, entity -> {
            entity.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
        });
        return true;
     }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || 
                enchantment.category.equals(EnchantmentType.WEAPON) && !(enchantment instanceof SweepingEnchantment);
    }
}
