package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.NetworkUtil;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrBarrageHitSoundPacket {
    private final int standEntityId;
    private final boolean hit;
    private final SoundEvent sound;
    private final Vector3d soundPos;
    
    public TrBarrageHitSoundPacket(int standEntityId, SoundEvent sound, Vector3d soundPos) {
        this(standEntityId, true, sound, soundPos);
    }
    
    public static TrBarrageHitSoundPacket noSound(int standEntityId) {
        return new TrBarrageHitSoundPacket(standEntityId, false, null, null);
    }
    
    private TrBarrageHitSoundPacket(int standEntityId, boolean hit, SoundEvent sound, Vector3d soundPos) {
        this.standEntityId = standEntityId;
        this.hit = hit;
        this.sound = sound;
        this.soundPos = soundPos;
    }
    
    public static void encode(TrBarrageHitSoundPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.standEntityId);
        buf.writeBoolean(msg.hit);
        if (msg.hit) {
            buf.writeRegistryId(msg.sound);
            NetworkUtil.writeVecApproximate(buf, msg.soundPos);
        }
    }
    
    public static TrBarrageHitSoundPacket decode(PacketBuffer buf) {
        int entityId = buf.readInt();
        if (buf.readBoolean()) {
            return new TrBarrageHitSoundPacket(entityId, buf.readRegistryIdSafe(SoundEvent.class), NetworkUtil.readVecApproximate(buf));
        }
        else {
            return TrBarrageHitSoundPacket.noSound(entityId);
        }
    }

    public static void handle(TrBarrageHitSoundPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.standEntityId);
            if (entity instanceof StandEntity) {
                StandEntity stand = (StandEntity) entity;
                if (msg.hit) {
                    stand.getBarrageHitSoundsHandler().hit(msg.sound, msg.soundPos);
                }
                else {
                    stand.getBarrageHitSoundsHandler().hitMissed();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
