package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.entity.SoulEntity;
import com.google.common.primitives.Floats;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClSoulRotationPacket {
    private final int entityId;
    private final float yRot;
    private final float xRot;
    private final float yBodyRot;

    public ClSoulRotationPacket(int entityId, float yRot, float xRot, float yBodyRot) {
        this.entityId = entityId;
        this.yRot = yRot;
        this.xRot = xRot;
        this.yBodyRot = yBodyRot;
    }

    public static void encode(ClSoulRotationPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeFloat(msg.yRot);
        buf.writeFloat(msg.xRot);
        buf.writeFloat(msg.yBodyRot);
    }

    public static ClSoulRotationPacket decode(PacketBuffer buf) {
        return new ClSoulRotationPacket(buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(ClSoulRotationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Floats.isFinite(msg.xRot) && Floats.isFinite(msg.yRot) && Floats.isFinite(msg.yBodyRot)) {
                Entity entity = ctx.get().getSender().level.getEntity(msg.entityId);
                if (entity instanceof SoulEntity) {
                    SoulEntity soul = (SoulEntity) entity;
                    soul.yRot = msg.yRot % 360F;
                    soul.yRotO = soul.yRot;
                    soul.xRot = MathHelper.clamp(msg.xRot, -90.0F, 90.0F) % 360.0F;
                    soul.xRotO = soul.xRot;
                    soul.yBodyRot = msg.yBodyRot % 360F;
                    soul.yBodyRotO = soul.yBodyRot;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
