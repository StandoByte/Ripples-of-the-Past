package com.github.standobyte.jojo.util.mc.entitysubtype;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    private final Set<EntitySubtype<?>> tryLazyInit = new HashSet<>();
    
    private EntityTypeToInstance(Stream<EntitySubtype<?>> entityTypes, World world) {
        entityTypes.forEach(subtype -> {
            try {
                Entity entity = createInstance(subtype, world);
                entityInstances.put(subtype.getId(), entity);
            }
            catch (Exception e) {
                tryLazyInit.add(subtype);
                JojoMod.getLogger().warn("Failed to initialize an entity of type {} on world load. Will try lazy initialization.", subtype.getId());
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T getEntityInstance(EntitySubtype<T> subType, World world) {
        if (instance == null) {
            JojoMod.getLogger().error("An operation with {} entity type needed an Entity instance, but the map for them hasn't been created yet!", subType.vanillaType.getRegistryName());
            return null;
        }
        Entity entity = null;
        if (!instance.entityInstances.containsKey(subType.getId()) && instance.tryLazyInit.remove(subType)) {
            entity = createInstance(subType, world);
            if (entity != null) {
                instance.entityInstances.put(subType.getId(), entity);
            }
        }
        else {
            entity = instance.entityInstances.get(subType.getId());
        }
        return (T) entity;
    }
    
    private static <T extends Entity> T createInstance(EntitySubtype<T> type, World world) {
        T entity = type.create(world);
        if (entity instanceof SlimeEntity) {
            entity.refreshDimensions();
        }
        return entity;
    }
    
}
