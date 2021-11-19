package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;

import com.github.standobyte.jojo.client.ClientUtil;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class PlaySoundAtClientPacket {
    private final SoundEvent sound;
    private final SoundCategory source;
    private final BlockPos soundPos;
    private final float volume;
    private final float pitch;

    public PlaySoundAtClientPacket(SoundEvent sound, SoundCategory source, BlockPos soundPos, float volume, float pitch) {
        Validate.notNull(sound, "sound");
        this.sound = sound;
        this.source = source;
        this.soundPos = soundPos;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static void encode(PlaySoundAtClientPacket msg, PacketBuffer buf) {
        buf.writeRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS, msg.sound);
        buf.writeEnum(msg.source);
        buf.writeBlockPos(msg.soundPos);
        buf.writeFloat(msg.volume);
        buf.writeFloat(msg.pitch);
    }

    public static PlaySoundAtClientPacket decode(PacketBuffer buf) {
        return new PlaySoundAtClientPacket(buf.readRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS), 
                buf.readEnum(SoundCategory.class), buf.readBlockPos(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(PlaySoundAtClientPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientUtil.playSoundAtClient(msg.sound, msg.source, msg.soundPos, msg.volume, msg.pitch);
        });
        ctx.get().setPacketHandled(true);
    }

}
