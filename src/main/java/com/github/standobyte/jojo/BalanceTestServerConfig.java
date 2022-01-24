package com.github.standobyte.jojo;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class BalanceTestServerConfig {

    public static class ServerConfig {
        
        public final ForgeConfigSpec.IntValue resolveEffectBaseDuration;
        public final ForgeConfigSpec.IntValue resolveEffectCapDuration;
        
        ServerConfig(ForgeConfigSpec.Builder builder) {
            resolveEffectBaseDuration = builder
                    .comment(" Base duration of the Resolve effect, ", " i.e., how long will it take for Resolve IV to drain the whole bar (if no points are added).")
                    .defineInRange("resolveEffectBaseDuration", 800, 0, 999999);
            
            resolveEffectCapDuration = builder
                    .comment(" Max duration of Resolve", " When the effect expires, the Resolve bar is set to 0.")
                    .defineInRange("resolveEffectBaseDuration", 1200, 0, 999999);
        }
    }


    static final ForgeConfigSpec serverTestSpec;
    public static final ServerConfig TEST_CONFIG;
    static {
        final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        serverTestSpec = specPair.getRight();
        TEST_CONFIG = specPair.getLeft();
    }
}
