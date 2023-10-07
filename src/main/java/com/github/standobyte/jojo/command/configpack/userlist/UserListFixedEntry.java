package com.github.standobyte.jojo.command.configpack.userlist;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

public class UserListFixedEntry {
    @Nullable
    private final GameProfile player;
    
    public UserListFixedEntry(@Nullable GameProfile user) {
        this.player = user;
    }
    
    
    @Nullable
    protected GameProfile getPlayer() {
        return player;
    }
    
    
    protected void serialize(JsonObject json) {
        if (getPlayer() != null) {
            json.addProperty("uuid", getPlayer().getId() == null ? "" : getPlayer().getId().toString());
            json.addProperty("name", getPlayer().getName());
        }
    }
    
    public static UserListFixedEntry deserialize(JsonObject json) {
        return new UserListFixedEntry(createGameProfile(json));
    }
    
    @Nullable
    protected static GameProfile createGameProfile(JsonObject json) {
        if (json.has("uuid") && json.has("name")) {
            String s = json.get("uuid").getAsString();

            UUID uuid;
            try {
                uuid = UUID.fromString(s);
            } catch (Throwable throwable) {
                return null;
            }

            return new GameProfile(uuid, json.get("name").getAsString());
        } else {
            return null;
        }
    }
}
