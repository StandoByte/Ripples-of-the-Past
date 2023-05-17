package com.github.standobyte.jojo.init.power.stand;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class EntityStandRegistryObject<ST extends EntityStandType<? extends StandStats>, ET extends StandEntityType<? extends StandEntity>> {
    private static final Set<EntityStandRegistryObject<?, ?>> ENTITY_STANDS_LINKAGE = new HashSet<>();
    private static final Set<Supplier<? extends EntityType<? extends StandEntity>>> DEFAULT_STAND_ATTRIBUTES = new HashSet<>();
    
    private final RegistryObject<ST> standType;
    private final RegistryObject<ET> entityType;

    public EntityStandRegistryObject(String name, 
            DeferredRegister<StandType<?>> standsRegister, Supplier<? extends ST> standType, 
            DeferredRegister<EntityType<?>> entitiesRegister, Supplier<? extends ET> entityType) {
        this.standType = standsRegister.register(name, standType);
        this.entityType = entitiesRegister.register(name, entityType);
        ENTITY_STANDS_LINKAGE.add(this);
    }
    
    public EntityStandRegistryObject<ST, ET> withDefaultStandAttributes() {
        DEFAULT_STAND_ATTRIBUTES.add(entityType);
        return this;
    }
    
    public ST getStandType() {
        return standType.get();
    }
    
    public ET getEntityType() {
        return entityType.get();
    }
    
    
    @SubscribeEvent
    public static void createDefaultStandAttributes(EntityAttributeCreationEvent event) {
        DEFAULT_STAND_ATTRIBUTES.forEach(standEntityType -> event.put(standEntityType.get(), StandEntity.createAttributes().build()));
    }
    
    @SubscribeEvent
    public static final void afterStandsRegister(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ENTITY_STANDS_LINKAGE.forEach(entry -> {
                entry.getStandType().setEntityType(entry.entityType);
                entry.getEntityType().setStandType(entry.standType);
            });
        });
    }
    
    
    
    public static class EntityStandSupplier<ST extends EntityStandType<? extends StandStats>, ET extends StandEntityType<? extends StandEntity>> {
        private final EntityStandRegistryObject<ST, ET> registryObjectsHolder;
        
        public EntityStandSupplier(EntityStandRegistryObject<ST, ET> registryObjectsHolder) {
            this.registryObjectsHolder = registryObjectsHolder;
        }
        
        public ST getStandType() {
            return registryObjectsHolder.getStandType();
        }
        
        public ET getEntityType() {
            return registryObjectsHolder.getEntityType();
        }
    }
}
