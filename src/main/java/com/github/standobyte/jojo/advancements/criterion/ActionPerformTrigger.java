package com.github.standobyte.jojo.advancements.criterion;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.ModActions;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;

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
        Action<?> action = deserializeAction(json);
        return new ActionPerformTrigger.Instance(id, playerPredicate, action);
    }

    @Nullable
    private static Action<?> deserializeAction(JsonObject json) {
        if (json.has("action")) {
            ResourceLocation resLoc = new ResourceLocation(JSONUtils.getAsString(json, "action"));
            return Optional.ofNullable(((ForgeRegistry<Action<?>>) ModActions.Registry.getRegistry()).getRaw(resLoc)).orElseThrow(() -> {
                return new JsonSyntaxException("Unknown action '" + resLoc + "'");
            });
        } else {
            return null;
        }
    }

    public static class Instance extends CriterionInstance {
        private final Action<?> action;

        public Instance(ResourceLocation criterion, AndPredicate player, Action<?> action) {
            super(criterion, player);
            this.action = action;
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            if (action != null) {
                jsonobject.addProperty("action", action.getRegistryName().toString());
            }
            return jsonobject;
        }

        private boolean matches(Action<?> action) {
            return this.action == null || this.action == action;
        }
    }

}
