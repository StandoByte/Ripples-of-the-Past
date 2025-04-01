package com.github.standobyte.jojo.util.general;

import java.util.Map;

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
    
    public static void mergeWithObjMember(JsonObject json, String objMemberKey, JsonElement overwriting) {
        if (overwriting.isJsonObject()) {
            JsonObject member = json.getAsJsonObject(objMemberKey);
            if (member != null) {
                merge(member, overwriting.getAsJsonObject());
                return;
            }
        }
        json.add(objMemberKey, overwriting);
    }
    
    public static void merge(JsonObject existing, JsonObject overwriting) {
        for (Map.Entry<String, JsonElement> entry : overwriting.entrySet()) {
            mergeWithObjMember(existing, entry.getKey(), entry.getValue());
        }
    }
}
