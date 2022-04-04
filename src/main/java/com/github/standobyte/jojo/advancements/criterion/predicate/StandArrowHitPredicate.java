package com.github.standobyte.jojo.advancements.criterion.predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.itemprojectile.StandArrowEntity;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.JSONUtils;

public class StandArrowHitPredicate {
    public static final StandArrowHitPredicate ANY = new StandArrowHitPredicate(EntityPredicate.ANY, null, PowerPredicate.ANY, null);
    private final EntityPredicate arrowEntity;
    @Nullable
    private final Boolean gaveStand;
    private final PowerPredicate targetStand;
    @Nullable
    private final Boolean shotSelf;
    
    public StandArrowHitPredicate(EntityPredicate arrowEntity, 
            @Nullable Boolean gaveStand, PowerPredicate targetStand, 
            @Nullable Boolean shotSelf) {
        this.arrowEntity = arrowEntity;
        this.gaveStand = gaveStand;
        this.targetStand = targetStand;
        this.shotSelf = shotSelf;
    }
    
    public boolean matches(ServerPlayerEntity player, StandArrowEntity arrow, boolean gaveStand, IStandPower targetStand, boolean shotSelf) {
        if (this == ANY) {
            return true;
        }
        return this.arrowEntity.matches(player, arrow) && this.targetStand.matches(PowerClassification.STAND, targetStand)
                && (this.gaveStand == null || this.gaveStand == gaveStand)
                && (this.shotSelf == null || this.shotSelf == shotSelf);
    }
    
    public static StandArrowHitPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return ANY;
        }
        else {
            JsonObject jsonObject = JSONUtils.convertToJsonObject(json, "Stand arrow hit");
            
            EntityPredicate arrowEntity = EntityPredicate.fromJson(jsonObject.get("arrow"));
            Boolean gaveStand = jsonObject.has("gave_stand") ? JSONUtils.getAsBoolean(jsonObject, "gave_stand") : null;
            PowerPredicate targetStand = PowerPredicate.fromJson(jsonObject.get("target_stand"), PowerClassification.STAND);
            Boolean shotSelf = jsonObject.has("shot_self") ? JSONUtils.getAsBoolean(jsonObject, "shot_self") : null;
            
            return new StandArrowHitPredicate(arrowEntity, gaveStand, targetStand, shotSelf);
        }
    }
    
    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonobject = new JsonObject();
            jsonobject.add("arrow", arrowEntity.serializeToJson());
            if (gaveStand != null) {
                jsonobject.addProperty("gave_stand", gaveStand);
            }
            jsonobject.add("target_stand", targetStand.serializeToJson());
            if (shotSelf != null) {
                jsonobject.addProperty("shot_self", gaveStand);
            }
            return jsonobject;
        }
    }

}
