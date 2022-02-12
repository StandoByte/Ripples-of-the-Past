package com.github.standobyte.jojo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModDataSerializers;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModLootModifierSerializers;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.init.ModPaintings;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModPotions;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.init.ModStructures;
import com.github.standobyte.jojo.init.ModTileEntities;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(JojoMod.MOD_ID)
public class JojoMod {
	public static final String MOD_ID = "jojo";
	public static final Logger LOGGER = LogManager.getLogger();
    public static final boolean TEST_BUILD = true;
    
	public static final ItemGroup MAIN_TAB = new ItemGroup("jojo_tab") {
	    @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.STONE_MASK.get());
        }
	};
	
    public JojoMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, JojoModConfig.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, JojoModConfig.clientSpec);
        if (TEST_BUILD) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, BalanceTestServerConfig.serverTestSpec);
        }
        
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        
        ModActions.Registry.initRegistry();
        ModActions.ACTIONS.register(modEventBus);
        ModNonStandPowers.Registry.initRegistry();
        ModNonStandPowers.POWERS.register(modEventBus);
        ModStandTypes.Registry.initRegistry();
        ModStandTypes.STANDS.register(modEventBus);
        
        ModEntityAttributes.ATTRIBUTES.register(modEventBus);
        ModDataSerializers.DATA_SERIALIZERS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModEntityTypes.ENTITIES.register(modEventBus);
        ModLootModifierSerializers.LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        ModPaintings.PAINTINGS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModPotions.POTIONS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModStructures.STRUCTURES.register(modEventBus);
        ModTileEntities.TILE_ENTITIES.register(modEventBus);
        
        MinecraftForge.EVENT_BUS.register(this);
    }
}
