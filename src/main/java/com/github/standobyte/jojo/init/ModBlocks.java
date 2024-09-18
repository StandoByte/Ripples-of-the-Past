package com.github.standobyte.jojo.init;

import java.util.Map;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.block.MagiciansRedFireBlock;
import com.github.standobyte.jojo.block.MeteoricOreBlock;
import com.github.standobyte.jojo.block.PillarmanBossMultiBlock;
import com.github.standobyte.jojo.block.StoneMaskBlock;
import com.github.standobyte.jojo.block.WoodenCoffinBlock;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, JojoMod.MOD_ID);
    
    
    public static final RegistryObject<StoneMaskBlock> STONE_MASK = BLOCKS.register("stone_mask", 
            () -> new StoneMaskBlock(Block.Properties.copy(Blocks.STONE).harvestLevel(0).requiresCorrectToolForDrops().noCollission().isValidSpawn((state, reader, pos, entityType) -> false)));
    
    public static final RegistryObject<StoneMaskBlock> AJA_STONE_MASK = BLOCKS.register("aja_stone_mask", 
            () -> new StoneMaskBlock(Block.Properties.copy(Blocks.STONE).harvestLevel(0).requiresCorrectToolForDrops().noCollission().isValidSpawn((state, reader, pos, entityType) -> false)));
    
    public static final RegistryObject<PillarmanBossMultiBlock> SLUMBERING_PILLARMAN = BLOCKS.register("slumbering_pillarman", 
            () -> new PillarmanBossMultiBlock(Block.Properties.copy(Blocks.BEDROCK).isValidSpawn((state, reader, pos, entityType) -> false)));
    
    public static final RegistryObject<FlowingFluidBlock> BOILING_BLOOD = BLOCKS.register("boiling_blood", 
            () -> new FlowingFluidBlock(ModFluids.BOILING_BLOOD, AbstractBlock.Properties.of(Material.LAVA)
                    .noCollission().randomTicks().strength(100.0F).lightLevel(blockState -> 15).noDrops()));
    
    public static final RegistryObject<Block> METEORIC_IRON = BLOCKS.register("meteoric_iron", 
           () -> new Block(Block.Properties.copy(Blocks.IRON_BLOCK).harvestTool(ToolType.PICKAXE).harvestLevel(1).requiresCorrectToolForDrops()));
    
    public static final RegistryObject<MeteoricOreBlock> METEORIC_ORE = BLOCKS.register("meteoric_ore", 
           () -> new MeteoricOreBlock(Block.Properties.of(Material.METAL).strength(10.0F, 3.0F).harvestTool(ToolType.PICKAXE).harvestLevel(3).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    
    public static final RegistryObject<MagiciansRedFireBlock> MAGICIANS_RED_FIRE = BLOCKS.register("magicians_red_fire", 
            () -> new MagiciansRedFireBlock(Block.Properties.of(Material.FIRE, MaterialColor.FIRE).noCollission().instabreak().lightLevel((blockState) -> {
                return 15;
            }).sound(SoundType.WOOL)));
    
    public static final Map<DyeColor, RegistryObject<WoodenCoffinBlock>> WOODEN_COFFIN_OAK = ModItems.register16colorsBlock("wooden_coffin_oak", 
            color -> new WoodenCoffinBlock(color, Block.Properties.copy(Blocks.OAK_PLANKS).harvestTool(ToolType.AXE).harvestLevel(1)));
    
}
