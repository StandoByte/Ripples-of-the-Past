package com.github.standobyte.jojo;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class BalanceTestServerConfig {
    
    public static class ServerConfig {
        public final ForgeConfigSpec.DoubleValue resolveDecay;
        public final ForgeConfigSpec.DoubleValue resolvePassive;
        public final ForgeConfigSpec.DoubleValue resolvePassiveSpeed;
        public final ForgeConfigSpec.DoubleValue resolveModifierHp;
        public final ForgeConfigSpec.IntValue noResolveDecayTicks;
        public final ForgeConfigSpec.IntValue resolveModeTicks;
        public final ForgeConfigSpec.IntValue resolveModeTicksCap;
        public final ForgeConfigSpec.DoubleValue maxResolveDmgReduction;
        public final ForgeConfigSpec.DoubleValue resolveEffectDmgReduction;
        
        ServerConfig(ForgeConfigSpec.Builder builder) {
            resolveDecay = builder
                    .comment(" Resolve decay value.")
                    .translation("jojo.config.resolveDecay") 
                    .defineInRange("resolveDecay", 0.25, 0, Float.MAX_VALUE);
            
            resolvePassive = builder
                    .comment(" Max fraction of resolve bar gained passively when Stand is summoned (when on max resolve level).")
                    .translation("jojo.config.resolvePassive") 
                    .defineInRange("resolvePassive", 0.5, 0, 1);
            
            resolvePassiveSpeed = builder
                    .comment(" Amount of resolve per tick gained passively when Stand is summoned.")
                    .translation("jojo.config.resolvePassiveSpeed") 
                    .defineInRange("resolvePassiveSpeed", 0.1, 0, Float.MAX_VALUE);
            
            resolveModifierHp = builder
                    .comment(" Limit of multiplier of resolve added from missing health ratio.")
                    .translation("jojo.config.resolveModifierHp") 
                    .defineInRange("resolveModifierHp", 10.0, 1, Float.MAX_VALUE);
            
            noResolveDecayTicks = builder
                    .comment(" Length of time period (in ticks) after gaining Resolve points during which they do not tick down.")
                    .translation("jojo.config.noResolveDecayTicks") 
                    .defineInRange("noResolveDecayTicks", 200, 0, Integer.MAX_VALUE);
            
            resolveModeTicks = builder
                    .comment(" Base Resolve effect length in ticks (when on max resolve level).")
                    .translation("jojo.config.resolveModeTicks") 
                    .defineInRange("resolveModeTicks", 1200, 0, Integer.MAX_VALUE);
            
            resolveModeTicksCap = builder
                    .comment(" Resolve effect length cap in ticks.")
                    .translation("jojo.config.resolveModeTicksCap") 
                    .defineInRange("resolveModeTicksCap", 2400, 0, Integer.MAX_VALUE);
            
            maxResolveDmgReduction = builder
                    .comment(" Max damage reduction from resolve.")
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
