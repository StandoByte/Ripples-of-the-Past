package com.github.standobyte.jojo.advancements.criterion;

import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

public class UnconditionalTrigger extends AbstractCriterionTrigger<UnconditionalTrigger.Instance> {
    private final ResourceLocation id;

    public UnconditionalTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(ServerPlayerEntity player) {
        trigger(player, (criterion) -> {
            return true;
        });
    }

    @Override
    public UnconditionalTrigger.Instance createInstance(JsonObject json, AndPredicate playerPredicate,
            ConditionArrayParser conditionArrayParser) {
        return new UnconditionalTrigger.Instance(id, playerPredicate);
    }

    public static class Instance extends CriterionInstance {
        public Instance(ResourceLocation criterion, AndPredicate player) {
            super(criterion, player);
        }
    }
}
