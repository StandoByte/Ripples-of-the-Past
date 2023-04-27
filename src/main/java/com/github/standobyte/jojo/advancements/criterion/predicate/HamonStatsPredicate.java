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
    private final MinMaxBounds.FloatBound breathingTrainingLevel;
    
    public HamonStatsPredicate(MinMaxBounds.IntBound strengthLevel, 
            MinMaxBounds.IntBound controlLevel, MinMaxBounds.FloatBound breathingTrainingLevel) {
        this.strengthLevel = strengthLevel;
        this.controlLevel = controlLevel;
        this.breathingTrainingLevel = breathingTrainingLevel;
    }
    
    public boolean matches(int strengthLevel, int controlLevel, float breathingTrainingLevel) {
        if (this == ANY) {
            return true;
        }
        return (this.strengthLevel == null || this.strengthLevel.matches(strengthLevel)) 
                && (this.controlLevel == null || this.controlLevel.matches(controlLevel)) 
                && (this.breathingTrainingLevel == null || this.breathingTrainingLevel.matches(breathingTrainingLevel));
    }
    
    public static HamonStatsPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return ANY;
        }
        else {
            JsonObject jsonObject = JSONUtils.convertToJsonObject(json, "Hamon stats");
            
            MinMaxBounds.IntBound strength = MinMaxBounds.IntBound.fromJson(jsonObject.get("strength_level"));
            MinMaxBounds.IntBound control = MinMaxBounds.IntBound.fromJson(jsonObject.get("control_level"));
            MinMaxBounds.FloatBound breathingTraining = MinMaxBounds.FloatBound.fromJson(jsonObject.get("breathing_training_level"));
            
            return new HamonStatsPredicate(strength, control, breathingTraining);
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
            if (breathingTrainingLevel != null) {
                jsonobject.add("breathing_training_level", breathingTrainingLevel.serializeToJson());
            }
            return jsonobject;
        }
    }

}
