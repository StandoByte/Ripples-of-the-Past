package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.block.MagiciansRedFireBlock;
import com.github.standobyte.jojo.block.MeteoricOreBlock;
import com.github.standobyte.jojo.block.PillarmanBossMultiBlock;
import com.github.standobyte.jojo.block.StoneMaskBlock;
import com.github.standobyte.jojo.block.WoodenCoffinBlock;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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
    
    public static final RegistryObject<PillarmanBossMultiBlock> SLUMBERING_PILLARMAN = BLOCKS.register("slumbering_pillarman", 
            () -> new PillarmanBossMultiBlock(Block.Properties.copy(Blocks.BEDROCK).isValidSpawn((state, reader, pos, entityType) -> false)));
    
    public static final RegistryObject<Block> METEORIC_IRON = BLOCKS.register("meteoric_iron", 
           () -> new Block(Block.Properties.copy(Blocks.IRON_BLOCK).harvestTool(ToolType.PICKAXE).harvestLevel(1).requiresCorrectToolForDrops()));
    
    public static final RegistryObject<MeteoricOreBlock> METEORIC_ORE = BLOCKS.register("meteoric_ore", 
           () -> new MeteoricOreBlock(Block.Properties.of(Material.METAL).strength(10.0F, 3.0F).harvestTool(ToolType.PICKAXE).harvestLevel(3).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    
    public static final RegistryObject<MagiciansRedFireBlock> MAGICIANS_RED_FIRE = BLOCKS.register("magicians_red_fire", 
            () -> new MagiciansRedFireBlock(Block.Properties.of(Material.FIRE, MaterialColor.FIRE).noCollission().instabreak().lightLevel((blockState) -> {
                return 15;
            }).sound(SoundType.WOOL)));
    
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_WHITE = BLOCKS.register("wooden_coffin_oak_white", () -> woodenCoffin(DyeColor.WHITE));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_ORANGE = BLOCKS.register("wooden_coffin_oak_orange", () -> woodenCoffin(DyeColor.ORANGE));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_MAGENTA = BLOCKS.register("wooden_coffin_oak_magenta", () -> woodenCoffin(DyeColor.MAGENTA));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_LIGHT_BLUE = BLOCKS.register("wooden_coffin_oak_light_blue", () -> woodenCoffin(DyeColor.LIGHT_BLUE));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_YELLOW = BLOCKS.register("wooden_coffin_oak_yellow", () -> woodenCoffin(DyeColor.YELLOW));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_LIME = BLOCKS.register("wooden_coffin_oak_lime", () -> woodenCoffin(DyeColor.LIME));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_PINK = BLOCKS.register("wooden_coffin_oak_pink", () -> woodenCoffin(DyeColor.PINK));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_GRAY = BLOCKS.register("wooden_coffin_oak_gray", () -> woodenCoffin(DyeColor.GRAY));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_LIGHT_GRAY = BLOCKS.register("wooden_coffin_oak_light_gray", () -> woodenCoffin(DyeColor.LIGHT_GRAY));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_CYAN = BLOCKS.register("wooden_coffin_oak_cyan", () -> woodenCoffin(DyeColor.CYAN));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_PURPLE = BLOCKS.register("wooden_coffin_oak_purple", () -> woodenCoffin(DyeColor.PURPLE));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_BLUE = BLOCKS.register("wooden_coffin_oak_blue", () -> woodenCoffin(DyeColor.BLUE));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_BROWN = BLOCKS.register("wooden_coffin_oak_brown", () -> woodenCoffin(DyeColor.BROWN));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_GREEN = BLOCKS.register("wooden_coffin_oak_green", () -> woodenCoffin(DyeColor.GREEN));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_RED = BLOCKS.register("wooden_coffin_oak_red", () -> woodenCoffin(DyeColor.RED));
    public static final RegistryObject<WoodenCoffinBlock> WOODEN_COFFIN_OAK_BLACK = BLOCKS.register("wooden_coffin_oak_black", () -> woodenCoffin(DyeColor.BLACK));
    
    private static WoodenCoffinBlock woodenCoffin(DyeColor color) {
        return new WoodenCoffinBlock(color, Block.Properties.copy(Blocks.OAK_PLANKS).harvestTool(ToolType.AXE).harvestLevel(1));
    }
}
