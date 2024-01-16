package com.github.standobyte.jojo.client.render.entity.bb;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.github.standobyte.jojo.JojoMod;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ModelJsonParser {
    
    private static final JsonParser JSON = new JsonParser();
    
    public static JsonObject getModelJson(String name) {
        InputStream inputStream = JojoMod.class.getResourceAsStream("/assets/jojo/geo/" + name);
        return JSON.parse(new InputStreamReader(inputStream)).getAsJsonObject();
    }
    
}
