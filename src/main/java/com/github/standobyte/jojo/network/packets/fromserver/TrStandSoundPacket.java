package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrStandSoundPacket {
    private final int standEntityId;
    private final StandSoundType soundType;
    private final boolean stopSound;

    public TrStandSoundPacket(int standEntityId, StandSoundType soundType) {
        this(standEntityId, soundType, false);
    }
    
    public static TrStandSoundPacket stopSound(int standEntityId, StandSoundType soundType) {
        return new TrStandSoundPacket(standEntityId, soundType, true);
    }
    
    private TrStandSoundPacket(int standEntityId, StandSoundType soundType, boolean stopSound) {
        this.standEntityId = standEntityId;
        this.soundType = soundType;
        this.stopSound = stopSound;
    }

    public static void encode(TrStandSoundPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.standEntityId);
        buf.writeEnum(msg.soundType);
        buf.writeBoolean(msg.stopSound);
    }

    public static TrStandSoundPacket decode(PacketBuffer buf) {
        return new TrStandSoundPacket(buf.readInt(), buf.readEnum(StandSoundType.class), buf.readBoolean());
    }

    public static void handle(TrStandSoundPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.standEntityId);
            if (entity instanceof StandEntity) {
                StandEntity stand = (StandEntity) entity;
                if (!msg.stopSound) {
                    stand.playStandSound(msg.soundType);
                }
                else {
                    stand.stopStandSound(msg.soundType);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public static enum StandSoundType {
        SUMMON,
        UNSUMMON,
        MELEE_ATTACK,
        MELEE_BARRAGE,
        RANGED_ATTACK
    }
}
