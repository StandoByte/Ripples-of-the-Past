package com.github.standobyte.jojo.advancements.criterion;

import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class PeopleDrainedTrigger extends AbstractCriterionTrigger<PeopleDrainedTrigger.Instance> {
    private final ResourceLocation id;

    public PeopleDrainedTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayerEntity player, int peopleDrained, int zombiesCreated) {
        trigger(player, criterion -> criterion.matches(peopleDrained, zombiesCreated));
    }

    @Override
    protected PeopleDrainedTrigger.Instance createInstance(JsonObject json, AndPredicate playerPredicate,
            ConditionArrayParser conditionArrayParser) {
        return new PeopleDrainedTrigger.Instance(id, playerPredicate, 
                MinMaxBounds.IntBound.fromJson(json.get("people_drained")), 
                MinMaxBounds.IntBound.fromJson(json.get("zombies_created")));
    }

    public static class Instance extends CriterionInstance {
        private MinMaxBounds.IntBound peopleDrained;
        private MinMaxBounds.IntBound zombiesCreated;

        public Instance(ResourceLocation criterion, AndPredicate player, 
                MinMaxBounds.IntBound peopleDrained, MinMaxBounds.IntBound zombiesCreated) {
            super(criterion, player);
            this.peopleDrained = peopleDrained;
            this.zombiesCreated = zombiesCreated;
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("people_drained", peopleDrained.serializeToJson());
            jsonobject.add("zombies_created", zombiesCreated.serializeToJson());
            return jsonobject;
        }

        private boolean matches(int peopleDrained, int zombiesCreated) {
            return this.peopleDrained.matches(peopleDrained) && this.zombiesCreated.matches(zombiesCreated);
        }
    }

}
