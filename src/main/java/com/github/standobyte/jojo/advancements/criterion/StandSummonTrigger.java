package com.github.standobyte.jojo.advancements.criterion;

import com.github.standobyte.jojo.advancements.criterion.predicate.PowerPredicate;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class StandSummonTrigger extends AbstractCriterionTrigger<StandSummonTrigger.Instance> {
    private final ResourceLocation id;

    public StandSummonTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayerEntity player, IStandPower standPower) {
        trigger(player, criterion -> criterion.matches(standPower.getPowerClassification(), standPower));
    }

    @Override
    protected StandSummonTrigger.Instance createInstance(JsonObject json, AndPredicate playerPredicate,
            ConditionArrayParser conditionArrayParser) {
        return new StandSummonTrigger.Instance(id, playerPredicate, PowerPredicate.fromJson(json.get("stand"), PowerClassification.STAND));
    }

    public static class Instance extends CriterionInstance {
        private final PowerPredicate powerPredicate;

        public Instance(ResourceLocation criterion, AndPredicate player, PowerPredicate powerPredicate) {
            super(criterion, player);
            this.powerPredicate = powerPredicate;
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("stand", this.powerPredicate.serializeToJson());
            return jsonobject;
        }

        private boolean matches(PowerClassification classification, IPower<?, ?> power) {
            return this.powerPredicate.matchesPower(classification, power);
        }
    }

}
