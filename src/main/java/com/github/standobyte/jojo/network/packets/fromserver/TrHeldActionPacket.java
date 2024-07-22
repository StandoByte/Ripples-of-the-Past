package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHeldActionPacket {
    private final int userId;
    private final PowerClassification classification;
    @Nullable private final Action<?> action;
    private final boolean requirementsFulfilled;
    
    public TrHeldActionPacket(int userId, PowerClassification classification, Action<?> action, boolean requirementsFulfilled) {
        this.userId = userId;
        this.classification = classification;
        this.action = action;
        this.requirementsFulfilled = requirementsFulfilled;
    }
    
    public static TrHeldActionPacket actionStopped(int userId, PowerClassification classification) {
        return new TrHeldActionPacket(userId, classification, null, false);
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHeldActionPacket> {

        @Override
        public void encode(TrHeldActionPacket msg, PacketBuffer buf) {
            boolean stopHeld = msg.action == null;
            buf.writeBoolean(stopHeld);
            buf.writeInt(msg.userId);
            buf.writeEnum(msg.classification);
            if (!stopHeld) {
                buf.writeRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry(), msg.action);
                buf.writeBoolean(msg.requirementsFulfilled);
            }
        }

        @Override
        public TrHeldActionPacket decode(PacketBuffer buf) {
            boolean stopHeld = buf.readBoolean();
            if (stopHeld) {
                return actionStopped(buf.readInt(), buf.readEnum(PowerClassification.class));
            }
            return new TrHeldActionPacket(buf.readInt(), buf.readEnum(PowerClassification.class), buf.readRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry()), buf.readBoolean());
        }

        @Override
        public void handle(TrHeldActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity user = ClientUtil.getEntityById(msg.userId);
            if (user instanceof LivingEntity) {
                IPower.getPowerOptional((LivingEntity) user, msg.classification).ifPresent(power -> {
                    boolean isClientPlayer = user.is(ClientUtil.getClientPlayer());
                    if (msg.action != null) {
                        if (power.getHeldAction() != msg.action) {
                            setHeldAction(power, msg.action);
                        }
                        power.refreshHeldActionTickState(msg.requirementsFulfilled);
                        if (user instanceof PlayerEntity && 
                                PlayerAnimationHandler.getPlayerAnimator().actionStartedHolding((PlayerEntity) user, msg.action)) {
                            user.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                                cap.setHeldActionWithAnim(msg.action);
                            });
                        }
                    }
                    else {
                        power.stopHeldAction(false);
                        if (isClientPlayer) {
                            InputHandler.getInstance().stopHeldAction(power, false);
                        }
                        if (user instanceof PlayerEntity) {
                            user.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                                cap.getHeldActionWithAnim().ifPresent(action -> {
                                    PlayerAnimationHandler.getPlayerAnimator().actionStoppedHolding((PlayerEntity) user, action);
                                });
                            });
                        }
                    }
                });
            }
        }
        
        private <P extends IPower<P, ?>> void setHeldAction(IPower<?, ?> power, Action<?> action) {
            ((P) power).setHeldAction((Action<P>) action);
        }

        @Override
        public Class<TrHeldActionPacket> getPacketClass() {
            return TrHeldActionPacket.class;
        }
    }
}
