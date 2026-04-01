package com.github.standobyte.jojo.mechanics.speechbubble;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class SaySpeechBubblePacket {
    private final int entityId;
    private final ITextComponent message;
    private final SpeechBubbleType type;
    private final boolean menacing;
    
    public SaySpeechBubblePacket(int entityId, ITextComponent message, SpeechBubbleType type, boolean menacing) {
        this.entityId = entityId;
        this.message = message;
        this.type = type;
        this.menacing = menacing;
    }
    
    
    
    public static class Handler implements IModPacketHandler<SaySpeechBubblePacket> {

        @Override
        public void encode(SaySpeechBubblePacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeComponent(msg.message);
            buf.writeEnum(msg.type);
            buf.writeBoolean(msg.menacing);
        }

        @Override
        public SaySpeechBubblePacket decode(PacketBuffer buf) {
            return new SaySpeechBubblePacket(
                    buf.readInt(), 
                    buf.readComponent(), 
                    buf.readEnum(SpeechBubbleType.class),
                    buf.readBoolean());
        }
        
        @Override
        public void handle(SaySpeechBubblePacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            SpeechBubblesFunctionality.clientSideAddSpeechBubble(entity, msg.message, msg.type, msg.menacing);
        }

        @Override
        public Class<SaySpeechBubblePacket> getPacketClass() {
            return SaySpeechBubblePacket.class;
        }
    }
}
