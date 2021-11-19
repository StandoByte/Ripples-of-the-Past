package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModDataSerializers {
    public static final DeferredRegister<DataSerializerEntry> DATA_SERIALIZERS = DeferredRegister.create(ForgeRegistries.DATA_SERIALIZERS, JojoMod.MOD_ID);

    public static final RegistryObject<DataSerializerEntry> STAND_POSE = DATA_SERIALIZERS.register("stand_pose", () -> new DataSerializerEntry(new IDataSerializer<StandPose>() {
        @Override
        public void write(PacketBuffer buf, StandPose value) { buf.writeEnum(value); }
        @Override
        public StandPose read(PacketBuffer buf) { return buf.readEnum(StandPose.class); }
        @Override
        public StandPose copy(StandPose value) { return value; }
    }));

    public static final RegistryObject<DataSerializerEntry> TASK_TARGET = DATA_SERIALIZERS.register("task_target", () -> new DataSerializerEntry(new IDataSerializer<ActionTarget>() {
        @Override
        public void write(PacketBuffer buf, ActionTarget value) {
            TargetType type = value.getType();
            buf.writeEnum(type);
            switch (type) {
            case ENTITY:
                buf.writeInt(value.getEntity(null).getId());
                break;
            case BLOCK:
                buf.writeBlockPos(value.getBlockPos());
                buf.writeEnum(value.getFace());
                break;
            default:
            }
        }

        @Override
        public ActionTarget read(PacketBuffer buf) {
            TargetType type = buf.readEnum(TargetType.class);
            switch (type) {
            case ENTITY:
                return new ActionTarget(buf.readInt());
            case BLOCK:
                return new ActionTarget(buf.readBlockPos(), buf.readEnum(Direction.class));
            default:
                return ActionTarget.EMPTY;
            }
        }

        @Override
        public ActionTarget copy(ActionTarget value) {
            return value; 
        }
    }));
}

