package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.renderer.RoadRollerISTER;
import com.github.standobyte.jojo.item.AjaStoneItem;
import com.github.standobyte.jojo.item.BladeHatItem;
import com.github.standobyte.jojo.item.ClackersItem;
import com.github.standobyte.jojo.item.CustomModelArmorItem;
import com.github.standobyte.jojo.item.GumItem;
import com.github.standobyte.jojo.item.KnifeItem;
import com.github.standobyte.jojo.item.ModArmorMaterials;
import com.github.standobyte.jojo.item.RoadRollerItem;
import com.github.standobyte.jojo.item.SatiporojaScarfItem;
import com.github.standobyte.jojo.item.SledgehammerItem;
import com.github.standobyte.jojo.item.StandArrowItem;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.item.StandRemoverItem;
import com.github.standobyte.jojo.item.StoneMaskItem;
import com.github.standobyte.jojo.item.SuperAjaStoneItem;
import com.github.standobyte.jojo.item.TommyGunItem;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;
import net.minecraft.item.Rarity;
import net.minecraft.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, JojoMod.MOD_ID);


    
    public static final RegistryObject<SledgehammerItem> IRON_SLEDGEHAMMER = ITEMS.register("sledgehammer", 
            () -> new SledgehammerItem(ItemTier.IRON, 9, -3.3F, new Item.Properties().tab(JojoMod.MAIN_TAB)));

    public static final RegistryObject<BladeHatItem> BLADE_HAT = ITEMS.register("blade_hat", 
            () -> new BladeHatItem(ModArmorMaterials.BLACK_CLOTH, EquipmentSlotType.HEAD, new Item.Properties().tab(JojoMod.MAIN_TAB)));
    
    public static final RegistryObject<StoneMaskItem> STONE_MASK = ITEMS.register("stone_mask", 
            () -> new StoneMaskItem(ModArmorMaterials.STONE_MASK, EquipmentSlotType.HEAD, new Item.Properties().tab(JojoMod.MAIN_TAB).rarity(Rarity.RARE), ModBlocks.STONE_MASK.get()));

    public static final RegistryObject<CustomModelArmorItem> BREATH_CONTROL_MASK = ITEMS.register("breath_control_mask", 
            () -> new CustomModelArmorItem(ModArmorMaterials.BREATH_CONTROL_MASK, EquipmentSlotType.HEAD, new Item.Properties().tab(JojoMod.MAIN_TAB)));

    public static final RegistryObject<SpawnEggItem> HAMON_MASTER_SPAWN_EGG = ITEMS.register("hamon_master_spawn_egg", 
            () -> new ForgeSpawnEggItem(ModEntityTypes.HAMON_MASTER, 0xF8D100, 0x542722, new Item.Properties().tab(JojoMod.MAIN_TAB)));

    public static final RegistryObject<SpawnEggItem> HUNGRY_ZOMBIE_SPAWN_EGG = ITEMS.register("hungry_zombie_spawn_egg", 
            () -> new ForgeSpawnEggItem(ModEntityTypes.HUNGRY_ZOMBIE, 0x00AFAF, 0x9B9B9B, new Item.Properties().tab(JojoMod.MAIN_TAB)));

    public static final RegistryObject<AjaStoneItem> AJA_STONE = ITEMS.register("aja_stone", 
            () -> new AjaStoneItem(new Item.Properties().tab(JojoMod.MAIN_TAB).rarity(Rarity.UNCOMMON).stacksTo(16)));

    public static final RegistryObject<AjaStoneItem> SUPER_AJA_STONE = ITEMS.register("super_aja_stone", 
            () -> new SuperAjaStoneItem(new Item.Properties().tab(JojoMod.MAIN_TAB).rarity(Rarity.RARE).durability(640)));

    public static final RegistryObject<SatiporojaScarfItem> SATIPOROJA_SCARF = ITEMS.register("satiporoja_scarf", 
            () -> new SatiporojaScarfItem(ModArmorMaterials.SATIPOROJA_SCARF, EquipmentSlotType.HEAD, new Item.Properties().tab(JojoMod.MAIN_TAB).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<ClackersItem> CLACKERS = ITEMS.register("clackers",
            () -> new ClackersItem(new Item.Properties().tab(JojoMod.MAIN_TAB).stacksTo(1)));

    public static final RegistryObject<TommyGunItem> TOMMY_GUN = ITEMS.register("tommy_gun",
            () -> new TommyGunItem(new Item.Properties().tab(JojoMod.MAIN_TAB).stacksTo(1)));

    public static final RegistryObject<BlockItem> SLUMBERING_PILLARMAN = ITEMS.register("slumbering_pillarman", 
            () -> new BlockItem(ModBlocks.SLUMBERING_PILLARMAN.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<KnifeItem> KNIFE = ITEMS.register("knife", 
            () -> new KnifeItem(new Item.Properties().tab(JojoMod.MAIN_TAB).stacksTo(16)));

    public static final RegistryObject<RoadRollerItem> ROAD_ROLLER = ITEMS.register("road_roller", 
            () -> new RoadRollerItem(new Item.Properties().tab(JojoMod.MAIN_TAB).stacksTo(1)
                    .setISTER(() -> RoadRollerISTER::new)));

    public static final RegistryObject<BlockItem> METEORIC_IRON = ITEMS.register("meteoric_iron", 
            () -> new BlockItem(ModBlocks.METEORIC_IRON.get(), new Item.Properties().tab(JojoMod.MAIN_TAB)));

    public static final RegistryObject<BlockItem> METEORITE_ORE = ITEMS.register("meteoric_ore", 
            () -> new BlockItem(ModBlocks.METEORIC_ORE.get(), new Item.Properties().tab(JojoMod.MAIN_TAB).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> METEORIC_SCRAP = ITEMS.register("meteoric_scrap", 
            () -> new Item(new Item.Properties().tab(JojoMod.MAIN_TAB).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> METEORIC_INGOT = ITEMS.register("meteoric_ingot", 
            () -> new Item(new Item.Properties().tab(JojoMod.MAIN_TAB).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<StandArrowItem> STAND_ARROW = ITEMS.register("stand_arrow", 
            () -> new StandArrowItem(new Item.Properties().tab(JojoMod.MAIN_TAB).rarity(Rarity.UNCOMMON).durability(25)));

    public static final RegistryObject<StandArrowItem> STAND_ARROW_BEETLE = ITEMS.register("stand_arrow_beetle", 
            () -> new StandArrowItem(new Item.Properties().tab(JojoMod.MAIN_TAB).rarity(Rarity.RARE).durability(250)));

    public static final RegistryObject<StandDiscItem> STAND_DISC = ITEMS.register("stand_disc",
            () -> new StandDiscItem(new Item.Properties().tab(JojoMod.MAIN_TAB).stacksTo(1)));

    public static final RegistryObject<StandRemoverItem> STAND_REMOVER = ITEMS.register("stand_remover",
            () -> new StandRemoverItem(new Item.Properties().tab(JojoMod.MAIN_TAB).stacksTo(1)));

    public static final RegistryObject<Item> COCOA_GUM = ITEMS.register("cocoa_gum", 
            () -> new GumItem(new Item.Properties()/*.tab(JojoMod.MAIN_TAB)*/.food(new Food.Builder().nutrition(2).saturationMod(0.1F).build())));

}
