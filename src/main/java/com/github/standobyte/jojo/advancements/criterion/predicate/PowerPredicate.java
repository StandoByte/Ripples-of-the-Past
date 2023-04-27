package com.github.standobyte.jojo.advancements.criterion.predicate;

import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.power.ModCommonRegistries;
import com.github.standobyte.jojo.init.power.stand.ModStandActions;
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
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistry;

public class PowerPredicate {
    public static final PowerPredicate ANY = new PowerPredicate(null, null, null, null);
    private final PowerClassification classification;
    @Nullable
    private final IPowerType<?, ?> type;
    @Nullable
    private final SpecialTypeCheck typeCheck;
    @Nullable
    private final MinMaxBounds.IntBound standTier;
    
    private PowerPredicate(PowerClassification classification, @Nullable IPowerType<?, ?> type, 
            @Nullable SpecialTypeCheck powerCheck, @Nullable MinMaxBounds.IntBound standTier) {
        this.classification = classification;
        this.type = type;
        this.typeCheck = powerCheck;
        this.standTier = standTier;
    }
    
    public boolean matches(LivingEntity powerUser) {
        for (PowerClassification classification : PowerClassification.values()) {
            if (matches(classification, IPower.getPowerOptional(powerUser, classification))) {
                return true;
            }
        }
        return false;
    }
    
    public boolean matches(PowerClassification classification, LazyOptional<? extends IPower<?, ?>> powerOptional) {
        return matches(classification, powerOptional.orElse(null));
    }

    public boolean matches(PowerClassification classification, @Nullable IPower<?, ?> power) {
        if (power == null) {
            return matches(classification, null, -1);
        }
        int standTier = -1;
        if (power.getPowerClassification() == PowerClassification.STAND && power.hasPower()) {
            standTier = ((IStandPower) power).getType().getTier();
        }
        return matches(power.getPowerClassification(), power.getType(), standTier);
    }

    public boolean matches(PowerClassification classification, @Nullable IPowerType<?, ?> type, int standTier) {
        if (this == ANY) {
            return true;
        }
        
        return (this.classification == null || this.classification == classification)
                && (this.type == null || this.type == type)
                && (this.typeCheck == null || this.typeCheck.matches(type))
                && (this.standTier == null || this.standTier.matches(standTier));
    }

    
    
    public static PowerPredicate fromJson(@Nullable JsonElement json, @Nullable PowerClassification defaultClassification) {
        if (json == null || json.isJsonNull()) {
            return ANY;
        }
        else {
            JsonObject jsonObject = JSONUtils.convertToJsonObject(json, "JoJo power");
            
            PowerClassification classification = jsonObject.has("classification") ? 
                    Enum.valueOf(PowerClassification.class, JSONUtils.getAsString(jsonObject, "classification").toUpperCase())
                    : defaultClassification;
            if (classification == null) {
                throw new JsonSyntaxException("No power classification specified");
            }
                    
            
            IPowerType<?, ?> type = null;
            SpecialTypeCheck typeCheck = SpecialTypeCheck.ANY;
            if (jsonObject.has("type")) {
                typeCheck = null;
                String typeString = JSONUtils.getAsString(jsonObject, "type");
                for (SpecialTypeCheck check : SpecialTypeCheck.values()) {
                    if (check.name().equals(typeString.toUpperCase())) {
                        typeCheck = check;
                        break;
                    }
                }
                if (typeCheck == null) {
                    ResourceLocation resLoc = new ResourceLocation(typeString);
                    ForgeRegistry<? extends IPowerType<?, ?>> registry;
                    switch (classification) {
                    case STAND:
                        registry = (ForgeRegistry<StandType<?>>) ModStandActions.STANDS.getRegistry();
                        break;
                    case NON_STAND:
                        registry = (ForgeRegistry<NonStandPowerType<?>>) ModCommonRegistries.NON_STAND_POWERS.getRegistry();
                        break;
                    default:
                        registry = null;
                        break;
                    }
                    type = Optional.ofNullable(registry.getRaw(resLoc)).orElseThrow(() -> {
                        return new JsonSyntaxException("Unknown power type '" + resLoc + "'");
                    });
                }
            }
            
            MinMaxBounds.IntBound standTier = MinMaxBounds.IntBound.fromJson(jsonObject.get("stand_tier"));
            
            return new PowerPredicate(classification, type, typeCheck, standTier);
        }
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("classification", classification.name().toLowerCase());
            if (type != null) {
                jsonObject.addProperty("type", type.getRegistryName().toString());
            }
            else if (typeCheck != null) {
                jsonObject.addProperty("type", typeCheck.name().toLowerCase());
            }
            if (classification == PowerClassification.STAND && standTier != null) {
                jsonObject.add("stand_tier", standTier.serializeToJson());
            }
            return jsonObject;
        }
    }
    
    
    
    private static enum SpecialTypeCheck {
        ANY(type -> type != null),
        NONE(type -> type == null);
        
        private final Predicate<IPowerType<?, ?>> typeCheck;
        
        private SpecialTypeCheck(Predicate<IPowerType<?, ?>> typeCheck) {
            this.typeCheck = typeCheck;
        }
        
        private boolean matches(IPowerType<?, ?> type) {
            return typeCheck.test(type);
        }
    }
}
