package com.github.standobyte.jojo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModContainers;
import com.github.standobyte.jojo.init.ModDataSerializers;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModEnchantments;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModLootModifierSerializers;
import com.github.standobyte.jojo.init.ModPaintings;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModPotions;
import com.github.standobyte.jojo.init.ModRecipeSerializers;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStructures;
import com.github.standobyte.jojo.init.ModTileEntities;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.util.mod.JojoModVersion;

import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(JojoMod.MOD_ID)
public class JojoMod {
    public static final String MOD_ID = "jojo";
    public static final Logger LOGGER = LogManager.getLogger();
    // implemented a simple class so that mod version is available in debug too
    public static final JojoModVersion CURRENT_VERSION = new JojoModVersion(2, 2, 0);
    
    public static final ItemGroup MAIN_TAB = (new ItemGroup("jojo_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.STONE_MASK.get());
        }
    }).setEnchantmentCategories(new EnchantmentType[]{ModEnchantments.STAND_ARROW});
    
    public static Logger getLogger() {
        return LOGGER;
    }
    
    public JojoMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, JojoModConfig.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, JojoModConfig.clientSpec);
        
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        
        JojoCustomRegistries.initCustomRegistries(modEventBus);
        registerVanillaDeferredRegisters(modEventBus);
    }

    private void registerVanillaDeferredRegisters(IEventBus modEventBus) {
        ModEntityAttributes.ATTRIBUTES.register(modEventBus);
        ModContainers.CONTAINERS.register(modEventBus);
        ModDataSerializers.DATA_SERIALIZERS.register(modEventBus);
        ModStatusEffects.EFFECTS.register(modEventBus);
        ModEnchantments.ENCHANTMENTS.register(modEventBus);
        ModEntityTypes.ENTITIES.register(modEventBus);
        ModLootModifierSerializers.LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        ModPaintings.PAINTINGS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModPotions.POTIONS.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModStructures.STRUCTURES.register(modEventBus);
        ModTileEntities.TILE_ENTITIES.register(modEventBus);
    }
}
