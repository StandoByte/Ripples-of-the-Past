package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.PowerBaseImpl;
import com.github.standobyte.jojo.util.general.ObjectWrapper;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClClickActionPacket2 {
    private final PowerClassification power;
    private final Action<?> action;
    private final ActionTarget target;
    private boolean sneak;
    
    private PacketBuffer extraInputData = null;
    
    public ClClickActionPacket2(PowerClassification power, Action<?> action, ActionTarget target, boolean sneak) {
        this.power = power;
        this.action = action;
        this.sneak = sneak;
        this.target = target;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClClickActionPacket2> {
    
        @Override
        public void encode(ClClickActionPacket2 msg, PacketBuffer buf) {
            buf.writeEnum(msg.power);
            buf.writeRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry(), msg.action);
            msg.target.writeToBuf(buf);
            buf.writeBoolean(msg.sneak);
            
            msg.action.clWriteExtraData(buf);
        }

        @Override
        public ClClickActionPacket2 decode(PacketBuffer buf) {
            PowerClassification power = buf.readEnum(PowerClassification.class);
            Action<?> action = buf.readRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry());
            ActionTarget target = ActionTarget.readFromBuf(buf);
            boolean sneak = buf.readBoolean();
            ClClickActionPacket2 packet = new ClClickActionPacket2(power, action, target, sneak);
            packet.extraInputData = buf;
            return packet;
        }

        @Override
        public void handle(ClClickActionPacket2 msg, Supplier<NetworkEvent.Context> ctx) {
            if (msg.action == null) return;
            PlayerEntity player = ctx.get().getSender();
            if (JojoModUtil.tmpSpectatorCantUsePowers(player) || !player.isAlive()) return;
            
            IPower.getPowerOptional(player, msg.power).ifPresent(power -> {
                msg.target.resolveEntityId(player.level);
                clickAction(power, msg.action, msg.sneak, msg.target, msg.extraInputData);
            });
        }
        
        public static <P extends IPower<P, ?>> boolean clickAction(IPower<?, ?> p, 
                Action<P> action, boolean sneak, ActionTarget target, PacketBuffer extraData) {
            P power = (P) p;
            PowerBaseImpl<P, ?> powerImpl = (PowerBaseImpl<P, ?>) power;
            LivingEntity user = power.getUser();
            if (power == null || user == null) return false;

            ObjectWrapper<ActionTarget> targetContainer = new ObjectWrapper<>(target);
            ActionConditionResult result = powerImpl.checkRequirements(action, targetContainer, true);
            target = targetContainer.get();
            if (action.getHoldDurationMax(power) <= 0 || action.getHoldDurationToFire(power) > 0) {
                boolean res;
                boolean wasActive = power.isActive();
                action.onClick(user.level, user, power);
                if (user instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) user).resetLastActionTime();
                }
                if (result.isPositive()) {
                    if (!user.level.isClientSide()) {
                        action.playVoiceLine(user, power, target, wasActive, sneak);
                    }
                    powerImpl.performAction(action, target, extraData);
                    power.stopHeldAction(false);
                    res = true;
                }
                else {
                    powerImpl.sendMessage(action, result);
                    res = false;
                }
                
                action.afterClick(user.level, user, power, res);
                return res;
            }
            else {
                action.startedHolding(user.level, user, power, target, result.isPositive());
                if (power.getHeldAction() != action) {
                    power.setHeldAction(action, target);
                    if (power.getHeldAction() == action) {
                        action.afterClick(user.level, user, power, true);
                        return true;
                    }
                    return false;
                }
                else {
                    power.stopHeldAction(true);
                    return false;
                }
            }
        }
        
        @Override
        public Class<ClClickActionPacket2> getPacketClass() {
            return ClClickActionPacket2.class;
        }
    }
}
