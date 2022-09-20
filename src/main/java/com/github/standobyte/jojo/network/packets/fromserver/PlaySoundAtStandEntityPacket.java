package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class PlaySoundAtStandEntityPacket {
    private final SoundEvent sound;
    private final int entityId;
    private final float volume;
    private final float pitch;

    public PlaySoundAtStandEntityPacket(SoundEvent sound, int entityId, float volume, float pitch) {
        this.sound = sound;
        this.entityId = entityId;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static void encode(PlaySoundAtStandEntityPacket msg, PacketBuffer buf) {
        buf.writeRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS, msg.sound);
        buf.writeInt(msg.entityId);
        buf.writeFloat(msg.volume);
        buf.writeFloat(msg.pitch);
    }

    public static PlaySoundAtStandEntityPacket decode(PacketBuffer buf) {
        return new PlaySoundAtStandEntityPacket(buf.readRegistryIdUnsafe(ForgeRegistries.SOUND_EVENTS), buf.readInt(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(PlaySoundAtStandEntityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof StandEntity) {
                ClientTickingSoundsHelper.playStandEntitySound((StandEntity) entity, msg.sound, msg.volume, msg.pitch);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
