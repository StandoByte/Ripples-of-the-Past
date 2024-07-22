package com.github.standobyte.jojo.advancements.criterion.predicate;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.JSONUtils;

public class PillarmanStagePredicate {
    public static final PillarmanStagePredicate ANY = new PillarmanStagePredicate(null);
    @Nullable
    private final MinMaxBounds.IntBound stage;
    
    public PillarmanStagePredicate(MinMaxBounds.IntBound stage) {
        this.stage = stage;
    }
    
    public boolean matches(LivingEntity pillarmanEntity) {
        if (this == ANY) {
            return true;
        }
        Optional<PillarmanData> pillarman = INonStandPower.getNonStandPowerOptional(pillarmanEntity)
                .resolve().flatMap(power -> power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()));
        if (!pillarman.isPresent()) {
            return false;
        }
        
        return (this.stage == null || this.stage.matches(pillarman.get().getEvolutionStage()));
    }
    
    public static PillarmanStagePredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return ANY;
        }
        else {
            JsonObject jsonObject = JSONUtils.convertToJsonObject(json, "Pillar Man stage");
            
            MinMaxBounds.IntBound stage = MinMaxBounds.IntBound.fromJson(jsonObject.get("stage"));
            
            return new PillarmanStagePredicate(stage);
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonobject = new JsonObject();
            if (stage != null) {
                jsonobject.add("stage", stage.serializeToJson());
            }
            return jsonobject;
        }
    }

}