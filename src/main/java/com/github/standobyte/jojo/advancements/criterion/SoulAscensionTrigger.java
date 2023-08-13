package com.github.standobyte.jojo.advancements.criterion;

import com.github.standobyte.jojo.advancements.criterion.predicate.PowerPredicate;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class SoulAscensionTrigger extends AbstractCriterionTrigger<SoulAscensionTrigger.Instance> {
    private final ResourceLocation id;

    public SoulAscensionTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayerEntity player, IStandPower stand, int ascensionTicks) {
        trigger(player, criterion -> criterion.matches(stand, ascensionTicks));
    }

    @Override
    protected SoulAscensionTrigger.Instance createInstance(JsonObject json, AndPredicate playerPredicate,
            ConditionArrayParser conditionArrayParser) {
        return new SoulAscensionTrigger.Instance(id, playerPredicate, 
                PowerPredicate.fromJson(json.get("stand"), null), MinMaxBounds.IntBound.fromJson(json.get("ascension_ticks")));
    }

    public static class Instance extends CriterionInstance {
        private PowerPredicate standPower;
        private MinMaxBounds.IntBound ascensionTicks;

        public Instance(ResourceLocation criterion, AndPredicate player, 
                PowerPredicate standPower, MinMaxBounds.IntBound ascensionTicks) {
            super(criterion, player);
            this.standPower = standPower;
            this.ascensionTicks = ascensionTicks;
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("stand", standPower.serializeToJson());
            jsonobject.add("ascension_ticks", ascensionTicks.serializeToJson());
            return jsonobject;
        }

        private boolean matches(IStandPower stand, int ascensionTicks) {
            return this.standPower.matchesPower(PowerClassification.STAND, stand) && this.ascensionTicks.matches(ascensionTicks);
        }
    }

}
