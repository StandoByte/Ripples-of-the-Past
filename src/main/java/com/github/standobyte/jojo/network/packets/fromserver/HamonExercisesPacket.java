package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonData.Exercise;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonExercisesPacket {
    private static final long MASK_MINING = (1L << MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Exercise.MINING.getMaxTicks(null)))) - 1L;
    private static final int BITS_RUNNING = MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Exercise.RUNNING.getMaxTicks(null)));
    private static final long MASK_RUNNING = (1L << BITS_RUNNING) - 1L;
    private static final int BITS_SWIMMING = MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Exercise.SWIMMING.getMaxTicks(null)));
    private static final long MASK_SWIMMING = (1L << BITS_SWIMMING) - 1L;
    private static final int BITS_MEDITATION = MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Exercise.MEDITATION.getMaxTicks(null)));
    private static final long MASK_MEDITATION = (1L << BITS_MEDITATION) - 1L;

    private final int miningTicks;
    private final int runningTicks;
    private final int swimmingTicks;
    private final int meditationTicks;
    private final float trainingBonus;
    
    public HamonExercisesPacket(HamonData hamon) {
        this(
                hamon.getExerciseTicks(Exercise.MINING), 
                hamon.getExerciseTicks(Exercise.RUNNING), 
                hamon.getExerciseTicks(Exercise.SWIMMING), 
                hamon.getExerciseTicks(Exercise.MEDITATION), 
                hamon.getTrainingBonus());
    }

    private HamonExercisesPacket(int miningTicks, int runningTicks, int swimmingTicks, int meditationTicks, float trainingBonus) {
        this.miningTicks = miningTicks;
        this.runningTicks = runningTicks;
        this.swimmingTicks = swimmingTicks;
        this.meditationTicks = meditationTicks;
        this.trainingBonus = trainingBonus;
    }
    
    
    
    public static class Handler implements IModPacketHandler<HamonExercisesPacket> {

        public void encode(HamonExercisesPacket msg, PacketBuffer buf) {
            long l = msg.miningTicks;
            l <<= BITS_RUNNING;
            l |= msg.runningTicks;
            l <<= BITS_SWIMMING;
            l |= msg.swimmingTicks;
            l <<= BITS_MEDITATION;
            l |= msg.meditationTicks;
            buf.writeLong(l);
            buf.writeFloat(msg.trainingBonus);
        }
    
        public HamonExercisesPacket decode(PacketBuffer buf) {
            long l = buf.readLong();
            int meditation = (int) (l & MASK_MEDITATION);
            l >>= BITS_MEDITATION;
            int swimming = (int) (l & MASK_SWIMMING);
            l >>= BITS_SWIMMING;
            int running = (int) (l & MASK_RUNNING);
            l >>= BITS_RUNNING;
            int mining = (int) (l & MASK_MINING);
            return new HamonExercisesPacket(mining, running, swimming, meditation, buf.readFloat());
        }
    
        public void handle(HamonExercisesPacket msg, Supplier<NetworkEvent.Context> ctx) {
            INonStandPower.getNonStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.setExerciseTicks(msg.miningTicks, msg.runningTicks, msg.swimmingTicks, msg.meditationTicks, true);
                    hamon.setTrainingBonus(msg.trainingBonus);
                });
            });
        }

        @Override
        public Class<HamonExercisesPacket> getPacketClass() {
            return HamonExercisesPacket.class;
        }
    }
}
