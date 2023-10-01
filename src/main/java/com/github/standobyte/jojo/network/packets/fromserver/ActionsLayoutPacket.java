package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.layout.ActionsLayout;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ActionsLayoutPacket {
    private final PowerClassification power;
    private final IPowerType<?, ?> powerType;
    private ActionsLayout<?> layout;
    
    public ActionsLayoutPacket(PowerClassification power, IPowerType<?, ?> powerType, ActionsLayout<?> layout) {
        this.power = power;
        this.powerType = powerType;
        this.layout = layout;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ActionsLayoutPacket> {
        
        @Override
        public void encode(ActionsLayoutPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.power);
            NetworkUtil.writePowerType(buf, msg.powerType, msg.power);
            msg.layout.toBuf(buf);
        }
        
        @Override
        public ActionsLayoutPacket decode(PacketBuffer buf) {
            PowerClassification power = buf.readEnum(PowerClassification.class);
            IPowerType<?, ?> powerType = NetworkUtil.readPowerType(buf, power);
            ActionsLayout<?> layout = ActionsLayout.fromBuf(powerType, buf);
            return new ActionsLayoutPacket(power, powerType, layout);
        }
        
        @Override
        public void handle(ActionsLayoutPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IPower.getPowerOptional(ClientUtil.getClientPlayer(), msg.power).ifPresent(power -> {
                setLayout(power, msg.layout);
            });
        }
        
        private <P extends IPower<P, ?>> void setLayout(IPower<?, ?> power, ActionsLayout<P> layout) {
            ((P) power).setActionsHudLayout(layout);
        }
        
        @Override
        public Class<ActionsLayoutPacket> getPacketClass() {
            return ActionsLayoutPacket.class;
        }
    }
    
    public static enum Field {
        QUICK_ACCESS_HUD_RENDER
    }
}
