package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class SuperAjaStoneItem extends AjaStoneItem {

    public SuperAjaStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void useStone(World world, LivingEntity player, ItemStack itemStack, float damage, boolean perk, boolean checkLight) {
        super.useStone(world, player, itemStack, damage * 4F, perk, checkLight);
    }

    @Override
    protected void breakItem(World world, PlayerEntity player, ItemStack itemStack, boolean perk) {
        if (!player.abilities.instabuild) {
            itemStack.hurtAndBreak(1, player, pl -> {
                pl.addItem(new ItemStack(Items.REDSTONE));
            });
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 50;
    }
    
    @Override
    protected int getCooldown() {
        return 250;
    }
    
    @Override
    protected float getHamonChargeCost() {
        return 1000;
    }
    
    protected SoundEvent getHamonChargeVoiceLine() {
        return ModSounds.LISA_LISA_SUPER_AJA.get();
    }
}
