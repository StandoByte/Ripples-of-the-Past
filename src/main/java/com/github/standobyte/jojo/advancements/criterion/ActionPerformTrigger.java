package com.github.standobyte.jojo.advancements.criterion;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.advancements.criterion.predicate.ActionPredicate;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class ActionPerformTrigger extends AbstractCriterionTrigger<ActionPerformTrigger.Instance> {
    private final ResourceLocation id;

    public ActionPerformTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayerEntity player, Action<?> action) {
        trigger(player, criterion -> criterion.matches(action));
    }

    @Override
    protected ActionPerformTrigger.Instance createInstance(JsonObject json, AndPredicate playerPredicate,
            ConditionArrayParser conditionArrayParser) {
        return new ActionPerformTrigger.Instance(id, playerPredicate, ActionPredicate.fromJson(json.get("action")));
    }

    public static class Instance extends CriterionInstance {
        private final ActionPredicate actionPredicate;

        public Instance(ResourceLocation criterion, AndPredicate player, ActionPredicate action) {
            super(criterion, player);
            this.actionPredicate = action;
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("action", actionPredicate.serializeToJson());
            return jsonobject;
        }

        private boolean matches(Action<?> action) {
            return actionPredicate.matches(action);
        }
    }

}
