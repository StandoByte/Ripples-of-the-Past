package com.github.standobyte.jojo;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TestServerConfig {
    
    public static class ServerConfig {
        public final ForgeConfigSpec.DoubleValue maxResolve;
        public final ForgeConfigSpec.DoubleValue resolveDecay;
        public final ForgeConfigSpec.IntValue noResolveDecayTicks;
        public final ForgeConfigSpec.IntValue resolveModeTicks;
        public final ForgeConfigSpec.DoubleValue maxResolveDmgReduction;
        
        ServerConfig(ForgeConfigSpec.Builder builder) {
            maxResolve = builder
                    .comment(" Max Resolve value.")
                    .translation("jojo.config.maxResolve") 
                    .defineInRange("maxResolve", 100D, 0, Float.MAX_VALUE);
            
            resolveDecay = builder
                    .comment(" Resolve decay value.")
                    .translation("jojo.config.resolveDecay") 
                    .defineInRange("resolveDecay", 1D, 0, Float.MAX_VALUE);
            
            noResolveDecayTicks = builder
                    .comment(" Length of time period (in ticks) after gaining Resolve points during which they do not tick down.")
                    .translation("jojo.config.noResolveDecayTicks") 
                    .defineInRange("noResolveDecayTicks", 100, 0, Integer.MAX_VALUE);
            
            resolveModeTicks = builder
                    .comment(" Resolve Mode length in ticks.")
                    .translation("jojo.config.resolveModeTicks") 
                    .defineInRange("resolveModeTicks", 800, 0, Integer.MAX_VALUE);
            
            maxResolveDmgReduction = builder
                    .comment(" Max damage reduction from resolve (in linear relationship with resolve).")
                    .translation("jojo.config.maxResolveDmgReduction") 
                    .defineInRange("maxResolveDmgReduction", 0.8, 0, 1);
        }
    }
    
    
    
    static final ForgeConfigSpec serverSpec;
    public static final ServerConfig SERVER_CONFIG;
    static {
        final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        serverSpec = specPair.getRight();
        SERVER_CONFIG = specPair.getLeft();
    }

}
