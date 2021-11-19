package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData.Exercise;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncHamonExercisesPacket {
    private static final long MASK_MINING = (1L << MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Exercise.MINING.maxTicks))) - 1L;
    private static final int BITS_RUNNING = MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Exercise.RUNNING.maxTicks));
    private static final long MASK_RUNNING = (1L << BITS_RUNNING) - 1L;
    private static final int BITS_SWIMMING = MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Exercise.SWIMMING.maxTicks));
    private static final long MASK_SWIMMING = (1L << BITS_SWIMMING) - 1L;
    private static final int BITS_MEDITATION = MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Exercise.MEDITATION.maxTicks));
    private static final long MASK_MEDITATION = (1L << BITS_MEDITATION) - 1L;

    private final int miningTicks;
    private final int runningTicks;
    private final int swimmingTicks;
    private final int meditationTicks;

    public SyncHamonExercisesPacket(int miningTicks, int runningTicks, int swimmingTicks, int meditationTicks) {
        this.miningTicks = miningTicks;
        this.runningTicks = runningTicks;
        this.swimmingTicks = swimmingTicks;
        this.meditationTicks = meditationTicks;
    }

    public static void encode(SyncHamonExercisesPacket msg, PacketBuffer buf) {
        long l = msg.miningTicks;
        l <<= BITS_RUNNING;
        l |= msg.runningTicks;
        l <<= BITS_SWIMMING;
        l |= msg.swimmingTicks;
        l <<= BITS_MEDITATION;
        l |= msg.meditationTicks;
        buf.writeLong(l);
    }

    public static SyncHamonExercisesPacket decode(PacketBuffer buf) {
        long l = buf.readLong();
        int meditation = (int) (l & MASK_MEDITATION);
        l >>= BITS_MEDITATION;
        int swimming = (int) (l & MASK_SWIMMING);
        l >>= BITS_SWIMMING;
        int running = (int) (l & MASK_RUNNING);
        l >>= BITS_RUNNING;
        int mining = (int) (l & MASK_MINING);
        return new SyncHamonExercisesPacket(mining, running, swimming, meditation);
    }

    public static void handle(SyncHamonExercisesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            INonStandPower.getNonStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.setExerciseTicks(msg.miningTicks, msg.runningTicks, msg.swimmingTicks, msg.meditationTicks);
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
