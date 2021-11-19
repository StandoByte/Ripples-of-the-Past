package com.github.standobyte.jojo.tileentity;

import com.github.standobyte.jojo.init.ModTileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class PillarmanBossTileEntity extends TileEntity implements ITickableTileEntity {
    private int absorbedLife;
    
    public PillarmanBossTileEntity() {
        super(ModTileEntities.SLUMBERING_PILLARMAN.get());
    }

    protected PillarmanBossTileEntity(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
    }
    
    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        absorbedLife = compound.getInt("AbsorbedLife");
    }
    
    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.putInt("AbsorbedLife", absorbedLife);
        return compound;
    }

    @Override
    public void tick() {
        // TODO AYAYAYAII
    }
    
    public void incAbsorbed() {
        absorbedLife++;
        setChanged();
        BlockState blockState = this.getBlockState();
        level.sendBlockUpdated(worldPosition, blockState, blockState, 2);
    }
    
    public int getAbsorbedLife() {
        return absorbedLife;
    }
}
