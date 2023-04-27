package com.github.standobyte.jojo.entity.stand;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ManualStandMovementLock {
    private final StandEntity stand;
    private final Map<UUID, InputDirection[]> locksById = new HashMap<>();
    private Set<InputDirection> locks = EnumSet.noneOf(InputDirection.class);
    
    ManualStandMovementLock(StandEntity stand) {
        this.stand = stand;
    }
    
    public void addLock(UUID id, InputDirection... directions) {
        locksById.put(id, directions);
        onLocksChanged();
    }
    
    public void removeLock(UUID id) {
        if (locksById.remove(id) != null) {
            onLocksChanged();
        }
    }
    
    private void onLocksChanged() {
        locks = locksById.values().stream().flatMap(Arrays::stream).distinct().collect(Collectors.toSet());
        stand.getEntityData().set(StandEntity.MANUAL_MOVEMENT_LOCK, encode(locks));
    }
    
    void onEntityDataUpdated(StandEntity standEntity) {
        locks = decode(standEntity.getEntityData().get(StandEntity.MANUAL_MOVEMENT_LOCK));
    }
    
    
    
    private byte encode(Set<InputDirection> locks) {
        byte b = 0;
        for (InputDirection lock : InputDirection.values()) {
            b <<= 1;
            if (locks.contains(lock)) {
                b |= 1;
            }
        }
        return b;
    }
    
    private Set<InputDirection> decode(byte encoded) {
        Set<InputDirection> set = EnumSet.noneOf(InputDirection.class);
        InputDirection[] values = InputDirection.values();
        for (int i = values.length - 1; i >= 0; i--) {
            if ((encoded & 1) > 0) {
                set.add(values[i]);
            }
            encoded >>= 1;
        }
        return set;
    }
    
    
    
    public float strafe(float left) {
        return left < 0 && locks.contains(InputDirection.RIGHT)
                || left > 0 && locks.contains(InputDirection.LEFT) ? 0 : left;
    }
    
    public float forward(float forward) {
        return forward < 0 && locks.contains(InputDirection.BACKWARDS)
                || forward > 0 && locks.contains(InputDirection.FORWARD) ? 0 : forward;
    }
    
    public boolean up(boolean up) {
        return up && !locks.contains(InputDirection.UP);
    }
    
    public boolean down(boolean down) {
        return down && !locks.contains(InputDirection.DOWN);
    }

    public enum InputDirection {
        LEFT,
        RIGHT,
        FORWARD,
        BACKWARDS,
        UP,
        DOWN
    }
}
