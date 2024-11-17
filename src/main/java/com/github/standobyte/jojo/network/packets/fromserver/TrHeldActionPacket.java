package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
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
    private final ActionTarget target;
    private final boolean actionFired;
    
    public TrHeldActionPacket(int userId, PowerClassification classification, Action<?> action, boolean requirementsFulfilled, ActionTarget target) {
        this(userId, classification, action, requirementsFulfilled, target, false);
    }
    
    public TrHeldActionPacket(int userId, PowerClassification classification, Action<?> action, boolean requirementsFulfilled, ActionTarget target, boolean actionFired) {
        this.userId = userId;
        this.classification = classification;
        this.action = action;
        this.requirementsFulfilled = requirementsFulfilled;
        this.target = target;
        this.actionFired = actionFired;
    }
    
    public static TrHeldActionPacket actionStopped(int userId, PowerClassification classification, boolean actionFired) {
        return new TrHeldActionPacket(userId, classification, null, false, ActionTarget.EMPTY, actionFired);
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHeldActionPacket> {

        @Override
        public void encode(TrHeldActionPacket msg, PacketBuffer buf) {
            boolean stopHeld = msg.action == null;
            buf.writeBoolean(stopHeld);
            buf.writeInt(msg.userId);
            buf.writeEnum(msg.classification);
            if (stopHeld) {
                buf.writeBoolean(msg.actionFired);
            }
            else {
                buf.writeRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry(), msg.action);
                buf.writeBoolean(msg.requirementsFulfilled);
                msg.target.writeToBuf(buf);
            }
        }

        @Override
        public TrHeldActionPacket decode(PacketBuffer buf) {
            boolean stopHeld = buf.readBoolean();
            if (stopHeld) {
                return actionStopped(buf.readInt(), buf.readEnum(PowerClassification.class), 
                        buf.readBoolean());
            }
            return new TrHeldActionPacket(buf.readInt(), buf.readEnum(PowerClassification.class), 
                    buf.readRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry()), buf.readBoolean(), ActionTarget.readFromBuf(buf));
        }

        @Override
        public void handle(TrHeldActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.userId);
            if (entity instanceof LivingEntity) {
                LivingEntity user = (LivingEntity) entity;
                IPower.getPowerOptional(user, msg.classification).ifPresent(power -> {
                    boolean isClientPlayer = user == ClientUtil.getClientPlayer();
                    if (msg.action != null) {
                        if (power.getHeldAction() != msg.action) {
                            setHeldAction(power, msg.action, msg.target);
                        }
                        power.refreshHeldActionTickState(msg.requirementsFulfilled);
                        if (user instanceof PlayerEntity && msg.action.clHeldStartAnim((PlayerEntity) user)) {
                            user.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                                cap.setHeldActionWithAnim(msg.action);
                            });
                        }
                    }
                    else {
                        power.stopHeldAction(msg.actionFired);
                        if (isClientPlayer) {
                            InputHandler.getInstance().stopHeldAction(power);
                        }
                        if (user instanceof PlayerEntity) {
                            user.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                                cap.getHeldActionWithAnim().ifPresent(action -> {
                                    action.clHeldStopAnim((PlayerEntity) user);
                                });
                            });
                        }
                    }
                });
            }
        }
        
        private <P extends IPower<P, ?>> void setHeldAction(IPower<?, ?> power, Action<?> action, ActionTarget target) {
            ((P) power).setHeldAction((Action<P>) action, target);
        }

        @Override
        public Class<TrHeldActionPacket> getPacketClass() {
            return TrHeldActionPacket.class;
        }
    }
}
