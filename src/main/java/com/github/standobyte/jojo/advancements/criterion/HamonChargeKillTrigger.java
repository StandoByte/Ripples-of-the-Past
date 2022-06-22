package com.github.standobyte.jojo.advancements.criterion;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.advancements.criterion.predicate.PowerPredicate;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

public class HamonChargeKillTrigger extends AbstractCriterionTrigger<HamonChargeKillTrigger.Instance> {
    private final ResourceLocation id;

    public HamonChargeKillTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public void trigger(ServerPlayerEntity player, LivingEntity killed, @Nullable Entity chargedEntity, @Nullable BlockPos chargedBlockPos) {
        LootContext killedLootCtx = EntityPredicate.createContext(player, killed);
        if (chargedEntity != null) {
            LootContext chargedLootCtx = EntityPredicate.createContext(player, chargedEntity);
            trigger(player, (criterion) -> {
               return criterion.matches(killed, killedLootCtx, chargedLootCtx);
            });
        }
        else if (chargedBlockPos != null) {
            BlockState blockState = player.getLevel().getBlockState(chargedBlockPos);
            trigger(player, (criterion) -> {
               return criterion.matches(killed, killedLootCtx, blockState, chargedBlockPos, player.getLevel());
            });
        }
    }

    @Override
    public HamonChargeKillTrigger.Instance createInstance(JsonObject json, EntityPredicate.AndPredicate playerPredicate, ConditionArrayParser conditionArrayParser) {
        Block block = deserializeBlock(json, "block");
        StatePropertiesPredicate blockState = StatePropertiesPredicate.fromJson(json.get("state"));
        if (block != null) {
            blockState.checkState(block.getStateDefinition(), (property) -> {
                throw new JsonSyntaxException("Block " + block + " has no property " + property + ":");
            });
        }

        return new HamonChargeKillTrigger.Instance(
                id, 
                playerPredicate, 
                EntityPredicate.AndPredicate.fromJson(json, "killed_entity", conditionArrayParser), 
                PowerPredicate.fromJson(json.get("killed_power"), null),
                EntityPredicate.AndPredicate.fromJson(json, "charged_entity", conditionArrayParser), 
                block, 
                blockState,
                LocationPredicate.fromJson(json.get("location")));
    }

    @Nullable
    private static Block deserializeBlock(JsonObject json, String key) {
        if (json.has(key)) {
            ResourceLocation resLoc = new ResourceLocation(JSONUtils.getAsString(json, key));
            return Optional.ofNullable(((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getRaw(resLoc)).orElseThrow(() -> {
                return new JsonSyntaxException("Unknown block type '" + resLoc + "'");
            });
        } else {
            return null;
        }
    }

    public static class Instance extends CriterionInstance {
        private final EntityPredicate.AndPredicate killedPredicate;
        private final PowerPredicate killedPowerPredicate;
        
        private final EntityPredicate.AndPredicate chargedPredicate;
        
        private final Block chargedBlock;
        private final StatePropertiesPredicate chargedBlockState;
        private final LocationPredicate chargedBlockLocation;

        public Instance(ResourceLocation id, EntityPredicate.AndPredicate player, 
                EntityPredicate.AndPredicate killedPredicate, PowerPredicate killedPowerPredicate, 
                EntityPredicate.AndPredicate chargedPredicate, 
                Block chargedBlock, StatePropertiesPredicate chargedBlockState, LocationPredicate chargedBlockLocation) {
            super(id, player);
            this.killedPredicate = killedPredicate;
            this.killedPowerPredicate = killedPowerPredicate;
            this.chargedPredicate = chargedPredicate;
            this.chargedBlock = chargedBlock;
            this.chargedBlockState = chargedBlockState;
            this.chargedBlockLocation = chargedBlockLocation;
        }

        public boolean matches(LivingEntity killed, LootContext killedCtx, LootContext chargedCtx) {
            return this.chargedBlock == null && chargedBlockState == StatePropertiesPredicate.ANY && chargedBlockLocation == LocationPredicate.ANY && 
                    this.killedPredicate.matches(killedCtx) && killedPowerPredicate.matches(killed) && 
                    this.chargedPredicate.matches(chargedCtx);
        }

        public boolean matches(LivingEntity killed, LootContext killedCtx, BlockState blockState, BlockPos blockPos, ServerWorld serverWorld) {
            return this.chargedPredicate == EntityPredicate.AndPredicate.ANY && 
                    this.killedPredicate.matches(killedCtx) && killedPowerPredicate.matches(killed) && 
                    (this.chargedBlock == null || blockState.is(this.chargedBlock)) && 
                    this.chargedBlockState.matches(blockState) && 
                    this.chargedBlockLocation.matches(serverWorld, blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("killed_entity", killedPredicate.toJson(serializer));
            jsonobject.add("killed_power", killedPowerPredicate.serializeToJson());
            
            jsonobject.add("charged_entity", chargedPredicate.toJson(serializer));
            
            if (chargedBlock != null) {
                jsonobject.addProperty("block", ForgeRegistries.BLOCKS.getKey(chargedBlock).toString());
            }
            jsonobject.add("state", chargedBlockState.serializeToJson());
            jsonobject.add("location", chargedBlockLocation.serializeToJson());
            return jsonobject;
        }
    }
}
