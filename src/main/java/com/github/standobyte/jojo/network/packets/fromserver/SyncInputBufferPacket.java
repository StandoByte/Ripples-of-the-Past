package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncInputBufferPacket {
    private final PowerClassification classification;
    @Nullable private final Action<?> action;
    
    public SyncInputBufferPacket(PowerClassification classification, @Nullable Action<?> action) {
        this.classification = classification;
        this.action = action;
    }

    public static void encode(SyncInputBufferPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.classification);
        buf.writeBoolean(msg.action != null);
        if (msg.action != null) {
            buf.writeRegistryIdUnsafe(ModActions.Registry.getRegistry(), msg.action);
        }
    }

    public static SyncInputBufferPacket decode(PacketBuffer buf) {
        return new SyncInputBufferPacket(buf.readEnum(PowerClassification.class), 
                buf.readBoolean() ? buf.readRegistryIdUnsafe(ModActions.Registry.getRegistry()) : null);
    }

    public static void handle(SyncInputBufferPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IPower.getPowerOptional(ClientUtil.getClientPlayer(), msg.classification).ifPresent(power -> {
                queueNextAction(power, msg.action);;
            });
        });
        ctx.get().setPacketHandled(true);
    }
    
    private static <P extends IPower<P, ?>> void queueNextAction(IPower<?, ?> power, Action<?> action) {
        ((P) power).queueNextAction((Action<P>) action);
    }
}
