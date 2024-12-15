package com.github.standobyte.jojo.util.mod;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.world.GameType;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IPlayerPossess {
    void jojoPossessEntity(@Nullable Entity entity, boolean asAlive, @Nullable IForgeRegistryEntry<?> context);
    @Nullable Entity jojoGetPossessedEntity();
    boolean jojoIsPossessingAsAlive();
    Optional<GameType> jojoGetPrePossessGameMode();
    void jojoSetPrePossessGameMode(Optional<GameType> gameType);
    @Nullable IForgeRegistryEntry<?> jojoGetPossessionContext();
    void jojoOnPossessingDead();
    
    public static Entity getPossessedEntity(Entity possessing) {
        return possessing instanceof IPlayerPossess ? ((IPlayerPossess) possessing).jojoGetPossessedEntity() : null;
    }
}
