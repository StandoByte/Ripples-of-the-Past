package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.entity.itemprojectile.BladeHatEntity;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BladeHatItem extends CustomModelArmorItem {
    
    public BladeHatItem(IArmorMaterial material, EquipmentSlotType slot, Properties builder) {
        super(material, slot, builder);

        DispenserBlock.registerBehavior(this, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(IBlockSource blockSource, ItemStack stack) {
                return ArmorItem.dispenseArmor(blockSource, stack) ? stack : shootProjectile(blockSource, stack);
            }
            
            private ItemStack shootProjectile(IBlockSource blockSource, ItemStack itemStack) {
                World world = blockSource.getLevel();
                IPosition position = DispenserBlock.getDispensePosition(blockSource);
                Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
                BladeHatEntity hat = new BladeHatEntity(world, position.x(), position.y(), position.z(), itemStack);
                hat.pickup = AbstractArrowEntity.PickupStatus.ALLOWED;
                hat.shoot(direction.getStepX(), direction.getStepY() + 0.1, direction.getStepZ(), 1.1F, 6.0F);
                world.addFreshEntity(hat);
                itemStack.shrink(1);
                return itemStack;
            }
        });
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
