package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData.Exercise;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonExercisesPacket {
    private final int miningTicks;
    private final int runningTicks;
    private final int swimmingTicks;
    private final int meditationTicks;

    private final boolean sendBonus;
    private final float trainingBonus;
    private final float prevDayExercises;
    
    public static HamonExercisesPacket allData(HamonData hamon) {
        return new HamonExercisesPacket(
                hamon.getExerciseTicks(Exercise.MINING), 
                hamon.getExerciseTicks(Exercise.RUNNING), 
                hamon.getExerciseTicks(Exercise.SWIMMING), 
                hamon.getExerciseTicks(Exercise.MEDITATION), 
                true,
                hamon.getTrainingBonus(false),
                hamon.getPrevDayExercises());
    }
    
    public static HamonExercisesPacket exercisesOnly(HamonData hamon) {
        return new HamonExercisesPacket(
                hamon.getExerciseTicks(Exercise.MINING), 
                hamon.getExerciseTicks(Exercise.RUNNING), 
                hamon.getExerciseTicks(Exercise.SWIMMING), 
                hamon.getExerciseTicks(Exercise.MEDITATION), 
                false, 0, 0);
    }

    private HamonExercisesPacket(int miningTicks, int runningTicks, int swimmingTicks, int meditationTicks, 
            boolean sendBonus, float trainingBonus, float prevDayExercises) {
        this.miningTicks = miningTicks;
        this.runningTicks = runningTicks;
        this.swimmingTicks = swimmingTicks;
        this.meditationTicks = meditationTicks;
        
        this.sendBonus = sendBonus;
        this.trainingBonus = trainingBonus;
        this.prevDayExercises = prevDayExercises;
    }
    
    
    
    public static class Handler implements IModPacketHandler<HamonExercisesPacket> {

        public void encode(HamonExercisesPacket msg, PacketBuffer buf) {
            buf.writeLong(writeExercises(msg.miningTicks, msg.runningTicks, msg.swimmingTicks, msg.meditationTicks));
            
            buf.writeBoolean(msg.sendBonus);
            if (msg.sendBonus) {
                buf.writeFloat(msg.trainingBonus);
                buf.writeFloat(msg.prevDayExercises);
            }
        }
    
        public HamonExercisesPacket decode(PacketBuffer buf) {
            long l = buf.readLong();
            int[] exerciseTicks = readExercises(l);
            
            boolean readBonus = buf.readBoolean();
            float trainingBonus = readBonus ? buf.readFloat() : 0;
            float prevDayExercises = readBonus ? buf.readFloat() : 0;
            
            return new HamonExercisesPacket(
                    exerciseTicks[0], 
                    exerciseTicks[1], 
                    exerciseTicks[2], 
                    exerciseTicks[3], 
                    readBonus, trainingBonus, prevDayExercises);
        }
        
        public void handle(HamonExercisesPacket msg, Supplier<NetworkEvent.Context> ctx) {
            INonStandPower.getNonStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.setExerciseTicks(msg.miningTicks, msg.runningTicks, msg.swimmingTicks, msg.meditationTicks, true);
                    if (msg.sendBonus) {
                        hamon.setTrainingBonus(msg.trainingBonus);
                        hamon.setPrevDayExercises(msg.prevDayExercises);
                    }
                });
            });
        }

        @Override
        public Class<HamonExercisesPacket> getPacketClass() {
            return HamonExercisesPacket.class;
        }
        
        
        
        private static final long MASK_MINING = (1L << MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Exercise.MINING.getMaxTicks(null)))) - 1L;
        private static final int BITS_RUNNING = MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Exercise.RUNNING.getMaxTicks(null)));
        private static final long MASK_RUNNING = (1L << BITS_RUNNING) - 1L;
        private static final int BITS_SWIMMING = MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Exercise.SWIMMING.getMaxTicks(null)));
        private static final long MASK_SWIMMING = (1L << BITS_SWIMMING) - 1L;
        private static final int BITS_MEDITATION = MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Exercise.MEDITATION.getMaxTicks(null)));
        private static final long MASK_MEDITATION = (1L << BITS_MEDITATION) - 1L;
        
        private long writeExercises(int mining, int running, int swimming, int meditation) {
            long encoded = mining;
            encoded <<= BITS_RUNNING;
            encoded |= running;
            encoded <<= BITS_SWIMMING;
            encoded |= swimming;
            encoded <<= BITS_MEDITATION;
            encoded |= meditation;
            
            return encoded;
        }
        
        private int[] readExercises(long encoded) {
            int meditation = (int) (encoded & MASK_MEDITATION);
            encoded >>= BITS_MEDITATION;
            int swimming = (int) (encoded & MASK_SWIMMING);
            encoded >>= BITS_SWIMMING;
            int running = (int) (encoded & MASK_RUNNING);
            encoded >>= BITS_RUNNING;
            int mining = (int) (encoded & MASK_MINING);
            
            return new int[] { mining, running, swimming, meditation };
        }
    }
}
