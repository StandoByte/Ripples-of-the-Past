package com.github.standobyte.jojo.advancements.criterion;

import com.github.standobyte.jojo.advancements.criterion.predicate.PillarmanStagePredicate;
import com.github.standobyte.jojo.advancements.criterion.predicate.PowerPredicate;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

public class KilledPillarManUserTrigger extends AbstractCriterionTrigger<KilledPillarManUserTrigger.Instance> {
    private final ResourceLocation id;
    private final boolean isPlayerKilled;

    public KilledPillarManUserTrigger(ResourceLocation id, boolean isPlayerKilled) {
        this.id = id;
        this.isPlayerKilled = isPlayerKilled;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public void trigger(ServerPlayerEntity player, Entity entity, DamageSource damageSource) {
        if (entity != null) {
            LootContext lootCtx = EntityPredicate.createContext(player, entity);
            LivingEntity livingEntity = entity instanceof LivingEntity ? (LivingEntity) entity : null;
            trigger(player, (criterion) -> {
                return criterion.matches(player, lootCtx, damageSource, 
                        isPlayerKilled ? livingEntity : player, 
                        isPlayerKilled ? player : livingEntity);
            });
        }
    }

    @Override
    public KilledPillarManUserTrigger.Instance createInstance(JsonObject json, EntityPredicate.AndPredicate playerPredicate, ConditionArrayParser conditionArrayParser) {
        return new KilledPillarManUserTrigger.Instance(
                this.id, 
                playerPredicate, 
                EntityPredicate.AndPredicate.fromJson(json, "entity", conditionArrayParser), 
                DamageSourcePredicate.fromJson(json.get("killing_blow")), 
                PowerPredicate.fromJson(json.get("power"), null),
                PowerPredicate.fromJson(json.get("killed_power"), null),
                PillarmanStagePredicate.fromJson(json.get("pillarman_stage")));
    }

    public static class Instance extends CriterionInstance {
        private final EntityPredicate.AndPredicate entityPredicate;
        private final DamageSourcePredicate killingBlow;
        private final PowerPredicate powerPredicate;
        private final PowerPredicate killedPowerPredicate;
        private final PillarmanStagePredicate pillarmanStagePredicate;

        public Instance(ResourceLocation id, EntityPredicate.AndPredicate player, 
                EntityPredicate.AndPredicate entityPredicate, DamageSourcePredicate killingBlow, 
                PowerPredicate powerPredicate, PowerPredicate killedPowerPredicate,
                PillarmanStagePredicate pillarmanStagePredicate) {
            super(id, player);
            this.entityPredicate = entityPredicate;
            this.killingBlow = killingBlow;
            this.powerPredicate = powerPredicate;
            this.killedPowerPredicate = killedPowerPredicate;
            this.pillarmanStagePredicate = pillarmanStagePredicate;
        }

        public boolean matches(ServerPlayerEntity player, LootContext lootCtx, DamageSource damageSource, 
                LivingEntity entity, LivingEntity killed) {
            return this.killingBlow.matches(player, damageSource) && this.entityPredicate.matches(lootCtx)
                    && powerPredicate.matches(entity) && killedPowerPredicate.matches(killed)
                    && pillarmanStagePredicate.matches(killed);
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("entity", this.entityPredicate.toJson(serializer));
            jsonobject.add("killing_blow", this.killingBlow.serializeToJson());
            jsonobject.add("power", this.powerPredicate.serializeToJson());
            jsonobject.add("killed_power", this.killedPowerPredicate.serializeToJson());
            jsonobject.add("pillarman_stage", this.pillarmanStagePredicate.serializeToJson());
            return jsonobject;
        }
    }
}