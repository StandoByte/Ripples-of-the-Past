package com.github.standobyte.jojo.advancements.criterion;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;

public class GetPowerTrigger extends AbstractCriterionTrigger<GetPowerTrigger.Instance> {
    private final ResourceLocation id;

    public GetPowerTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayerEntity player, PowerClassification classification, IPowerType<?, ?> powerType) {
        trigger(player, criterion -> criterion.matches(classification, powerType));
    }

    @Override
    protected GetPowerTrigger.Instance createInstance(JsonObject json, AndPredicate playerPredicate,
            ConditionArrayParser conditionArrayParser) {
        PowerClassification powerClassification = Enum.valueOf(PowerClassification.class, 
                JSONUtils.getAsString(json, "classification").toUpperCase());
        IPowerType<?, ?> powerType = deserializeType(powerClassification, json);
        return new GetPowerTrigger.Instance(id, playerPredicate, powerClassification, powerType);
    }

    @Nullable
    private static IPowerType<?, ?> deserializeType(PowerClassification powerClassification, JsonObject json) {
        if (json.has("type")) {
            ForgeRegistry<? extends IPowerType<?, ?>> registry;
            switch (powerClassification) {
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
            ResourceLocation resLoc = new ResourceLocation(JSONUtils.getAsString(json, "type"));
            return Optional.ofNullable(registry.getRaw(resLoc)).orElseThrow(() -> {
                return new JsonSyntaxException("Unknown action '" + resLoc + "'");
            });
        } else {
            return null;
        }
    }

    public static class Instance extends CriterionInstance {
        private final PowerClassification powerClassification;
        private final IPowerType<?, ?> powerType;

        public Instance(ResourceLocation criterion, AndPredicate player, PowerClassification powerClassification, IPowerType<?, ?> powerType) {
            super(criterion, player);
            this.powerClassification = powerClassification;
            this.powerType = powerType;
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.addProperty("classification", powerClassification.name().toLowerCase());
            if (powerType != null) {
                jsonobject.addProperty("type", powerType.getRegistryName().toString());
            }
            return jsonobject;
        }

        private boolean matches(PowerClassification powerClassification, IPowerType<?, ?> powerType) {
            return this.powerClassification == powerClassification && 
                    (this.powerType == null || this.powerType == powerType);
        }
    }

}
