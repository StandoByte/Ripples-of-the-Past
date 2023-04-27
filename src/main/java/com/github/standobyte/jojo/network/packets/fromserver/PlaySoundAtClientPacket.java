package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

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
    
    
    
    public static class Handler implements IModPacketHandler<PlaySoundAtClientPacket> {

        @Override
        public void encode(PlaySoundAtClientPacket msg, PacketBuffer buf) {
            buf.writeRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS, msg.sound);
            buf.writeEnum(msg.source);
            buf.writeBlockPos(msg.soundPos);
            buf.writeFloat(msg.volume);
            buf.writeFloat(msg.pitch);
        }

        @Override
        public PlaySoundAtClientPacket decode(PacketBuffer buf) {
            return new PlaySoundAtClientPacket(buf.readRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS), 
                    buf.readEnum(SoundCategory.class), buf.readBlockPos(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public void handle(PlaySoundAtClientPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ClientUtil.playSoundAtClient(msg.sound, msg.source, msg.soundPos, msg.volume, msg.pitch);
        }

        @Override
        public Class<PlaySoundAtClientPacket> getPacketClass() {
            return PlaySoundAtClientPacket.class;
        }
    }

}
