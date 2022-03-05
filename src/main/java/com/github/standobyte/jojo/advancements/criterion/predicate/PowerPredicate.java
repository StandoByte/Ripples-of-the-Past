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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistry;

public class PowerPredicate {
    public static final PowerPredicate TRUE = new PowerPredicate(null, null, null);
    public static final PowerPredicate ANY_POWER = new PowerPredicate(null, null, null);
    @Nullable
    private final PowerClassification classification;
    @Nullable
    private final IPowerType<?, ?> type;
    @Nullable
    private final MinMaxBounds.IntBound standTier;
    
    private PowerPredicate(@Nullable PowerClassification classification, @Nullable IPowerType<?, ?> type, 
            @Nullable MinMaxBounds.IntBound standTier) {
        this.classification = classification;
        this.type = type;
        this.standTier = standTier;
    }
    
    public boolean matches(LazyOptional<? extends IPower<?, ?>> powerOptional) {
        return matches(powerOptional.orElse(null));
    }

    public boolean matches(IPower<?, ?> power) {
        if (power == null) {
            return this == TRUE;
        }
        int standTier = -1;
        if (power.getPowerClassification() == PowerClassification.STAND && power.hasPower()) {
            standTier = ((IStandPower) power).getType().getTier();
        }
        return matches(power.hasPower(), power.getPowerClassification(), power.getType(), standTier);
    }

    public boolean matches(boolean hasPower, PowerClassification classification, IPowerType<?, ?> type, int standTier) {
        if (!hasPower) {
            return false;
        }
        if (this == ANY_POWER) {
            return true;
        }
        
        return !(this.classification != null && this.classification != classification
                || this.type != null && this.type != type
                || this.standTier != null && !this.standTier.matches(standTier));
    }

    
    
    public static PowerPredicate fromJson(@Nullable JsonElement json) {
        if (json == null) {
            return TRUE;
        }
        if (json.isJsonNull()) {
            return ANY_POWER;
        }
        else {
            JsonObject jsonObject = JSONUtils.convertToJsonObject(json, "JoJo power");
            
            PowerClassification classification = jsonObject.has("classification") ? 
                    Enum.valueOf(PowerClassification.class, JSONUtils.getAsString(jsonObject, "classification").toUpperCase())
                    : null;
                    
            IPowerType<?, ?> type = deserializePowerType(classification, jsonObject);
            
            MinMaxBounds.IntBound standTier = MinMaxBounds.IntBound.fromJson(jsonObject.get("stand_tier"));
            
            return new PowerPredicate(classification, type, standTier);
        }
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

    public void serializeToJson(JsonObject jsonobject, String key) {
        if (this == TRUE) {
            return;
        } 
        else {
            JsonElement serialized;
            if (this == ANY_POWER) {
                serialized = JsonNull.INSTANCE;
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
                serialized = jsonObject;
            }
            
            jsonobject.add(key, serialized);
        }
    }
}
