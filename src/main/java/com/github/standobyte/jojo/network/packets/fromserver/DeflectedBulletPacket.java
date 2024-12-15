package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class DeflectedBulletPacket {
    private final int entityId;
    private final Vector3d deflectVec;
    private final Vector3d deflectedPos;
    private final Vector3d bulletPos;
    
    public DeflectedBulletPacket(int entityId, Vector3d deflectVec, Vector3d deflectedPos, Vector3d bulletPos) {
        this.entityId = entityId;
        this.deflectVec = deflectVec;
        this.deflectedPos = deflectedPos;
        this.bulletPos = bulletPos;
    }
    
    
    
    public static class Handler implements IModPacketHandler<DeflectedBulletPacket> {

        @Override
        public void encode(DeflectedBulletPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeDouble(msg.deflectVec.x);
            buf.writeDouble(msg.deflectVec.y);
            buf.writeDouble(msg.deflectVec.z);
            buf.writeDouble(msg.deflectedPos.x);
            buf.writeDouble(msg.deflectedPos.y);
            buf.writeDouble(msg.deflectedPos.z);
            buf.writeDouble(msg.bulletPos.x);
            buf.writeDouble(msg.bulletPos.y);
            buf.writeDouble(msg.bulletPos.z);
        }

        @Override
        public DeflectedBulletPacket decode(PacketBuffer buf) {
            return new DeflectedBulletPacket(buf.readInt(), 
                    new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }

        @Override
        public void handle(DeflectedBulletPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof ModdedProjectileEntity) {
                entity.setPacketCoordinates(msg.bulletPos.x, msg.bulletPos.y, msg.bulletPos.z);
                entity.xo = msg.deflectVec.x;
                entity.yo = msg.deflectVec.y;
                entity.zo = msg.deflectVec.z;
                entity.xOld = msg.deflectVec.x;
                entity.yOld = msg.deflectVec.y;
                entity.zOld = msg.deflectVec.z;
                entity.setPos(msg.bulletPos.x, msg.bulletPos.y, msg.bulletPos.z);
                ((ModdedProjectileEntity) entity).setIsDeflected(msg.deflectVec, msg.deflectedPos);
            }
        }

        @Override
        public Class<DeflectedBulletPacket> getPacketClass() {
            return DeflectedBulletPacket.class;
        }
    }
}
