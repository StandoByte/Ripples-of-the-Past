package com.github.standobyte.jojo.util.mc.entitysubtype;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.world.World;

/* 
 * A map to hold entity instances which aren't added into a world, 
 * for cases where something specific for entity types
 * requires actual entity instances to exist by the vanilla code design
 * (entity texture paths, AI goals, etc.)
 */
public class EntityTypeToInstance {
    private static EntityTypeToInstance instance;
    
    public static void init(World world) {
        if (instance == null) {
            Stream<EntitySubtype<?>> entityTypes = EntitySubtype.values();
            instance = new EntityTypeToInstance(entityTypes, world);
        }
    }
    
    private final Map<SubtypeResourceLocation, Entity> entityInstances = new HashMap<>();
    
    private EntityTypeToInstance(Stream<EntitySubtype<?>> entityTypes, World world) {
        entityTypes.forEach(subtype -> entityInstances.put(subtype.getId(), createInstance(subtype, world)));
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T getEntityInstance(EntitySubtype<T> subType, World world) {
        if (instance == null) {
            JojoMod.getLogger().error("An operation with {} entity type needed an Entity instance, but the map for them hasn't been created yet!", subType.vanillaType.getRegistryName());
            return null;
        }
        return (T) instance.entityInstances.computeIfAbsent(subType.getId(), __ -> createInstance(subType, world));
    }
    
    private static <T extends Entity> T createInstance(EntitySubtype<T> type, World world) {
        T entity = type.create(world);
        if (entity instanceof SlimeEntity) {
            entity.refreshDimensions();
        }
        return entity;
    }
    
}
