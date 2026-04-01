package com.github.standobyte.jojo.mechanics.speechbubble;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClSaySpeechBubblePacket {
    private final int entityId;
    private final String message;
    private final SpeechBubbleType type;
    private final boolean menacing;
    
    public ClSaySpeechBubblePacket(int entityId, String message, SpeechBubbleType type, boolean menacing) {
        this.entityId = entityId;
        this.message = message;
        this.type = type;
        this.menacing = menacing;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClSaySpeechBubblePacket> {

        @Override
        public void encode(ClSaySpeechBubblePacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeUtf(msg.message);
            buf.writeEnum(msg.type);
            buf.writeBoolean(msg.menacing);
        }

        @Override
        public ClSaySpeechBubblePacket decode(PacketBuffer buf) {
            return new ClSaySpeechBubblePacket(
                    buf.readInt(),
                    buf.readUtf(), 
                    buf.readEnum(SpeechBubbleType.class), 
                    buf.readBoolean());
        }
        
        @Override
        public void handle(ClSaySpeechBubblePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            Entity character = player.level.getEntity(msg.entityId);
            SpeechBubblesFunctionality.broadcastSaySpeechBubble(character, new StringTextComponent(msg.message), msg.type, msg.menacing);
        }

        @Override
        public Class<ClSaySpeechBubblePacket> getPacketClass() {
            return ClSaySpeechBubblePacket.class;
        }
    }
}
