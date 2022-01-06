package com.github.standobyte.jojo.capability.world;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.power.stand.type.StandType;

public class SaveFileUtilCap {
    Map<StandType<?>, Integer> timesStandsTaken = new HashMap<>();
    
    public void addPlayerStand(StandType<?> type) {
        int prevValue = timesStandsTaken.containsKey(type) ? timesStandsTaken.get(type) : 0;
        timesStandsTaken.put(type, ++prevValue);
    }
    
    public void removePlayerStand(StandType<?> type) {
        if (timesStandsTaken.containsKey(type)) {
            int prevValue = timesStandsTaken.get(type);
            if (prevValue <= 1) {
                timesStandsTaken.remove(type);
            }
            else {
                timesStandsTaken.put(type, --prevValue);
            }
        }
    }
    
    public List<StandType<?>> leastTakenStands(Collection<StandType<?>> fromStands) {
        if (fromStands.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Map.Entry<StandType<?>, Integer>> entriesRequested = timesStandsTaken
                .entrySet()
                .stream()
                .filter(entry -> fromStands.contains(entry.getKey()))
                .collect(Collectors.toSet());
        if (entriesRequested.size() == fromStands.size()) {
            int min = Collections.min(entriesRequested, Comparator.comparingInt(entry -> entry.getValue())).getValue();
            return entriesRequested
                    .stream()
                    .filter(entry -> entry.getValue() == min)
                    .map(entry -> entry.getKey())
                    .collect(Collectors.toList());
        }
        else {
            List<StandType<?>> st = entriesRequested.stream().map(Map.Entry::getKey).collect(Collectors.toList());
            return fromStands
                    .stream()
                    .filter(stand -> !st.contains(stand))
                    .collect(Collectors.toList());
        }
    }
}
