package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrSyncHeldActionPacket {
    private final int userId;
    private final PowerClassification classification;
    @Nullable private final Action<?> action;
    private final boolean requirementsFulfilled;
    
    public TrSyncHeldActionPacket(int userId, PowerClassification classification, Action<?> action, boolean requirementsFulfilled) {
        this.userId = userId;
        this.classification = classification;
        this.action = action;
        this.requirementsFulfilled = requirementsFulfilled;
    }
    
    public static TrSyncHeldActionPacket actionStopped(int userId, PowerClassification classification) {
        return new TrSyncHeldActionPacket(userId, classification, null, false);
    }

    public static void encode(TrSyncHeldActionPacket msg, PacketBuffer buf) {
        boolean stopHeld = msg.action == null;
        buf.writeBoolean(stopHeld);
        buf.writeInt(msg.userId);
        buf.writeEnum(msg.classification);
        if (!stopHeld) {
            buf.writeRegistryIdUnsafe(ModActions.Registry.getRegistry(), msg.action);
            buf.writeBoolean(msg.requirementsFulfilled);
        }
    }

    public static TrSyncHeldActionPacket decode(PacketBuffer buf) {
        boolean stopHeld = buf.readBoolean();
        if (stopHeld) {
            return actionStopped(buf.readInt(), buf.readEnum(PowerClassification.class));
        }
        return new TrSyncHeldActionPacket(buf.readInt(), buf.readEnum(PowerClassification.class), buf.readRegistryIdUnsafe(ModActions.Registry.getRegistry()), buf.readBoolean());
    }

    public static void handle(TrSyncHeldActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity user = ClientUtil.getEntityById(msg.userId);
            if (user instanceof LivingEntity) {
                IPower.getPowerOptional((LivingEntity) user, msg.classification).ifPresent(power -> {
                    if (msg.action == null) {
                        power.stopHeldAction(false);
                        if (user.is(ClientUtil.getClientPlayer())) {
                            InputHandler.getInstance().stopHeldAction(power, false);
                        }
                    }
                    else {
                        if (power.getHeldAction() != msg.action) {
                            power.setHeldAction(msg.action);
                        }
                        power.refreshHeldActionTickState(msg.requirementsFulfilled);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
