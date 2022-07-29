package com.github.standobyte.jojo.advancements.criterion;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class RPSGameTrigger extends AbstractCriterionTrigger<RPSGameTrigger.Instance> {
    private final ResourceLocation id;

    public RPSGameTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }
    
    public void trigger(ServerPlayerEntity player, RockPaperScissorsGame game, boolean standTaken) {
        trigger(player, criterion -> criterion.matches(player, game, standTaken));
    }

    @Override
    protected RPSGameTrigger.Instance createInstance(JsonObject json, AndPredicate playerPredicate,
            ConditionArrayParser conditionArrayParser) {
        return new RPSGameTrigger.Instance(id, playerPredicate, 
                json.has("won_game") ? JSONUtils.getAsBoolean(json, "won_game") : null, 
                json.has("stand_taken") ? JSONUtils.getAsBoolean(json, "stand_taken") : null);
    }

    public static class Instance extends CriterionInstance {
        @Nullable
        private final Boolean gameWon;
        @Nullable
        private final Boolean standTaken;

        public Instance(ResourceLocation criterion, AndPredicate player, 
                Boolean gameWon, Boolean standTaken) {
            super(criterion, player);
            this.gameWon = gameWon;
            this.standTaken = standTaken;
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            if (gameWon != null) jsonobject.addProperty("won_game", gameWon);
            if (standTaken != null) jsonobject.addProperty("stand_taken", standTaken);
            return jsonobject;
        }

        private boolean matches(ServerPlayerEntity player, RockPaperScissorsGame game, boolean standTaken) {
            return (this.gameWon == null || this.gameWon.booleanValue() == player.getUUID().equals(game.getWinner()))
                    && (this.standTaken == null || this.standTaken.booleanValue() == standTaken);
        }
    }
}
