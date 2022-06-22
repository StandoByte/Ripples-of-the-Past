package com.github.standobyte.jojo.advancements.criterion;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;

public class LastHamonTrigger extends AbstractCriterionTrigger<LastHamonTrigger.Instance> {
    private final ResourceLocation id;

    public LastHamonTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayerEntity player, @Nullable Entity hamonSource) {
        LootContext sourceCtx = EntityPredicate.createContext(player, hamonSource);
        trigger(player, criterion -> criterion.matches(sourceCtx));
    }

    @Override
    protected LastHamonTrigger.Instance createInstance(JsonObject json, AndPredicate playerPredicate,
            ConditionArrayParser conditionArrayParser) {
        EntityPredicate.AndPredicate sourcePredicate = EntityPredicate.AndPredicate
                .fromJson(json, "source", conditionArrayParser);
        return new LastHamonTrigger.Instance(id, playerPredicate, sourcePredicate);
    }

    public static class Instance extends CriterionInstance {
        private final EntityPredicate.AndPredicate hamonSource;

        public Instance(ResourceLocation criterion, AndPredicate player, EntityPredicate.AndPredicate hamonSource) {
            super(criterion, player);
            this.hamonSource = hamonSource;
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("source", hamonSource.toJson(serializer));
            return jsonobject;
        }

        private boolean matches(LootContext sourceCtx) {
            return hamonSource.matches(sourceCtx);
        }
    }

}
