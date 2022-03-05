package com.github.standobyte.jojo.advancements.criterion.predicate;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.util.JSONUtils;

public class HamonStatsPredicate {
    public static final HamonStatsPredicate ANY = new HamonStatsPredicate(null, null, null);
    @Nullable
    private final MinMaxBounds.IntBound strengthLevel;
    @Nullable
    private final MinMaxBounds.IntBound controlLevel;
    @Nullable
    private final MinMaxBounds.FloatBound breathingTechniqueLevel;
    
    public HamonStatsPredicate(MinMaxBounds.IntBound strengthLevel, 
            MinMaxBounds.IntBound controlLevel, MinMaxBounds.FloatBound breathingTechniqueLevel) {
        this.strengthLevel = strengthLevel;
        this.controlLevel = controlLevel;
        this.breathingTechniqueLevel = breathingTechniqueLevel;
    }
    
    public boolean matches(int strengthLevel, int controlLevel, float breathingTechniqueLevel) {
        if (this == ANY) {
            return true;
        }
        return (this.strengthLevel == null || this.strengthLevel.matches(strengthLevel)) 
                && (this.controlLevel == null || this.controlLevel.matches(controlLevel)) 
                && (this.breathingTechniqueLevel == null || this.breathingTechniqueLevel.matches(breathingTechniqueLevel));
    }
    
    public static HamonStatsPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return ANY;
        }
        else {
            JsonObject jsonObject = JSONUtils.convertToJsonObject(json, "Hamon stats");
            
            MinMaxBounds.IntBound strength = MinMaxBounds.IntBound.fromJson(jsonObject.get("strength_level"));
            MinMaxBounds.IntBound control = MinMaxBounds.IntBound.fromJson(jsonObject.get("control_level"));
            MinMaxBounds.FloatBound breathingTechnique = MinMaxBounds.FloatBound.fromJson(jsonObject.get("breathing_technique_level"));
            
            return new HamonStatsPredicate(strength, control, breathingTechnique);
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonobject = new JsonObject();
            if (strengthLevel != null) {
                jsonobject.add("strength_level", strengthLevel.serializeToJson());
            }
            if (controlLevel != null) {
                jsonobject.add("control_level", controlLevel.serializeToJson());
            }
            if (breathingTechniqueLevel != null) {
                jsonobject.add("breathing_technique_level", breathingTechniqueLevel.serializeToJson());
            }
            return jsonobject;
        }
    }

}
