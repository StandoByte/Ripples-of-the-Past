package com.github.standobyte.jojo.util.mc.entitysubtype;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.Streams;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class EntitySubtype<T extends Entity> {
    public final EntityType<T> vanillaType;
    private final SubtypeResourceLocation id;
    private final Consumer<T> onInstanceInit;
    private final Predicate<T> entityIsOfSubtype;
    
    public EntitySubtype(EntityType<T> type, String subtypeId, 
            Consumer<T> onInstanceInit, Predicate<T> entityIsOfSubtype) {
        this.vanillaType = type;
        this.id = new SubtypeResourceLocation(type.getRegistryName(), subtypeId);
        this.onInstanceInit = onInstanceInit;
        this.entityIsOfSubtype = entityIsOfSubtype;
    }
    
    public static <T extends Entity> EntitySubtype<?> base(EntityType<T> type) {
        return BASE_SUBTYPES.computeIfAbsent(type.getRegistryName(), __ -> new EntitySubtype<>(type, null, null, null));
    }
    
    public T create(World world) {
        T entity = vanillaType.create(world);
        if (onInstanceInit != null) {
            onInstanceInit.accept(entity);
        }
        return entity;
    }
    
    public boolean matches(T entity) {
        return entity.getType() == vanillaType && (entityIsOfSubtype == null || entityIsOfSubtype.test(entity));
    }
    
    public SubtypeResourceLocation getId() {
        return id;
    }

    public ITextComponent getDescription() {
        return vanillaType.getDescription();
    }
    
    
    private static final Map<ResourceLocation, Map<String, EntitySubtype<?>>> SUBTYPES = new HashMap<>();
    private static final Map<ResourceLocation, EntitySubtype<?>> BASE_SUBTYPES = new HashMap<>();
    private static Collection<EntitySubtype<?>> allValuesCache;
    
    public static <T extends Entity> EntitySubtype<T> registerSubtype(EntityType<T> entityType, String subtypeId, 
            Consumer<T> onInstanceInit, Predicate<T> entityIsOfSubtype) {
        Objects.requireNonNull(subtypeId);
        EntitySubtype<T> subType = new EntitySubtype<>(entityType, subtypeId, onInstanceInit, entityIsOfSubtype);
        Map<String, EntitySubtype<?>> subtypes = SUBTYPES.computeIfAbsent(entityType.getRegistryName(), __ -> new HashMap<>());
        subtypes.put(subtypeId, subType);
        return subType;
    }
    
    public static Stream<EntitySubtype<?>> values() {
        if (allValuesCache == null) {
            allValuesCache = ForgeRegistries.ENTITIES.getValues().stream()
                    .flatMap(entityType -> {
                        Stream<EntitySubtype<?>> base = Stream.of(base(entityType));
                        Map<String, EntitySubtype<?>> subtypes = SUBTYPES.get(entityType.getRegistryName());
                        if (subtypes != null && !subtypes.isEmpty()) {
                            return Streams.concat(base, subtypes.values().stream());
                        }
                        return base;
                    })
                    .collect(Collectors.toList());
        }
        return allValuesCache.stream();
    }
    
    @Nullable
    public static EntitySubtype<?> getSubtype(SubtypeResourceLocation id) {
        if (id.getSubtypeId() == null) {
            return base(ForgeRegistries.ENTITIES.getValue(id));
        }
        Map<String, EntitySubtype<?>> subtypes = SUBTYPES.get(id.withoutSubtype);
        return subtypes != null ? subtypes.get(id.getSubtypeId()) : null;
    }
    
    public static <T extends Entity> Stream<EntitySubtype<?>> getMatchingSubtypes(T entity) {
        Map<String, EntitySubtype<?>> subtypes = SUBTYPES.get(entity.getType().getRegistryName());
        Stream<EntitySubtype<?>> base = Stream.of(base(entity.getType()));
        if (subtypes == null || subtypes.isEmpty()) {
            return base;
        }
        return Streams.concat(base, subtypes.values().stream().filter(subType -> ((EntitySubtype<T>) subType).matches(entity)));
    }
    
    
    public void toBuf(PacketBuffer buf) {
        buf.writeUtf(this.getId().toString());
    }
    
    public static EntitySubtype<?> fromBuf(PacketBuffer buf) {
        return getSubtype(new SubtypeResourceLocation(buf.readUtf()));
    }

}
