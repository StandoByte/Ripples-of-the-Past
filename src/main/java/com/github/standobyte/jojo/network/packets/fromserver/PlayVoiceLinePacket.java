package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class PlayVoiceLinePacket {
    private final SoundEvent sound;
    private final SoundCategory source;
    private final int entityId;
    private final float volume;
    private final float pitch;

    public PlayVoiceLinePacket(SoundEvent sound, SoundCategory source, int entityId, float volume, float pitch) {
        Validate.notNull(sound, "sound");
        this.sound = sound;
        this.source = source;
        this.entityId = entityId;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static void encode(PlayVoiceLinePacket msg, PacketBuffer buf) {
        buf.writeRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS, msg.sound);
        buf.writeEnum(msg.source);
        buf.writeInt(msg.entityId);
        buf.writeFloat(msg.volume);
        buf.writeFloat(msg.pitch);
    }

    public static PlayVoiceLinePacket decode(PacketBuffer buf) {
        return new PlayVoiceLinePacket(buf.readRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS), 
                buf.readEnum(SoundCategory.class), buf.readInt(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(PlayVoiceLinePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
                ClientTickingSoundsHelper.playVoiceLine(entity, msg.sound, msg.source, msg.volume, msg.pitch);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
