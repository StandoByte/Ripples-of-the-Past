package com.github.standobyte.jojo.mrpresident;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;
import com.github.standobyte.jojo.world.dimension.ModDimensions;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;

public class MrPresidentEnteredRoomEffect extends StandEffectInstance {
    public UUID roomId;
    private Set<UUID> enteredEntities = new HashSet<>();

    public MrPresidentEnteredRoomEffect(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    public void onEntityEntered(Entity entity) {
        enteredEntities.add(entity.getUUID());
    }
    
    public void onEntityQuit(Entity entity) {
        enteredEntities.remove(entity.getUUID());
    }

    
    @Override
    protected void start() {}

    @Override
    protected void tick() {}

    @Override
    protected void stop() {
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            MinecraftServer server = serverWorld.getServer();
            ServerWorld mrPresidentWorld = server.getLevel(ModDimensions.MR_PRESIDENT);
            if (mrPresidentWorld != null) {
                for (UUID entityId : enteredEntities) {
                    Entity entity = mrPresidentWorld.getEntity(entityId);
                    if (entity != null) {
                        MrPresidentStandType.teleportFromRoom(entity, roomId, server);
                    }
                }
            }
        }
    }

    @Override
    protected boolean needsTarget() {
        return false;
    }

}
