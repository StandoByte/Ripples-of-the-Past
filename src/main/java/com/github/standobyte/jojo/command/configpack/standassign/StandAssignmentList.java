package com.github.standobyte.jojo.command.configpack.standassign;

import com.github.standobyte.jojo.command.configpack.userlist.DataConfigUserList;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

public class StandAssignmentList extends DataConfigUserList<StandAssignmentEntry> {
    
    public boolean addAssignedStand(GameProfile player, StandType<?> standType) {
        if (!contains(player)) {
            add(new StandAssignmentEntry(player, null));
        }
        StandAssignmentEntry entry = get(player);
        return entry.addStandType(standType);
    }
    
    public boolean removeAssignedStand(GameProfile player, StandType<?> standType, 
            boolean removeEmptyListEntry) {
        if (contains(player)) {
            StandAssignmentEntry entry = get(player);
            boolean removed = entry.removeStandType(standType, removeEmptyListEntry);
            if (removed && removeEmptyListEntry && entry.isStandListEmpty()) {
                remove(player);
            }
            return removed;
        }
        
        return false;
    }
    
    
    public static final Serializer<StandAssignmentList, StandAssignmentEntry> SERIALIZER = new Serializer<StandAssignmentList, StandAssignmentEntry>() {

        @Override
        protected StandAssignmentList createListObject() {
            return new StandAssignmentList();
        }

        @Override
        protected StandAssignmentEntry deserializeEntry(JsonObject entryJson) {
            return StandAssignmentEntry.deserialize(entryJson);
        }
        
    };
}