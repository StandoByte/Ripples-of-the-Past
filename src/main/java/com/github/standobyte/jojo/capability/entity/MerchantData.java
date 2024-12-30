package com.github.standobyte.jojo.capability.entity;

import java.util.UUID;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

public class MerchantData implements INBTSerializable<CompoundNBT> {
    private final LivingEntity entity;
    private final IMerchant asMerchant;
    
    private final Multimap<UUID, String> playersTriedTrading = ArrayListMultimap.create();
    
    public MerchantData(LivingEntity entity, IMerchant asMerchant) {
        this.entity = entity;
        this.asMerchant = asMerchant;
    }
    
    public boolean gaveUniqueTrade() {
        return entity.getTags().contains("JojoUniqueTrade");
    }
    
    public void setGaveUniqueTrade() {
        entity.addTag("JojoUniqueTrade");
    }
    
    public void setPlayerTriedTrading(PlayerEntity player, String tradeType) {
        playersTriedTrading.put(player.getUUID(), tradeType);
    }
    
    public boolean getPlayerTriedTrading(PlayerEntity player, String tradeType) {
        return playersTriedTrading.containsEntry(player.getUUID(), tradeType);
    }
    
    public void resetPlayerTrades(PlayerEntity player) {
        playersTriedTrading.removeAll(player.getUUID());
    }
    
    public boolean resetPlayerTrade(PlayerEntity player, String tradeType) {
        return playersTriedTrading.remove(player.getUUID(), tradeType);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        
        CompoundNBT playerTriesNbt = new CompoundNBT();
        playersTriedTrading.asMap().forEach((id, tradeTypes) -> {
            if (!tradeTypes.isEmpty()) {
                ListNBT typesListNbt = new ListNBT();
                tradeTypes.forEach(tradeType -> typesListNbt.add(StringNBT.valueOf(tradeType)));
                playerTriesNbt.put(id.toString(), typesListNbt);
            }
        });
        nbt.put("PlayerTries", playerTriesNbt);
        
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        MCUtil.nbtGetCompoundOptional(nbt, "PlayerTries").ifPresent(playerTriesNbt -> {
            playerTriesNbt.getAllKeys().forEach(key -> {
                try {
                    UUID playerUuid = UUID.fromString(key);
                    playerTriesNbt.getList(key, Constants.NBT.TAG_STRING).forEach(tradeType -> {
                        playersTriedTrading.put(playerUuid, ((StringNBT) tradeType).getAsString());
                    });
                }
                catch (Exception e) {
                    JojoMod.getLogger().error(e);
                }
            });
        });
    }
}
