package com.github.standobyte.jojo.command.configpack;

import com.google.gson.Gson;

import net.minecraft.client.resources.JsonReloadListener;

public abstract class JsonDataConfig extends JsonReloadListener implements IDataConfig {
    private final Gson gson;
    
    public JsonDataConfig(String directory) {
        super(GSON, directory);
        this.gson = GSON;
    }
    
    public JsonDataConfig(Gson gson, String directory) {
        super(gson, directory);
        this.gson = gson;
    }
    
    @Override
    public Gson getGson() {
        return gson;
    }

}
