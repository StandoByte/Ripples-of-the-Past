package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.block.MeteoricOreBlock;
import com.github.standobyte.jojo.block.PillarmanBossMultiBlock;
import com.github.standobyte.jojo.block.StoneMaskBlock;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, JojoMod.MOD_ID);
    
    
    public static final RegistryObject<StoneMaskBlock> STONE_MASK = BLOCKS.register("stone_mask", 
           () -> new StoneMaskBlock(Block.Properties.copy(Blocks.STONE).harvestLevel(0).requiresCorrectToolForDrops().noCollission().isValidSpawn((state, reader, pos, entityType) -> false)));
    
    public static final RegistryObject<PillarmanBossMultiBlock> SLUMBERING_PILLARMAN = BLOCKS.register("slumbering_pillarman", 
            () -> new PillarmanBossMultiBlock(Block.Properties.copy(Blocks.BEDROCK).isValidSpawn((state, reader, pos, entityType) -> false)));
    
    public static final RegistryObject<Block> METEORIC_IRON = BLOCKS.register("meteoric_iron", 
           () -> new Block(Block.Properties.copy(Blocks.IRON_BLOCK).harvestTool(ToolType.PICKAXE).harvestLevel(1).requiresCorrectToolForDrops()));
    
    public static final RegistryObject<MeteoricOreBlock> METEORIC_ORE = BLOCKS.register("meteoric_ore", 
           () -> new MeteoricOreBlock(Block.Properties.of(Material.METAL).strength(10.0F, 3.0F).harvestTool(ToolType.PICKAXE).harvestLevel(3).requiresCorrectToolForDrops().sound(SoundType.METAL)));
}
