package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClLayoutQuickAccessPacket {
    private final PowerClassification classification;
    private final Action<?> action;

    public ClLayoutQuickAccessPacket(PowerClassification classification, Action<?> action) {
        this.classification = classification;
        this.action = action;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClLayoutQuickAccessPacket> {

        @Override
        public void encode(ClLayoutQuickAccessPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.classification);
            NetworkUtil.writeOptionally(buf, msg.action, (buffer, action) -> buffer.writeRegistryId(action));
        }

        @Override
        public ClLayoutQuickAccessPacket decode(PacketBuffer buf) {
            return new ClLayoutQuickAccessPacket(buf.readEnum(PowerClassification.class), 
                    NetworkUtil.readOptional(buf, buffer -> {
                        Action<?> action = buffer.readRegistryIdSafe(Action.class);
                        return action;
                    }).orElse(null));
        }

        @Override
        public void handle(ClLayoutQuickAccessPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            IPower.getPowerOptional(player, msg.classification).ifPresent(power -> {
                setQuickAccess(power, msg.action);
            });
        }
        
        private <P extends IPower<P, ?>> void setQuickAccess(IPower<?, ?> power, Action<P> action) {
            ((P) power).getActionsLayout().setQuickAccessAction(action);
        }

        @Override
        public Class<ClLayoutQuickAccessPacket> getPacketClass() {
            return ClLayoutQuickAccessPacket.class;
        }
    }
}
