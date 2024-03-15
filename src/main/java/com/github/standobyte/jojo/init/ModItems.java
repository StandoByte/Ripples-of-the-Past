package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.item.RoadRollerISTER;
import com.github.standobyte.jojo.client.render.item.standdisc.StandDiscISTER;
import com.github.standobyte.jojo.item.AjaStoneItem;
import com.github.standobyte.jojo.item.BladeHatItem;
import com.github.standobyte.jojo.item.BreathControlMaskItem;
import com.github.standobyte.jojo.item.BubbleGlovesItem;
import com.github.standobyte.jojo.item.CassetteBlankItem;
import com.github.standobyte.jojo.item.CassetteRecordedItem;
import com.github.standobyte.jojo.item.ClackersItem;
import com.github.standobyte.jojo.item.CustomModelArmorItem;
import com.github.standobyte.jojo.item.GlovesItem;
import com.github.standobyte.jojo.item.KnifeItem;
import com.github.standobyte.jojo.item.ModArmorMaterials;
import com.github.standobyte.jojo.item.RoadRollerItem;
import com.github.standobyte.jojo.item.SatiporojaScarfItem;
import com.github.standobyte.jojo.item.SledgehammerItem;
import com.github.standobyte.jojo.item.SoapItem;
import com.github.standobyte.jojo.item.StandArrowItem;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.item.StandRemoverItem;
import com.github.standobyte.jojo.item.StoneMaskItem;
import com.github.standobyte.jojo.item.SuperAjaStoneItem;
import com.github.standobyte.jojo.item.TommyGunItem;
import com.github.standobyte.jojo.item.WalkmanItem;

import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.Rarity;
import net.minecraft.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, JojoMod.MOD_ID);
    
    public static final ItemGroup MAIN_TAB = (new ItemGroup("jojo_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.STONE_MASK.get());
        }
    }).setEnchantmentCategories(new EnchantmentType[]{ModEnchantments.STAND_ARROW});
    
    
    
    public static final RegistryObject<SledgehammerItem> IRON_SLEDGEHAMMER = ITEMS.register("sledgehammer", 
            () -> new SledgehammerItem(ItemTier.IRON, 9, -3.3F, new Item.Properties().tab(MAIN_TAB)));
    
    public static final RegistryObject<BladeHatItem> BLADE_HAT = ITEMS.register("blade_hat", 
            () -> new BladeHatItem(ModArmorMaterials.BLACK_CLOTH, EquipmentSlotType.HEAD, new Item.Properties().tab(MAIN_TAB)));
    
    public static final RegistryObject<StoneMaskItem> STONE_MASK = ITEMS.register("stone_mask", 
            () -> new StoneMaskItem(ModArmorMaterials.STONE_MASK, EquipmentSlotType.HEAD, new Item.Properties().tab(MAIN_TAB).rarity(Rarity.RARE), ModBlocks.STONE_MASK.get()));
    
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_WHITE = ITEMS.register("wooden_coffin_oak_white", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_WHITE.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_ORANGE = ITEMS.register("wooden_coffin_oak_orange", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_ORANGE.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_MAGENTA = ITEMS.register("wooden_coffin_oak_magenta", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_MAGENTA.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_LIGHT_BLUE = ITEMS.register("wooden_coffin_oak_light_blue", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_LIGHT_BLUE.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_YELLOW = ITEMS.register("wooden_coffin_oak_yellow", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_YELLOW.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_LIME = ITEMS.register("wooden_coffin_oak_lime", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_LIME.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_PINK = ITEMS.register("wooden_coffin_oak_pink", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_PINK.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_GRAY = ITEMS.register("wooden_coffin_oak_gray", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_GRAY.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_LIGHT_GRAY = ITEMS.register("wooden_coffin_oak_light_gray", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_LIGHT_GRAY.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_CYAN = ITEMS.register("wooden_coffin_oak_cyan", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_CYAN.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_PURPLE = ITEMS.register("wooden_coffin_oak_purple", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_PURPLE.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_BLUE = ITEMS.register("wooden_coffin_oak_blue", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_BLUE.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_BROWN = ITEMS.register("wooden_coffin_oak_brown", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_BROWN.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_GREEN = ITEMS.register("wooden_coffin_oak_green", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_GREEN.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_RED = ITEMS.register("wooden_coffin_oak_red", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_RED.get(), new Item.Properties().tab(MAIN_TAB).stacksTo(1)));
    public static final RegistryObject<BlockItem> WOODEN_COFFIN_OAK_BLACK = ITEMS.register("wooden_coffin_oak_black", () -> new BlockItem(ModBlocks.WOODEN_COFFIN_OAK_BLACK.get(), new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<CustomModelArmorItem> BREATH_CONTROL_MASK = ITEMS.register("breath_control_mask", 
            () -> new BreathControlMaskItem(new Item.Properties().tab(MAIN_TAB)));
    
    public static final RegistryObject<GlovesItem> GLOVES = ITEMS.register("gloves", 
            () -> new GlovesItem(new Item.Properties().tab(MAIN_TAB).stacksTo(1)));
    
    public static final RegistryObject<GlovesItem> BUBBLE_GLOVES = ITEMS.register("bubble_gloves", 
            () -> new BubbleGlovesItem(new Item.Properties().tab(MAIN_TAB).stacksTo(1))); 
    
    public static final RegistryObject<SoapItem> SOAP = ITEMS.register("soap", 
            () -> new SoapItem(new Item.Properties().tab(MAIN_TAB).stacksTo(1)));

    public static final RegistryObject<SpawnEggItem> HAMON_MASTER_SPAWN_EGG = ITEMS.register("hamon_master_spawn_egg", 
            () -> new ForgeSpawnEggItem(ModEntityTypes.HAMON_MASTER, 0xF8D100, 0x542722, new Item.Properties().tab(MAIN_TAB)));

    public static final RegistryObject<SpawnEggItem> HUNGRY_ZOMBIE_SPAWN_EGG = ITEMS.register("hungry_zombie_spawn_egg", 
            () -> new ForgeSpawnEggItem(ModEntityTypes.HUNGRY_ZOMBIE, 0x00AFAF, 0x9B9B9B, new Item.Properties().tab(MAIN_TAB)));

    public static final RegistryObject<AjaStoneItem> AJA_STONE = ITEMS.register("aja_stone", 
            () -> new AjaStoneItem(new Item.Properties().tab(MAIN_TAB).rarity(Rarity.UNCOMMON).stacksTo(16)));

    public static final RegistryObject<AjaStoneItem> SUPER_AJA_STONE = ITEMS.register("super_aja_stone", 
            () -> new SuperAjaStoneItem(new Item.Properties().tab(MAIN_TAB).rarity(Rarity.RARE).durability(640)));

    public static final RegistryObject<SatiporojaScarfItem> SATIPOROJA_SCARF = ITEMS.register("satiporoja_scarf", 
            () -> new SatiporojaScarfItem(ModArmorMaterials.SATIPOROJA_SCARF, EquipmentSlotType.HEAD, new Item.Properties().tab(MAIN_TAB).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<ClackersItem> CLACKERS = ITEMS.register("clackers",
            () -> new ClackersItem(new Item.Properties().tab(MAIN_TAB).stacksTo(1)));

    public static final RegistryObject<TommyGunItem> TOMMY_GUN = ITEMS.register("tommy_gun",
            () -> new TommyGunItem(new Item.Properties().tab(MAIN_TAB).stacksTo(1)));

    public static final RegistryObject<BlockItem> SLUMBERING_PILLARMAN = ITEMS.register("slumbering_pillarman", 
            () -> new BlockItem(ModBlocks.SLUMBERING_PILLARMAN.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<KnifeItem> KNIFE = ITEMS.register("knife", 
            () -> new KnifeItem(new Item.Properties().tab(MAIN_TAB).stacksTo(16)));

    public static final RegistryObject<RoadRollerItem> ROAD_ROLLER = ITEMS.register("road_roller", 
            () -> new RoadRollerItem(new Item.Properties().tab(MAIN_TAB).stacksTo(1)
                    .setISTER(() -> RoadRollerISTER::new)));

    public static final RegistryObject<Item> CRAZY_DIAMOND_NON_BLOCK_ANCHOR = ITEMS.register("crazy_diamond_non_block_anchor", 
            () -> new Item(new Item.Properties()));

//    public static final RegistryObject<SpawnEggItem> ROCK_PAPER_SCISSORS_KID_SPAWN_EGG = ITEMS.register("rps_kid_spawn_egg", 
//            () -> new ForgeSpawnEggItem(ModEntityTypes.ROCK_PAPER_SCISSORS_KID, 0x563C33, 0xBD8B72, new Item.Properties().tab(MAIN_TAB)));

    public static final RegistryObject<BlockItem> METEORIC_IRON = ITEMS.register("meteoric_iron", 
            () -> new BlockItem(ModBlocks.METEORIC_IRON.get(), new Item.Properties().tab(MAIN_TAB)));

    public static final RegistryObject<BlockItem> METEORITE_ORE = ITEMS.register("meteoric_ore", 
            () -> new BlockItem(ModBlocks.METEORIC_ORE.get(), new Item.Properties().tab(MAIN_TAB).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> METEORIC_SCRAP = ITEMS.register("meteoric_scrap", 
            () -> new Item(new Item.Properties().tab(MAIN_TAB).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> METEORIC_INGOT = ITEMS.register("meteoric_ingot", 
            () -> new Item(new Item.Properties().tab(MAIN_TAB).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<StandArrowItem> STAND_ARROW = ITEMS.register("stand_arrow", 
            () -> new StandArrowItem(new Item.Properties().tab(MAIN_TAB).rarity(Rarity.UNCOMMON).durability(25), 10));

    public static final RegistryObject<StandArrowItem> STAND_ARROW_BEETLE = ITEMS.register("stand_arrow_beetle", 
            () -> new StandArrowItem(new Item.Properties().tab(MAIN_TAB).rarity(Rarity.RARE).durability(250), 25));

    public static final RegistryObject<StandDiscItem> STAND_DISC = ITEMS.register("stand_disc",
            () -> new StandDiscItem(new Item.Properties().tab(MAIN_TAB).stacksTo(1)
                    .setISTER(() -> StandDiscISTER::new)));

    public static final RegistryObject<StandRemoverItem> STAND_REMOVER = ITEMS.register("stand_remover",
            () -> new StandRemoverItem(new Item.Properties().tab(MAIN_TAB).stacksTo(1), StandRemoverItem.Mode.REMOVE, false));

    public static final RegistryObject<StandRemoverItem> STAND_REMOVER_ONE_TIME = ITEMS.register("stand_remover_one_time",
            () -> new StandRemoverItem(new Item.Properties().tab(MAIN_TAB).stacksTo(64), StandRemoverItem.Mode.REMOVE, true));

    public static final RegistryObject<StandRemoverItem> STAND_EJECT = ITEMS.register("stand_eject",
            () -> new StandRemoverItem(new Item.Properties().tab(MAIN_TAB).stacksTo(1), StandRemoverItem.Mode.EJECT, false));

    public static final RegistryObject<StandRemoverItem> STAND_EJECT_ONE_TIME = ITEMS.register("stand_eject_one_time",
            () -> new StandRemoverItem(new Item.Properties().tab(MAIN_TAB).stacksTo(64), StandRemoverItem.Mode.EJECT, true));

    public static final RegistryObject<StandRemoverItem> STAND_FULL_CLEAR = ITEMS.register("stand_full_clear",
            () -> new StandRemoverItem(new Item.Properties().tab(MAIN_TAB).stacksTo(1), StandRemoverItem.Mode.FULL_CLEAR, false));

    public static final RegistryObject<StandRemoverItem> STAND_FULL_CLEAR_ONE_TIME = ITEMS.register("stand_full_clear_one_time",
            () -> new StandRemoverItem(new Item.Properties().tab(MAIN_TAB).stacksTo(64), StandRemoverItem.Mode.FULL_CLEAR, true));

//    public static final RegistryObject<Item> COCOA_GUM = ITEMS.register("cocoa_gum", 
//            () -> new GumItem(new Item.Properties()/*.tab(MAIN_TAB)*/.food(new Food.Builder().nutrition(2).saturationMod(0.1F).build())));

    public static final RegistryObject<Item> WALKMAN = ITEMS.register("walkman", 
            () -> new WalkmanItem(new Item.Properties().tab(MAIN_TAB).stacksTo(1)));

    public static final RegistryObject<Item> CASSETTE_BLANK = ITEMS.register("cassette_blank", 
            () -> new CassetteBlankItem(new Item.Properties().tab(MAIN_TAB)));

    public static final RegistryObject<CassetteRecordedItem> CASSETTE_RECORDED = ITEMS.register("cassette_recorded", 
            () -> new CassetteRecordedItem(new Item.Properties().stacksTo(1).tab(MAIN_TAB)));


}
