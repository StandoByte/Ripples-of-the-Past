package com.github.standobyte.jojo.entity.stand;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.FMLPlayMessages.SpawnEntity;

public class StandEntityType<T extends StandEntity> extends EntityType<T> {
    private final StandEntityType.IStandFactory<T> factory;
    private Supplier<? extends StandType<?>> standTypeSupplier = null;
    private Supplier<SoundEvent> summonSound = ModSounds.STAND_SUMMON_DEFAULT;
    private Supplier<SoundEvent> unsummonSound = ModSounds.STAND_UNSUMMON_DEFAULT;

    public StandEntityType(IStandFactory<T> factory) {
        this(factory, 0.65F, 1.95F);
    }

    public StandEntityType(IStandFactory<T> factory, float width, float height) {
        this(factory, false, width, height, 
                t -> true, t -> 8, t -> 2, null);
    }
    
    public static StandEntityType<StandEntity> basicEntity() {
        return new StandEntityType<StandEntity>(StandEntity::new);
    }
    
    public static StandEntityType<StandEntity> basicEntity(float width, float height) {
        return new StandEntityType<StandEntity>(StandEntity::new, width, height);
    }

    protected StandEntityType(IStandFactory<T> factory, 
            boolean immuneToFire, float width, float height,
            Predicate<EntityType<?>> velocityUpdateSupplier, ToIntFunction<EntityType<?>> trackingRangeSupplier,
            ToIntFunction<EntityType<?>> updateIntervalSupplier, BiFunction<SpawnEntity, World, T> customClientFactory) {
        super(null, EntityClassification.MISC, true, false, immuneToFire, false, null, EntitySize.scalable(width, height),
                -1, -1, velocityUpdateSupplier, trackingRangeSupplier, updateIntervalSupplier, customClientFactory);
        this.factory = factory;
    }
    
    public void setStandType(Supplier<? extends StandType<?>> standTypeSupplier) {
        if (this.standTypeSupplier == null) {
            this.standTypeSupplier = standTypeSupplier;
        }
    }
    
    public StandEntityType<T> summonSound(Supplier<SoundEvent> soundSupplier) {
        this.summonSound = soundSupplier;
        return this;
    }
    
    public StandEntityType<T> unsummonSound(Supplier<SoundEvent> soundSupplier) {
        this.unsummonSound = soundSupplier;
        return this;
    }
    
    private StandType<?> getStandType() {
        return standTypeSupplier.get();
    }
    
    public StandStats getStats() {
        return getStandType().getStats();
    }
    
    @Nullable
    public SoundEvent getSummonSound() {
        return summonSound != null ? summonSound.get() : null;
    }
    
    @Nullable
    public SoundEvent getUnsummonSound() {
        return unsummonSound != null ? unsummonSound.get() : null;
    }

    @Nullable
    @Override
    public T create(World world) {
        return factory.create(this, world);
    }
    
    @Override
    public T customClientSpawn(FMLPlayMessages.SpawnEntity packet, World world) {
        T entity = super.customClientSpawn(packet, world);
        entity.beforeClientSpawn(packet, world);
        return entity;
    }

    public interface IStandFactory<T extends StandEntity> {
        T create(StandEntityType<T> type, World world);
    }
}
