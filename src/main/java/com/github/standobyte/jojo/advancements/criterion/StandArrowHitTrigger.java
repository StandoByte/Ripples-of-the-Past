package com.github.standobyte.jojo.advancements.criterion;

import com.github.standobyte.jojo.advancements.criterion.predicate.StandArrowHitPredicate;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;

public class StandArrowHitTrigger extends AbstractCriterionTrigger<StandArrowHitTrigger.Instance> {
    private final ResourceLocation id;

    public StandArrowHitTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public void trigger(ServerPlayerEntity player, LivingEntity target, boolean gaveStand) {
        LootContext targetCtx = EntityPredicate.createContext(player, target);
        IStandPower targetStand = IStandPower.getStandPowerOptional(target).orElse(null);
        trigger(player, (criterion) -> {
            return criterion.matches(player, targetCtx, gaveStand, targetStand, player.is(target));
        });
    }

    @Override
    public StandArrowHitTrigger.Instance createInstance(JsonObject json, EntityPredicate.AndPredicate playerPredicate, ConditionArrayParser conditionArrayParser) {
        return new StandArrowHitTrigger.Instance(
                this.id, 
                playerPredicate, 
                EntityPredicate.AndPredicate.fromJson(json, "target", conditionArrayParser), 
                StandArrowHitPredicate.fromJson(json.get("arrow_hit")));
    }

    public static class Instance extends CriterionInstance {
        private final EntityPredicate.AndPredicate targetPredicate;
        private final StandArrowHitPredicate standArrowHitPredicate;

        public Instance(ResourceLocation id, EntityPredicate.AndPredicate player, 
                EntityPredicate.AndPredicate targetPredicate, StandArrowHitPredicate standArrowHitPredicate) {
            super(id, player);
            this.targetPredicate = targetPredicate;
            this.standArrowHitPredicate = standArrowHitPredicate;
        }

        public boolean matches(ServerPlayerEntity player, LootContext targetCtx, 
                boolean gaveStand, IStandPower targetStand, boolean shotSelf) {
            return targetPredicate.matches(targetCtx) && standArrowHitPredicate.matches(player, gaveStand, targetStand, shotSelf);
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("target", this.targetPredicate.toJson(serializer));
            jsonobject.add("arrow_hit", this.standArrowHitPredicate.serializeToJson());
            return jsonobject;
        }
    }
}
