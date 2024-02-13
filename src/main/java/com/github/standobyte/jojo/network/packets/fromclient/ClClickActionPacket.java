package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClClickActionPacket {
    private final PowerClassification power;
    private final Action<?> action;
    private final ActionTarget target;
    private boolean sneak;
    
    private PacketBuffer extraInputData = null;
    
    public ClClickActionPacket(PowerClassification power, Action<?> action, ActionTarget target, boolean sneak) {
        this.power = power;
        this.action = action;
        this.sneak = sneak;
        this.target = target;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClClickActionPacket> {
    
        @Override
        public void encode(ClClickActionPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.power);
            buf.writeRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry(), msg.action);
            msg.target.writeToBuf(buf);
            buf.writeBoolean(msg.sneak);
            
            msg.action.clWriteExtraData(buf);
        }

        @Override
        public ClClickActionPacket decode(PacketBuffer buf) {
            PowerClassification power = buf.readEnum(PowerClassification.class);
            Action<?> action = buf.readRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry());
            ActionTarget target = ActionTarget.readFromBuf(buf);
            boolean sneak = buf.readBoolean();
            
            ClClickActionPacket packet = new ClClickActionPacket(power, action, target, sneak);
            packet.extraInputData = buf;
            return packet;
        }

        @Override
        public void handle(ClClickActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
            if (msg.action == null) return;
            PlayerEntity player = ctx.get().getSender();
            if (player.isSpectator() || !player.isAlive()) return;
            
            IPower.getPowerOptional(player, msg.power).ifPresent(power -> {
                msg.target.resolveEntityId(player.level);
                clickAction(power, msg.action, msg.sneak, msg.target, msg.extraInputData);
            });
        }
        
        private <P extends IPower<P, ?>> void clickAction(IPower<?, ?> power, 
                Action<P> action, boolean sneak, ActionTarget target, PacketBuffer extraData) {
            ((P) power).clickAction(action, sneak, target, extraData);
        }
        
        @Override
        public Class<ClClickActionPacket> getPacketClass() {
            return ClClickActionPacket.class;
        }
    }
}
