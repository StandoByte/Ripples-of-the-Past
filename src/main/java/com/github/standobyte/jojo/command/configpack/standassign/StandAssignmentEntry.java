package com.github.standobyte.jojo.command.configpack.standassign;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.command.configpack.userlist.UserListFixedEntry;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import net.minecraft.util.ResourceLocation;

public class StandAssignmentEntry extends UserListFixedEntry {
    private List<StandType<?>> assignedStands;
    
    public StandAssignmentEntry(GameProfile user, @Nullable List<StandType<?>> assignedStands) {
        super(user);
        this.assignedStands = assignedStands;
    }
    
    
    @Nullable
    public List<StandType<?>> getAssignedStands() {
        return this.assignedStands;
    }
    
    public boolean addStandType(StandType<?> standType) {
        if (assignedStands == null) {
            assignedStands = new ArrayList<>();
        }
        return !assignedStands.contains(standType) && assignedStands.add(standType);
    }
    
    public boolean removeStandType(StandType<?> standType, boolean emptyToNull) {
        if (assignedStands == null) {
            return false;
        }
        return assignedStands.remove(standType);
    }
    
    public boolean isStandListNull() {
        return assignedStands == null;
    }
    
    public boolean isStandListEmpty() {
        return assignedStands != null && assignedStands.isEmpty();
    }
    
    
    @Override
    protected void serialize(JsonObject json) {
        super.serialize(json);
        
        if (assignedStands != null) {
            JsonArray standsArray = new JsonArray();
            assignedStands.forEach(stand -> {
                standsArray.add(stand.getRegistryName().toString());
            });
            json.add("stands", standsArray);
        }
    }
    
    public static StandAssignmentEntry deserialize(JsonObject json) {
        GameProfile user = createGameProfile(json);
        
        List<StandType<?>> assignedStands;
        if (json.has("stands")) {
            JsonArray standsArray = json.get("stands").getAsJsonArray();
            assignedStands = new ArrayList<>();
            standsArray.forEach(standJson -> {
                String standId = standJson.getAsString();
                StandType<?> stand = JojoCustomRegistries.STANDS.getRegistry().getValue(new ResourceLocation(standId));
                if (stand != null && !assignedStands.contains(stand)) {
                    assignedStands.add(stand);
                }
            });
        }
        else {
            assignedStands = null;
        }
        
        return new StandAssignmentEntry(user, assignedStands);
    }
}
