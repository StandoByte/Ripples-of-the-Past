package com.github.standobyte.jojo.capability.entity;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.GENativeMobsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.MetEntityTypesPacket;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;
import com.github.standobyte.jojo.util.mc.entitysubtype.SubtypeResourceLocation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

public class LifeformsMetMobs {
    private Set<ResourceLocation> metBaseEntityTypesId = new HashSet<>();
    private Set<SubtypeResourceLocation> metEntityTypesId = new HashSet<>();
    
    
    public boolean add(SubtypeResourceLocation entityTypeId) {
        metBaseEntityTypesId.add(entityTypeId.withoutSubtype);
        return metEntityTypesId.add(entityTypeId);
    }
    
    public boolean contains(SubtypeResourceLocation entityTypeId) {
        return metEntityTypesId.contains(entityTypeId);
    }
    
    public boolean isEmpty() {
        return metEntityTypesId.isEmpty();
    }
    
    public void serverTick() {
        if (nativeMobsUpdateDelay > 0) --nativeMobsUpdateDelay;
        if (nativeMobsUpdateDelay == 0 && nativeMobsUpdatePending != null && nativeMobsUpdatePending.user.isAlive()) {
            updateNativeMobs((ServerWorld) nativeMobsUpdatePending.user.level, nativeMobsUpdatePending.user, nativeMobsUpdatePending.syncToClient);
        }
    }
    
    
    public ListNBT toNBT() {
        ListNBT metEntities = new ListNBT();
        metEntityTypesId.forEach(entityTypeId -> metEntities.add(StringNBT.valueOf(entityTypeId.toString())));
        return metEntities;
    }
    
    public void fromNBT(ListNBT metEntitiesId) {
        metEntitiesId.forEach(idNBT -> {
            String idString = ((StringNBT) idNBT).getAsString(); 
            if (!idString.isEmpty()) {
                SubtypeResourceLocation registryName = new SubtypeResourceLocation(idString);
                add(registryName);
            }
        });
    }
    
    public void syncToClient(ServerPlayerEntity player) {
        PacketManager.sendToClient(new MetEntityTypesPacket(metEntityTypesId), player);
    }
    
    
    private Map<EntityClassification, List<EntityType<?>>> nativeMobs;
    private int nativeMobsUpdateDelay;
    @Nullable private PendingUpdate nativeMobsUpdatePending;
    
    public void updateNativeMobs(ServerWorld world, LivingEntity user, boolean sendToClient) {
        if (nativeMobs == null) {
            nativeMobs = new EnumMap<>(EntityClassification.class);
        }
        else if (nativeMobsUpdateDelay > 0) {
            return;
        }
        if (!(user.isOnGround() || user.isInWater())) {
            nativeMobsUpdatePending = new PendingUpdate(user, sendToClient || nativeMobsUpdatePending != null && nativeMobsUpdatePending.syncToClient);
            return;
        }
        nativeMobsUpdatePending = null;
        nativeMobsUpdateDelay = 20;
        
        BlockPos pos = user.blockPosition();
        StructureManager structureManager = world.structureFeatureManager();
        ChunkGenerator chunkGenerator = world.getChunkSource().getGenerator();
        Biome biome = world.getBiome(pos);
        Random notRandom = new SpawnRulesCheckNotRandom();
        
        nativeMobs.clear();
        for (EntityClassification classification : EntityClassification.values()) {
            List<MobSpawnInfo.Spawners> spawners;
            if (classification == EntityClassification.MONSTER && structureManager.getStructureAt(pos, false, Structure.NETHER_BRIDGE).isValid()) {
                spawners = Structure.NETHER_BRIDGE.getSpecialEnemies();
            }
            else {
                spawners = chunkGenerator.getMobsAt(biome, structureManager, classification, pos);
            }
            nativeMobs.put(classification, spawners.stream()
                    .map(spawner -> spawner.type)
                    .filter(type -> metBaseEntityTypesId.contains(type.getRegistryName())
                            && GoldExperienceChooseLifeform.isValidLifeform(EntitySubtype.base(type), world)
                            && ((EntitySpawnPlacementRegistry.getPlacementType(type) == EntitySpawnPlacementRegistry.PlacementType.IN_WATER) == world.getFluidState(pos).is(FluidTags.WATER))
                            && EntitySpawnPlacementRegistry.checkSpawnRules(type, world, SpawnReason.SPAWNER, pos, notRandom))
                    .collect(Collectors.toList()));
        }
        
        if (sendToClient && user instanceof ServerPlayerEntity) {
            PacketManager.sendToClient(new GENativeMobsPacket(this), (ServerPlayerEntity) user);
        }
    }
    
    public boolean isMobNativeToPlayerPos(World world, Entity mobInstance, LivingEntity geUser) {
        if (!world.isClientSide()) {
            updateNativeMobs((ServerWorld) world, geUser, false);
        }
        return nativeMobs != null && nativeMobs.get(mobInstance.getClassification(false)).contains(mobInstance.getType());
    }
    
    public void nativeMobsToBuf(PacketBuffer buf) {
        for (EntityClassification classification : EntityClassification.values()) {
            List<EntityType<?>> types = nativeMobs.get(classification);
            NetworkUtil.writeCollection(buf, types, 
                    (type, buffer) -> buffer.writeResourceLocation(type.getRegistryName()), false);
        }
    }
    
    public void nativeMobsFromBuf(PacketBuffer buf) {
        if (nativeMobs == null) {
            nativeMobs = new EnumMap<>(EntityClassification.class);
        }
        else {
            nativeMobs.clear();
        }
        
        for (EntityClassification classification : EntityClassification.values()) {
            List<ResourceLocation> typeIds = NetworkUtil.readCollection(buf, buf::readResourceLocation);
            nativeMobs.put(classification, typeIds
                    .stream()
                    .flatMap(id -> ForgeRegistries.ENTITIES.containsKey(id) ? Stream.of(ForgeRegistries.ENTITIES.getValue(id)) : Stream.empty())
                    .collect(Collectors.toList()));
        }
    }
    
    private static class PendingUpdate {
        private final LivingEntity user;
        private final boolean syncToClient;
        
        public PendingUpdate(LivingEntity user, boolean syncToClient) {
            this.user = user;
            this.syncToClient = syncToClient;
        }
    }
    
    private static class SpawnRulesCheckNotRandom extends Random {
        private static final long serialVersionUID = 1215500605941818217L;
        @Override protected int next(int bits) { return 0; }
    }
    
}
