package com.github.standobyte.jojo.tileentity;

import com.github.standobyte.jojo.block.StoneMaskBlock;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModTileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;

public class StoneMaskTileEntity extends TileEntity implements ITickableTileEntity {
    protected ItemStack maskStack = new ItemStack(ModItems.STONE_MASK.get());
    private int activationTicks;
    
    public StoneMaskTileEntity() {
        super(ModTileEntities.STONE_MASK.get());
    }

    protected StoneMaskTileEntity(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
    }
    
    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        if (compound.contains("Item", 10)) {
            maskStack = ItemStack.of(compound.getCompound("Item"));
        }
        activationTicks = compound.getInt("ActivationTicks");
    }
    
    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.put("Item", maskStack.save(new CompoundNBT()));
        compound.putInt("ActivationTicks", activationTicks);
        return compound;
    }
    
    @Override
    public void tick() {
        if (activationTicks > 0) {
            activationTicks--;
            setChanged();
            if (activationTicks == 0) {
                level.playSound(null, this.getBlockPos(), ModSounds.STONE_MASK_DEACTIVATION.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(StoneMaskBlock.BLOOD_ACTIVATION, false));
            }
        }
    }
    
    public void activate() {
        activationTicks = 100;
        setChanged();
        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(StoneMaskBlock.BLOOD_ACTIVATION, true));
    }
    
    public boolean isActivated() {
        return activationTicks > 0;
    }
    
    public void setStack(ItemStack stack) {
        this.maskStack = stack;
    }
    
    public ItemStack getStack() {
        return maskStack.copy();
    }
}
