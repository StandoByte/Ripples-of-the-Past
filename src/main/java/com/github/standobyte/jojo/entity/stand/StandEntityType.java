package com.github.standobyte.jojo.entity.stand;

import java.util.EnumMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandSoundPacket.StandSoundType;
import com.github.standobyte.jojo.power.stand.stats.StandStatsV2;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages.SpawnEntity;

public class StandEntityType<T extends StandEntity> extends EntityType<T> {
    private final StandEntityType.IStandFactory<T> factory;
    private final Supplier<? extends StandType<?>> standTypeSupplier;
    private final StandEntityStats stats;
    private final EnumMap<StandSoundType, Supplier<SoundEvent>> sounds = new EnumMap<>(StandSoundType.class);

    public StandEntityType(IStandFactory<T> factory, Supplier<? extends StandType<?>> standType, 
            boolean immuneToFire, float width, float height, 
            StandEntityStats stats) {
        this(factory, standType, immuneToFire, width, height, 
                t -> true, t -> 8, t -> 2, null, 
                stats);
    }

    protected StandEntityType(IStandFactory<T> factory, Supplier<? extends StandType<?>> standType, 
            boolean immuneToFire, float width, float height,
            Predicate<EntityType<?>> velocityUpdateSupplier, ToIntFunction<EntityType<?>> trackingRangeSupplier,
            ToIntFunction<EntityType<?>> updateIntervalSupplier, BiFunction<SpawnEntity, World, T> customClientFactory, 
            StandEntityStats stats) {
        super(null, EntityClassification.MISC, false, false, immuneToFire, false, null, EntitySize.scalable(width, height),
                -1, -1, velocityUpdateSupplier, trackingRangeSupplier, updateIntervalSupplier, customClientFactory);
        this.factory = factory;
        this.stats = stats;
        this.standTypeSupplier = standType;
        this.sounds.put(StandSoundType.SUMMON, ModSounds.STAND_SUMMON_DEFAULT);
        this.sounds.put(StandSoundType.UNSUMMON, ModSounds.STAND_UNSUMMON_DEFAULT);
    }
    
    public StandEntityType<T> addStandSound(StandSoundType soundType, Supplier<SoundEvent> soundSupplier) {
        if (soundType != null) {
            sounds.put(soundType, soundSupplier);
        }
        return this;
    }

    public StandEntityStats getStats() {
        return stats;
    }
    
    public StandStatsV2 getStatsV2() {
        return standTypeSupplier.get().getStats();
    }
    
    @Nullable
    public SoundEvent getSound(StandSoundType soundType) {
        Supplier<SoundEvent> soundSupplier = sounds.get(soundType);
        return soundSupplier != null ? sounds.get(soundType).get() : null;
    }

    @Nullable
    @Override
    public T create(World world) {
        return factory.create(this, world);
    }

    public interface IStandFactory<T extends StandEntity> {
        T create(StandEntityType<T> type, World world);
    }
}
