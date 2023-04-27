package com.github.standobyte.jojo.advancements.criterion;

import com.github.standobyte.jojo.advancements.criterion.predicate.HamonStatsPredicate;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class HamonStatsTrigger extends AbstractCriterionTrigger<HamonStatsTrigger.Instance> {
    private final ResourceLocation id;

    public HamonStatsTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayerEntity player, int strengthLevel, int controlLevel, float breathingTrainingLevel) {
        trigger(player, criterion -> criterion.matches(strengthLevel, controlLevel, breathingTrainingLevel));
    }

    @Override
    protected HamonStatsTrigger.Instance createInstance(JsonObject json, AndPredicate playerPredicate,
            ConditionArrayParser conditionArrayParser) {
        HamonStatsPredicate statsPredicate = HamonStatsPredicate.fromJson(json.get("hamon_stats"));
        return new HamonStatsTrigger.Instance(id, playerPredicate, statsPredicate);
    }

    public static class Instance extends CriterionInstance {
        private final HamonStatsPredicate statsPredicate;

        public Instance(ResourceLocation criterion, AndPredicate player, HamonStatsPredicate statsPredicate) {
            super(criterion, player);
            this.statsPredicate = statsPredicate;
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("hamon_stats", statsPredicate.serializeToJson());
            return jsonobject;
        }

        private boolean matches(int strengthLevel, int controlLevel, float breathingTrainingLevel) {
            return this.statsPredicate.matches(strengthLevel, controlLevel, breathingTrainingLevel);
        }
    }

}
