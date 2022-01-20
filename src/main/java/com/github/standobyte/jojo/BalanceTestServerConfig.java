package com.github.standobyte.jojo;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class BalanceTestServerConfig {
    
    public static class ServerConfig {
        public final ForgeConfigSpec.DoubleValue resolveDecay;
        public final ForgeConfigSpec.DoubleValue resolveModifierAchieved;
        public final ForgeConfigSpec.DoubleValue resolveModifierHp;
        public final ForgeConfigSpec.IntValue noResolveDecayTicks;
        public final ForgeConfigSpec.IntValue resolveModeTicks;
        public final ForgeConfigSpec.DoubleValue maxResolveDmgReduction;
        public final ForgeConfigSpec.DoubleValue resolveEffectDmgReduction;
        
        ServerConfig(ForgeConfigSpec.Builder builder) {
            resolveDecay = builder
                    .comment(" Resolve decay value.")
                    .translation("jojo.config.resolveDecay") 
                    .defineInRange("resolveDecay", 1D, 0, Float.MAX_VALUE);
            
            resolveModifierAchieved = builder
                    .comment(" Multiplier of resolve added when lower than once achieved level.")
                    .translation("jojo.config.resolveModifierAchieved") 
                    .defineInRange("resolveModifierAchieved", 4D, 1D, Float.MAX_VALUE);
            
            resolveModifierHp = builder
                    .comment(" Limit of multiplier of resolve added from missing health ratio (linear relationship).")
                    .translation("jojo.config.resolveModifierHp") 
                    .defineInRange("resolveModifierHp", 10D, 1D, Float.MAX_VALUE);
            
            noResolveDecayTicks = builder
                    .comment(" Length of time period (in ticks) after gaining Resolve points during which they do not tick down.")
                    .translation("jojo.config.noResolveDecayTicks") 
                    .defineInRange("noResolveDecayTicks", 200, 0, Integer.MAX_VALUE);
            
            resolveModeTicks = builder
                    .comment(" Resolve Mode length in ticks.")
                    .translation("jojo.config.resolveModeTicks") 
                    .defineInRange("resolveModeTicks", 800, 0, Integer.MAX_VALUE);
            
            maxResolveDmgReduction = builder
                    .comment(" Max damage reduction from resolve (in linear relationship with resolve).")
                    .translation("jojo.config.maxResolveDmgReduction") 
                    .defineInRange("maxResolveDmgReduction", 0.5, 0, 1);
            
            resolveEffectDmgReduction = builder
                    .comment(" Damage reduction in Resolve effect.")
                    .translation("jojo.config.resolveEffectDmgReduction") 
                    .defineInRange("resolveEffectDmgReduction", 0.8, 0, 1);
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
