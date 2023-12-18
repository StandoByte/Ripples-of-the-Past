package com.github.standobyte.jojo.network.packets.fromserver.ability_specific;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ControllerConsciousness;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class GESplitConsciousnessPacket {
    private final Vector3d deltaMovement;
    
    public GESplitConsciousnessPacket(Vector3d deltaMovement) {
        this.deltaMovement = deltaMovement;
    }
    
    
    
    public static class Handler implements IModPacketHandler<GESplitConsciousnessPacket> {

        @Override
        public void encode(GESplitConsciousnessPacket msg, PacketBuffer buf) {
            buf.writeDouble(msg.deltaMovement.x);
            buf.writeDouble(msg.deltaMovement.y);
            buf.writeDouble(msg.deltaMovement.z);
        }

        @Override
        public GESplitConsciousnessPacket decode(PacketBuffer buf) {
            return new GESplitConsciousnessPacket(new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }

        @Override
        public void handle(GESplitConsciousnessPacket msg, Supplier<Context> ctx) {
            if (ClientUtil.getClientPlayer().hasEffect(ModStatusEffects.SENSORY_OVERLOAD.get())) {
                ControllerConsciousness csns = ControllerConsciousness.getInstance();
                csns.spawnConsciousness();
                csns.getCsnsEntity().lerpMotion(msg.deltaMovement.x, msg.deltaMovement.y, msg.deltaMovement.z);
            }
        }

        @Override
        public Class<GESplitConsciousnessPacket> getPacketClass() {
            return GESplitConsciousnessPacket.class;
        }
    }
}
