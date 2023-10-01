package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.layout.ActionsLayout;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClActionsLayoutPacket {
    private final PowerClassification power;
    private final IPowerType<?, ?> powerType;
    private ActionsLayout<?> layout;
    
    public ClActionsLayoutPacket(PowerClassification power, IPowerType<?, ?> powerType, ActionsLayout<?> layout) {
        this.power = power;
        this.powerType = powerType;
        this.layout = layout;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClActionsLayoutPacket> {
        
        @Override
        public void encode(ClActionsLayoutPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.power);
            NetworkUtil.writePowerType(buf, msg.powerType, msg.power);
            msg.layout.toBuf(buf);
        }
        
        @Override
        public ClActionsLayoutPacket decode(PacketBuffer buf) {
            PowerClassification power = buf.readEnum(PowerClassification.class);
            IPowerType<?, ?> powerType = NetworkUtil.readPowerType(buf, power);
            ActionsLayout<?> layout = ActionsLayout.fromBuf(powerType, buf);
            return new ClActionsLayoutPacket(power, powerType, layout);
        }
        
        @Override
        public void handle(ClActionsLayoutPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            IPower.getPowerOptional(player, msg.power).ifPresent(power -> {
                saveLayout(power, msg);
            });
        }
        private <P extends IPower<P, T>, T extends IPowerType<P, T>> void saveLayout(IPower<?, ?> power, ClActionsLayoutPacket msg) {
            ((P) power).saveActionsHudLayout((T) msg.powerType, (ActionsLayout<P>) msg.layout);
        }
        
        @Override
        public Class<ClActionsLayoutPacket> getPacketClass() {
            return ClActionsLayoutPacket.class;
        }
    }
}
