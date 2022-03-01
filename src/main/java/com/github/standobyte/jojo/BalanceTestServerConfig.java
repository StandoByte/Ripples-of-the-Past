package com.github.standobyte.jojo;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class BalanceTestServerConfig {

    public static class ServerConfig {
        
        
        ServerConfig(ForgeConfigSpec.Builder builder) {
            
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
