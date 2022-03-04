package com.github.standobyte.jojo.advancements.criterion;

import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

public class KilledPowerUserTrigger extends AbstractCriterionTrigger<KilledPowerUserTrigger.Instance> {
    private final ResourceLocation id;

    public KilledPowerUserTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public void trigger(ServerPlayerEntity player, Entity entity, DamageSource damageSource) {
        LootContext lootCtx = EntityPredicate.createContext(player, entity);
        trigger(player, (criterion) -> {
            return criterion.matches(player, lootCtx, damageSource);
        });
    }

    @Override
    public KilledPowerUserTrigger.Instance createInstance(JsonObject json, EntityPredicate.AndPredicate playerPredicate, ConditionArrayParser conditionArrayParser) {
        return new KilledPowerUserTrigger.Instance(
                this.id, 
                playerPredicate, 
                EntityPredicate.AndPredicate.fromJson(json, "entity", conditionArrayParser), 
                DamageSourcePredicate.fromJson(json.get("killing_blow")));
    }

    public static class Instance extends CriterionInstance {
        private final EntityPredicate.AndPredicate entityPredicate;
        private final DamageSourcePredicate killingBlow;

        public Instance(ResourceLocation id, EntityPredicate.AndPredicate player, 
                EntityPredicate.AndPredicate entityPredicate, DamageSourcePredicate killingBlow) {
            super(id, player);
            this.entityPredicate = entityPredicate;
            this.killingBlow = killingBlow;
        }

        public boolean matches(ServerPlayerEntity player, LootContext lootCtx, DamageSource damageSource) {
            return !this.killingBlow.matches(player, damageSource) ? false : this.entityPredicate.matches(lootCtx);
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("entity", this.entityPredicate.toJson(serializer));
            jsonobject.add("killing_blow", this.killingBlow.serializeToJson());
            return jsonobject;
        }
    }
}
