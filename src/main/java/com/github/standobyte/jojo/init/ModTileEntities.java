package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.tileentity.PillarmanBossTileEntity;
import com.github.standobyte.jojo.tileentity.StoneMaskTileEntity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTileEntities {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, JojoMod.MOD_ID);
    
    public static final RegistryObject<TileEntityType<StoneMaskTileEntity>> STONE_MASK = TILE_ENTITIES.register("stone_mask", 
            () -> TileEntityType.Builder.of(StoneMaskTileEntity::new, ModBlocks.STONE_MASK.get()).build(null));
    
    public static final RegistryObject<TileEntityType<PillarmanBossTileEntity>> SLUMBERING_PILLARMAN = TILE_ENTITIES.register("slumbering_pillarman", 
            () -> TileEntityType.Builder.of(PillarmanBossTileEntity::new, ModBlocks.SLUMBERING_PILLARMAN.get()).build(null));
}
