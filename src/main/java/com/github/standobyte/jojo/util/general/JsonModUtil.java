package com.github.standobyte.jojo.util.general;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonModUtil {

    public static void replaceValues(JsonObject in, JsonObject from) {
        from.entrySet().forEach(sourceEntry -> {
            String key = sourceEntry.getKey();
            if (in.has(key)) {
                JsonElement oldValue = in.get(key);
                JsonElement newValue = from.get(key);
                if (oldValue.isJsonObject() && newValue.isJsonObject()) {
                    replaceValues(oldValue.getAsJsonObject(), newValue.getAsJsonObject());
                }
                else {
                    in.add(key, newValue);
                }
            }
        });
    }
}
