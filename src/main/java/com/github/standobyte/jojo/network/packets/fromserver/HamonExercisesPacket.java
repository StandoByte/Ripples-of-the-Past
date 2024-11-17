package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Arrays;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData.Exercise;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonExercisesPacket {
    private final int[] exerciseTicks;
    private final boolean sendBonus;
    private final float trainingBonus;
    private final int canSkipTrainingDays;
    
    public static HamonExercisesPacket allData(HamonData hamon) {
        return new HamonExercisesPacket(
                Arrays.stream(Exercise.values()).mapToInt(ex -> hamon.getExerciseTicks(ex)).toArray(),
                true,
                hamon.getTrainingBonus(false),
                hamon.getCanSkipTrainingDays());
    }
    
    public static HamonExercisesPacket exercisesOnly(HamonData hamon) {
        return new HamonExercisesPacket(
                Arrays.stream(Exercise.values()).mapToInt(ex -> hamon.getExerciseTicks(ex)).toArray(),
                false, 0, 0);
    }

    private HamonExercisesPacket(int[] exerciseTicks, boolean sendBonus, float trainingBonus, int canSkipTrainingDays) {
        this.exerciseTicks = exerciseTicks;
        this.sendBonus = sendBonus;
        this.trainingBonus = trainingBonus;
        this.canSkipTrainingDays = canSkipTrainingDays;
    }
    
    
    
    public static class Handler implements IModPacketHandler<HamonExercisesPacket> {

        public void encode(HamonExercisesPacket msg, PacketBuffer buf) {
            NetworkUtil.writeIntArray(buf, msg.exerciseTicks);
            buf.writeBoolean(msg.sendBonus);
            if (msg.sendBonus) {
                buf.writeFloat(msg.trainingBonus);
                buf.writeVarInt(msg.canSkipTrainingDays);
            }
        }
    
        public HamonExercisesPacket decode(PacketBuffer buf) {
            int[] exerciseTicks = NetworkUtil.readIntArray(buf);
            boolean readBonus = buf.readBoolean();
            float trainingBonus = readBonus ? buf.readFloat() : 0;
            int canSkipTrainingDays = readBonus ? buf.readVarInt() : 0;
            return new HamonExercisesPacket(exerciseTicks, readBonus, trainingBonus, canSkipTrainingDays);
        }
        
        public void handle(HamonExercisesPacket msg, Supplier<NetworkEvent.Context> ctx) {
            INonStandPower.getNonStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.setExerciseTicks(msg.exerciseTicks, true);
                    if (msg.sendBonus) {
                        hamon.setTrainingBonus(msg.trainingBonus);
                        hamon.setCanSkipTrainingDays(msg.canSkipTrainingDays);
                    }
                });
            });
        }

        @Override
        public Class<HamonExercisesPacket> getPacketClass() {
            return HamonExercisesPacket.class;
        }
    }
}
