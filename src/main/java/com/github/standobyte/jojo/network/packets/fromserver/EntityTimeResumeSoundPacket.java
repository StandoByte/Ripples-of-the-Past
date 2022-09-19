package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.network.NetworkUtil;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class EntityTimeResumeSoundPacket {
    private final Vector3d pos;
    private final SoundEvent sound;

    public EntityTimeResumeSoundPacket(Vector3d pos, SoundEvent sound) {
        this.pos = pos;
        this.sound = sound;
    }

    public static void encode(EntityTimeResumeSoundPacket msg, PacketBuffer buf) {
        NetworkUtil.writeVecApproximate(buf, msg.pos);
        buf.writeRegistryId(msg.sound);
    }

    public static EntityTimeResumeSoundPacket decode(PacketBuffer buf) {
        return new EntityTimeResumeSoundPacket(NetworkUtil.readVecApproximate(buf), buf.readRegistryIdSafe(SoundEvent.class));
    }

    public static void handle(EntityTimeResumeSoundPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientEventHandler.getInstance().addTimeResumeSound(new SoundPos(msg.sound, msg.pos));
        });
        ctx.get().setPacketHandled(true);
    }

    public static class SoundPos {
        public final SoundEvent sound;
        public final Vector3d pos;
        
        private SoundPos(SoundEvent sound, Vector3d pos) {
            this.sound = sound;
            this.pos = pos;
        }
    }
}
