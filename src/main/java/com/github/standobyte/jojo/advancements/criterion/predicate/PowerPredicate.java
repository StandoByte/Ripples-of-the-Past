package com.github.standobyte.jojo.advancements.criterion.predicate;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;

public class PowerPredicate {
    public static final PowerPredicate ANY = new PowerPredicate(null, null, null);
    @Nullable
    private final PowerClassification classification;
    @Nullable
    private final IPowerType<?, ?> type;
    @Nullable
    private final MinMaxBounds.IntBound standTier;

    public PowerPredicate(@Nullable PowerClassification classification, @Nullable IPowerType<?, ?> type, @Nullable MinMaxBounds.IntBound standTier) {
        this.classification = classification;
        this.type = type;
        this.standTier = standTier;
    }

    public boolean matches(IPower<?, ?> power) {
        if (power == null) {
            return false;
        }
        int standTier = 0;
        if (power.getPowerClassification() == PowerClassification.STAND) {
            standTier = ((IStandPower) power).getTier();
        }
        return matches(power.getPowerClassification(), power.getType(), standTier);
    }

    public boolean matches(PowerClassification classification, IPowerType<?, ?> type, int standTier) {
        if (this == ANY) {
            return true;
        }
        else if (this.classification != null && this.classification != classification
                || this.type != null && this.type != type
                || this.standTier != null && !this.standTier.matches(standTier)) {
            return false;
        }
        return true;
    }

    
    
    public static PowerPredicate fromJson(@Nullable JsonElement json) {
        if (json != null && !json.isJsonNull()) {
            JsonObject jsonObject = JSONUtils.convertToJsonObject(json, "power");
            
            PowerClassification classification = jsonObject.has("classification") ? 
                    Enum.valueOf(PowerClassification.class, JSONUtils.getAsString(jsonObject, "classification").toUpperCase())
                    : null;
                    
            IPowerType<?, ?> type = deserializePowerType(classification, jsonObject);
            
            MinMaxBounds.IntBound standTier = MinMaxBounds.IntBound.fromJson(jsonObject.get("stand_tier"));
            
            return new PowerPredicate(classification, type, standTier);
        }
        return ANY;
    }

    @Nullable
    private static IPowerType<?, ?> deserializePowerType(PowerClassification classification, JsonObject jsonObject) {
        if (jsonObject.has("type")) {
            ResourceLocation resLoc = new ResourceLocation(JSONUtils.getAsString(jsonObject, "type"));
            if (classification == null) {
                throw new JsonSyntaxException("No power classification specified for power type '" + resLoc + "'");
            }
            else {
                ForgeRegistry<? extends IPowerType<?, ?>> registry;
                switch (classification) {
                case STAND:
                    registry = (ForgeRegistry<StandType<?>>) ModStandTypes.Registry.getRegistry();
                    break;
                case NON_STAND:
                    registry = (ForgeRegistry<NonStandPowerType<?>>) ModNonStandPowers.Registry.getRegistry();
                    break;
                default:
                    registry = null;
                    break;
                }
                return Optional.ofNullable(registry.getRaw(resLoc)).orElseThrow(() -> {
                    return new JsonSyntaxException("Unknown power type '" + resLoc + "'");
                });
            }
        }
        return null;
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } 
        else {
            JsonObject jsonObject = new JsonObject();
            if (classification != null) {
                jsonObject.addProperty("classification", classification.name().toLowerCase());
                if (type != null) {
                    jsonObject.addProperty("type", type.getRegistryName().toString());
                }
                if (classification == PowerClassification.STAND && standTier != null) {
                    jsonObject.add("stand_tier", standTier.serializeToJson());
                }
            }
            return jsonObject;
        }
    }
}
