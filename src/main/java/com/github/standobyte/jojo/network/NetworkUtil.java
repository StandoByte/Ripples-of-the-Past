package com.github.standobyte.jojo.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;

import io.netty.handler.codec.DecoderException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgePacketBuffer;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

public class NetworkUtil {

    public static void broadcastWithCondition(List<ServerPlayerEntity> players, @Nullable PlayerEntity clientHandled, 
            double x, double y, double z, double radius, World world, 
            IPacket<IClientPlayNetHandler> packet, Predicate<PlayerEntity> condition) {
        for (ServerPlayerEntity player : players) {
            if (player != clientHandled && player.level.dimension() == world.dimension()
                    && condition.test(player) && player.position().subtract(x, y, z).lengthSqr() < radius * radius) {
                player.connection.send(packet);
            }
        }
    }

    public static void broadcastWithCondition(List<ServerPlayerEntity> players, 
            IPacket<IClientPlayNetHandler> packet, Predicate<PlayerEntity> condition) {
        for (ServerPlayerEntity player : players) {
            if (condition.test(player)) {
                player.connection.send(packet);
            }
        }
    }
    
    
    
    public static <T extends IForgeRegistryEntry<T>> void writeRegistryIds(IForgePacketBuffer buf, @Nonnull List<T> entries) {
        Objects.requireNonNull(entries, "Cannot write a null registry entries list!");
        buf.getBuffer().writeBoolean(!entries.isEmpty());
        if (entries.isEmpty()) return;
        IForgeRegistry<T> retrievedRegistry = null;
        for (T entry : entries) {
            Class<T> entryRegType = Objects.requireNonNull(entry, "Cannot write a null registry entry!").getRegistryType();
            IForgeRegistry<T> entryRegistry = RegistryManager.ACTIVE.getRegistry(entryRegType);
            Preconditions.checkArgument(entryRegistry != null, "Cannot write registry id for an unknown registry type: %s", entryRegType.getName());
            if (retrievedRegistry == null) retrievedRegistry = entryRegistry;
            Preconditions.checkArgument(retrievedRegistry == entryRegistry, "Cannot write entries of different registry types: %s, %s", 
                    retrievedRegistry.getRegistrySuperType().getName(), entryRegType.getName());
            Preconditions.checkArgument(retrievedRegistry.containsValue(entry), "Cannot find %s in %s", 
                    entry.getRegistryName() != null ? entry.getRegistryName() : entry, retrievedRegistry.getRegistryName());
        }
        ResourceLocation name = retrievedRegistry.getRegistryName();
        ForgeRegistry<T> reg = (ForgeRegistry<T>) retrievedRegistry;
        buf.getBuffer().writeResourceLocation(name);
        buf.getBuffer().writeVarInt(entries.size());
        for (T entry : entries) {
            buf.getBuffer().writeVarInt(reg.getID(entry));
        }
    }

    public static <T extends IForgeRegistryEntry<T>> List<T> readRegistryIds(IForgePacketBuffer buf) {
        if (!buf.getBuffer().readBoolean()) return Collections.emptyList();
        ResourceLocation location = buf.getBuffer().readResourceLocation();
        ForgeRegistry<T> registry = RegistryManager.ACTIVE.getRegistry(location);
        int size = buf.getBuffer().readVarInt();
        List<T> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(registry.getValue(buf.getBuffer().readVarInt()));
        }
        return entries;
    }

    public static <T extends IForgeRegistryEntry<T>> List<T> readRegistryIdsSafe(IForgePacketBuffer buf, Class<? super T> registrySuperType) {
        List<T> values = readRegistryIds(buf);
        for (T value : values) {
            if (!value.getRegistryType().equals(registrySuperType))
                throw new IllegalArgumentException("Attempted to read an registryValue of the wrong type from the Buffer!");
        }
        return values;
    }
    
    
    public static PacketBuffer writeFloatArray(PacketBuffer buf, float[] arr) {
        buf.writeVarInt(arr.length);
        for (float num : arr) {
            buf.writeFloat(num);
        }
        return buf;
    }
    
    public static float[] readFloatArray(PacketBuffer buf) {
        return readFloatArray(buf, buf.readableBytes() / 4);
    }

    public static float[] readFloatArray(PacketBuffer buf, int maxAllowed) {
        int n = buf.readVarInt();
        if (n > maxAllowed) {
            throw new DecoderException("FloatArray with size " + n + " is bigger than allowed " + maxAllowed);
        } else {
            float[] arr = new float[n];
            for (int i = 0; i < n; i++) {
                arr[i] = buf.readFloat();
            }
            return arr;
        }
    }

    public static PacketBuffer writeIntArray(PacketBuffer buf, int[] arr) {
        buf.writeVarInt(arr.length);

        for (int i : arr) {
            buf.writeInt(i);
        }

        return buf;
    }

    public static int[] readIntArray(PacketBuffer buf) {
        return readIntArray(buf, buf.readableBytes());
    }

    public static int[] readIntArray(PacketBuffer buf, int maxAllowed) {
        int n = buf.readVarInt();
        if (n > maxAllowed) {
            throw new DecoderException("IntArray with size " + n + " is bigger than allowed " + maxAllowed);
        } else {
            int[] arr = new int[n];

            for(int i = 0; i < arr.length; ++i) {
                arr[i] = buf.readInt();
            }

            return arr;
        }
    }
    
    /**
     * Supports enums with up to 255 elements
     */
    public static <T extends Enum<T>> PacketBuffer writeSmallEnumArray(PacketBuffer buf, T[] input) {
        int[] ordinals = GeneralUtil.toOrdinals(input);
        buf.writeVarInt(input.length);
        for (int i = 0; i < input.length; i++) {
            int ordinal = ordinals[i];
            if (ordinal >= 0 && ordinal < 255) {
                buf.writeByte(ordinal);
            }
            else {
                buf.writeByte(255);
            }
        }
        return buf;
    }
    
    public static <T extends Enum<T>> T[] readSmallEnumArray(PacketBuffer buf, Class<T> enumClass) {
        int length = buf.readVarInt();
        T[] enumValues = enumClass.getEnumConstants();
        T[] ret = ObjectArrays.newArray(enumClass, length);
        for (int i = 0; i < length; i++) {
            int ordinal = 0xFF & buf.readByte();
            if (ordinal >= 0 && ordinal < 255 && ordinal < enumValues.length) {
                ret[i] = enumValues[ordinal];
            }
        }
        return ret;
    }
    
    public static void writeVecApproximate(PacketBuffer buf, Vector3d vec) {
        buf.writeInt((int) (vec.x * 8.0));
        buf.writeInt((int) (vec.y * 8.0));
        buf.writeInt((int) (vec.z * 8.0));
    }
    
    public static Vector3d readVecApproximate(PacketBuffer buf) {
        return new Vector3d(
                buf.readInt() / 8.0, 
                buf.readInt() / 8.0, 
                buf.readInt() / 8.0);
    }
    
    
    public static <T> void writeOptionally(PacketBuffer buf, @Nullable T obj, Consumer<T> write) {
        buf.writeBoolean(obj != null);
        if (obj != null) {
            write.accept(obj);
        }
    }
    
    public static <T> void writeOptionally(PacketBuffer buf, @Nullable T obj, BiConsumer<T, PacketBuffer> write) {
        buf.writeBoolean(obj != null);
        if (obj != null) {
            write.accept(obj, buf);
        }
    }

    public static <T> void writeOptional(PacketBuffer buf, @Nonnull Optional<T> objOptional, Consumer<T> write) {
        buf.writeBoolean(objOptional.isPresent());
        objOptional.ifPresent(obj -> write.accept(obj));
    }
    
    public static <T> void writeOptional(PacketBuffer buf, @Nonnull Optional<T> objOptional, BiConsumer<T, PacketBuffer> write) {
        buf.writeBoolean(objOptional.isPresent());
        objOptional.ifPresent(obj -> write.accept(obj, buf));
    }

    public static <T> Optional<T> readOptional(PacketBuffer buf, Supplier<T> read) {
        return buf.readBoolean() ? Optional.ofNullable(read.get()) : Optional.empty();
    }
    
    public static <T> Optional<T> readOptional(PacketBuffer buf, Function<PacketBuffer, T> read) {
        return buf.readBoolean() ? Optional.ofNullable(read.apply(buf)) : Optional.empty();
    }
    
    
    public static <T> int writeCollection(PacketBuffer buf, Collection<T> collection, Consumer<T> writeElement, 
            boolean removeWrittenFromCollection) {
        int i = 0;
        int initialWriterIndex = buf.writerIndex();
        buf.writeInt(0);
        
        int lastWriterIndex = initialWriterIndex;
        int maxElemSize = 0;
        Iterator<T> iter = collection.iterator();
        while (iter.hasNext()) {
            if (buf.capacity() < maxElemSize) break;
            T element = iter.next();
            writeElement.accept(element);
            i++;
            if (removeWrittenFromCollection) {
                iter.remove();
            }
            int writerIndex = buf.writerIndex();
            maxElemSize = Math.max(maxElemSize, writerIndex - lastWriterIndex);
            lastWriterIndex = writerIndex;
        }
                
        buf.setInt(initialWriterIndex, i);
        return i;
    }
    
    public static <T, C extends Collection<T>> C readCollection(Supplier<C> createCollection, PacketBuffer buf, Supplier<T> readElement) {
        C collection = createCollection.get();
        int size = buf.readInt();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                collection.add(readElement.get());
            }
        }
        return collection;
    }
    
    public static <T> List<T> readCollection(PacketBuffer buf, Supplier<T> readElement) {
        return readCollection(ArrayList::new, buf, readElement);
    }
    
    public static <T> int writeCollection(PacketBuffer buf, Collection<T> collection, BiConsumer<T, PacketBuffer> writeElement, 
            boolean removeWrittenFromCollection) {
        int i = 0;
        int initialWriterIndex = buf.writerIndex();
        buf.writeInt(0);
        
        int lastWriterIndex = initialWriterIndex;
        int maxElemSize = 0;
        Iterator<T> iter = collection.iterator();
        while (iter.hasNext()) {
            if (buf.capacity() < maxElemSize) break;
            T element = iter.next();
            writeElement.accept(element, buf);
            i++;
            if (removeWrittenFromCollection) {
                iter.remove();
            }
            int writerIndex = buf.writerIndex();
            maxElemSize = Math.max(maxElemSize, writerIndex - lastWriterIndex);
            lastWriterIndex = writerIndex;
        }
                
        buf.setInt(initialWriterIndex, i);
        return i;
    }
    
    public static <T, C extends Collection<T>> C readCollection(Supplier<C> createCollection, PacketBuffer buf, Function<PacketBuffer, T> readElement) {
        C collection = createCollection.get();
        int size = buf.readInt();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                collection.add(readElement.apply(buf));
            }
        }
        return collection;
    }
    
    public static <T> List<T> readCollection(PacketBuffer buf, Function<PacketBuffer, T> readElement) {
        return readCollection(ArrayList::new, buf, readElement);
    }
    
    
    public static void writePowerType(PacketBuffer buf, IPowerType<?, ?> powerType, PowerClassification powerClassification) {
        switch (powerClassification) {
        case STAND:
            buf.writeRegistryId((StandType<?>) powerType);
            break;
        case NON_STAND:
            buf.writeRegistryId((NonStandPowerType<?>) powerType);
            break;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static IPowerType<?, ?> readPowerType(PacketBuffer buf, PowerClassification powerClassification) {
        switch (powerClassification) {
        case STAND:
            return buf.readRegistryIdSafe(StandType.class);
        case NON_STAND:
            return buf.readRegistryIdSafe(NonStandPowerType.class);
        default:
            throw new IllegalArgumentException();
        }
    }
    
    
    public static void writeEntity(PacketBuffer buf, @Nullable Entity entity) {
        buf.writeBoolean(entity != null);
        if (entity != null) {
            buf.writeInt(entity.getId());
        }
    }
    
    @Nullable
    public static Entity readEntity(PacketBuffer buf, World world) {
        boolean hasEntity = buf.readBoolean();
        if (hasEntity) {
            int entityId = buf.readInt();
            return world.getEntity(entityId);
        }
        return null;
    }
}
