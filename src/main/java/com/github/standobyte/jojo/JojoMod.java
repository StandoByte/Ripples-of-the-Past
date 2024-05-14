package com.github.standobyte.jojo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.command.ConfigPackCommand;
import com.github.standobyte.jojo.command.NonStandTypeArgument;
import com.github.standobyte.jojo.command.StandArgument;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModContainers;
import com.github.standobyte.jojo.init.ModDataSerializers;
import com.github.standobyte.jojo.init.ModEnchantments;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModFluids;
import com.github.standobyte.jojo.init.ModGamerules;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModLootModifierSerializers;
import com.github.standobyte.jojo.init.ModPaintings;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModPotions;
import com.github.standobyte.jojo.init.ModRecipeSerializers;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModStructures;
import com.github.standobyte.jojo.init.ModTags;
import com.github.standobyte.jojo.init.ModTileEntities;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.modcompat.OptionalDependencyHelper;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkillTree;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.ForgeBusEventSubscriber;
import com.github.standobyte.jojo.util.mod.JojoModVersion;

import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(JojoMod.MOD_ID)
public class JojoMod {
    public static final String MOD_ID = "jojo";
    public static final Logger LOGGER = LogManager.getLogger();
    // implemented a simple class so that mod version is available in debug too
    public static final JojoModVersion CURRENT_VERSION = new JojoModVersion(2, 2, 0);
    
    @Deprecated
    // Use the field in ModItems
    public static final ItemGroup MAIN_TAB = ModItems.MAIN_TAB;
    
    public static Logger getLogger() {
        return LOGGER;
    }
    
    public JojoMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, JojoModConfig.commonSpec);
        
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        
        JojoCustomRegistries.initCustomRegistries(modEventBus);
        registerVanillaDeferredRegisters(modEventBus);

        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::interMod);
        ModTags.initTags();
    }

    private void registerVanillaDeferredRegisters(IEventBus modEventBus) {
        ModEntityAttributes.ATTRIBUTES.register(modEventBus);
        ModContainers.CONTAINERS.register(modEventBus);
        ModDataSerializers.DATA_SERIALIZERS.register(modEventBus);
        ModStatusEffects.EFFECTS.register(modEventBus);
        ModEnchantments.ENCHANTMENTS.register(modEventBus);
        ModEntityTypes.ENTITIES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModLootModifierSerializers.LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        ModPaintings.PAINTINGS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModPotions.POTIONS.register(modEventBus);
        ModRecipeSerializers.SERIALIZERS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModStructures.STRUCTURES.register(modEventBus);
        ModTileEntities.TILE_ENTITIES.register(modEventBus);
    }
    
    
    
    private void preInit(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ForgeBusEventSubscriber.registerCapabilities();
            
            StandArgument.commonSetupRegister();
            NonStandTypeArgument.commonSetupRegister();

            ModCriteriaTriggers.CriteriaTriggerSupplier.registerAll();
            
            PacketManager.init();
            
            ConfigPackCommand.initConfigs(MinecraftForge.EVENT_BUS);
            
            // things to do after registry events
            ModPotions.registerRecipes();
            
            Action.initShiftVariations();
            for (AbstractHamonSkill hamonSkill : JojoCustomRegistries.HAMON_SKILLS.getRegistry()) {
                hamonSkill.onCommonSetup();
            }
            for (Action<?> action : JojoCustomRegistries.ACTIONS.getRegistry()) {
                action.onCommonSetup();
            }
            for (StandType<?> stand : JojoCustomRegistries.STANDS.getRegistry()) {
                stand.onCommonSetup();
            }
            
            BaseHamonSkillTree.initTrees();
            
            ModGamerules.load();
        });
        
        Attributes.ATTACK_DAMAGE.setSyncable(true);
    }
    
    private void interMod(InterModEnqueueEvent event) {
        event.enqueueWork(() -> {
            OptionalDependencyHelper.init();
        });
    }
}
