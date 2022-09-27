package com.github.standobyte.jojo.init;

import java.util.Optional;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.network.NetworkUtil;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModDataSerializers {
    public static final DeferredRegister<DataSerializerEntry> DATA_SERIALIZERS = DeferredRegister.create(ForgeRegistries.DATA_SERIALIZERS, JojoMod.MOD_ID);
    
    public static final RegistryObject<DataSerializerEntry> STAND_ENTITY_TASK = DATA_SERIALIZERS.register("stand_action", StandEntityTask.SERIALIZER);
    
    public static final RegistryObject<DataSerializerEntry> OPTIONAL_VECTOR3D = DATA_SERIALIZERS.register("optional_vector3d", () -> new DataSerializerEntry(
            new IDataSerializer<Optional<Vector3d>>() {

        @Override
        public void write(PacketBuffer buf, Optional<Vector3d> value) {
            NetworkUtil.writeOptional(buf, value, (buffer, vector) -> {
                buffer.writeDouble(vector.x);
                buffer.writeDouble(vector.y);
                buffer.writeDouble(vector.z);
            });
        }

        @Override
        public Optional<Vector3d> read(PacketBuffer buf) {
            return NetworkUtil.readOptional(buf, buffer -> {
                Vector3d vec = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
                return vec;
            });
        }

        @Override
        public Optional<Vector3d> copy(Optional<Vector3d> value) {
            return value;
        }
    }));
}

