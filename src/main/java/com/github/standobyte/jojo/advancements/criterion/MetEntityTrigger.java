package com.github.standobyte.jojo.advancements.criterion;

import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SummonedEntityTrigger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;

public class MetEntityTrigger extends AbstractCriterionTrigger<MetEntityTrigger.Instance> {
    private final ResourceLocation id;

    public MetEntityTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }
    
    public void trigger(ServerPlayerEntity player, Entity pEntity) {
        LootContext lootContext = EntityPredicate.createContext(player, pEntity);
        this.trigger(player, instance -> {
            return instance.matches(lootContext);
        });
    }

    @Override
    public MetEntityTrigger.Instance createInstance(JsonObject json, 
            EntityPredicate.AndPredicate playerPredicate, ConditionArrayParser conditionsParser) {
        EntityPredicate.AndPredicate entityPredicate = EntityPredicate.AndPredicate.fromJson(json, "entity", conditionsParser);
        return new MetEntityTrigger.Instance(id, playerPredicate, entityPredicate);
    }

    public static class Instance extends CriterionInstance {
        private final EntityPredicate.AndPredicate entity;

        public Instance(ResourceLocation criterion, EntityPredicate.AndPredicate player, EntityPredicate.AndPredicate entity) {
            super(criterion, player);
            this.entity = entity;
        }

        public static SummonedEntityTrigger.Instance metEntity(EntityPredicate.Builder entityBuilder) {
            return new SummonedEntityTrigger.Instance(EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.wrap(entityBuilder.build()));
        }
        
        public boolean matches(LootContext pLootContext) {
            return this.entity.matches(pLootContext);
        }
        
        @Override
        public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("entity", this.entity.toJson(pConditions));
            return jsonobject;
        }
    }
}
