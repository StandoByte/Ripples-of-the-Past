package com.github.standobyte.jojo.command.configpack.userlist;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.GameProfile;

import net.minecraft.util.JSONUtils;
import net.minecraft.util.Util;

public class DataConfigUserList<V extends UserListFixedEntry> {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected final Map<String, V> map = Maps.newHashMap();
    
    public void add(V playerEntry) {
        map.put(getKeyForUser(playerEntry.getPlayer()), playerEntry);
    }
    
    @Nullable
    public V get(GameProfile player) {
//        this.removeExpired();
        return map.get(getKeyForUser(player));
    }
    
    public boolean remove(GameProfile player) {
        return map.remove(getKeyForUser(player)) != null;
    }
    
    public boolean remove(UserListFixedEntry playerEntry) {
        return remove(playerEntry.getPlayer());
    }
    
    public void clear() {
        map.clear();
    }
    
    public String[] getUserList() {
        return getEntries().stream()
                .map(entry -> entry.getPlayer().getName())
                .toArray(String[]::new);
    }
    
    public boolean isEmpty() {
        return map.size() < 1;
    }
    
    protected String getKeyForUser(GameProfile playerGameProfile) {
        return playerGameProfile.getId().toString();
    }
    
    protected boolean contains(GameProfile player) {
        return map.containsKey(getKeyForUser(player));
    }
    
    public Collection<V> getEntries() {
        return map.values();
    }
    
    
    public abstract static class Serializer<L extends DataConfigUserList<V>, V extends UserListFixedEntry> implements JsonDeserializer<L>, JsonSerializer<L> {

        @Override
        public JsonElement serialize(L src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray entriesJsonArray = new JsonArray();
            
            src.map.values().stream().map(entry -> {
                return Util.make(new JsonObject(), entry::serialize);
            }).forEach(entriesJsonArray::add);
            
            return entriesJsonArray;
        }

        @Override
        public L deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonArray entriesJsonArray = json.getAsJsonArray();
            L list = createListObject();
            
            for (JsonElement entryJson : entriesJsonArray) {
                JsonObject jsonObject = JSONUtils.convertToJsonObject(entryJson, "entry");
                V playerEntry = deserializeEntry(jsonObject);
                if (playerEntry.getPlayer() != null) {
                    list.add(playerEntry);
                }
            }
            
            return list;
        }

        protected abstract L createListObject();
        protected abstract V deserializeEntry(JsonObject entryJson);
    }
}
