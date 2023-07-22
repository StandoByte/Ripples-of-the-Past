package com.github.standobyte.jojo.util;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.chunk.ChunkCap;
import com.github.standobyte.jojo.capability.chunk.ChunkCapStorage;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.EntityUtilCap;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapStorage;
import com.github.standobyte.jojo.capability.entity.LivingUtilCap;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapStorage;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapStorage;
import com.github.standobyte.jojo.capability.entity.ProjectileHamonChargeCap;
import com.github.standobyte.jojo.capability.entity.ProjectileHamonChargeCapStorage;
import com.github.standobyte.jojo.capability.entity.power.NonStandCapStorage;
import com.github.standobyte.jojo.capability.entity.power.StandCapStorage;
import com.github.standobyte.jojo.capability.item.cassette.CassetteCap;
import com.github.standobyte.jojo.capability.item.cassette.CassetteCapStorage;
import com.github.standobyte.jojo.capability.item.walkman.WalkmanDataCap;
import com.github.standobyte.jojo.capability.item.walkman.WalkmanDataCapStorage;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCap;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapStorage;
import com.github.standobyte.jojo.capability.world.WorldUtilCap;
import com.github.standobyte.jojo.capability.world.WorldUtilCapStorage;
import com.github.standobyte.jojo.command.StandArgument;
import com.github.standobyte.jojo.command.configpack.StandStatsConfig;
import com.github.standobyte.jojo.init.ModPotions;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.NonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkillTree;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandPower;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CommonSetup {
    
    @SubscribeEvent
    public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CapabilityManager.INSTANCE.register(IStandPower.class, new StandCapStorage(), () -> new StandPower(null));
            CapabilityManager.INSTANCE.register(INonStandPower.class, new NonStandCapStorage(), () -> new NonStandPower(null));
            CapabilityManager.INSTANCE.register(PlayerUtilCap.class, new PlayerUtilCapStorage(), () -> new PlayerUtilCap(null));
            CapabilityManager.INSTANCE.register(ClientPlayerUtilCap.class, new IStorage<ClientPlayerUtilCap>() {
                @Override public INBT writeNBT(Capability<ClientPlayerUtilCap> capability, ClientPlayerUtilCap instance, Direction side) { return null; }
                @Override public void readNBT(Capability<ClientPlayerUtilCap> capability, ClientPlayerUtilCap instance, Direction side, INBT nbt) {}
            }, () -> new ClientPlayerUtilCap(null));
            CapabilityManager.INSTANCE.register(LivingUtilCap.class, new LivingUtilCapStorage(), () -> new LivingUtilCap(null));
            CapabilityManager.INSTANCE.register(EntityUtilCap.class, new EntityUtilCapStorage(), () -> new EntityUtilCap(null));
            CapabilityManager.INSTANCE.register(ProjectileHamonChargeCap.class, new ProjectileHamonChargeCapStorage(), () -> new ProjectileHamonChargeCap(null));
            
            CapabilityManager.INSTANCE.register(WorldUtilCap.class, new WorldUtilCapStorage(), () -> new WorldUtilCap(null));
            CapabilityManager.INSTANCE.register(SaveFileUtilCap.class, new SaveFileUtilCapStorage(), () -> new SaveFileUtilCap(null));

            CapabilityManager.INSTANCE.register(ChunkCap.class, new ChunkCapStorage(), () -> new ChunkCap(null));
            
            CapabilityManager.INSTANCE.register(CassetteCap.class, new CassetteCapStorage(), () -> new CassetteCap(null));
            CapabilityManager.INSTANCE.register(WalkmanDataCap.class, new WalkmanDataCapStorage(), () -> new WalkmanDataCap(null));
            
            ArgumentTypes.register("stand", StandArgument.class, new ArgumentSerializer<>(StandArgument::new));

            ModCriteriaTriggers.CriteriaTriggerSupplier.registerAll();
            
            PacketManager.init();
            
            StandStatsConfig.init(MinecraftForge.EVENT_BUS);
            
            // things to do after registry events
            ModPotions.registerRecipes();
            
            Action.initShiftVariations();
            for (Action<?> action : JojoCustomRegistries.ACTIONS.getRegistry()) {
                action.onCommonSetup();
            }
            for (StandType<?> stand : JojoCustomRegistries.STANDS.getRegistry()) {
                stand.onCommonSetup();
            }
            
            BaseHamonSkillTree.initTrees();
        });
    }
}
